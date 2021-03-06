package cn.hdussta.link.linkServer.manager

import cn.hdussta.link.linkServer.common.BaseMicroserviceVerticle
import cn.hdussta.link.linkServer.common.DEVICE_TABLE
import cn.hdussta.link.linkServer.manager.impl.DeviceManagerServiceImpl
import cn.hdussta.link.linkServer.service.DeviceManagerService
import cn.hdussta.link.linkServer.service.ScriptService
import cn.hdussta.link.linkServer.utils.jsonArray
import io.vertx.core.Future
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.AsyncMap
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.web.sstore.SessionStore
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
  private lateinit var managerService: DeviceManagerService
  private val sqlClient by lazy {
    MySQLClient.createShared(vertx,config().getJsonObject("mysql"))
  }
  override fun start(startFuture: Future<Void>) {
    super.start()
    GlobalScope.launch {
      publishManagerService()
    }.invokeOnCompletion {
      if(it!=null) logger.error(it.localizedMessage)
      startFuture.complete()
    }
  }
  /**
   * 部署ManagerService
   */
  private suspend fun publishManagerService(){
    val eventBus = vertx.eventBus()

    val sessionStore = SessionStore.create(vertx)
    val asyncMap = awaitResult<AsyncMap<String,String>> {
      vertx.sharedData().getAsyncMap(MANAGER_MAP_NAME,it)
    }
    val scriptService = awaitResult<ScriptService> { handler->
      EventBusService.getProxy(discovery, ScriptService::class.java) {
        if (it.failed()) {
          logger.error(it.cause().localizedMessage)
          this.stop()
        } else {
          handler.handle(it)
        }
      }
    }
    managerService = DeviceManagerServiceImpl(vertx,eventBus,asyncMap,sqlClient,sessionStore,scriptService)
    binder = ServiceBinder(vertx).setAddress(DeviceManagerService.SERVICE_ADDRESS)
    consumer = binder.register(DeviceManagerService::class.java, managerService)

    val proxyBuilder = ServiceProxyBuilder(vertx).setAddress(DeviceManagerService.SERVICE_ADDRESS)
    proxyBuilder.build(DeviceManagerService::class.java)

    publishEventBusService(DeviceManagerService.SERVICE_NAME, DeviceManagerService.SERVICE_ADDRESS, DeviceManagerService::class.java)

    logger.info(PUBLISH_SUCCESS)
    refreshAllDevices(sqlClient)
  }

  private fun refreshAllDevices(sqlClient: AsyncSQLClient){
    sqlClient.update("UPDATE $DEVICE_TABLE SET state=0 WHERE state=1"){}
  }


  companion object {
    private const val MANAGER_MAP_NAME = "async-manager-map"
    private const val PUBLISH_SUCCESS = "设备管理服务发布成功"
  }
}
