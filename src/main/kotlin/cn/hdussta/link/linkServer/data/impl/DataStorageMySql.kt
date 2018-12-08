package cn.hdussta.link.linkServer.data.impl

import cn.hdussta.link.linkServer.service.DataHandleService
import cn.hdussta.link.linkServer.device.DataType
import cn.hdussta.link.linkServer.device.DeviceInfo
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.sql.SQLClient
import io.vertx.kotlin.core.json.get
import io.vertx.servicediscovery.ServiceDiscovery

class DataStorageMySql(private val vertx:Vertx,override val discovery: ServiceDiscovery,private val sqlClient: SQLClient): AbstractDataHandleService() {
  override val ruleName: String = RULE_NAME
  override val logger = LoggerFactory.getLogger(DataStorageMySql::class.java)!!
  override fun handle(device: DeviceInfo, sensorId: Int, data: JsonObject, handler: Handler<AsyncResult<JsonObject>>): DataHandleService {
    val sensorInfo = device.sensors[sensorId]
    if(sensorInfo==null){
      fail(SENSOR_NOT_FOUND,data,handler)
      return this
    }
    val input = data.get<Any>("data")
    when(sensorInfo.type){
      DataType.NUMBER->{
        if(input !is Int && input !is Double && input !is Float && input !is Long){
          fail(WRONG_DATA_TYPE,data,handler)
          return this
        }
      }
      DataType.TEXT->{
        if(input !is String){
          fail(WRONG_DATA_TYPE,data,handler)
          return this
        }
      }
      DataType.POINT->{
        if(input !is JsonArray){
          fail(WRONG_DATA_TYPE,data,handler)
          return this
        }
      }
      DataType.BOOLEAN->{
        if(input !is Boolean){
          fail(WRONG_DATA_TYPE,data,handler)
          return this
        }
      }
      DataType.IMAGE->{
        if(input !is String && input !is ByteArray){
          fail(WRONG_DATA_TYPE,data,handler)
          return this
        }
      }
      DataType.OBJECT->{
        if(input !is JsonObject){
          fail(WRONG_DATA_TYPE,data,handler)
          return this
        }
      }
      DataType.UNKNOWN->{
        fail(WRONG_DATA_TYPE,data,handler)
        return this
      }
    }
    sqlClient.updateWithParams("INSERT INTO sstalink_data_${sensorInfo.type.name.toLowerCase()} (deviceid,sensorid,data) VALUES (?,?,?)"
      , JsonArray(listOf(device.id,sensorId,input))){
      if(it.failed()){
        fail(it.cause().localizedMessage,data,handler)
      }else{
        next(UPDATE_SUCCESS,device,sensorId,data,handler)
      }
    }
    return this
  }

  companion object {
    private const val UPDATE_SUCCESS = "上传成功"
    private const val SENSOR_NOT_FOUND = "没有找到对应传感器"
    private const val WRONG_DATA_TYPE = "数据类型错误"
    private const val RULE_NAME = "mysql"
    const val SERVICE_ADDRESS = "service.data.storage"
    const val SERVICE_NAME = "data-storage-service"
  }
}
