package cn.hdussta.link.linkServer.dashboard.impl

import cn.hdussta.link.linkServer.common.USER_LOG_TABLE
import cn.hdussta.link.linkServer.common.USER_TABLE
import cn.hdussta.link.linkServer.dashboard.*
import cn.hdussta.link.linkServer.service.dashboard.UserLogService
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
import io.vertx.kotlin.ext.asyncsql.querySingleAwait
import io.vertx.kotlin.ext.sql.queryAwait
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserLogServiceImpl(private val vertx: Vertx,private val sqlClient: AsyncSQLClient):UserLogService {
  override fun countUserLogs(username: String?, type: Int?, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val level = context.getULevel()
    val uid = context.getUid()
    val sql = when(level) {
      UserLevel.USER.ordinal->{
        if(username!=null) return resultHandler.handleError(-1,"权限不足")
        "SELECT COUNT(l.id) FROM $USER_LOG_TABLE l JOIN $USER_TABLE u ON l.user_id = u.id WHERE l.user_id=${context.getUid()}"+
          (if(type!=null) " AND l.type=$type" else "")
      }
      UserLevel.ADMIN.ordinal->{
        val adminId = context.getAdmin()
        "SELECT COUNT(l.id)" +
          " FROM $USER_LOG_TABLE l JOIN $USER_TABLE u ON l.user_id=u.id" +
          " WHERE (u.admin_id=$adminId OR u.id=$uid)" +
          (if(username!=null) " AND u.email LIKE ?" else "") +
          (if(type!=null) " AND l.type=$type" else "")
      }
      UserLevel.SUPER.ordinal->{
        "SELECT COUNT(l.id)" +
          " FROM $USER_LOG_TABLE l JOIN $USER_TABLE u ON l.user_id=u.id" +
          " WHERE 1=1" +
          (if(username!=null) " AND u.email LIKE ?" else "") +
          (if(type!=null) " AND l.type=$type" else "")
      }
      else->return resultHandler.handleError(-1,"未知的等级")
    }
    GlobalScope.launch(vertx.dispatcher()) {
      val result = sqlClient.querySingleWithParamsAwait(sql, JsonArray().apply {
        username?.let(::add)
      })?.getInteger(0)?:throw Exception("未知错误")
      resultHandler.handleJson(JsonObject().put("count",result))
    }.invokeOnCompletion {
      if(it!=null) resultHandler.handleError(-1,it.localizedMessage)
    }
  }

  override fun getUserLogs(offset: Int, limit: Int, username: String?, type: Int?, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val level = context.getULevel()
    val uid = context.getUid()
    val sql = when(level){
      UserLevel.USER.ordinal->{
        if(username!=null) return resultHandler.handleError(-1,"权限不足")
        "SELECT l.id,u.email,l.action,l.create_time,l.type FROM $USER_LOG_TABLE l JOIN $USER_TABLE u ON l.user_id = u.id WHERE l.user_id=${context.getUid()}"+
          (if(type!=null) " AND l.type=$type" else "") +
          " ORDER BY id DESC LIMIT $offset,$limit"
      }
      UserLevel.ADMIN.ordinal->{
        val adminId = context.getAdmin()
        "SELECT l.id,u.email,l.action,l.create_time,l.type" +
          " FROM $USER_LOG_TABLE l JOIN $USER_TABLE u ON l.user_id=u.id" +
          " WHERE (u.admin_id=$adminId OR u.id=$uid)" +
          (if(username!=null) " AND u.email LIKE ?" else "") +
          (if(type!=null) " AND l.type=$type" else "") +
          " ORDER BY id DESC LIMIT $offset,$limit"
      }
      UserLevel.SUPER.ordinal->{
        "SELECT l.id,u.email,l.action,l.create_time,l.type" +
          " FROM $USER_LOG_TABLE l JOIN $USER_TABLE u ON l.user_id=u.id" +
          " WHERE 1=1" +
          (if(username!=null) " AND u.email LIKE ?" else "") +
          (if(type!=null) " AND l.type=$type" else "") +
          " ORDER BY id DESC LIMIT $offset,$limit"
      }
      else->return resultHandler.handleError(-1,"未知的等级")
    }
    GlobalScope.launch(vertx.dispatcher()) {
      val result = sqlClient.queryWithParamsAwait(sql, JsonArray().apply {
        username?.let(::add)
      })
      result.results.map {
        JsonObject()
          .put("id",it.getValue(0))
          .put("username",it.getValue(1))
          .put("action",it.getValue(2))
          .put("createTime",it.getValue(3))
          .put("type",it.getValue(4))
      }.let {
        resultHandler.handleJson(JsonArray(it))
      }
    }.invokeOnCompletion {
      if(it!=null) resultHandler.handleError(-1,it.localizedMessage)
    }
  }
}
