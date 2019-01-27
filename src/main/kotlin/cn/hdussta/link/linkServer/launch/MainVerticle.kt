package cn.hdussta.link.linkServer.launch

import cn.hdussta.link.linkServer.dashboard.DashBoardVerticle
import cn.hdussta.link.linkServer.manager.ManagerVerticle
import cn.hdussta.link.linkServer.transport.http.HTTPVerticle
import cn.hdussta.link.linkServer.transport.mqtt.MQTTVerticle
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
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
    GlobalScope.launch(vertx.dispatcher()) {
      val managerVerticleId = awaitResult<String> {
        vertx.deployVerticle(ManagerVerticle(), it)
      }
      println(managerVerticleId)
      val httpVerticleId = awaitResult<String> {
        vertx.deployVerticle(HTTPVerticle(), it)
      }
      println(httpVerticleId)
      val mqttVerticleId = awaitResult<String> {
        vertx.deployVerticle(MQTTVerticle(),it)
      }
      println(mqttVerticleId)
      val dashboardId = awaitResult<String> {
        vertx.deployVerticle(DashBoardVerticle(),it)
      }
      println(dashboardId)
      startFuture.complete()
    }
  }
}
