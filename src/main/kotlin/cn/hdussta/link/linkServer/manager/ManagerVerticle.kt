package cn.hdussta.link.linkServer.manager

import cn.hdussta.link.linkServer.common.BaseMicroserviceVerticle
import cn.hdussta.link.linkServer.service.DeviceInfoService
import cn.hdussta.link.linkServer.device.DeviceInfoVerticle
import cn.hdussta.link.linkServer.manager.impl.ManagerServiceImpl
import cn.hdussta.link.linkServer.service.ManagerService
import cn.hdussta.link.linkServer.utils.message
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.AsyncMap
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.servicediscovery.types.EventBusService
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
  private lateinit var managerService: ManagerService
  override fun start() {
    super.start()
    //发布DeviceInfoService
    publishEventBusService(DeviceInfoService.SERVICE_NAME, DeviceInfoService.SERVICE_ADDRESS, DeviceInfoService::class.java)
    //发布ManagerService
    publishEventBusService(ManagerService.SERVICE_NAME, ManagerService.SERVICE_ADDRESS, ManagerService::class.java)

    GlobalScope.launch {
      publishManagerService()
      initRouter()
      awaitResult<String> {
        vertx.deployVerticle(DeviceInfoVerticle(),it)
      }
    }.invokeOnCompletion {
      if(it!=null) logger.error(it.localizedMessage)
    }
  }
  /**
   * 部署ManagerService
   */
  private suspend fun publishManagerService(){
    val eventBus = vertx.eventBus()
    val deviceInfoService = awaitResult<DeviceInfoService> {
      EventBusService.getProxy(discovery, DeviceInfoService::class.java,it)
    }
    val asyncMap = awaitResult<AsyncMap<String,ManageableDeviceInfo>> {
      vertx.sharedData().getAsyncMap(MANAGER_MAP_NAME,it)
    }
    managerService = ManagerServiceImpl(vertx,eventBus,deviceInfoService,asyncMap)
    binder = ServiceBinder(vertx).setAddress(ManagerService.SERVICE_ADDRESS)
    consumer = binder.register(ManagerService::class.java, managerService)

    val proxyBuilder = ServiceProxyBuilder(vertx).setAddress(ManagerService.SERVICE_ADDRESS)
    proxyBuilder.build(DeviceInfoService::class.java)

    logger.info(PUBLISH_SUCCESS)
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
    managerService.setState(deviceId,desired){
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
        context.response().end(message(1, GET_STATE_SUCCESS,it.result()))
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
