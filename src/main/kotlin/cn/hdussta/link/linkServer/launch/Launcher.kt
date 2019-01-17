package cn.hdussta.link.linkServer.launch

import cn.hdussta.link.linkServer.dashboard.DashBoardVerticle
import cn.hdussta.link.linkServer.data.DataRedirectVerticle
import cn.hdussta.link.linkServer.data.DataStorageVerticle
import cn.hdussta.link.linkServer.manager.ManagerVerticle
import cn.hdussta.link.linkServer.transport.http.HTTPVerticle
import cn.hdussta.link.linkServer.transport.mqtt.MQTTVerticle
import io.vertx.core.Vertx
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.core.VertxOptions



/**
 * 测试用入口函数
 */
suspend fun main(args: Array<String>) {
  val options = VertxOptions()
  options.blockedThreadCheckInterval = (1000 * 60 * 60).toLong()
  val vertx = Vertx.vertx(options)

  awaitResult<String> {
    vertx.deployVerticle(ManagerVerticle(), it)
  }.let(::println)

  awaitResult<String> {
    vertx.deployVerticle(DataStorageVerticle(), it)
  }.let(::println)

  awaitResult<String> {
    vertx.deployVerticle(DataRedirectVerticle(), it)
  }.let(::println)

  awaitResult<String> {
    vertx.deployVerticle(HTTPVerticle(), it)
  }.let(::println)

  awaitResult<String> {
    vertx.deployVerticle(MQTTVerticle(),it)
  }.let(::println)

  awaitResult<String> {
    vertx.deployVerticle(DashBoardVerticle(),it)
  }.let(::println)

}

