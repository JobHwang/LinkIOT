package cn.hdussta.link.linkServer.dashboard.impl

import cn.hdussta.link.linkServer.common.DATA_TABLE
import cn.hdussta.link.linkServer.common.DEVICE_TABLE
import cn.hdussta.link.linkServer.common.DataType
import cn.hdussta.link.linkServer.common.SENSOR_TABLE
import cn.hdussta.link.linkServer.dashboard.getAdmin
import cn.hdussta.link.linkServer.dashboard.handleError
import cn.hdussta.link.linkServer.dashboard.handleJson
import cn.hdussta.link.linkServer.service.dashboard.DataService
import cn.hdussta.link.linkServer.utils.jsonArray
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.web.api.OperationRequest
import io.vertx.ext.web.api.OperationResponse
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.asyncsql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DataServiceImpl(private val vertx: Vertx,private val sqlClient:AsyncSQLClient):DataService {
  override fun countData(deviceId: String, sensorId: Int, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val adminId = context.getAdmin()
    GlobalScope.launch(vertx.dispatcher()) {
      val sensor = sqlClient.querySingleWithParamsAwait("SELECT s.datatype FROM $SENSOR_TABLE s JOIN $DEVICE_TABLE d ON s.deviceid=d.deviceid WHERE s.id=$sensorId AND d.deviceid=? AND d.ownerid=$adminId"
        , jsonArray(deviceId))?.getInteger(0)?.let {
        when(it){
          DataType.NUMBER.ordinal->{
            "number"
          }
          DataType.TEXT.ordinal->{
            "text"
          }
          DataType.POINT.ordinal->{
            "point"
          }
          DataType.BOOLEAN.ordinal->{
            "boolean"
          }
          DataType.IMAGE.ordinal->{
            "image"
          }
          DataType.OBJECT.ordinal->{
            "object"
          }
          else-> throw Exception("意外的数据类型")
        }
      }?:throw Exception("传感器不存在")
      val count = sqlClient.querySingleWithParamsAwait("SELECT COUNT(id) FROM ${DATA_TABLE(sensor)}" +
        " WHERE deviceid=? AND sensorid=?", jsonArray(deviceId,sensorId))?.let { it.getInteger(0) }?:throw Exception("传感器不存在")
      resultHandler.handleJson(JsonObject().put("count",count))
    }.invokeOnCompletion {
      if(it!=null) resultHandler.handleError(-1,it.localizedMessage)
    }
  }

  override fun listData(deviceId: String, sensorId: Int, offset: Int, limit: Int, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val adminId = context.getAdmin()
    GlobalScope.launch(vertx.dispatcher()) {
      val sensor = sqlClient.querySingleWithParamsAwait("SELECT s.datatype FROM $SENSOR_TABLE s JOIN $DEVICE_TABLE d ON s.deviceid=d.deviceid WHERE s.id=$sensorId AND d.deviceid=? AND d.ownerid=$adminId"
        , jsonArray(deviceId))?.getInteger(0)?.let {
        when(it){
          DataType.NUMBER.ordinal->{
            "number"
          }
          DataType.TEXT.ordinal->{
            "text"
          }
          DataType.POINT.ordinal->{
            "point"
          }
          DataType.BOOLEAN.ordinal->{
            "boolean"
          }
          DataType.IMAGE.ordinal->{
            "image"
          }
          DataType.OBJECT.ordinal->{
            "object"
          }
          else-> throw Exception("意外的数据类型")
        }
      }?:throw Exception("传感器不存在")
      val result = sqlClient.queryWithParamsAwait("SELECT data,updatetime FROM ${DATA_TABLE(sensor)} WHERE deviceid=? AND sensorid=?" +
        " ORDER BY id DESC LIMIT $offset,$limit", jsonArray(deviceId,sensorId))
      result.results.map {
        JsonObject()
          .put("data",it.getValue(0))
          .put("updateTime",it.getValue(1))
      }.let {
        resultHandler.handleJson(JsonArray(it))
      }
    }.invokeOnCompletion {
      if(it!=null) resultHandler.handleError(-1,it.localizedMessage)
    }
  }
}
