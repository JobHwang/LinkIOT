package cn.hdussta.link.linkServer.data

import cn.hdussta.link.linkServer.common.BaseMicroserviceVerticle
import cn.hdussta.link.linkServer.data.impl.RedirectServiceImpl
import cn.hdussta.link.linkServer.data.impl.MySqlStorageServiceImpl
import cn.hdussta.link.linkServer.data.impl.ScriptServiceImpl
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.web.client.WebClient
import io.vertx.serviceproxy.ServiceBinder
import io.vertx.serviceproxy.ServiceProxyBuilder
import java.util.function.Consumer
import javax.script.ScriptContext
import javax.script.ScriptEngineManager


class ScriptVerticle:BaseMicroserviceVerticle() {
  private val logger = LoggerFactory.getLogger(ScriptVerticle::class.java)
  private val engine = ScriptEngineManager().getEngineByName("JavaScript")
  private val bindings = engine.createBindings()
  private val eventBus:EventBus by lazy { this.vertx.eventBus() }
  private lateinit var consumer: MessageConsumer<JsonObject>
  private val sqlConfig = JsonObject(mapOf(
    "host" to "link.hdussta.cn",
    "username" to "root",
    "password" to "Admin88888",
    "database" to "sstalink"))
  override fun start() {
    super.start()
    val sqlClient = MySQLClient.createShared(vertx, sqlConfig)
    val scriptService = ScriptServiceImpl(vertx,vertx.sharedData().getLocalMap("script-map"),sqlClient,engine)
    val binder = ServiceBinder(vertx).setAddress(ScriptServiceImpl.SERVICE_ADDRESS)
    consumer = binder.register(cn.hdussta.link.linkServer.service.ScriptService::class.java, scriptService)
    val proxyBuilder = ServiceProxyBuilder(vertx).setAddress(ScriptServiceImpl.SERVICE_ADDRESS)
    proxyBuilder.build(cn.hdussta.link.linkServer.service.ScriptService::class.java)
    publishEventBusService(ScriptServiceImpl.SERVICE_NAME, ScriptServiceImpl.SERVICE_ADDRESS, cn.hdussta.link.linkServer.service.ScriptService::class.java)
    logger.info(PUBLISH_SUCCESS)

    eventBus.consumer<JsonObject>(AbstractDataHandleService.DATA_HANDLE_SERVICE_PUBLISH_ADDRESS){
      val funcName = "func${it.body().getString("name")}"
      val address = it.body().getString("address")
      val consumer = Consumer<Map<String,Map<String,Any>>> {map->
        eventBus.send(address,JsonObject(map))
      }
      bindings[funcName] = consumer
      this.engine.setBindings(bindings,ScriptContext.GLOBAL_SCOPE)
    }
    vertx.deployVerticle(MySqlStorageServiceImpl())
    vertx.deployVerticle(RedirectServiceImpl(WebClient.create(vertx)))
  }

  companion object {
    private const val PUBLISH_SUCCESS = "成功发布自定义脚本服务"
  }
}
