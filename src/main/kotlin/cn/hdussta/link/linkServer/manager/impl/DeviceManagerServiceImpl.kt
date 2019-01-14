package cn.hdussta.link.linkServer.manager.impl

import cn.hdussta.link.linkServer.manager.DeviceInfo
import cn.hdussta.link.linkServer.common.DeviceStatus
import cn.hdussta.link.linkServer.service.DeviceManagerService
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.AsyncMap
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.sstore.SessionStore
import io.vertx.kotlin.core.shareddata.getAwait
import io.vertx.kotlin.core.shareddata.removeAwait
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import io.vertx.kotlin.ext.web.sstore.deleteAwait
import io.vertx.kotlin.ext.web.sstore.getAwait
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class DeviceManagerServiceImpl(private val vertx: Vertx, private val eventBus: EventBus, private val asyncDeviceMap: AsyncMap<String, String>, private val sqlClient: SQLClient, private val sessionStore: SessionStore): DeviceManagerService {
  private val logger = LoggerFactory.getLogger(DeviceManagerService::class.java)

  private fun publishCommand(action:String,body:JsonObject){
    eventBus.publish(DeviceManagerService.PUBLISH_MANAGE_COMMAND,JsonObject().put("action",action).put("body",body))
  }

  override fun login(id: String, secret: String, isLongConnection:Boolean,handler: Handler<AsyncResult<String>>): DeviceManagerService {
    val future = Future.future<String>().setHandler(handler)
    GlobalScope.launch(vertx.dispatcher()) {
      val result = sqlClient.querySingleWithParamsAwait("SELECT deviceid,name,secret,state,config FROM sstalink_device WHERE deviceid=? and secret=? and state=${DeviceStatus.OFF.ordinal}"
        , JsonArray(listOf(id, secret)))
      if (result == null) {
        future.fail(DEVICE_NOT_FOUND)
        return@launch
      }
      val sensorResult = sqlClient.queryWithParamsAwait("SELECT id,datatype FROM sstalink_sensor WHERE deviceid=?"
        , JsonArray(listOf(id)))

      if (sensorResult.results.size == 0) {
        future.fail(SENSOR_NOT_FOUND)
        return@launch
      }
      sqlClient.updateWithParamsAwait("UPDATE sstalink_device SET state=? WHERE deviceid=?", JsonArray(listOf(DeviceStatus.ON.ordinal, id)))
      val session = sessionStore.createSession(SESSION_TIME_OUT)
      val deviceInfo = DeviceInfo(result, sensorResult.results)
      session.put("device", deviceInfo)
      deviceInfo.token = session.id()
      awaitResult<Void> { sessionStore.put(session,it) }
      awaitResult<Void> { asyncDeviceMap.put(deviceInfo.id,session.id(),it) }
      future.complete(session.id())

      if(!isLongConnection) {
        //如果不是长连接，则需要检测设备状态更新情况。如果设备长时间不更新状态，则认为连接丢失
        vertx.setPeriodic(STATE_CHECK_DELAY) { timerId ->
          if (Date().time - session.lastAccessed() > STATE_CHECK_DELAY) {
            logout(session.id()) {
              logger.warn("Device $id lost connection")
              vertx.cancelTimer(timerId)
            }
          }
        }
      }
    }.invokeOnCompletion {
      if (it != null) {
        future.fail(it)
      }
    }
    return this
  }

  override fun logout(token: String, handler: Handler<AsyncResult<Void>>): DeviceManagerService {
    val future = Future.future<Void>().setHandler(handler)
    GlobalScope.launch(vertx.dispatcher()) {
      val deviceInfo = sessionStore.getAwait(token)?.get<DeviceInfo>("device")
      if (deviceInfo == null) {
        future.fail(DEVICE_NOT_FOUND)
        return@launch
      }
      val result = sqlClient.updateWithParamsAwait("UPDATE sstalink_device SET state=? WHERE deviceid=?"
        , JsonArray(listOf(DeviceStatus.OFF.ordinal, deviceInfo.id)))
      if (result.updated == 0) {
        future.fail(UPDATE_DEVICE_INFO_FAIL)
      }
      asyncDeviceMap.removeAwait(deviceInfo.id)
      sessionStore.deleteAwait(token)
      future.complete()
    }.invokeOnCompletion {
      if (it != null)
        future.fail(it)
    }
    return this
  }

  override fun getDeviceByToken(token: String, handler: Handler<AsyncResult<DeviceInfo>>): DeviceManagerService {
    sessionStore.get(token) {
      val future = Future.future<DeviceInfo>().setHandler(handler)
      if (it.failed() || it.result() == null) {
        future.fail(DEVICE_NOT_FOUND)
      } else {
        future.complete(it.result().get("device"))
      }
    }
    return this
  }

  override fun updateState(token: String, state: String, handler: Handler<AsyncResult<String>>): DeviceManagerService {
    val future = Future.future<String>().setHandler(handler)
    GlobalScope.launch(vertx.dispatcher()) {
      val session = sessionStore.getAwait(token)
      if(session==null) {
        future.fail(DEVICE_NOT_FOUND)
        return@launch
      }
      val desired = session.get<String>("desired")
      if(desired ==null || desired == state){
        session.remove<String>("desired")
        session.put("state",state)
        future.complete()
      }else{
        future.complete(desired)
      }
    }.invokeOnCompletion {
      if(it!=null)
        future.fail(it)
    }
    return this
  }

  override fun forceClose(deviceId: String, handler: Handler<AsyncResult<Void>>): DeviceManagerService {
    GlobalScope.launch(vertx.dispatcher()) {
      val token = asyncDeviceMap.getAwait(deviceId)
      if(token==null){
        val future = Future.future<Void>().setHandler(handler)
        future.fail(DEVICE_NOT_FOUND)
      }else {
        logout(token, handler)
        publishCommand("forceClose",JsonObject().put("token",token))
      }
    }
    return this
  }

  override fun setState(deviceId: String, desired: String, handler: Handler<AsyncResult<Void>>):DeviceManagerService {
    val future = Future.future<Void>().setHandler(handler)
    GlobalScope.launch(vertx.dispatcher()) {
      val token = asyncDeviceMap.getAwait(deviceId)
      if(token==null){
        future.fail(DEVICE_NOT_FOUND)
        return@launch
      }
      val session = sessionStore.getAwait(token)
      if(session==null){
        future.fail(DEVICE_NOT_FOUND)
        return@launch
      }
      session.put("desired",desired)
      //向PUBLISH_DESIRED_STATE_ADDRESS发布期望状态
      //如果设备已连接到某个TransportVerticle，则由该Verticle直接向设备发送期望状态
      //如果没有连接，则等到下次设备更新状态时返回期望状态。
      val cmdBody = JsonObject().put("desired",desired).put("token",token)
      eventBus.publish(DeviceManagerService.PUBLISH_MANAGE_COMMAND
        ,JsonObject().put("action","setState").put("body",cmdBody))
      future.complete()
    }
    return this
  }

  override fun getState(deviceId: String, handler: Handler<AsyncResult<String>>): DeviceManagerService {
    val future = Future.future<String>().setHandler(handler)
    GlobalScope.launch(vertx.dispatcher()) {
      val token = asyncDeviceMap.getAwait(deviceId)
      if(token==null){
        future.fail(DEVICE_NOT_FOUND)
        return@launch
      }
      val session = sessionStore.getAwait(token)
      if(session==null){
        future.fail(DEVICE_NOT_FOUND)
        return@launch
      }
      future.complete(session.get<String>("state"))
    }.invokeOnCompletion {
      if(it!=null)
        future.fail(it)
    }
    return this
  }


  companion object {
    private const val DEVICE_NOT_FOUND = "没有找到设备"
    private const val SENSOR_NOT_FOUND = "没有传感器"
    private const val UPDATE_DEVICE_INFO_FAIL = "更新数据失败"
    private const val SESSION_TIME_OUT = 7*24*60*1000L
    private const val STATE_CHECK_DELAY = 10*60*1000L
  }
}
