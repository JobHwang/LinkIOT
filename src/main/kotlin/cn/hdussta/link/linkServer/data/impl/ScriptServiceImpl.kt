package cn.hdussta.link.linkServer.data.impl

import cn.hdussta.link.linkServer.common.DEVICE_TABLE
import cn.hdussta.link.linkServer.manager.DeviceInfo
import cn.hdussta.link.linkServer.service.ScriptService
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.LocalMap
import io.vertx.ext.sql.SQLClient
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.sql.querySingleAwait
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.script.Compilable
import javax.script.CompiledScript
import javax.script.ScriptEngine

class ScriptServiceImpl(private val vertx: Vertx, private val sqlClient:SQLClient, private val engine: ScriptEngine): ScriptService {
  private val localMap = LinkedHashMap<String,CompiledScript>()
  override fun updateScript(deviceId: String, handler: Handler<AsyncResult<Void>>): ScriptService {
    val future = Future.future<Void>().setHandler(handler)
    sqlClient.querySingle("SELECT script FROM $DEVICE_TABLE WHERE deviceid='$deviceId'"){
      if(it.succeeded()){
        this.localMap[deviceId] = (engine as Compilable).compile(it.result().getString(0))
        future.complete()
      }else{
        future.fail(it.cause())
      }
    }
    return this
  }

  override fun withdrawScript(deviceId: String, handler: Handler<AsyncResult<Void>>): ScriptService {
    val future = Future.future<Void>().setHandler(handler)
    if(this.localMap.remove(deviceId)!=null){
      future.complete()
    }else{
      future.fail(NO_SCRIPT)
    }
    return this
  }

  override fun handle(device: DeviceInfo, data: JsonObject, handler: Handler<AsyncResult<JsonObject>>): ScriptService {
    val future = Future.future<JsonObject>()
    future.setHandler(handler)
    GlobalScope.launch(vertx.dispatcher()) {
      val script = localMap[device.id]?: sqlClient.querySingleAwait("SELECT script FROM $DEVICE_TABLE WHERE deviceid='${device.id}'")?.let{
        (engine as Compilable).compile(it.getString(0)).also {compiled->
          localMap[device.id] = compiled
        }
      }?:return@launch
      val tempBindings = engine.createBindings()
      tempBindings["things"] = mapOf("info" to device.toJson().map,"data" to data.map)
      val result = (script.eval(tempBindings) as? Map<String, Any>)?.let{ JsonObject(it) }?:JsonObject()
      future.complete(result)
    }.invokeOnCompletion {
      it?.let(future::fail)
    }
    return this
  }

  companion object {
    const val SERVICE_ADDRESS = "service.data.script"
    const val SERVICE_NAME = "script-data-service"
    private const val NO_SCRIPT = "没有找到缓存的脚本"
  }
}
