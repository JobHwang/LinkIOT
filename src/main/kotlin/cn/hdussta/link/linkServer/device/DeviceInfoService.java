package cn.hdussta.link.linkServer.device;

import cn.hdussta.link.linkServer.device.impl.DeviceInfoServiceImpl;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.sstore.SessionStore;

@ProxyGen
@VertxGen
public interface DeviceInfoService {
  String SERVICE_NAME = "device-info-service";
  String SERVICE_ADDRESS = "service.device.info";
  static DeviceInfoService createService(Vertx vertx,SQLClient sqlClient, SessionStore map){
    return new DeviceInfoServiceImpl(vertx,sqlClient,map);
  }

  @Fluent
  DeviceInfoService getDeviceInfo(String id,Handler<AsyncResult<DeviceInfo>> handler);

  @Fluent
  DeviceInfoService getDeviceByToken(String token,Handler<AsyncResult<DeviceInfo>> handler);

  @Fluent
  DeviceInfoService login(String id,String secret,Handler<AsyncResult<String>> handler);

  @Fluent
  DeviceInfoService logout(String token,Handler<AsyncResult<Void>> handler);
}
