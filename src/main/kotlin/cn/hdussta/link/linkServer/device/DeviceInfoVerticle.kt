package cn.hdussta.link.linkServer.device

import cn.hdussta.link.linkServer.common.BaseMicroserviceVerticle
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.sstore.SessionStore
import io.vertx.serviceproxy.ServiceBinder
import io.vertx.serviceproxy.ServiceProxyBuilder

class DeviceInfoVerticle : BaseMicroserviceVerticle() {
  private val logger = LoggerFactory.getLogger(DeviceInfoVerticle::class.java)

  lateinit var deviceInfoService: DeviceInfoService
  lateinit var sqlClient: SQLClient
  lateinit var binder: ServiceBinder
  lateinit var consumer: MessageConsumer<JsonObject>
  override fun start() {
    super.start()
    sqlClient = MySQLClient.createShared(vertx, configMySQLClient())

    deviceInfoService = DeviceInfoService.createService(vertx, sqlClient, SessionStore.create(vertx))
    binder = ServiceBinder(vertx).setAddress(DeviceInfoService.SERVICE_ADDRESS)
    consumer = binder.register(DeviceInfoService::class.java, deviceInfoService)

    val proxyBuilder = ServiceProxyBuilder(vertx).setAddress(DeviceInfoService.SERVICE_ADDRESS)
    proxyBuilder.build(DeviceInfoService::class.java)

    publishEventBusService(DeviceInfoService.SERVICE_NAME, DeviceInfoService.SERVICE_ADDRESS, DeviceInfoService::class.java)

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
    const val DEVICE_MAP_NAME = "async-device-map"
    const val PUBLISH_SUCCESS = "设备信息服务发布成功"
  }

}
