package cn.hdussta.link.linkServer.dashboard

import cn.hdussta.link.linkServer.common.BaseMicroserviceVerticle
import cn.hdussta.link.linkServer.dashboard.impl.*
import cn.hdussta.link.linkServer.service.DeviceManagerService
import cn.hdussta.link.linkServer.service.MessageService
import cn.hdussta.link.linkServer.service.dashboard.*
import io.vertx.core.Future
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.servicediscovery.types.EventBusService
import io.vertx.serviceproxy.ServiceBinder
import io.vertx.serviceproxy.ServiceProxyBuilder


class DashBoardVerticle: BaseMicroserviceVerticle() {
  private val logger: Logger = LoggerFactory.getLogger(DashBoardVerticle::class.java)
  private val sockMap = HashMap<String,SockJSSocket>()
  private val sqlConfig by lazy {
    config().getJsonObject("mysql")
  }
  private val sqlClient by lazy { MySQLClient.createShared(vertx,sqlConfig) }
  private lateinit var managerService: DeviceManagerService
  override fun start(startFuture:Future<Void>) {
    super.start()
    EventBusService.getProxy(discovery, DeviceManagerService::class.java) {
      if (it.failed()) {
        logger.error(it.cause().localizedMessage)
        startFuture.fail(it.cause().localizedMessage)
      } else {
        this.managerService = it.result()
        init()
        startFuture.complete()
      }
    }
  }

  private fun init(){
    OpenAPI3RouterFactory.create(vertx,"src/yaml/dashboard.yaml"){
      if(it.failed()) {
        it.cause().printStackTrace()
        stop()
        return@create
      }
      val deviceServiceImpl = DeviceServiceImpl(vertx,sqlClient,managerService)
      ServiceBinder(vertx)
        .setAddress("dashboard-device-service")
        .register(DeviceService::class.java,deviceServiceImpl)

      val sensorServiceImpl = SensorServiceImpl(vertx,sqlClient)
      ServiceBinder(vertx)
        .setAddress("dashboard-sensor-service")
        .register(SensorService::class.java,sensorServiceImpl)

      val userServiceImpl = UserServiceImpl(vertx,sqlClient)
      ServiceBinder(vertx)
        .setAddress("dashboard-user-service")
        .register(UserService::class.java,userServiceImpl)

      val alarmLogServiceImpl = AlarmLogServiceImpl(vertx,sqlClient)
      ServiceBinder(vertx)
        .setAddress("dashboard-alarm-log-service")
        .register(AlarmLogService::class.java,alarmLogServiceImpl)

      val userLogServiceImpl = UserLogServiceImpl(vertx,sqlClient)
      ServiceBinder(vertx)
        .setAddress("dashboard-user-log-service")
        .register(UserLogService::class.java,userLogServiceImpl)

      val dataServiceImpl = DataServiceImpl(vertx,sqlClient)
      ServiceBinder(vertx)
        .setAddress("dashboard-data-service")
        .register(DataService::class.java,dataServiceImpl)

      it.result().setExtraOperationContextPayloadMapper {
        JsonObject()
          .put("id",it.session().get<Int>("id"))
          .put("level",it.session().get<Int>("level"))
          .put("admin",it.session().get<Int>("admin"))
          .put("username",it.session().get<String>("username"))
      }
      it.result().addGlobalHandler(CookieHandler.create())
      it.result().addGlobalHandler(SessionHandler.create(LocalSessionStore.create(vertx)))
      it.result().addHandlerByOperationId("getLogin",::login)
      it.result().addHandlerByOperationId("getLogout",::logout)
      it.result().addSecurityHandler("user",::normalUser)
      it.result().addSecurityHandler("admin",::normalAdmin)
      it.result().addSecurityHandler("super",::superAdmin)
      it.result().mountServiceInterface(DeviceService::class.java,"dashboard-device-service")
      it.result().mountServiceInterface(SensorService::class.java,"dashboard-sensor-service")
      it.result().mountServiceInterface(UserService::class.java,"dashboard-user-service")
      it.result().mountServiceInterface(AlarmLogService::class.java,"dashboard-alarm-log-service")
      it.result().mountServiceInterface(UserLogService::class.java,"dashboard-user-log-service")
      it.result().mountServiceInterface(DataService::class.java,"dashboard-data-service")
      val subRouter = it.result().router
      initMessageService(subRouter)
      val router = Router.router(vertx).mountSubRouter("/v1",subRouter)
      vertx.createHttpServer().requestHandler(router).listen(28081){
        logger.info("Listen at 28081")
      }
    }
  }

