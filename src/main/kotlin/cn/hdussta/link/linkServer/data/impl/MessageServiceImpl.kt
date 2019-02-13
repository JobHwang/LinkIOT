package cn.hdussta.link.linkServer.data.impl

import cn.hdussta.link.linkServer.data.AbstractDataHandleService
import cn.hdussta.link.linkServer.manager.DeviceInfo
import cn.hdussta.link.linkServer.service.MessageService
import io.vertx.core.json.JsonObject
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.types.EventBusService

class MessageServiceImpl(private val discovery: ServiceDiscovery) : AbstractDataHandleService() {
  override val address: String = "service.data.message"
  override val name: String = "Msg"
  private var ms: MessageService? = null
  override suspend fun handle(info: DeviceInfo, data: JsonObject, param: JsonObject) {
    val username = param.getString("username")
    val json = JsonObject()
      .put("device",JsonObject().put("name",info.name).put("id",info.id))
      .put("data", data)
      .put("action","data")
    ms?.let {
      it.sendMessageToUser(username, json) {}
    }?:run {
      EventBusService.getProxy(discovery, MessageService::class.java) {
        if(it.succeeded()){
          it.result().sendMessageToUser(username, json) {}
          ms = it.result()
        }
      }
    }
  }
}
