package cn.hdussta.link.linkServer.transport.mqtt

import cn.hdussta.link.linkServer.common.BaseMicroserviceVerticle
import cn.hdussta.link.linkServer.manager.DeviceInfo
import cn.hdussta.link.linkServer.service.DataHandleService
import cn.hdussta.link.linkServer.service.DeviceManagerService
import cn.hdussta.link.linkServer.transport.AbstractTransportVerticle
import io.netty.handler.codec.mqtt.MqttConnectReturnCode
import io.vertx.core.logging.LoggerFactory
import io.vertx.mqtt.MqttEndpoint
import io.vertx.mqtt.MqttServer
import io.vertx.mqtt.MqttServerOptions
import io.netty.handler.codec.mqtt.MqttQoS
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.mqtt.MqttClient
import io.vertx.servicediscovery.types.EventBusService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.ArrayList



/**
 * @name MQTTVerticle
 * @description (Unfinished!) 部署MQTT服务端
 * @author Wooyme
 */
class MQTTVerticle:AbstractTransportVerticle() {
  override val logger: Logger = LoggerFactory.getLogger(MQTTVerticle::class.java)
  private val options = MqttServerOptions()
    .setPort(1883)
    .setHost("0.0.0.0")
  override fun start() {
    super.start()
    val server = MqttServer.create(vertx,options)
    server.endpointHandler { endpoint ->
      logger.info("connected client ${endpoint.clientIdentifier()}")
      doConnect(endpoint)
    }
    vertx.executeBlocking<MqttServer>({future->
      server.listen(future.completer())
    }){
      logger.info("MqttServer listen at 1883")
    }
    initProxy()
  }

  private fun doConnect(endpoint: MqttEndpoint){
    if(endpoint.auth()==null){
      endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED)
      return
    }
    val id = endpoint.auth().username
    val secret = endpoint.auth().password
    deviceManagerService.login(id,secret){
      if(it.failed()){
        endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD)
      }else{
        acceptConnect(endpoint,it.result())
      }
    }
  }

  private fun acceptConnect(endpoint: MqttEndpoint,token:String) {
    val clientId = endpoint.clientIdentifier()
    endpoint.accept(false)
      .closeHandler {
        logger.debug("[{}] closed", clientId)
        deviceManagerService.logout(token){result->
          if(result.failed()){
            logger.error(result.cause())
          }
        }
      }
      .subscribeHandler { subscribe ->
        val grantedQosLevels = ArrayList<MqttQoS>()

        for (s in subscribe.topicSubscriptions()) {
          logger.info("[{}] Subscription for {} with QoS {}", clientId, s.topicName(), s.qualityOfService())
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
        if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
          endpoint.publishAcknowledge(message.messageId())
        } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
          endpoint.publishReceived(message.messageId())
        }
        when(topicName){
          "device-state"-> handleState(endpoint,token,buffer.toString())
          "device-data"-> handleData(endpoint,token,buffer.toJsonObject())
        }

      }
      .publishReleaseHandler { messageId ->
        logger.debug("complete message :{}", messageId)
        endpoint.publishComplete(messageId)
      }
  }

  private fun handleState(endpoint: MqttEndpoint,token: String,state:String) {
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

  private fun handleData(endpoint: MqttEndpoint,token: String,data:JsonObject) {
    GlobalScope.launch(vertx.dispatcher()) {
      val device = awaitResult<DeviceInfo> {
        deviceManagerService.getDeviceByToken(token, it)
      }
      val result = awaitResult<JsonObject> {
        basicDataHandleService.handle(device,data,it)
      }
      endpoint.publish("device-data", result.toBuffer(),MqttQoS.AT_LEAST_ONCE,false,false)

    }.invokeOnCompletion {
      if (it != null) {
        endpoint.publish("device-data", Buffer.buffer(it.localizedMessage), MqttQoS.AT_LEAST_ONCE, false, false)
      }
    }
  }
}
