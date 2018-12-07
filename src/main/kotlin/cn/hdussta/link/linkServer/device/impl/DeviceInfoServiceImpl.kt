package cn.hdussta.link.linkServer.device.impl

import cn.hdussta.link.linkServer.device.DeviceInfo
import cn.hdussta.link.linkServer.device.DeviceInfoService
import cn.hdussta.link.linkServer.device.DeviceStatus
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.sstore.SessionStore
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import io.vertx.kotlin.ext.web.sstore.deleteAwait
import io.vertx.kotlin.ext.web.sstore.getAwait
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DeviceInfoServiceImpl(private val vertx: Vertx, private val sqlClient: SQLClient, private val sessionStore: SessionStore) : DeviceInfoService {
  private val logger = LoggerFactory.getLogger(DeviceInfoService::class.java)
  override fun login(id: String, secret: String, handler: Handler<AsyncResult<String>>): DeviceInfoService {
    val future: Future<String> = Future.future()
    future.setHandler(handler)
    GlobalScope.launch(vertx.dispatcher()){
      val result = sqlClient.querySingleWithParamsAwait("SELECT deviceid,name,secret,state,config FROM sstalink_device WHERE deviceid=? and secret=? and state=${DeviceStatus.OFF.ordinal}"
          , JsonArray(listOf(id, secret)))
      if(result==null){
        future.fail(DEVICE_NOT_FOUND)
        return@launch
      }
      val sensorResult = sqlClient.queryWithParamsAwait("SELECT id,datatype FROM sstalink_sensor WHERE deviceid=?"
        , JsonArray(listOf(id)))

      if(sensorResult.results.size==0){
        future.fail(SENSOR_NOT_FOUND)
        return@launch
      }
      sqlClient.updateWithParamsAwait("UPDATE sstalink_device SET state=? WHERE deviceid=?", JsonArray(listOf(DeviceStatus.ON.ordinal, id)))
      val session = sessionStore.createSession(SESSION_TIME_OUT)
      session.put("device", DeviceInfo(result,sensorResult.results))
      sessionStore.put(session) { put ->
        if (put.failed())
          future.fail(put.cause())
        else
          future.complete(session.id())
      }
    }.invokeOnCompletion {
      if(it!=null){
        future.fail(it)
      }
    }
    return this
  }

  override fun logout(token: String, handler: Handler<AsyncResult<Void>>): DeviceInfoService {
    val future = Future.future<Void>()
    future.setHandler(handler)
    GlobalScope.launch(vertx.dispatcher()) {
      val deviceInfo = sessionStore.getAwait(token)?.get<DeviceInfo>("device")
      if(deviceInfo==null) {
        future.fail(DEVICE_NOT_FOUND)
        return@launch
      }
      sessionStore.deleteAwait(token)
      try {
        val result = sqlClient.updateWithParamsAwait("UPDATE sstalink_device SET state=? WHERE deviceid=?"
          , JsonArray(listOf(DeviceStatus.OFF.ordinal, deviceInfo.id)))
        if(result.updated==0){
          future.fail(UPDATE_DEVICE_INFO_FAIL)
        }
      }catch (e:Throwable){
        logger.error(e.localizedMessage)
        future.fail(UPDATE_DEVICE_INFO_FAIL)
        return@launch
      }
      future.complete()
    }
    return this
  }


  override fun getDeviceByToken(token: String, handler: Handler<AsyncResult<DeviceInfo>>): DeviceInfoService {
    sessionStore.get(token) {
      val future = Future.future<DeviceInfo>()
      future.setHandler(handler)
      if (it.failed()) {
        future.fail(DEVICE_NOT_FOUND)
      } else {
        future.complete(it.result().get("device"))
      }
    }
    return this
  }

  override fun getDeviceInfo(id: String, handler: Handler<AsyncResult<DeviceInfo>>): DeviceInfoService {
    //TODO
    return this
  }

  companion object {
    const val SESSION_TIME_OUT = 24 * 60 * 60 * 1000L
    const val DEVICE_NOT_FOUND = "没有找到设备或设备状态有误"
    const val SENSOR_NOT_FOUND = "设备未设置传感器"
    const val UPDATE_DEVICE_INFO_FAIL = "更新设备信息失败"
  }
}
