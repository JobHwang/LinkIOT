package cn.hdussta.link.linkServer.launch

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions


/**
 * 测试用入口函数
 */
fun main(args: Array<String>) {
  val options = VertxOptions()
  val vertx = Vertx.vertx(options)
  vertx.deployVerticle(MainVerticle())
}

