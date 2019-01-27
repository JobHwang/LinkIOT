package cn.hdussta.link.linkServer.data

import cn.hdussta.link.linkServer.common.LOG_TABLE
import cn.hdussta.link.linkServer.manager.DeviceInfo
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class AbstractDataHandleService:AbstractVerticle() {
  abstract val address:String
  abstract val name:String
  abstract suspend fun handle(info:DeviceInfo,data:JsonObject,param:JsonObject)
  private val sqlConfig = JsonObject(mapOf(
    "host" to "link.hdussta.cn",
    "username" to "root",
    "password" to "Admin88888",
    "database" to "sstalink"))
  protected val sqlClient by lazy { MySQLClient.createShared(vertx, sqlConfig) }
  override fun start() {
    super.start()
    this.consume()
    this.publish()
  }

  private fun consume(){
    this.vertx.eventBus().consumer<JsonObject>(address){ it ->
      if(!it.headers().contains(name)){
        return@consumer
      }
      val info = DeviceInfo(it.body().getJsonObject("info"))
      GlobalScope.launch(vertx.dispatcher()) {
        handle(info,it.body().getJsonObject("data"),it.body().getJsonObject("param"))
      }.invokeOnCompletion {t->
        if(t!=null)
          fail(info,t.localizedMessage)
      }
    }
  }

  private fun publish(){
    this.vertx.eventBus().publish(DATA_HANDLE_SERVICE_PUBLISH_ADDRESS,JsonObject().put("name",name).put("address",address))
  }

  protected fun fail(info:DeviceInfo,cause:String){
    sqlClient.updateWithParams("INSERT INTO $LOG_TABLE (deviceid,cause) VALUES (?,?)", JsonArray().add(info.id).add(cause)){}
  }

  companion object {
    const val DATA_HANDLE_SERVICE_PUBLISH_ADDRESS = "service.data.publish"
  }
}
