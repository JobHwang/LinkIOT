package cn.hdussta.link.linkServer.data.impl

import cn.hdussta.link.linkServer.service.DataHandleService
import cn.hdussta.link.linkServer.service.DataHandleService.generated
import cn.hdussta.link.linkServer.device.DeviceInfo
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.types.EventBusService

abstract class AbstractDataHandleService: DataHandleService {
  abstract val discovery:ServiceDiscovery
  abstract val logger:io.vertx.core.logging.Logger
  abstract val ruleName:String

  fun fail(cause:String,data: JsonObject,handler: Handler<AsyncResult<JsonObject>>){
    val future = Future.future<JsonObject>()
    val results = data.getJsonObject(DataHandleService.generated).put(ruleName,cause)
    future.setHandler(handler)
    future.complete(results)
  }

  fun next(result:String,device: DeviceInfo, sensorId: Int, data: JsonObject, handler: Handler<AsyncResult<JsonObject>>){
    if(!data.containsKey(generated)){
      data.put(generated, JsonObject())
    }
    data.getJsonObject(generated).put(ruleName,result)
    val index = device.rules.indexOf(ruleName) + 1
    if(index>=device.rules.size ){
      val future = Future.future<JsonObject>()
      val results = data.getJsonObject(DataHandleService.generated)
      future.setHandler(handler)
      future.complete(results)
      return
    }
    val nextRule = device.rules[index + 1]
    EventBusService.getServiceProxyWithJsonFilter(discovery, JsonObject().put("rule",nextRule), DataHandleService::class.java){
      if(it.failed()){
        logger.warn(it.cause().localizedMessage)
        val future = Future.future<JsonObject>()
        val results = data.getJsonObject(DataHandleService.generated).put(nextRule,it.cause().localizedMessage)
        future.setHandler(handler)
        future.complete(results)
        return@getServiceProxyWithJsonFilter
      }else{
        it.result().handle(device,sensorId,data,handler)
      }
    }
  }

}
