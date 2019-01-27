package cn.hdussta.link.linkServer.transport.http

import cn.hdussta.link.linkServer.manager.DeviceInfo
import cn.hdussta.link.linkServer.transport.AbstractTransportVerticle
import cn.hdussta.link.linkServer.utils.message
import cn.hdussta.link.linkServer.utils.messageState
import cn.hdussta.link.linkServer.utils.messageToken
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @name HTTPVerticle
 * @description 部署Http服务端
 * @author Wooyme
 */
class HTTPVerticle : AbstractTransportVerticle() {
  override val logger = LoggerFactory.getLogger(HTTPVerticle::class.java)
  override fun start() {
    super.start()
    initProxy()
    initRouter()
  }

  /**
   * 创建Restful服务
   * @API 登录 GET /api/auth/login/{设备id}/{设备秘钥}
   * @API 登出 GET /api/auth/logout/{messageToken}
   * @API 上传数据 POST /api/data/{messageToken}
   * @API 上传状态 POST /api/state/{messageToken}
   */
  private fun initRouter(){
    val router = Router.router(vertx)
    router.route().handler(BodyHandler.create().setBodyLimit(MAX_BODY_SIZE))
    router.route("/api/auth/login/:id/:secret").handler { handleAuthLogin(it) }
    router.route("/api/auth/logout/:messageToken").handler { handleAuthLogout(it, it.request().getParam("messageToken")) }
    router.route(HttpMethod.POST, "/api/data/:messageToken").handler { handleData(it, it.request().getParam("messageToken")) }
    router.route(HttpMethod.POST,"/api/state/:messageToken").handler{ handleState(it,it.request().getParam("messageToken")) }
    vertx.executeBlocking<Void>({
      vertx.createHttpServer().requestHandler(router::accept).listen(28080){
        logger.info("Listen at 28080")
      }
    }){}

  }

  private fun handleAuthLogin(context: RoutingContext) {
    val id = context.request().getParam("id")
    val secret = context.request().getParam("secret")
    deviceManagerService.login(id, secret,false) {
      if (it.failed())
        context.response().end(message(-1, it.cause().localizedMessage))
      else
        context.response().end(messageToken(1, LOGIN_SUCCESS_MSG, it.result()))
    }
  }

  private fun handleAuthLogout(context: RoutingContext, token: String) {
    deviceManagerService.logout(token) {
      if (it.failed())
        context.response().end(message(-1, it.cause().localizedMessage))
      else
        context.response().end(message(1, LOGOUT_SUCCESS_MSG))
    }
  }

  private fun handleData(context: RoutingContext, token: String) {
    GlobalScope.launch(vertx.dispatcher()) {
      val deviceInfo = awaitResult<DeviceInfo> {
        deviceManagerService.getDeviceByToken(token, it)
      }
      val json = context.bodyAsJson
      if(json!=null){
        val result = awaitResult<JsonObject> {
          scriptService.handle(deviceInfo,json,it)
        }
        context.response().end(message(1,"OK",result))
      }
    }.invokeOnCompletion {
      if (it != null) {
        context.response().end(message(-1, it.localizedMessage?: UNKNOWN_EXCEPTION))
      }
    }
  }

  private fun handleState(context: RoutingContext,token:String){
    val state = context.bodyAsJson
    deviceManagerService.updateState(token,state.toString()){
      when {
          it.failed() -> context.response().end(message(-1,it.cause().localizedMessage))
          it.result()==null -> context.response().end(message(1, UPDATE_STATE_SUCCESS))
          else -> context.response().end(messageState(2, NEED_UPDATE_STATE,it.result()))
      }
    }
  }


  companion object {
    private const val MAX_BODY_SIZE = 100 * 1024L
    private const val LOGIN_SUCCESS_MSG = "登录成功"
    private const val LOGOUT_SUCCESS_MSG = "登出成功"
    private const val UPDATE_STATE_SUCCESS = "更新状态成功"
    private const val NEED_UPDATE_STATE = "需要更新状态"
    private const val UNKNOWN_EXCEPTION = "未知的错误"
  }
}