  private fun initMessageService(router: Router){
    val options = SockJSHandlerOptions().setHeartbeatInterval(2000)

    val sockJSHandler = SockJSHandler.create(vertx, options)

    sockJSHandler.socketHandler { socket ->
      if(socket.webSession().get<Int>("id")==null){
        socket.close(-1,"没有权限")
      }else{
        socket.write("OK")
        this.sockMap[socket.webSession()["username"]] = socket
        socket.endHandler {
          this.sockMap.remove(socket.webSession()["username"])
        }.exceptionHandler {
          this.sockMap.remove(socket.webSession()["username"])
        }
      }
    }
    router.route("/sock/*").handler(sockJSHandler)

    val messageService = MessageServiceImpl(sockMap)
    val binder = ServiceBinder(vertx).setAddress(MessageService.SERVICE_ADDRESS)
    binder.register(MessageService::class.java, messageService)

    val proxyBuilder = ServiceProxyBuilder(vertx).setAddress(MessageService.SERVICE_ADDRESS)
    proxyBuilder.build(MessageService::class.java)
    publishEventBusService(MessageService.SERVICE_NAME, MessageService.SERVICE_ADDRESS, MessageService::class.java)
  }

  private fun login(context: RoutingContext){
    val user = context.request().getParam("user")
    val pass = context.request().getParam("pass")
    val sql = "SELECT id,level,admin_id,email FROM $USER_TABLE WHERE email=? AND password=?"
    sqlClient.querySingleWithParams(sql, JsonArray().add(user).add(pass)){
      if(it.failed() || it.result()==null){
        context.response().end(JsonObject().put("status",-1).put("message", if(it.failed()) it.cause() else LOGIN_FAILURE).toBuffer())
      }else{
        val id = it.result().getInteger(0)
        val level = it.result().getInteger(1)
        val admin = it.result().getInteger(2)
        val username = it.result().getString(3)
        context.session()
          .put("id",id)
          .put("level",level)
          //管理员没有没有admin_id，将自身用户ID设为管理员ID
          .put("admin",if(level<2) id else admin)
          .put("username",username)
        context.response().end(JsonObject().put("status",1).put("message", LOGIN_SUCCESS).toBuffer())
        sqlClient.userLog(id,UserAction.AUTH,"登录")
      }
    }
  }
  private fun logout(context: RoutingContext){
    if(context.session().remove<Int>("id")!=null){
      context.session().remove<Int>("level")
      context.session().remove<Int>("admin")
      context.session().remove<String>("username")
      context.response().end(JsonObject().put("status",1).put("message", LOGOUT_SUCCESS).toBuffer())
    }else{
      context.response().end(JsonObject().put("status",-1).put("message", LOGOUT_FAILURE).toBuffer())
    }
  }

  private fun superAdmin(context: RoutingContext){
    if(context.session()!=null && context.session().get<Int>("level")!=null && context.session().get<Int>("level")==UserLevel.SUPER.ordinal){
      context.next()
    }else{
      context.response().end(JsonObject().put("status",-1).put("message", AUTH_FAILURE).toBuffer())
    }
  }

  private fun normalAdmin(context: RoutingContext){
    if(context.session()!=null && context.session().get<Int>("level")!=null && context.session().get<Int>("level")<=UserLevel.ADMIN.ordinal){
      context.next()
    }else{
      context.response().end(JsonObject().put("status",-1).put("message", AUTH_FAILURE).toBuffer())
    }
  }

  private fun normalUser(context: RoutingContext){
    if(context.session()!=null && context.session().get<Int>("level")!=null && context.session().get<Int>("level")<=UserLevel.USER.ordinal){
      context.next()
    }else{
      context.response().end(JsonObject().put("status",-1).put("message", AUTH_FAILURE).toBuffer())
    }
  }

  companion object {
    private const val LOGIN_SUCCESS = "登录成功"
    private const val LOGIN_FAILURE = "登录失败"
    private const val LOGOUT_SUCCESS = "登出成功"
    private const val LOGOUT_FAILURE = "登出失败"
    private const val AUTH_FAILURE = "没有权限"
    private const val USER_TABLE = "sstalink_user"
  }
}
