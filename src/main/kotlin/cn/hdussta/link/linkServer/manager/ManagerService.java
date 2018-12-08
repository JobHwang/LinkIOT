package cn.hdussta.link.linkServer.manager;

import cn.hdussta.link.linkServer.device.DeviceInfo;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;


@ProxyGen
@VertxGen
public interface ManagerService {

  String SERVICE_ADDRESS = "service.device.manage";
  String SERVICE_NAME = "device-manager-service";

  @Fluent
  ManagerService setState(String deviceId, JsonObject desired, Handler<AsyncResult<Void>> handler);

  @Fluent
  ManagerService register(DeviceInfo deviceInfo,String token,  Handler<AsyncResult<Void>> handler);

  @Fluent
  ManagerService unregister(String deviceId,Handler<AsyncResult<Void>> handler);

  @Fluent
  ManagerService getState(String deviceId, Handler<AsyncResult<JsonObject>> handler);

  @Fluent
  ManagerService updateState(String deviceId,JsonObject state,Handler<AsyncResult<JsonObject>> handler);
}
