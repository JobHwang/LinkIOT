package cn.hdussta.link.linkServer.transport.mqtt

import cn.hdussta.link.linkServer.common.BaseMicroserviceVerticle
import io.vertx.core.logging.LoggerFactory
import io.vertx.mqtt.MqttServer
import io.vertx.mqtt.MqttServerOptions

/**
 * @name MQTTVerticle
 * @description (Unfinished!) 部署MQTT服务端
 * @author Wooyme
 */
class MQTTVerticle:BaseMicroserviceVerticle() {
  val log = LoggerFactory.getLogger(MQTTVerticle::class.java)
  val options = MqttServerOptions()
    .setPort(1883)
    .setHost("0.0.0.0")
  override fun start() {
    super.start()
    val server = MqttServer.create(vertx)
    server.endpointHandler { endpoint ->
      if(endpoint.auth()==null){
        return@endpointHandler
      }

      log.info("connected client ${endpoint.clientIdentifier()}")
      endpoint.publishHandler { message->

      }
    }
  }
}
