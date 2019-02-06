package cn.hdussta.link.linkServer.data.impl

import cn.hdussta.link.linkServer.data.AbstractDataHandleService
import cn.hdussta.link.linkServer.manager.DeviceInfo
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.ext.web.client.sendJsonObjectAwait

class RedirectServiceImpl(private val webClient: WebClient) : AbstractDataHandleService() {
  override val address: String
    get() = SERVICE_ADDRESS
  override val name: String
    get() = "Redirect"

  override suspend fun handle(info: DeviceInfo, data: JsonObject,param:JsonObject) {
    param.getJsonArray("urls").forEach {
      val url = it as String
      webClient.postAbs(url).sendJsonObjectAwait(data.getJsonObject("data"))
    }
  }

  companion object {
    const val SERVICE_ADDRESS = "service.data.redirect"
  }
}
