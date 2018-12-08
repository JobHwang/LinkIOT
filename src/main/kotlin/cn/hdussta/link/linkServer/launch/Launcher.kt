package cn.hdussta.link.linkServer.launch

import cn.hdussta.link.linkServer.data.DataStorageVerticle
import cn.hdussta.link.linkServer.manager.ManagerVerticle
import cn.hdussta.link.linkServer.transport.http.HTTPVerticle
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitResult

/**
 * 测试用入口函数
 */
suspend fun main(args: Array<String>) {
  val vertx = Vertx.vertx()
  val managerVerticleId = awaitResult<String> {
    vertx.deployVerticle(ManagerVerticle(), it)
  }
  println(managerVerticleId)
  val dataStorageVerticleId = awaitResult<String> {
    vertx.deployVerticle(DataStorageVerticle(), it)
  }
  println(dataStorageVerticleId)
  val httpVerticleId = awaitResult<String> {
    vertx.deployVerticle(HTTPVerticle(), it)
  }
  println(httpVerticleId)

}
