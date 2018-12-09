package cn.hdussta.link.linkServer.transport.mqtt

import io.netty.handler.codec.mqtt.MqttQoS
import io.vertx.core.Vertx
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.mqtt.connectAwait
import io.vertx.mqtt.MqttClient
import io.vertx.mqtt.MqttClientOptions

suspend fun main() {
  val vertx = Vertx.vertx()
  test(vertx,"ffff922cd","89ea539f930d28962e01fb9c9e9ac24cd62cf8d5e0b1ca5ad08c7e42bee0d5a8")
}

suspend fun test(vertx: Vertx,id:String,secret:String){
  val client = MqttClient.create(vertx
    , MqttClientOptions()
    .setUsername(id)
    .setPassword(secret))
  val message = client.connectAwait(1883, "127.0.0.1")
  client.subscribe(mapOf("device-state" to MqttQoS.AT_LEAST_ONCE.value()))
  client.publishHandler {
    println(it.payload().toString())
  }
  client.publish("device-data"
    , JsonObject("data" to JsonObject("61" to -100,"67" to "world")).toBuffer()
    ,MqttQoS.AT_LEAST_ONCE
    ,false
    ,false)
  vertx.setTimer(60*1000L){
    client.publish("device-state"
      , JsonObject("led" to "red").toBuffer()
      , MqttQoS.AT_LEAST_ONCE
      , false
      , false)
  }
}
