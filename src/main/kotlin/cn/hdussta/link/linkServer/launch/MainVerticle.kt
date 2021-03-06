package cn.hdussta.link.linkServer.launch

import cn.hdussta.link.linkServer.common.*
import cn.hdussta.link.linkServer.dashboard.DashBoardVerticle
import cn.hdussta.link.linkServer.data.ScriptVerticle
import cn.hdussta.link.linkServer.manager.ManagerVerticle
import cn.hdussta.link.linkServer.transport.http.HTTPVerticle
import cn.hdussta.link.linkServer.transport.mqtt.MQTTVerticle
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4JLoggerFactory
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.ConfigStoreOptions
import io.vertx.kotlin.config.getConfigAwait
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @name MainVerticle
 * @description 入口Verticle
 * @author Wooyme
 */
class MainVerticle : AbstractVerticle() {
  override fun start(startFuture: Future<Void>) {
    val configFile = ConfigStoreOptions()
      .setType("file")
      .setFormat("json")
      .setConfig(JsonObject().put("path","config.json"))
    InternalLoggerFactory.setDefaultFactory(Log4JLoggerFactory.INSTANCE)
    GlobalScope.launch(vertx.dispatcher()) {
      val config = ConfigRetriever.create(vertx, ConfigRetrieverOptions().addStore(configFile)).getConfigAwait()
      configureTables(config.getJsonObject("tables"))
      awaitResult<String> {
        vertx.deployVerticle(ScriptVerticle(), DeploymentOptions().setConfig(config), it)
      }.let(::println)

      awaitResult<String> {
        vertx.deployVerticle(ManagerVerticle(), DeploymentOptions().setConfig(config), it)
      }.let(::println)

      awaitResult<String> {
        vertx.deployVerticle(HTTPVerticle(), DeploymentOptions().setConfig(config), it)
      }.let(::println)

      awaitResult<String> {
        vertx.deployVerticle(MQTTVerticle(), DeploymentOptions().setConfig(config),it)
      }.let(::println)

      awaitResult<String> {
        vertx.deployVerticle(DashBoardVerticle(), DeploymentOptions().setConfig(config),it)
      }.let(::println)

    }.invokeOnCompletion {
      startFuture.complete()
    }
  }

  private fun configureTables(config:JsonObject){
    if(config.containsKey("sensor")){
      SENSOR_TABLE = config.getString("sensor")
    }
    if(config.containsKey("device")){
      DEVICE_TABLE = config.getString("device")
    }
    if(config.containsKey("user")){
      USER_TABLE = config.getString("user")
    }
    if(config.containsKey("device_log")){
      DEVICE_LOG_TABLE = config.getString("device_log")
    }
    if(config.containsKey("user_log")){
      USER_LOG_TABLE = config.getString("user_log")
    }
    if(config.containsKey("alarm_log")){
      ALARM_LOG_TABLE = config.getString("alarm_log")
    }
    if(config.containsKey("data")){
      DATA_TABLE = {type:String->config.getString("data")+"_$type"}
    }
  }
}
