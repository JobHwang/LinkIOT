package cn.hdussta.link.linkServer.manager

import cn.hdussta.link.linkServer.common.BaseMicroserviceVerticle
import cn.hdussta.link.linkServer.manager.impl.DeviceManagerServiceImpl
import cn.hdussta.link.linkServer.service.DeviceManagerService
import cn.hdussta.link.linkServer.utils.message
import cn.hdussta.link.linkServer.utils.messageState
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.AsyncMap
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.sstore.SessionStore
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.serviceproxy.ServiceBinder
import io.vertx.serviceproxy.ServiceProxyBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @name ManagerVerticle
 * @description 用于部署ManagerService,并提供一个Restful服务
 * @author Wooyme
 */
class ManagerVerticle:BaseMicroserviceVerticle() {
  private val logger = LoggerFactory.getLogger(ManagerVerticle::class.java)
  private lateinit var binder: ServiceBinder
  private lateinit var consumer: MessageConsumer<JsonObject>
  private lateinit var managerService: DeviceManagerService
  override fun start() {
    super.start()
    GlobalScope.launch {
      publishManagerService()
      initRouter()
    }.invokeOnCompletion {
      if(it!=null) logger.error(it.localizedMessage)
    }
  }
  /**
   * 部署ManagerService
   */
  private suspend fun publishManagerService(){
    val eventBus = vertx.eventBus()
    val sqlClient = MySQLClient.createShared(vertx,configMySQLClient())
    val sessionStore = SessionStore.create(vertx)
    val asyncMap = awaitResult<AsyncMap<String,String>> {
      vertx.sharedData().getAsyncMap(MANAGER_MAP_NAME,it)
    }
    managerService = DeviceManagerServiceImpl(vertx,eventBus,asyncMap,sqlClient,sessionStore)
    binder = ServiceBinder(vertx).setAddress(DeviceManagerService.SERVICE_ADDRESS)
    consumer = binder.register(DeviceManagerService::class.java, managerService)

    val proxyBuilder = ServiceProxyBuilder(vertx).setAddress(DeviceManagerService.SERVICE_ADDRESS)
    proxyBuilder.build(DeviceManagerService::class.java)

    publishEventBusService(DeviceManagerService.SERVICE_NAME, DeviceManagerService.SERVICE_ADDRESS, DeviceManagerService::class.java)

    logger.info(PUBLISH_SUCCESS)
  }

  private fun configMySQLClient(): JsonObject {
    return JsonObject(mapOf(
      "host" to "link.hdussta.cn",
      "username" to "root",
      "password" to "Admin88888",
      "database" to "sstalink"))
  }

  /**
   * 部署Restful服务
   * @API 下发设备状态 POST /api/manage/state/{设备id}
   * @API 查询设备状态 GET /api/manage/state/{设备id}
   */
  private fun initRouter(){
    val router = Router.router(vertx)
    router.route().handler(BodyHandler.create().setBodyLimit(100*1024))
    router.route(HttpMethod.POST,"/api/manage/state/:id").handler{ handlePostState(it,it.request().getParam("id")) }
    router.route(HttpMethod.GET,"/api/manage/state/:id").handler { handleGetState(it,it.request().getParam("id")) }
    vertx.createHttpServer().requestHandler(router::accept).listen(28081)
  }

  private fun handlePostState(context: RoutingContext, deviceId:String){
    val desired = context.bodyAsJson
    managerService.setState(deviceId,desired.toString()){
      if(it.failed())
        context.response().end(message(-1,it.cause().localizedMessage))
      else{
        context.response().end(message(1, SET_STATE_SUCCESS))
      }
    }
  }

  private fun handleGetState(context: RoutingContext,deviceId: String){
    managerService.getState(deviceId){
      if(it.failed()){
        context.response().end(message(-1,it.cause().localizedMessage))
      }else{
        context.response().end(messageState(1, GET_STATE_SUCCESS,it.result()))
      }
    }
  }

  companion object {
    private const val MANAGER_MAP_NAME = "async-manager-map"
    private const val PUBLISH_SUCCESS = "设备管理服务发布成功"
    private const val SET_STATE_SUCCESS = "状态设置成功"
    private const val GET_STATE_SUCCESS = "获取状态成功"
  }
}
