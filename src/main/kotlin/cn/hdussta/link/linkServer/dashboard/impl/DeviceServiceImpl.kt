package cn.hdussta.link.linkServer.dashboard.impl

import cn.hdussta.link.linkServer.common.DEVICE_TABLE
import cn.hdussta.link.linkServer.dashboard.*
import cn.hdussta.link.linkServer.dashboard.bean.PostDeviceBody
import cn.hdussta.link.linkServer.dashboard.bean.PutDeviceBody
import cn.hdussta.link.linkServer.service.DeviceManagerService
import cn.hdussta.link.linkServer.service.dashboard.DeviceService
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.api.OperationRequest
import io.vertx.ext.web.api.OperationResponse
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.sql.querySingleAwait
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.lang3.RandomStringUtils

@Suppress("UNSUPPORTED")
class DeviceServiceImpl(private val vertx: Vertx, private val sqlClient: SQLClient, private val managerService: DeviceManagerService) : DeviceService {

  override fun listDevices(offset: Int, limit: Int, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val sql = "SELECT name,deviceid,secret,description,state,last_login_time FROM $DEVICE_TABLE " +
      "WHERE ownerid=? ORDER BY id DESC LIMIT ?,?"
    val admin = context.extra.getInteger("admin")
    sqlClient.queryWithParams(sql, JsonArray(listOf(admin, offset, limit))) {
      if (it.failed()) {
        resultHandler.handleError(-1, it.cause().localizedMessage)
      } else {
        it.result().results.map { array ->
          JsonObject().put("name", array.getString(0))
            .put("deviceId", array.getString(1))
            .put("secret", array.getString(2))
            .put("description", array.getString(3))
            .put("state", array.getInteger(4))
            .put("lastLoginTime", array.getString(5))
        }.let { list ->
          resultHandler.handleJson(JsonArray(list))
        }
      }
    }
  }

  override fun getState(deviceId: String, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    GlobalScope.launch(vertx.dispatcher()) {
      val result = sqlClient.querySingleWithParamsAwait("SELECT id FROM $DEVICE_TABLE WHERE deviceid=? AND ownerid=?"
        , JsonArray().add(deviceId).add(context.extra.getInteger("admin")))
      if (result != null) {
        val state = awaitResult<String> { managerService.getState(deviceId,it) }
        resultHandler.handleJson(JsonObject().put("status", 1)
          .put("message", GET_STATE_SUCCESS)
          .put("state", JsonObject(state)))
      }else {
        resultHandler.handleError(-1, NO_AUTH)
      }
    }.invokeOnCompletion {
      if(it!=null) resultHandler.handleError(-1, it.localizedMessage)
    }
  }

  override fun postState(deviceId: String, body: JsonObject, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    GlobalScope.launch(vertx.dispatcher()) {
      val result = sqlClient.querySingleWithParamsAwait("SELECT id FROM $DEVICE_TABLE WHERE deviceid=? AND ownerid=?"
        , JsonArray().add(deviceId).add(context.extra.getInteger("admin")))
      if(result!=null){
        awaitResult<Void> {
          managerService.setState(deviceId,body.toString(),it)
        }
        resultHandler.handleMessage(1, SET_STATE_SUCCESS)
      }else{
        resultHandler.handleError(-1, NO_AUTH)
      }
    }.invokeOnCompletion {
      if(it!=null) resultHandler.handleError(-1, it.localizedMessage)
    }
  }

  override fun putDevice(body: PutDeviceBody, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val ownerId = context.getAdmin()
    val uid = context.getUid()
    GlobalScope.launch(vertx.dispatcher()) {
      val sql = "INSERT INTO $DEVICE_TABLE (deviceid,name,ownerid,secret,description,script) VALUES (?,?,?,?,?,?)"
      var deviceId = RandomStringUtils.randomAlphanumeric(9)
      while (sqlClient.querySingleAwait("SELECT id FROM $DEVICE_TABLE WHERE deviceid='$deviceId'") != null) {
        deviceId = RandomStringUtils.randomAlphanumeric(9).toLowerCase()
      }

      val secret = RandomStringUtils.randomAlphanumeric(64).toLowerCase()
      sqlClient.updateWithParamsAwait(sql, JsonArray(listOf(deviceId, body.name!!, ownerId, secret, body.description
        ?: "", body.script ?: "")))
      resultHandler.handleMessage(1, PUT_DEVICE_SUCCESS)
    }.invokeOnCompletion {
      if (it != null) resultHandler.handleError(-1, it.localizedMessage)
      else sqlClient.userLog(uid,UserAction.DEVICE,"创建设备:${body.toJson()}")
    }
  }

  override fun postDevice(body: PostDeviceBody, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    GlobalScope.launch(vertx.dispatcher()) {
      val sql = ("UPDATE $DEVICE_TABLE SET " +
        (body.name?.let { "name=?," } ?: "") +
        (body.description?.let { "description=?," } ?: "") +
        (body.script?.let { "script=?," } ?: "")).removeSuffix(",") +
        " WHERE deviceid=? AND ownerid=?"
      val result = sqlClient.updateWithParamsAwait(sql, JsonArray().apply {
        body.name?.let { this.add(it) }
        body.description?.let { this.add(it) }
        body.script?.let { this.add(it) }
        this.add(body.deviceId!!)
        this.add(context.extra.getInteger("admin"))
      })
      if (result.updated != 1)
        resultHandler.handleError(-2, POST_DEVICE_FAILURE)
      else
        resultHandler.handleMessage(1, POST_DEVICE_SUCCESS)
    }.invokeOnCompletion {
      if (it != null) resultHandler.handleError(-1, it.localizedMessage)
      else sqlClient.userLog(context.getUid(),UserAction.DEVICE,"修改设备:${body.toJson()}")
    }
  }

  override fun deleteDevice(deviceId: String, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    GlobalScope.launch(vertx.dispatcher()) {
      val sql = "DELETE FROM $DEVICE_TABLE WHERE deviceid=? AND ownerid=?"
      val ownerId = context.extra.getInteger("admin")
      val result = sqlClient.updateWithParamsAwait(sql, JsonArray(listOf(deviceId, ownerId)))
      if (result.updated != 1) {
        resultHandler.handleError(-2, DELETE_DEVICE_FAILURE)
      } else {
        resultHandler.handleMessage(1, DELETE_DEVICE_SUCCESS)
      }
    }.invokeOnCompletion {
      if (it != null) resultHandler.handleError(-1, it.localizedMessage)
      else sqlClient.userLog(context.getUid(),UserAction.DEVICE,"删除设备:$deviceId")
    }
  }

  override fun forceClose(deviceId: String, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    managerService.forceClose(deviceId) {
      if (it.failed()) {
        resultHandler.handleError(-1, it.cause().localizedMessage)
      } else {
        resultHandler.handleMessage(1, FORCE_CLOSE_SUCCESS)
      }
    }
  }

  companion object {
    private const val SET_STATE_SUCCESS = "状态设置成功"
    private const val GET_STATE_SUCCESS = "获取状态成功"
    private const val FORCE_CLOSE_SUCCESS = "下线成功"
    private const val PUT_DEVICE_SUCCESS = "创建设备成功"
    private const val POST_DEVICE_SUCCESS = "更新设备成功"
    private const val POST_DEVICE_FAILURE = "更新设备失败"
    private const val DELETE_DEVICE_SUCCESS = "删除设备成功"
    private const val DELETE_DEVICE_FAILURE = "删除设备失败"
    private const val NO_AUTH = "没有权限"
  }
}
