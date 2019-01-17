package cn.hdussta.link.linkServer.dashboard.impl

import cn.hdussta.link.linkServer.common.DEVICE_TABLE
import cn.hdussta.link.linkServer.common.USER_TABLE
import cn.hdussta.link.linkServer.dashboard.*
import cn.hdussta.link.linkServer.dashboard.bean.PostUserBody
import cn.hdussta.link.linkServer.dashboard.bean.RegisterBody
import cn.hdussta.link.linkServer.service.dashboard.UserService
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.api.OperationRequest
import io.vertx.ext.web.api.OperationResponse
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.sql.querySingleAwait
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserServiceImpl(private val vertx: Vertx,private val sqlClient: SQLClient):UserService {
  override fun getUser(context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val uid = context.extra.getInteger("id")
    GlobalScope.launch(vertx.dispatcher()) {
      val user = sqlClient.querySingleAwait("SELECT email,nickname,avatar,createtime FROM $USER_TABLE WHERE id=$uid")
        ?: throw Exception(GET_USER_FAILURE)
      val deviceNumber = sqlClient.querySingleAwait("SELECT COUNT(id) FROM $DEVICE_TABLE WHERE ownerid=$uid")?.let {
        it.getInteger(0)
      }?:throw Exception(GET_USER_FAILURE)
      resultHandler.handleJson(JsonObject()
        .put("username",user.getString(0))
        .put("nickname",user.getString(1))
        .put("avatar",user.getString(2))
        .put("createTime",user.getString(3))
        .put("createdDeviceNumber",deviceNumber))
    }.invokeOnCompletion {
      if(it!=null) resultHandler.handleError(-1,it.localizedMessage)
    }
  }

  override fun postUser(body: PostUserBody, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val uid = context.extra.getInteger("id")
    GlobalScope.launch(vertx.dispatcher()) {
      val sql = "UPDATE $USER_TABLE SET" +
        ((body.username?.let { "email=?," }?:"") +
        (body.password?.let { "password=?," }?:"") +
        (body.nickname?.let { "nickname=?," }?:"") +
        (body.avatar?.let { "avatar=?," }?:"")).removeSuffix(",") +
        " WHERE id=$uid"
      val result = sqlClient.updateWithParamsAwait(sql,JsonArray().apply {
        body.username?.let(::add)
        body.password?.let(::add)
        body.nickname?.let(::add)
        body.avatar?.let(::add)
      })
      if(result.updated!=1){
        resultHandler.handleError(-2, POST_USER_FAILURE)
      }else{
        resultHandler.handleMessage(1, POST_USER_SUCCESS)
      }
    }.invokeOnCompletion {
      if(it!=null) resultHandler.handleError(-1,it.localizedMessage)
    }
  }

  override fun register(body: RegisterBody, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    GlobalScope.launch(vertx.dispatcher()) {
      val sql = "INSERT INTO $USER_TABLE (email,password,nickname) VALUES (?,?,?)"
      if(sqlClient.querySingleWithParamsAwait("SELECT id FROM $USER_TABLE WHERE email=?", JsonArray(listOf(body.username)))!=null){
        resultHandler.handleError(-2, USER_ALREADY_EXISTS)
      }else{
        sqlClient.updateWithParamsAwait(sql, JsonArray(listOf(body.username,body.password,body.nickname)))
        resultHandler.handleMessage(1, REGISTER_SUCCESS)
      }
    }.invokeOnCompletion {
      if(it!=null){
        resultHandler.handleError(-1,it.localizedMessage)
      }
    }
  }
  companion object {
    private const val USER_ALREADY_EXISTS = "用户已存在"
    private const val REGISTER_SUCCESS = "注册成功"
    private const val GET_USER_FAILURE = "无法获取用户信息"
    private const val POST_USER_FAILURE = "更新用户信息失败"
    private const val POST_USER_SUCCESS = "更新用户信息成功"
  }
}
