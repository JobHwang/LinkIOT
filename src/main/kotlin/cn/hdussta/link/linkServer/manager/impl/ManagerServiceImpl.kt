package cn.hdussta.link.linkServer.manager.impl

import cn.hdussta.link.linkServer.device.DeviceInfo
import cn.hdussta.link.linkServer.device.DeviceInfoService
import cn.hdussta.link.linkServer.manager.ManageableDeviceInfo
import cn.hdussta.link.linkServer.manager.ManagerService
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.AsyncMap

class ManagerServiceImpl(private val vertx: Vertx,private val eventBus:EventBus, private val deviceInfoService: DeviceInfoService,private val asyncDeviceMap:AsyncMap<String,ManageableDeviceInfo>):ManagerService {
  override fun setState(deviceId: String, desired: JsonObject, handler: Handler<AsyncResult<Void>>): ManagerService {
    val future = Future.future<Void>().setHandler(handler)
    this.asyncDeviceMap.get(deviceId){
      if(it.failed()){
        future.fail(it.cause())
        return@get
      }
      val info = it.result()
      if(info == null){
        future.fail(DEVICE_NOT_FOUND)
        return@get
      }
      info.shadow.put("desired",desired)
      eventBus.publish(PUBLISH_DESIRED_STATE_ADDRESS,desired)
    }
    return this
  }

  override fun register(deviceInfo: DeviceInfo, token: String, handler: Handler<AsyncResult<Void>>): ManagerService {
    val future = Future.future<Void>().setHandler(handler)
    this.asyncDeviceMap.put(deviceInfo.id,ManageableDeviceInfo(deviceInfo,token)){
      if(it.failed()){
        future.fail(it.cause())
      }else{
        future.complete()
      }
    }
    return this
  }

  override fun unregister(deviceId: String, handler: Handler<AsyncResult<Void>>): ManagerService {
    val future = Future.future<Void>().setHandler(handler)
    this.asyncDeviceMap.remove(deviceId){
      if(it.failed()){
        future.fail(it.cause())
      }else{
        future.complete()
      }
    }
    return this
  }

  override fun getState(deviceId: String, handler: Handler<AsyncResult<JsonObject>>): ManagerService {
    val future = Future.future<JsonObject>().setHandler(handler)
    asyncDeviceMap.get(deviceId){
      if(it.failed() || it.result()==null){
        future.fail(DEVICE_NOT_FOUND)
      }else{
        deviceInfoService.getDeviceState(it.result().token){ state ->
          if(state.failed() || state.result()==null){
            future.fail(DEVICE_STATE_NOT_FOUND)
          }else{
            future.complete(state.result())
          }
        }
      }
    }
    return this
  }

  override fun updateState(deviceId: String, state: JsonObject, handler: Handler<AsyncResult<JsonObject>>): ManagerService {
    val future = Future.future<JsonObject>().setHandler(handler)
    asyncDeviceMap.get(deviceId){
      if(it.failed() || it.result()==null){
        future.fail(DEVICE_NOT_FOUND)
      }else{
        val info = it.result()
        val desired = info.shadow.getJsonObject("desired")
        if(desired == state){
          info.shadow.remove("desired")
          future.complete()
        }else{
          future.complete(desired)
        }
      }
    }
    return this
  }

  companion object {
    const val DEVICE_STATE_NOT_FOUND = "没有找到设备状态"
    const val DEVICE_NOT_FOUND = "没有找到设备"
    const val PUBLISH_DESIRED_STATE_ADDRESS = "publish.manager.state"
  }
}
