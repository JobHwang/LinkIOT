package cn.hdussta.link.linkServer.transport

import cn.hdussta.link.linkServer.common.BaseMicroserviceVerticle
import cn.hdussta.link.linkServer.data.impl.DataStorageMySql
import cn.hdussta.link.linkServer.service.DataHandleService
import cn.hdussta.link.linkServer.service.DeviceManagerService
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.servicediscovery.types.EventBusService

abstract class AbstractTransportVerticle: BaseMicroserviceVerticle() {
  abstract val logger:Logger
  lateinit var deviceManagerService: DeviceManagerService
  lateinit var basicDataHandleService: DataHandleService
  protected fun initProxy(){
    EventBusService.getProxy(discovery, DeviceManagerService::class.java) {
      if (it.failed()) {
        logger.error(it.cause().localizedMessage)
        this.stop()
      } else {
        this.deviceManagerService = it.result()
      }
    }
    EventBusService.getServiceProxyWithJsonFilter(discovery, JsonObject().put("name", BASIC_SERVICE_NAME), DataHandleService::class.java) {
      if (it.failed()) {
        logger.error(it.cause().localizedMessage)
        this.stop()
      } else {
        this.basicDataHandleService = it.result()
      }
    }
  }
  companion object {
    const val BASIC_SERVICE_NAME = DataStorageMySql.SERVICE_NAME
  }
}
