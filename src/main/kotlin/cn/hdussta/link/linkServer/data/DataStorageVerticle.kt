package cn.hdussta.link.linkServer.data

import cn.hdussta.link.linkServer.common.BaseMicroserviceVerticle
import cn.hdussta.link.linkServer.data.impl.DataStorageMySql
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.serviceproxy.ServiceBinder
import io.vertx.serviceproxy.ServiceProxyBuilder

class DataStorageVerticle:BaseMicroserviceVerticle() {
  private val logger = LoggerFactory.getLogger(DataStorageVerticle::class.java)
  lateinit var consumer: MessageConsumer<JsonObject>
  override fun start() {
    super.start()
    val sqlClient = MySQLClient.createShared(vertx, configMySQLClient())
    val dataStorageService = DataStorageMySql(vertx,discovery,sqlClient)
    val binder = ServiceBinder(vertx).setAddress(DataStorageMySql.SERVICE_ADDRESS)
    consumer = binder.register(DataHandleService::class.java, dataStorageService)

    val proxyBuilder = ServiceProxyBuilder(vertx).setAddress(DataStorageMySql.SERVICE_ADDRESS)
    proxyBuilder.build(DataHandleService::class.java)

    publishEventBusService(DataStorageMySql.SERVICE_NAME, DataStorageMySql.SERVICE_ADDRESS, DataHandleService::class.java)

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
    const val PUBLISH_SUCCESS = "数据存储服务发布成功"
  }
}
