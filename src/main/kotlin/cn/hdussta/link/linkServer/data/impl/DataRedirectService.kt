package cn.hdussta.link.linkServer.data.impl

import cn.hdussta.link.linkServer.manager.DeviceInfo
import cn.hdussta.link.linkServer.service.DataHandleService
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.servicediscovery.ServiceDiscovery

class DataRedirectService:AbstractDataHandleService() {
  override val discovery: ServiceDiscovery
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
  override val logger: Logger
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
  override val ruleName: String
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

  override fun handle(device: DeviceInfo?, data: JsonObject?, handler: Handler<AsyncResult<JsonObject>>?): DataHandleService {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}
