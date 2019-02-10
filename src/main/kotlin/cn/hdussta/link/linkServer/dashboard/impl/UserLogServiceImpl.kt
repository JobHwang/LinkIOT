package cn.hdussta.link.linkServer.dashboard.impl

import cn.hdussta.link.linkServer.common.USER_LOG_TABLE
import cn.hdussta.link.linkServer.common.USER_TABLE
import cn.hdussta.link.linkServer.dashboard.*
import cn.hdussta.link.linkServer.service.dashboard.UserLogService
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserLogServiceImpl(private val vertx: Vertx,private val sqlClient: AsyncSQLClient):UserLogService {
  override fun countUserLogs(username: String?, type: Int?, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val level = context.getULevel()
    val uid = context.getUid()
    val me = context.getUsername()
    val sql = when(level) {
      UserLevel.USER.ordinal -> {
        "SELECT COUNT(l.id) FROM $USER_LOG_TABLE l JOIN $USER_TABLE u ON l.user_id = u.id WHERE l.user_id=${context.getUid()}" +
          (if (type != null) " AND l.type=$type" else "")
      }
      else -> {
        val adminId = context.getAdmin()
        if (username == null) {
          "SELECT COUNT(l.id)" +
            " FROM $USER_LOG_TABLE l JOIN $USER_TABLE u ON l.user_id=u.id" +
            " WHERE (u.admin_id=$adminId OR u.id=$uid)" +
            (if (type != null) " AND l.type=$type" else "")
        } else {
          if (username == me)
            "SELECT COUNT(l.id) FROM $USER_LOG_TABLE l JOIN $USER_TABLE u ON l.user_id=u.id WHERE u.email=?" +
              (if (type != null) " AND l.type=$type" else "")
          else
            "SELECT COUNT(l.id) FROM $USER_LOG_TABLE l JOIN $USER_TABLE u ON l.user_id=u.id WHERE u.email=?" +
              (if (type != null) " AND l.type=$type" else "") +
              (if (level == UserLevel.ADMIN.ordinal) " AND admin_id=$uid" else "")
        }
      }
    }
    GlobalScope.launch(vertx.dispatcher()) {
      val result = sqlClient.querySingleAwait(sql)?.getInteger(0)?:throw Exception("未知错误")
      resultHandler.handleJson(JsonObject().put("count",result))
    }.invokeOnCompletion {
      if(it!=null) resultHandler.handleError(-1,it.localizedMessage)
    }
  }

  override fun getUserLogs(offset: Int, limit: Int, username: String?, type: Int?, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val level = context.getULevel()
    val uid = context.getUid()
    val me = context.getUsername()
    val sql = when(level){
      UserLevel.USER.ordinal->{
        "SELECT l.id,u.email,l.action,l.create_time,l.type FROM $USER_LOG_TABLE l JOIN $USER_TABLE u ON l.user_id = u.id WHERE l.user_id=${context.getUid()}"+
          (if(type!=null) " AND l.type=$type" else "") +
          " ORDER BY id DESC LIMIT $offset,$limit"
      }
      else->{
        val adminId = context.getAdmin()
        if(username==null){
          "SELECT l.id,u.email,l.action,l.create_time,l.type" +
            " FROM $USER_LOG_TABLE l JOIN $USER_TABLE u ON l.user_id=u.id" +
            " WHERE (u.admin_id=$adminId OR u.id=$uid)" +
            (if(type!=null) " AND l.type=$type" else "") +
            " ORDER BY id DESC LIMIT $offset,$limit"
        } else {
          if(username==me)
            "SELECT l.id,u.email,l.action,l.create_time,l.type FROM $USER_LOG_TABLE l JOIN $USER_TABLE u ON l.user_id=u.id WHERE u.email=?"+
              (if(type!=null) " AND l.type=$type" else "") +
              " ORDER BY id DESC LIMIT $offset,$limit"
          else
            "SELECT l.id,u.email,l.action,l.create_time,l.type FROM $USER_LOG_TABLE l JOIN $USER_TABLE u ON l.user_id=u.id WHERE u.email=?" +
              (if(type!=null) " AND l.type=$type" else "") +
              (if(level==UserLevel.ADMIN.ordinal) " AND admin_id=$uid" else "") +
              " ORDER BY id DESC LIMIT $offset,$limit"
        }
      }
    }
    GlobalScope.launch(vertx.dispatcher()) {
      val result = sqlClient.queryAwait(sql)
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
