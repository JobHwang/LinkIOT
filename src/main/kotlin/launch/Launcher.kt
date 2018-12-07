package launch

import cn.hdussta.link.linkServer.data.DataStorageVerticle
import cn.hdussta.link.linkServer.device.DeviceInfoVerticle
import cn.hdussta.link.linkServer.transport.http.HTTPVerticle
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitResult

suspend fun main(args: Array<String>) {
  val vertx = Vertx.vertx()
  val deviceInfoVerticleId = awaitResult<String> {
    vertx.deployVerticle(DeviceInfoVerticle(), it)
  }
  println(deviceInfoVerticleId)
  val dataStorageVerticleId = awaitResult<String> {
    vertx.deployVerticle(DataStorageVerticle(), it)
  }
  println(dataStorageVerticleId)
  val httpVerticleId = awaitResult<String> {
    vertx.deployVerticle(HTTPVerticle(), it)
  }
  println(httpVerticleId)
}
