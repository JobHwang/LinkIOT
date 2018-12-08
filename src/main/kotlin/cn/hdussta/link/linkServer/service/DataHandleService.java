package cn.hdussta.link.linkServer.service;


import cn.hdussta.link.linkServer.device.DeviceInfo;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface DataHandleService {
  String generated = "generated";
  @Fluent
  DataHandleService handle(DeviceInfo device, int sensorId, JsonObject data, Handler<AsyncResult<JsonObject>> handler);
}
