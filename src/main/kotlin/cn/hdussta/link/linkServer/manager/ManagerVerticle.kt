package cn.hdussta.link.linkServer.manager

import cn.hdussta.link.linkServer.common.BaseMicroserviceVerticle
import cn.hdussta.link.linkServer.manager.impl.DeviceManagerServiceImpl
import cn.hdussta.link.linkServer.service.DeviceManagerService
import cn.hdussta.link.linkServer.service.ScriptService
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
  override fun start() {
    super.start()
    GlobalScope.launch {
      publishManagerService()
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
  }

  private fun configMySQLClient(): JsonObject {
    return JsonObject(mapOf(
      "host" to "link.hdussta.cn",
      "username" to "root",
      "password" to "Admin88888",
      "database" to "sstalink"))
  }


  companion object {
    private const val MANAGER_MAP_NAME = "async-manager-map"
    private const val PUBLISH_SUCCESS = "设备管理服务发布成功"
  }
}
