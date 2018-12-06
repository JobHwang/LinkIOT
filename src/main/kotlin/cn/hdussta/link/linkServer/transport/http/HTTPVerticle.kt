package cn.hdussta.link.linkServer.transport.http

import cn.hdussta.link.linkServer.common.BaseMicroserviceVerticle
import cn.hdussta.link.linkServer.device.DeviceInfoService
import cn.hdussta.link.linkServer.utils.message
import io.vertx.core.http.HttpMethod
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.servicediscovery.types.EventBusService

class HTTPVerticle:BaseMicroserviceVerticle() {
  private val logger = LoggerFactory.getLogger(HTTPVerticle::class.java)
  lateinit var deviceInfoService: DeviceInfoService
  override fun start() {
    super.start()
    val router = Router.router(vertx)
    router.route("/api/auth/login/:id/:secret").handler { handleAuthLogin(it) }
    router.route("/api/auth/logout/:token").handler { handleAuthLogout(it,it.request().getParam("token")) }
    router.route(HttpMethod.POST,"/api/data/:method/:token").handler { handleData(it,it.request().getParam("method"),it.request().getParam("token")) }
    EventBusService.getProxy(discovery,DeviceInfoService::class.java) {
      if (it.failed()) {
        logger.error(it.cause().localizedMessage)
      } else {
        this.deviceInfoService = it.result()
        logger.info("Got device-info-service")
      }
    }
    vertx.createHttpServer().requestHandler(router::accept).listen(28080)
  }

  private fun handleAuthLogin(context: RoutingContext){
    val id = context.request().getParam("id")
    val secret = context.request().getParam("secret")
    deviceInfoService.login(id,secret){
      if(it.failed())
        context.response().end(message(-1,it.cause().localizedMessage))
      else
        context.response().end(message(1, LOGIN_SUCCESS_MSG,it.result()))
    }
  }

  private fun handleAuthLogout(context: RoutingContext,token:String){
    deviceInfoService.logout(token){
      if(it.failed())
        context.response().end(message(-1,it.cause().localizedMessage))
      else
        context.response().end(message(1, LOGOUT_SUCCESS_MSG))
    }
  }

  private fun handleData(context: RoutingContext,method:String,token:String){

  }

  companion object {
    const val LOGIN_SUCCESS_MSG = "登录成功"
    const val LOGOUT_SUCCESS_MSG = "登出成功"
  }
}
