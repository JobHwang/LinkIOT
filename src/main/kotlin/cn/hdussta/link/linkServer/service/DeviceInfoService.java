package cn.hdussta.link.linkServer.service;

import cn.hdussta.link.linkServer.device.DeviceInfo;
import cn.hdussta.link.linkServer.device.impl.DeviceInfoServiceImpl;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.sstore.SessionStore;

@ProxyGen
@VertxGen
public interface DeviceInfoService {
  String SERVICE_NAME = "device-info-service";
  String SERVICE_ADDRESS = "service.device.info";
  static DeviceInfoService createService(Vertx vertx, SQLClient sqlClient, SessionStore map, ManagerService managerService){
    return new DeviceInfoServiceImpl(vertx,sqlClient,map,managerService);
  }

  /**
   * 通过token获取设备状态
   * @param token
   * @param handler
   * @return
   */
  @Fluent
  DeviceInfoService getDeviceByToken(String token,Handler<AsyncResult<DeviceInfo>> handler);

  /**
   * 设备登录
   * @param id
   * @param secret
   * @param handler
   * @return
   */
  @Fluent
  DeviceInfoService login(String id,String secret,Handler<AsyncResult<String>> handler);

  /**
   * 设备登出
   * @param token
   * @param handler
   * @return
   */
  @Fluent
  DeviceInfoService logout(String token,Handler<AsyncResult<Void>> handler);

  /**
   * 设备上传状态
   * 内部实现中会调用ManagerService中的updateState判断更新情况
   * @param token
   * @param state
   * @param handler
   * @return
   */
  @Fluent
  DeviceInfoService setDeviceState(String token,JsonObject state, Handler<AsyncResult<JsonObject>> handler);

  /**
   * 查询设备当前状态
   * @param token
   * @param handler
   * @return
   */
  @Fluent
  DeviceInfoService getDeviceState(String token, Handler<AsyncResult<JsonObject>> handler);
}
