package cn.hdussta.link.linkServer.dashboard.impl

import cn.hdussta.link.linkServer.common.USER_TABLE
import cn.hdussta.link.linkServer.dashboard.*
import cn.hdussta.link.linkServer.dashboard.bean.PostRegisterBody
import cn.hdussta.link.linkServer.service.dashboard.UserService
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.api.OperationRequest
import io.vertx.ext.web.api.OperationResponse
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserServiceImpl(private val vertx: Vertx,private val sqlClient: SQLClient):UserService {
  override fun postRegister(body:PostRegisterBody, context: OperationRequest, resultHandler: Handler<AsyncResult<OperationResponse>>) {
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
  }
}
