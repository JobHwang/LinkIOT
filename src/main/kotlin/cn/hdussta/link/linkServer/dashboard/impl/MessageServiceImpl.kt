package cn.hdussta.link.linkServer.dashboard.impl

import cn.hdussta.link.linkServer.service.MessageService
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.handler.sockjs.SockJSSocket

class MessageServiceImpl(private val socketMap:Map<String,SockJSSocket>):MessageService {
  override fun sendMessageToUser(username: String, json: JsonObject, handler: Handler<AsyncResult<Void>>): MessageService {
    val future = Future.future<Void>().setHandler(handler)
    socketMap[username]?.write(json.toBuffer())?:future.fail("用户未连接")
    return this
  }
}
