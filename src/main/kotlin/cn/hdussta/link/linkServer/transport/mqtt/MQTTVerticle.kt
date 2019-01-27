package cn.hdussta.link.linkServer.transport.mqtt

import cn.hdussta.link.linkServer.manager.DeviceInfo
import cn.hdussta.link.linkServer.service.DeviceManagerService
import cn.hdussta.link.linkServer.transport.AbstractTransportVerticle
import io.netty.handler.codec.mqtt.MqttConnectReturnCode
import io.netty.handler.codec.mqtt.MqttQoS
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.LocalMap
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.mqtt.MqttEndpoint
import io.vertx.mqtt.MqttServer
import io.vertx.mqtt.MqttServerOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


/**
 * @name MQTTVerticle
 * @description 部署MQTT服务端
 * @author Wooyme
 */
class MQTTVerticle : AbstractTransportVerticle() {
  override val logger: Logger = LoggerFactory.getLogger(MQTTVerticle::class.java)
  private val options = MqttServerOptions()
    .setPort(1883)
    .setHost("0.0.0.0")
  private lateinit var localMap: LocalMap<String, MqttEndpoint>
  override fun start() {
    super.start()
    val server = MqttServer.create(vertx, options)
    server.endpointHandler { endpoint ->
      logger.info("connected client ${endpoint.clientIdentifier()}")
      doConnect(endpoint)
    }
    vertx.executeBlocking<MqttServer>({ future ->
      server.listen(future.completer())
    }) {
      logger.info("MqttServer listen at 1883")
    }
    initProxy()
    localMap = vertx.sharedData().getLocalMap("mqtt-local-map")

    vertx.eventBus().consumer<JsonObject>(DeviceManagerService.PUBLISH_MANAGE_COMMAND) {
      handleCommand(it.body())
    }

  }

  private fun doConnect(endpoint: MqttEndpoint) {
    if (endpoint.auth() == null) {
      endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED)
      return
    }
    val id = endpoint.auth().username
    val secret = endpoint.auth().password
    deviceManagerService.login(id, secret, true) {
      if (it.failed()) {
        endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD)
      } else {
        acceptConnect(endpoint, it.result())
      }
    }
  }

  private fun acceptConnect(endpoint: MqttEndpoint, token: String) {
    val clientId = endpoint.clientIdentifier()
    endpoint.accept(false)
      .closeHandler {
        deviceManagerService.logout(token) { result ->
          if (result.failed()) {
            logger.error(result.cause())
          }
        }
      }
      .subscribeHandler { subscribe ->
        val grantedQosLevels = ArrayList<MqttQoS>()
        for (s in subscribe.topicSubscriptions()) {
          grantedQosLevels.add(s.qualityOfService())
        }
        endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels)
      }
      .unsubscribeHandler { unsubscribe -> endpoint.unsubscribeAcknowledge(unsubscribe.messageId()) }
      .exceptionHandler { e -> logger.error(clientId, e) }
      .publishHandler { message ->
        //设备推送了消息
        val topicName = message.topicName()
        val buffer = message.payload()
        logger.info("Topic:$topicName,Payload:$buffer")
        if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
          endpoint.publishAcknowledge(message.messageId())
        } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
          endpoint.publishReceived(message.messageId())
        }
        when (topicName) {
          "device-state" -> handleState(endpoint, token, buffer.toString())
          "device-data" -> handleData(endpoint, token, buffer.toJsonObject())
        }
      }
      .publishReleaseHandler { messageId -> endpoint.publishComplete(messageId) }
  }

  private fun handleState(endpoint: MqttEndpoint, token: String, state: String) {
    deviceManagerService.updateState(token, state) { updateResult ->
      if (updateResult.failed()) {
        logger.error(updateResult.cause())
      } else {
        if (updateResult.result() != null) {
          endpoint.publish("device-state", Buffer.buffer(updateResult.result()), MqttQoS.AT_LEAST_ONCE, false, false)
        }
      }
    }
  }

  private fun handleData(endpoint: MqttEndpoint, token: String, data: JsonObject) {
    GlobalScope.launch(vertx.dispatcher()) {
      val device = awaitResult<DeviceInfo> {
        deviceManagerService.getDeviceByToken(token, it)
      }
      val result = awaitResult<JsonObject> {
        scriptService.handle(device, data, it)
      }
      endpoint.publish("device-data", result.toBuffer(), MqttQoS.AT_LEAST_ONCE, false, false)

    }.invokeOnCompletion {
      if (it != null) {
        endpoint.publish("device-data", Buffer.buffer(it.localizedMessage), MqttQoS.AT_LEAST_ONCE, false, false)
      }
    }
  }

  private fun handleCommand(cmd:JsonObject){
    val body = cmd.getJsonObject("body")
    when(cmd.getString("action")){
      "setState"->{
        val desired = body.getString("desired")
        val token = body.getString("token")
        val endpoint = localMap[token]
        endpoint?.publish("device-state", Buffer.buffer(desired), MqttQoS.AT_LEAST_ONCE, false, false)
      }
      "forceClose"-> {
        val token = body.getString("token")
        localMap[token]?.close()
      }
    }
  }
}
