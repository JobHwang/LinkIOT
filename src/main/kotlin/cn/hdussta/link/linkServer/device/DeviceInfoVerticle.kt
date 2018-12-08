package cn.hdussta.link.linkServer.device

import cn.hdussta.link.linkServer.common.BaseMicroserviceVerticle
import cn.hdussta.link.linkServer.manager.ManagerService
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.sstore.SessionStore
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.servicediscovery.types.EventBusService
import io.vertx.serviceproxy.ServiceBinder
import io.vertx.serviceproxy.ServiceProxyBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DeviceInfoVerticle : BaseMicroserviceVerticle() {
  private val logger = LoggerFactory.getLogger(DeviceInfoVerticle::class.java)

  private lateinit var deviceInfoService: DeviceInfoService
  private lateinit var sqlClient: SQLClient
  private lateinit var binder: ServiceBinder
  private lateinit var consumer: MessageConsumer<JsonObject>
  override fun start() {
    super.start()
    GlobalScope.launch(vertx.dispatcher()) {
      sqlClient = MySQLClient.createShared(vertx, configMySQLClient())
      val managerService = awaitResult<ManagerService> {
        EventBusService.getProxy(discovery,ManagerService::class.java,it)
      }
      deviceInfoService = DeviceInfoService.createService(vertx, sqlClient, SessionStore.create(vertx),managerService)
      binder = ServiceBinder(vertx).setAddress(DeviceInfoService.SERVICE_ADDRESS)
      consumer = binder.register(DeviceInfoService::class.java, deviceInfoService)

      val proxyBuilder = ServiceProxyBuilder(vertx).setAddress(DeviceInfoService.SERVICE_ADDRESS)
      proxyBuilder.build(DeviceInfoService::class.java)

      logger.info(PUBLISH_SUCCESS)
    }.invokeOnCompletion {
      if(it!=null) logger.error(it.localizedMessage)
    }
  }

  private fun configMySQLClient(): JsonObject {
    return JsonObject(mapOf(
      "host" to "link.hdussta.cn",
      "username" to "root",
      "password" to "Admin88888",
      "database" to "sstalink"))
  }

  companion object {
    const val PUBLISH_SUCCESS = "设备信息服务发布成功"
  }

}
