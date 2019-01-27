package cn.hdussta.link.linkServer.dashboard

import cn.hdussta.link.linkServer.common.BaseMicroserviceVerticle
import cn.hdussta.link.linkServer.dashboard.impl.DeviceServiceImpl
import cn.hdussta.link.linkServer.dashboard.impl.SensorServiceImpl
import cn.hdussta.link.linkServer.dashboard.impl.UserServiceImpl
import cn.hdussta.link.linkServer.service.DeviceManagerService
import cn.hdussta.link.linkServer.service.dashboard.DeviceService
import cn.hdussta.link.linkServer.service.dashboard.SensorService
import cn.hdussta.link.linkServer.service.dashboard.UserService
import cn.hdussta.link.linkServer.utils.message
import cn.hdussta.link.linkServer.utils.messageState
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import io.vertx.ext.web.handler.*
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.kotlin.ext.consul.Service
import io.vertx.servicediscovery.types.EventBusService
import io.vertx.serviceproxy.ServiceBinder

class DashBoardVerticle: BaseMicroserviceVerticle() {
  private val logger: Logger = LoggerFactory.getLogger(DashBoardVerticle::class.java)
  private val sqlConfig = JsonObject(mapOf(
    "host" to "link.hdussta.cn",
    "username" to "root",
    "password" to "Admin88888",
    "database" to "sstalink"))
  private val sqlClient by lazy { MySQLClient.createShared(vertx,sqlConfig) }
  private lateinit var managerService: DeviceManagerService
  override fun start() {
    super.start()
    EventBusService.getProxy(discovery, DeviceManagerService::class.java) {
      if (it.failed()) {
        logger.error(it.cause().localizedMessage)
        this.stop()
      } else {
        this.managerService = it.result()
        init()
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

      it.result().setExtraOperationContextPayloadMapper {
        JsonObject()
          .put("id",it.session().get<Int>("id"))
          .put("level",it.session().get<Int>("level"))
          .put("admin",it.session().get<Int>("admin"))
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
      val router = Router.router(vertx).mountSubRouter("/v1",it.result().router)
      vertx.createHttpServer().requestHandler(router).listen(28081){
        logger.info("Listen at 28081")
      }
    }
  }

  private fun login(context: RoutingContext){
    val user = context.request().getParam("user")
    val pass = context.request().getParam("pass")
    val sql = "SELECT id,level,admin_id FROM $USER_TABLE WHERE email=? AND password=?"
    sqlClient.querySingleWithParams(sql, JsonArray().add(user).add(pass)){
      if(it.failed() || it.result()==null){
        context.response().end(JsonObject().put("status",-1).put("message", LOGIN_FAILURE).toBuffer())
      }else{
        val id = it.result().getInteger(0)
        val level = it.result().getInteger(1)
        val admin = it.result().getInteger(2)
        context.session()
          .put("id",id)
          .put("level",level)
          //管理员没有没有admin_id，将自身用户ID设为管理员ID
          .put("admin",if(level<2) id else admin)
        context.response().end(JsonObject().put("status",1).put("message", LOGIN_SUCCESS).toBuffer())
      }
    }
  }
  private fun logout(context: RoutingContext){
    if(context.session().remove<Int>("id")!=null){
      context.response().end(JsonObject().put("status",1).put("message", LOGOUT_SUCCESS).toBuffer())
    }else{
      context.response().end(JsonObject().put("status",-1).put("message", LOGOUT_FAILURE).toBuffer())
    }
  }


  private fun superAdmin(context: RoutingContext){
    if(context.session().get<Int>("level")==1){
      context.next()
    }else{
      context.response().end(JsonObject().put("status",-1).put("message", AUTH_FAILURE).toBuffer())
    }
  }

  private fun normalAdmin(context: RoutingContext){
    if(context.session().get<Int>("level")<=2){
      context.next()
    }else{
      context.response().end(JsonObject().put("status",-1).put("message", AUTH_FAILURE).toBuffer())
    }
  }

  private fun normalUser(context: RoutingContext){
    if(context.session().get<Int>("level")<=3){
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
