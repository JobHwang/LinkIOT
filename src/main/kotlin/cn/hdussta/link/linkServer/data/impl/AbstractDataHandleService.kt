package cn.hdussta.link.linkServer.data.impl

import cn.hdussta.link.linkServer.service.DataHandleService
import cn.hdussta.link.linkServer.service.DataHandleService.generated
import cn.hdussta.link.linkServer.manager.DeviceInfo
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.types.EventBusService

/**
 * 提供通用的DataHandle方法
 */
abstract class AbstractDataHandleService: DataHandleService {
  abstract val discovery:ServiceDiscovery
  abstract val logger:io.vertx.core.logging.Logger
  abstract val ruleName:String

  /**
   * 当运行到错误分支时，调用该方法，服务结束运行，并返回服务链存储在data中的所有结果
   * @param cause
   * @param data
   * @param handler
   */
  fun fail(cause:String,data: JsonObject,handler: Handler<AsyncResult<JsonObject>>){
    val future = Future.future<JsonObject>()
    val results = data.getJsonObject(DataHandleService.generated).put(ruleName,cause)
    future.setHandler(handler)
    future.complete(results)
  }

  /**
   * 如果设备规则链存在下一个服务则，保存本次结果到data中，获取下一个服务并执行handle方法。
   * 如果规则链结束，则返回data中存储的所有结果
   * @param result
   * @param device
   * @param sensorId
   * @param data
   * @param handler
   */
  fun next(result:String, device: DeviceInfo, sensorId: Int, data: JsonObject, handler: Handler<AsyncResult<JsonObject>>){
    if(!data.containsKey(generated)){
      data.put(generated, JsonObject())
    }
    data.getJsonObject(generated).put(ruleName,result)
    val nextIndex = device.rules.indexOf(ruleName)+1
    if(nextIndex>=device.rules.size || device.rules[nextIndex].isEmpty()){
      val future = Future.future<JsonObject>()
      val results = data.getJsonObject(DataHandleService.generated)
      future.setHandler(handler)
      future.complete(results)
      return
    }
    val nextRule = device.rules[nextIndex]
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
