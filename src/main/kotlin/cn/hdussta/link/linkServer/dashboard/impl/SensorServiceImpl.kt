package cn.hdussta.link.linkServer.dashboard.impl

import cn.hdussta.link.linkServer.common.DATA_TABLE
import cn.hdussta.link.linkServer.common.DEVICE_TABLE
import cn.hdussta.link.linkServer.common.DataType
import cn.hdussta.link.linkServer.common.SENSOR_TABLE
import cn.hdussta.link.linkServer.dashboard.*
import cn.hdussta.link.linkServer.dashboard.bean.PostSensorBody
import cn.hdussta.link.linkServer.dashboard.bean.PutSensorBody
import cn.hdussta.link.linkServer.service.dashboard.SensorService
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.api.OperationRequest
import io.vertx.ext.web.api.OperationResponse
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SensorServiceImpl(private val vertx: Vertx,private val sqlClient: SQLClient):SensorService {
  override fun getSensors(deviceId: String, offset: Int, limit: Int, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    GlobalScope.launch(vertx.dispatcher()) {
      val ownerId = context.extra.getInteger("id")
      if(sqlClient.querySingleWithParamsAwait("SELECT id FROM $DEVICE_TABLE WHERE ownerid=? AND deviceid=?", JsonArray(listOf(ownerId,deviceId)))==null){
        resultHandler.handleError(-2, NOT_OWNER_OF_DEVICE)
        return@launch
      }
      val sql = "SELECT id,name,deviceid,dataType,showType,description,updatetime,createtime FROM $SENSOR_TABLE WHERE deviceid=? limit ?,?"
      val result = sqlClient.queryWithParamsAwait(sql, JsonArray(listOf(deviceId,offset,limit)))
      result.results.map { 
        JsonObject().put("id",it.getInteger(0))
          .put("name",it.getString(1))
          .put("device_id",it.getString(2))
          .put("data_type",it.getInteger(3))
          .put("show_type",it.getString(4))
          .put("description",it.getString(5))
          .put("update_time",it.getString(6))
          .put("create_time",it.getString(7))
      }.let { 
        resultHandler.handleJson(JsonArray(it))
      }
    }.invokeOnCompletion {
      if(it!=null){
        resultHandler.handleError(-1,it.localizedMessage)
      }
    }
  }

  override fun putSensor(deviceId: String, body:PutSensorBody, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    GlobalScope.launch(vertx.dispatcher()) {
      val ownerId = context.extra.getInteger("id")
      if(sqlClient.querySingleWithParamsAwait("SELECT id FROM $DEVICE_TABLE WHERE ownerid=? AND deviceid=?", JsonArray(listOf(ownerId,deviceId)))==null){
        resultHandler.handleError(-2, NOT_OWNER_OF_DEVICE)
        return@launch
      }
      val sql = "INSERT INTO $SENSOR_TABLE (name,deviceid,datatype,showtype,description) VALUES (?,?,?,?,?)"
      val result = sqlClient.updateWithParamsAwait(sql, JsonArray(listOf(body.name!!,deviceId,body.dataType!!,body.showType!!,body.description?:"")))
      if(result.updated==1)
        resultHandler.handleMessage(1, PUT_SENSOR_SUCCESS)
      else
        resultHandler.handleError(-2, PUT_SENSOR_FAILURE)
    }.invokeOnCompletion {
      if(it!=null)
        resultHandler.handleError(-1,it.localizedMessage)
    }
  }

  override fun postSensor(deviceId: String, body:PostSensorBody, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    GlobalScope.launch(vertx.dispatcher()) {
      val ownerId = context.extra.getInteger("id")
      if(sqlClient.querySingleWithParamsAwait("SELECT id FROM $DEVICE_TABLE WHERE ownerid=? AND deviceid=?", JsonArray(listOf(ownerId,deviceId)))==null){
        resultHandler.handleError(-2, NOT_OWNER_OF_DEVICE)
        return@launch
      }
      val sql = ("UPDATE $SENSOR_TABLE SET ${body.name?.let { "name=?," }?:""}" +
        " ${body.dataType?.let { "datatype=?," }?:""}" +
        " ${body.showType?.let { "showtype=?," }?:""}" +
        " ${body.description?.let { "description=?," }?:""} ").removeSuffix(",") +
        "WHERE deviceid=? AND sensorid=?"
      val result = sqlClient.updateWithParamsAwait(sql,JsonArray().apply {
        body.name?.let(::add)
        body.dataType?.let(::add)
        body.showType?.let(::add)
        body.description?.let(::add)
        this.add(deviceId)
        this.add(body.sensorId!!)
      })
      if(result.updated==1){
        resultHandler.handleMessage(1, POST_SENSOR_SUCCESS)
      }else{
        resultHandler.handleError(-2, POST_SENSOR_FAILURE)
      }
    }.invokeOnCompletion {
      if(it!=null)
        resultHandler.handleError(-1,it.localizedMessage)
    }
  }

  override fun deleteSensor(deviceId: String, sensorId: Int, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    GlobalScope.launch(vertx.dispatcher()) {
      val ownerId = context.extra.getInteger("id")
      if(sqlClient.querySingleWithParamsAwait("SELECT id FROM $DEVICE_TABLE WHERE ownerid=? AND deviceid=?", JsonArray(listOf(ownerId,deviceId)))==null){
        resultHandler.handleError(-2, NOT_OWNER_OF_DEVICE)
        return@launch
      }
      val sql = "DELETE FROM $SENSOR_TABLE WHERE deviceid=? AND sensorid=?"
      val result = sqlClient.updateWithParamsAwait(sql, JsonArray(listOf(deviceId,sensorId)))
      if(result.updated==1){
        resultHandler.handleMessage(1, DELETE_SENSOR_SUCCESS)
      }else{
        resultHandler.handleError(-2, DELETE_SENSOR_FAILURE)
      }
    }.invokeOnCompletion {
      if(it!=null)
        resultHandler.handleError(-1,it.localizedMessage)
    }
  }

  override fun getData(deviceId: String, sensorId: Int, offset: Int, limit: Int, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    GlobalScope.launch(vertx.dispatcher()) {
      val ownerId = context.extra.getInteger("id")
      if(sqlClient.querySingleWithParamsAwait("SELECT id FROM $DEVICE_TABLE WHERE ownerid=? AND deviceid=?", JsonArray(listOf(ownerId,deviceId)))==null){
        resultHandler.handleError(-2, NOT_OWNER_OF_DEVICE)
        return@launch
      }
      val sensorInfo = sqlClient.querySingleWithParamsAwait("SELECT datatype,showtype FROM $SENSOR_TABLE WHERE deviceid=? AND sensorid=?", JsonArray(listOf(deviceId,sensorId)))
        ?: throw Exception(SENSOR_NOT_FOUND)
      val dataType = DataType.values()[sensorInfo.getInteger(0)].name
      val sql = "SELECT data,updatetime FROM ${DATA_TABLE(dataType)} WHERE deviceid=? AND sensorid=? LIMIT ?,?"
      val result = sqlClient.queryWithParamsAwait(sql, JsonArray(listOf(deviceId,sensorId,offset,limit)))
      result.results.map {
        JsonObject().put("data",it.getValue(0)).put("update_time",it.getString(1))
      }.let {
        resultHandler.handleJson(JsonArray(it))
      }
    }.invokeOnCompletion {
      if(it!=null)
        resultHandler.handleError(-1,it.localizedMessage)
    }
  }

  companion object {
    private const val NOT_OWNER_OF_DEVICE = "不是设备所有人"
    private const val PUT_SENSOR_SUCCESS = "创建传感器成功"
    private const val PUT_SENSOR_FAILURE = "创建传感器失败"
    private const val POST_SENSOR_SUCCESS = "更新传感器成功"
    private const val POST_SENSOR_FAILURE = "更新传感器失败"
    private const val DELETE_SENSOR_SUCCESS = "删除传感器成功"
    private const val DELETE_SENSOR_FAILURE = "删除传感器失败"
    private const val SENSOR_NOT_FOUND = "没有找到传感器"
  }

}
