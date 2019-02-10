package cn.hdussta.link.linkServer.dashboard.impl

import cn.hdussta.link.linkServer.common.ALARM_LOG_TABLE
import cn.hdussta.link.linkServer.common.DEVICE_TABLE
import cn.hdussta.link.linkServer.dashboard.*
import cn.hdussta.link.linkServer.dashboard.bean.PostAlarmLogBody
import cn.hdussta.link.linkServer.service.dashboard.AlarmLogService
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
import io.vertx.kotlin.ext.sql.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AlarmLogServiceImpl(private val vertx:Vertx,private val sqlClient: AsyncSQLClient): AlarmLogService {
  override fun countAlarmLogs(level: Int?, deviceId: String?, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val adminId = context.getAdmin()
    GlobalScope.launch(vertx.dispatcher()) {
      val sql = "SELECT COUNT(a.id)" +
        " FROM $ALARM_LOG_TABLE a JOIN $DEVICE_TABLE d ON a.device_id=d.deviceId" +
        " WHERE d.ownerid=$adminId"+
        (if(level!=null) " AND a.level=?" else "") +
        (if(deviceId!=null) " AND a.device_id=?" else "")
      val result = sqlClient.querySingleWithParamsAwait(sql, JsonArray().apply {
        level?.let(::add)
        deviceId?.let(::add)
      })?.let { JsonObject().put("count",it.getInteger(0)) }?:throw Exception("未知的错误")
      resultHandler.handleJson(result)
    }.invokeOnCompletion {
      if(it!=null) resultHandler.handleError(-1,it.localizedMessage)
    }
  }

  override fun getAlarmLogs(offset: Int, limit: Int, level: Int?, deviceId: String?, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val adminId = context.getAdmin()
    GlobalScope.launch(vertx.dispatcher()) {
      val sql = "SELECT a.id,a.device_id,a.data,a.create_time,a.level,a.handle" +
        " FROM $ALARM_LOG_TABLE a JOIN $DEVICE_TABLE d ON a.device_id=d.deviceId" +
        " WHERE d.ownerid=$adminId"+
        (if(level!=null) " AND a.level=?" else "") +
        (if(deviceId!=null) " AND a.device_id=?" else "") +
        " ORDER BY id DESC LIMIT $offset,$limit"
      val result = sqlClient.queryWithParamsAwait(sql, JsonArray().apply {
        level?.let(::add)
        deviceId?.let(::add)
      })
      result.results.map {
        JsonObject()
          .put("id",it.getInteger(0))
          .put("deviceId",it.getString(1))
          .put("data",it.getString(2))
          .put("createTime",it.getString(3))
          .put("level",it.getInteger(4))
          .put("handle",it.getString(5))
      }.let {
        resultHandler.handleJson(JsonArray(it))
      }
    }.invokeOnCompletion {
      if(it!=null) resultHandler.handleError(-1,it.localizedMessage)
    }
  }

  override fun deleteAlarmLog(logId: Int, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val adminId = context.getAdmin()
    GlobalScope.launch(vertx.dispatcher()) {
      val sql = "DELETE FROM $ALARM_LOG_TABLE WHERE id=$logId AND device_id IN (SELECT deviceid FROM $DEVICE_TABLE WHERE ownerid=$adminId)"
      if(sqlClient.updateAwait(sql).updated!=1)
        throw Exception("删除失败，日志不存在")
    }.invokeOnCompletion {
      if(it!=null) resultHandler.handleError(-1,it.localizedMessage)
      else{
        resultHandler.handleMessage(1,"删除成功")
        sqlClient.userLog(context.getUid(),UserAction.ALARM_LOG,"删除报警日志")
      }
    }
  }

  override fun postAlarmLog(body: PostAlarmLogBody, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val adminId = context.getAdmin()
    GlobalScope.launch(vertx.dispatcher()) {
      val sql = "UPDATE $ALARM_LOG_TABLE SET handle=? WHERE id=${body.logId} AND device_id IN (SELECT deviceid FROM $DEVICE_TABLE WHERE ownerid=$adminId)"
      if(sqlClient.updateWithParamsAwait(sql, jsonArray(body.handle)).updated!=1)
        throw Exception("修改失败，日志不存在")
    }.invokeOnCompletion {
      if(it!=null) resultHandler.handleError(-1,it.localizedMessage)
      else{
        resultHandler.handleMessage(1,"修改成功")
        sqlClient.userLog(context.getUid(),UserAction.ALARM_LOG,"修改报警处理结果")
      }
    }
  }
}
