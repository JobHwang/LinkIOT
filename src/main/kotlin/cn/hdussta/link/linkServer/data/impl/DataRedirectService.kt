package cn.hdussta.link.linkServer.data.impl

import cn.hdussta.link.linkServer.manager.DeviceInfo
import cn.hdussta.link.linkServer.service.DataHandleService
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.sql.queryAwait
import io.vertx.servicediscovery.ServiceDiscovery
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DataRedirectService(private val vertx: Vertx,private val sqlClient: SQLClient,private val webClient: WebClient,override val discovery: ServiceDiscovery ):AbstractDataHandleService() {
  override val logger: Logger = LoggerFactory.getLogger(DataRedirectService::class.java)
  override val ruleName: String = RULE_NAME
  override fun handle(device: DeviceInfo, data: JsonObject, handler: Handler<AsyncResult<JsonObject>>): DataHandleService {
    GlobalScope.launch(vertx.dispatcher()) {
      val result = sqlClient.queryAwait("SELECT url FROM $REDIRECT_TABLE WHERE deviceid='${device.id}'")
      result.results.forEach {
        val url = it.getString(0)
        webClient.postAbs(url).sendJsonObject(data.getJsonObject("data")){ response ->
          if(response.failed())
            fail(response.cause().localizedMessage,data,handler)
          else
            next(response.result().bodyAsString(),device,data,handler)
        }
      }
    }.invokeOnCompletion {
      if(it!=null)
        fail(it.localizedMessage,data,handler)
    }
    return this
  }

  companion object {
    private const val RULE_NAME = "redirect"
    private const val REDIRECT_TABLE = "sstalink_redirect"
    const val SERVICE_ADDRESS = "service.data.redirect"
    const val SERVICE_NAME = "data-redirect-service"
  }
}
