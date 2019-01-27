package cn.hdussta.link.linkServer.dashboard.impl

import cn.hdussta.link.linkServer.common.DEVICE_TABLE
import cn.hdussta.link.linkServer.common.USER_TABLE
import cn.hdussta.link.linkServer.dashboard.bean.PostUserBody
import cn.hdussta.link.linkServer.dashboard.bean.PutUserBody
import cn.hdussta.link.linkServer.dashboard.handleError
import cn.hdussta.link.linkServer.dashboard.handleJson
import cn.hdussta.link.linkServer.dashboard.handleMessage
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

class UserServiceImpl(private val vertx: Vertx, private val sqlClient: SQLClient) : UserService {
  override fun delUser(username: String, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val uLevel = context.extra.getInteger("level")
    GlobalScope.launch(vertx.dispatcher()) {
      val result = sqlClient.updateWithParamsAwait("DELETE FROM $USER_TABLE WHERE email=? AND level<?"
        , JsonArray().add(username).add(uLevel))
      if (result.updated > 0) {
        resultHandler.handleMessage(1, DEL_USER_SUCCESS)
      } else {
        resultHandler.handleError(-1, DEL_USER_FAILURE)
      }
    }.invokeOnCompletion {
      if (it != null) resultHandler.handleError(-1, DEL_USER_FAILURE)
    }
  }

  override fun getUser(context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val uid = context.extra.getInteger("id")
    GlobalScope.launch(vertx.dispatcher()) {
      val user = sqlClient.querySingleAwait("SELECT email,nickname,avatar,createtime FROM $USER_TABLE WHERE id=$uid")
        ?: throw Exception(GET_USER_FAILURE)
      val deviceNumber = sqlClient.querySingleAwait("SELECT COUNT(id) FROM $DEVICE_TABLE WHERE ownerid=$uid")?.let {
        it.getInteger(0)
      } ?: throw Exception(GET_USER_FAILURE)
      resultHandler.handleJson(JsonObject()
        .put("username", user.getString(0))
        .put("nickname", user.getString(1))
        .put("avatar", user.getString(2))
        .put("createTime", user.getString(3))
        .put("createdDeviceNumber", deviceNumber))
    }.invokeOnCompletion {
      if (it != null) resultHandler.handleError(-1, it.localizedMessage)
    }
  }

  override fun postUser(body: PostUserBody, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    GlobalScope.launch(vertx.dispatcher()) {
      val uid = context.extra.getInteger("id")
      val uLevel = context.extra.getInteger("level")
      val sql = if (body.uid == null) {
        "UPDATE $USER_TABLE SET" +
          ((body.password?.let { "password=?," } ?: "") +
            (body.nickname?.let { "nickname=?," } ?: "") +
            (body.avatar?.let { "avatar=?," } ?: "")).removeSuffix(",") +
          " WHERE id=$uid"
      } else {
        "UPDATE $USER_TABLE SET" +
          ((body.password?.let { "password=?," } ?: "") +
            (body.nickname?.let { "nickname=?," } ?: "") +
            (body.avatar?.let { "avatar=?," } ?: "")).removeSuffix(",") +
          " WHERE id=${body.uid} AND level>$uLevel " +
          //如果是普通管理员，则只能修改属于自己的用户，超级管理员没有限制
          (if(uLevel==2) "AND admin=$uid" else "")
      }
      val result = sqlClient.updateWithParamsAwait(sql, JsonArray().apply {
        body.password?.let(::add)
        body.nickname?.let(::add)
        body.avatar?.let(::add)
      })
      if (result.updated != 1) {
        resultHandler.handleError(-2, POST_USER_FAILURE)
      } else {
        resultHandler.handleMessage(1, POST_USER_SUCCESS)
      }
    }.invokeOnCompletion {
      if (it != null) resultHandler.handleError(-1, it.localizedMessage)
    }
  }

  override fun putUser(body: PutUserBody, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    //创建用户等级，不能高于自身等级
    if (body.level >= context.extra.getInteger("level"))
      return resultHandler.handleError(-1, NO_AUTH)
    GlobalScope.launch(vertx.dispatcher()) {
      if (sqlClient.querySingleWithParamsAwait("SELECT id FROM $USER_TABLE WHERE email=?", JsonArray(listOf(body.username))) != null) {
        resultHandler.handleError(-1, USER_ALREADY_EXISTS)
      } else {
        val sql = "INSERT INTO $USER_TABLE (admin_id,level,email,password,nickname) VALUES (?,?,?,?,?)"
        sqlClient.updateWithParamsAwait(sql, JsonArray(listOf(
          //如果创建普通管理员，则不设置admin_id，登录时会将id设为admin_id
          //如果是普通管理员创建用户，则admin_id只能是该管理员id，超级管理员可设置任意admin_id
          if (body.level == 2) 0 else if( context.extra.getInteger("level")==1 )body.adminId else context.extra.getInteger("uid")
          , body.level
          , body.username
          , body.password
          , body.nickname)))
        resultHandler.handleMessage(1, REGISTER_SUCCESS)
      }
    }.invokeOnCompletion {
      if (it != null) {
        resultHandler.handleError(-1, it.localizedMessage)
      }
    }
  }

  companion object {
    private const val USER_ALREADY_EXISTS = "用户已存在"
    private const val REGISTER_SUCCESS = "注册成功"
    private const val GET_USER_FAILURE = "无法获取用户信息"
    private const val POST_USER_FAILURE = "更新用户信息失败"
    private const val POST_USER_SUCCESS = "更新用户信息成功"
    private const val DEL_USER_FAILURE = "删除用户失败，请检查用户名是否正确或是否有删除权限"
    private const val DEL_USER_SUCCESS = "删除用户成功"
    private const val NO_AUTH = "权限不足"
  }
}
