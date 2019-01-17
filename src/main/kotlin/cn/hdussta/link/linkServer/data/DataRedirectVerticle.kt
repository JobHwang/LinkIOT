package cn.hdussta.link.linkServer.data

import cn.hdussta.link.linkServer.common.BaseMicroserviceVerticle
import cn.hdussta.link.linkServer.data.impl.DataRedirectService
import cn.hdussta.link.linkServer.data.impl.DataStorageMySql
import cn.hdussta.link.linkServer.service.DataHandleService
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.serviceproxy.ServiceBinder
import io.vertx.serviceproxy.ServiceProxyBuilder

class DataRedirectVerticle:BaseMicroserviceVerticle() {
  private val logger = LoggerFactory.getLogger(DataRedirectVerticle::class.java)
  private val sqlConfig = JsonObject(mapOf(
    "host" to "link.hdussta.cn",
    "username" to "root",
    "password" to "Admin88888",
    "database" to "sstalink"))
  private lateinit var consumer: MessageConsumer<JsonObject>

  override fun start() {
    super.start()
    val sqlClient = MySQLClient.createShared(vertx, sqlConfig)
    val dataStorageService = DataStorageMySql(vertx,discovery,sqlClient)
    val binder = ServiceBinder(vertx).setAddress(DataStorageMySql.SERVICE_ADDRESS)
    consumer = binder.register(DataHandleService::class.java, dataStorageService)

    val proxyBuilder = ServiceProxyBuilder(vertx).setAddress(DataStorageMySql.SERVICE_ADDRESS)
    proxyBuilder.build(DataHandleService::class.java)

    publishEventBusService(DataRedirectService.SERVICE_NAME, DataRedirectService.SERVICE_ADDRESS, DataHandleService::class.java)

    logger.info(DataRedirectVerticle.PUBLISH_SUCCESS)
  }

  companion object {
    private const val PUBLISH_SUCCESS = "数据转发服务发布成功"
  }
}
