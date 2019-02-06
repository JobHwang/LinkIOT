package cn.hdussta.link.linkServer.data

import cn.hdussta.link.linkServer.common.DEVICE_LOG_TABLE
import cn.hdussta.link.linkServer.manager.DeviceInfo
import cn.hdussta.link.linkServer.utils.jsonArray
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class AbstractDataHandleService:AbstractVerticle() {
  abstract val address:String
  abstract val name:String
  abstract suspend fun handle(info:DeviceInfo,data:JsonObject,param:JsonObject)
  private val logger = LoggerFactory.getLogger(this::class.java)
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
      val info = DeviceInfo(it.body().getJsonObject("info"))
      GlobalScope.launch(vertx.dispatcher()) {
        handle(info,it.body().getJsonObject("data"),it.body().getJsonObject("param")?: JsonObject())
      }.invokeOnCompletion {t->
        if(t!=null)
          fail(info,t.localizedMessage)
      }
    }
  }

  private fun publish(){
    logger.info("数据服务<$address>发布成功")
    this.vertx.eventBus().publish(DATA_HANDLE_SERVICE_PUBLISH_ADDRESS,JsonObject().put("name",name).put("address",address))
  }

  protected fun fail(info:DeviceInfo,cause:String){
    sqlClient.updateWithParams("INSERT INTO $DEVICE_LOG_TABLE (deviceid,level,cause) VALUES (?,?,?)"
      , JsonArray().add(info.id).add(LOG.SERVE.ordinal).add(cause)){
      if(it.failed()) println(it.cause())
    }
  }

  companion object {
    const val DATA_HANDLE_SERVICE_PUBLISH_ADDRESS = "service.data.publish"
  }
}
