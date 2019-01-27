package cn.hdussta.link.linkServer.transport

import cn.hdussta.link.linkServer.common.BaseMicroserviceVerticle
import cn.hdussta.link.linkServer.data.impl.ScriptServiceImpl
import cn.hdussta.link.linkServer.service.DeviceManagerService
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.servicediscovery.types.EventBusService

abstract class AbstractTransportVerticle: BaseMicroserviceVerticle() {
  abstract val logger:Logger
  lateinit var deviceManagerService: DeviceManagerService
  lateinit var scriptService: cn.hdussta.link.linkServer.service.ScriptService

  /**
   * 初始化Transport模块必要的Service
   */
  protected fun initProxy(){
    EventBusService.getProxy(discovery, DeviceManagerService::class.java) {
      if (it.failed()) {
        logger.error(it.cause().localizedMessage)
        this.stop()
      } else {
        this.deviceManagerService = it.result()
      }
    }
    EventBusService.getServiceProxyWithJsonFilter(discovery, JsonObject().put("name", BASIC_SERVICE_NAME), cn.hdussta.link.linkServer.service.ScriptService::class.java) {
      if (it.failed()) {
        logger.error(it.cause().localizedMessage)
        this.stop()
      } else {
        this.scriptService = it.result()
      }
    }
  }
  companion object {
    const val BASIC_SERVICE_NAME = ScriptServiceImpl.SERVICE_NAME
  }
}
