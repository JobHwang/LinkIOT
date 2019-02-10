package cn.hdussta.link.linkServer.dashboard.impl

import cn.hdussta.link.linkServer.common.DEVICE_TABLE
import cn.hdussta.link.linkServer.common.USER_TABLE
import cn.hdussta.link.linkServer.dashboard.*
import cn.hdussta.link.linkServer.dashboard.bean.PostUserBody
import cn.hdussta.link.linkServer.dashboard.bean.PutUserBody
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
import io.vertx.kotlin.ext.sql.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserServiceImpl(private val vertx: Vertx, private val sqlClient: SQLClient) : UserService {
  override fun countUser(adminId: Int?, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val uLevel = context.getULevel()
    if(uLevel==UserLevel.USER.ordinal) return resultHandler.handleError(-1, NO_AUTH)
    val uid = context.getUid()
    GlobalScope.launch(vertx.dispatcher()) {
      val sql = "SELECT COUNT(id) FROM $USER_TABLE WHERE " +
        (if(uLevel==UserLevel.SUPER.ordinal) if(adminId!=null) "admin_id=$adminId" else "1=1" else "admin_id=$uid")
      val result = sqlClient.querySingleAwait(sql)?.let { JsonObject().put("count",it.getInteger(0)) }
        ?:throw Exception("未知错误")
      resultHandler.handleJson(result)
    }.invokeOnCompletion {
      if(it!=null) resultHandler.handleError(-1,it.localizedMessage)
    }
  }

  override fun listUser(adminId:Int?,offset: Int, limit: Int, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val uLevel = context.getULevel()
    if(uLevel==UserLevel.USER.ordinal) return resultHandler.handleError(-1, NO_AUTH)
    val uid = context.getUid()
    GlobalScope.launch(vertx.dispatcher()) {
      val sql = "SELECT email,password,nickname,avatar,createtime,level,phone FROM $USER_TABLE WHERE " +
        (if(uLevel==UserLevel.SUPER.ordinal) if(adminId!=null) "admin_id=$adminId" else "1=1" else "admin_id=$uid") +
        " ORDER BY id DESC LIMIT $offset,$limit"
      val result = sqlClient.queryAwait(sql)
      resultHandler.handleJson(JsonArray(result.results.map { user->
        JsonObject()
          .put("username", user.getString(0))
          .put("password",user.getString(1))
          .put("nickname", user.getString(2))
          .put("avatar", user.getString(3))
          .put("createTime", user.getString(4))
          .put("level",user.getInteger(5))
          .put("phone",user.getString(6))
      }))
    }.invokeOnCompletion {
      if(it!=null) resultHandler.handleError(-1,it.localizedMessage)
    }
  }

  override fun delUser(username: String, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val uLevel = context.getULevel()
    val uid = context.getUid()
    GlobalScope.launch(vertx.dispatcher()) {
      val result = sqlClient.updateWithParamsAwait("DELETE FROM $USER_TABLE WHERE email=? AND level>?" +
        (if(uLevel==UserLevel.ADMIN.ordinal) " AND admin_id=$uid" else "")
        , JsonArray().add(username).add(uLevel))
      if (result.updated > 0) {
        resultHandler.handleMessage(1, DEL_USER_SUCCESS)
      } else {
        resultHandler.handleError(-1, DEL_USER_FAILURE)
      }
    }.invokeOnCompletion {
      if (it != null) resultHandler.handleError(-1, DEL_USER_FAILURE)
      else sqlClient.userLog(context.getUid(),UserAction.USER,"删除用户$username")
    }
  }

  override fun getUser(context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    val uid = context.getUid()
    val admin = context.getAdmin()
    GlobalScope.launch(vertx.dispatcher()) {
      val user = sqlClient.querySingleAwait("SELECT email,nickname,avatar,createtime,level,phone FROM $USER_TABLE WHERE id=$uid")
        ?: throw Exception(GET_USER_FAILURE)
      val deviceNumber = sqlClient.querySingleAwait("SELECT COUNT(id) FROM $DEVICE_TABLE WHERE ownerid=$admin")?.getInteger(0)
        ?: throw Exception(GET_USER_FAILURE)
      resultHandler.handleJson(JsonObject()
        .put("username", user.getString(0))
        .put("nickname", user.getString(1))
        .put("avatar", user.getString(2))
        .put("createTime", user.getString(3))
        .put("level",user.getInteger(4))
        .put("phone",user.getString(5))
        .put("createdDeviceNumber", deviceNumber))
    }.invokeOnCompletion {
      if (it != null) resultHandler.handleError(-1, it.localizedMessage)
    }
  }

  override fun postUser(body: PostUserBody, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    if(body.password==null && body.nickname==null && body.avatar==null)
      return resultHandler.handleMessage(1, POST_USER_SUCCESS)
    val uid = context.getUid()
    val uLevel = context.getULevel()
    GlobalScope.launch(vertx.dispatcher()) {
      //请求中包含用户ID或是该用户为普通用户时只能修改自身
      val sql = if (body.username == null || uLevel==UserLevel.USER.ordinal) {
        //所有等级用户修改自己时都采用这个逻辑
        "UPDATE $USER_TABLE SET " +
          ((body.password?.let { "password=?," } ?: "") +
            (body.nickname?.let { "nickname=?," } ?: "") +
            (body.phone?.let { "phone=?," } ?: "") +
            (body.avatar?.let { "avatar=?," } ?: "")).removeSuffix(",") +
          " WHERE id=$uid"
      } else {
        //管理员和超级管理员修改其他用户的逻辑
        "UPDATE $USER_TABLE SET " +
          ((body.password?.let { "password=?," } ?: "") +
            (body.nickname?.let { "nickname=?," } ?: "") +
            (body.phone?.let { "phone=?," } ?: "") +
            (body.avatar?.let { "avatar=?," } ?: "")).removeSuffix(",") +
          " WHERE email=? AND level>$uLevel " +
          //如果是普通管理员，则只能修改属于自己的用户，超级管理员没有限制
          (if(uLevel==UserLevel.ADMIN.ordinal) "AND admin_id=$uid" else "")
      }
      val result = sqlClient.updateWithParamsAwait(sql, JsonArray().apply {
        body.password?.let(::add)
        body.nickname?.let(::add)
        body.phone?.let(::add)
        body.avatar?.let(::add)
        body.username?.let(::add)
      })
      if (result.updated != 1)
        throw Exception(POST_USER_FAILURE)
      else
        resultHandler.handleMessage(1, POST_USER_SUCCESS)

    }.invokeOnCompletion {
      if (it != null) resultHandler.handleError(-1, it.localizedMessage)
      else {
        sqlClient.userLog(context.getUid(), UserAction.USER,
          "修改用户" + (body.username?:""))
      }
    }
  }

  override fun putUser(body: PutUserBody, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
    //创建用户等级，只能低于自己等级
    if (body.level < context.getULevel())
      return resultHandler.handleError(-1, NO_AUTH)
    GlobalScope.launch(vertx.dispatcher()) {
      if (sqlClient.querySingleWithParamsAwait("SELECT id FROM $USER_TABLE WHERE email=?", JsonArray(listOf(body.username))) != null) {
        throw Exception(USER_ALREADY_EXISTS)
      } else {
        val sql = "INSERT INTO $USER_TABLE (admin_id,level,email,password,nickname) VALUES (?,?,?,?,?)"
        sqlClient.updateWithParamsAwait(sql, JsonArray(listOf(
          //如果创建普通管理员，则不设置admin_id，登录时会将id设为admin_id
          //如果是普通管理员创建用户，则admin_id只能是该管理员id，超级管理员可设置任意admin_id
          if (body.level == UserLevel.ADMIN.ordinal) 0 else if( context.getULevel() == UserLevel.SUPER.ordinal ) body.adminId else context.getUid()
          , body.level
          , body.username
          , body.password
          , body.nickname)))
        resultHandler.handleMessage(1, REGISTER_SUCCESS)
      }
    }.invokeOnCompletion {
      if (it != null)
        resultHandler.handleError(-1, it.localizedMessage)
      else {
        sqlClient.userLog(context.getUid(),UserAction.USER,
          "创建用户${body.username}")
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
