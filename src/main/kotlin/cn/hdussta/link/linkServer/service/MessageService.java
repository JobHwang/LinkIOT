package cn.hdussta.link.linkServer.service;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface MessageService {
  String SERVICE_NAME = "message-service";
  String SERVICE_ADDRESS = "service.message";

  @Fluent
  MessageService sendMessageToUser(String username,JsonObject json, Handler<AsyncResult<Void>> handler);
}
