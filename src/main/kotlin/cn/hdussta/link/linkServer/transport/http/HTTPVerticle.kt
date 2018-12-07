package cn.hdussta.link.linkServer.transport.http

import cn.hdussta.link.linkServer.common.BaseMicroserviceVerticle
import cn.hdussta.link.linkServer.data.DataHandleService
import cn.hdussta.link.linkServer.data.impl.DataStorageMySql
import cn.hdussta.link.linkServer.device.DeviceInfo
import cn.hdussta.link.linkServer.device.DeviceInfoService
import cn.hdussta.link.linkServer.utils.message
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.servicediscovery.types.EventBusService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HTTPVerticle : BaseMicroserviceVerticle() {
  private val logger = LoggerFactory.getLogger(HTTPVerticle::class.java)
  lateinit var deviceInfoService: DeviceInfoService
  lateinit var basicHandleService: DataHandleService
  override fun start() {
    super.start()
    val router = Router.router(vertx)
    router.route().handler(BodyHandler.create().setBodyLimit(MAX_BODY_SIZE))
    router.route("/api/auth/login/:id/:secret").handler { handleAuthLogin(it) }
    router.route("/api/auth/logout/:token").handler { handleAuthLogout(it, it.request().getParam("token")) }
    router.route(HttpMethod.POST, "/api/data/:token").handler { handleData(it, it.request().getParam("token")) }
    EventBusService.getProxy(discovery, DeviceInfoService::class.java) {
      if (it.failed()) {
        logger.error(it.cause().localizedMessage)
        this.stop()
      } else {
        this.deviceInfoService = it.result()
        logger.info(GET_DEVICEINFO_SERVICE)
      }
    }
    EventBusService.getServiceProxyWithJsonFilter(discovery, JsonObject().put("name", BASIC_SERVICE_NAME), DataHandleService::class.java) {
      if (it.failed()) {
        logger.error(it.cause().localizedMessage)
        this.stop()
      } else {
        this.basicHandleService = it.result()
        logger.info(GET_BASIC_SERVICE)
      }
    }
    vertx.executeBlocking<Void>({
      vertx.createHttpServer().requestHandler(router::accept).listen(28080){
        logger.info("Listen at 28080")
      }
    }){}
  }

  private fun handleAuthLogin(context: RoutingContext) {
    val id = context.request().getParam("id")
    val secret = context.request().getParam("secret")
    deviceInfoService.login(id, secret) {
      if (it.failed())
        context.response().end(message(-1, it.cause().localizedMessage))
      else
        context.response().end(message(1, LOGIN_SUCCESS_MSG, it.result()))
    }
  }

  private fun handleAuthLogout(context: RoutingContext, token: String) {
    deviceInfoService.logout(token) {
      if (it.failed())
        context.response().end(message(-1, it.cause().localizedMessage))
      else
        context.response().end(message(1, LOGOUT_SUCCESS_MSG))
    }
  }

  private fun handleData(context: RoutingContext, token: String) {
    GlobalScope.launch(vertx.dispatcher()) {
      val deviceInfo = awaitResult<DeviceInfo> {
        deviceInfoService.getDeviceByToken(token, it)
      }
      val json = context.bodyAsJson
      if(json!=null){
        val sensorId = json.getInteger("sensorid")
        val result = awaitResult<JsonObject> {
          basicHandleService.handle(deviceInfo,sensorId,json,it)
        }
        context.response().end(message(1,"OK",result))
      }
    }.invokeOnCompletion {
      if (it != null) {
        context.response().end(message(-1, it.localizedMessage))
      }
    }
  }

  companion object {
    private const val MAX_BODY_SIZE = 100 * 1024L
    private const val LOGIN_SUCCESS_MSG = "登录成功"
    private const val LOGOUT_SUCCESS_MSG = "登出成功"
    private const val GET_DEVICEINFO_SERVICE = "成功获取设备信息服务"
    private const val GET_BASIC_SERVICE = "成功获取基础处理服务"
    const val BASIC_SERVICE_NAME = DataStorageMySql.SERVICE_NAME
  }
}
