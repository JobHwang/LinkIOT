package cn.hdussta.link.linkServer.dashboard

import cn.hdussta.link.linkServer.common.USER_LOG_TABLE
import cn.hdussta.link.linkServer.utils.jsonArray
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.api.OperationRequest
import io.vertx.ext.web.api.OperationResponse

inline fun Handler<AsyncResult<OperationResponse>>.handleJson(json: JsonObject) =
  this.handle(Future.succeededFuture(OperationResponse.completedWithJson(json)))

inline fun Handler<AsyncResult<OperationResponse>>.handleJson(array: JsonArray) =
  this.handle(Future.succeededFuture(OperationResponse.completedWithJson(array)))

inline fun Handler<AsyncResult<OperationResponse>>.handleMessage(status: Int,message: String) =
  this.handle(Future.succeededFuture(OperationResponse.completedWithJson(JsonObject().put("status",status).put("message",message))))

inline fun Handler<AsyncResult<OperationResponse>>.handleError(status:Int, message:String) =
  this.handle(Future.succeededFuture(OperationResponse.completedWithJson(JsonObject().put("status",status).put("message",message))))

inline fun OperationRequest.getUid() = this.extra.getInteger("id")!!
inline fun OperationRequest.getAdmin() = this.extra.getInteger("admin")!!
inline fun OperationRequest.getULevel() = this.extra.getInteger("level")!!

inline fun SQLClient.userLog(uid:Int,type:UserAction,action:String)
  = this.updateWithParams("INSERT INTO $USER_LOG_TABLE (user_id,type,action) VALUES (?,?,?)", jsonArray(uid,type.ordinal,action)){}
