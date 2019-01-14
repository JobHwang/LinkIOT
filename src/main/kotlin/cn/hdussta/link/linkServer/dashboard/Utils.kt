package cn.hdussta.link.linkServer.dashboard

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.api.OperationResponse

inline fun Handler<AsyncResult<OperationResponse>>.handleJson(json: JsonObject) =
  this.handle(Future.succeededFuture(OperationResponse.completedWithJson(json)))

inline fun Handler<AsyncResult<OperationResponse>>.handleJson(array: JsonArray) =
  this.handle(Future.succeededFuture(OperationResponse.completedWithJson(array)))

inline fun Handler<AsyncResult<OperationResponse>>.handleMessage(status: Int,message: String) =
  this.handle(Future.succeededFuture(OperationResponse.completedWithJson(JsonObject().put("status",status).put("message",message))))

inline fun Handler<AsyncResult<OperationResponse>>.handleError(status:Int, message:String) =
  this.handle(Future.succeededFuture(OperationResponse.completedWithJson(JsonObject().put("status",status).put("message",message))))
