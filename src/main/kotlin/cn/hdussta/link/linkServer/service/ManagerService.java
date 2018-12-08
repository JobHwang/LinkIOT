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
public interface ManagerService {

  String SERVICE_ADDRESS = "service.device.manage";
  String SERVICE_NAME = "device-manager-service";

  /**
   * 用户设置设备预期状态
   * 如果设备在线或可以下发状态，则直接下发，否则将存储在ManagerService中，等到设备下次上传状态时再下发
   * @param deviceId
   * @param desired
   * @param handler
   * @return
   */
  @Fluent
  ManagerService setState(String deviceId, JsonObject desired, Handler<AsyncResult<Void>> handler);

  /**
   * 注册设备到ManagerService中
   * @param deviceInfo
   * @param token
   * @param handler
   * @return
   */
  @Fluent
  ManagerService register(DeviceInfo deviceInfo,String token,  Handler<AsyncResult<Void>> handler);

  /**
   * 取消ManagerService中注册的设备
   * @param deviceId
   * @param handler
   * @return
   */
  @Fluent
  ManagerService unregister(String deviceId,Handler<AsyncResult<Void>> handler);

  /**
   * 用户获取设备当前状态
   * @param deviceId
   * @param handler
   * @return
   */
  @Fluent
  ManagerService getState(String deviceId, Handler<AsyncResult<JsonObject>> handler);

  /**
   * 设备更新数据到ManagerService
   * 如果设备更新的状态与ManagerService中存储的预期状态相同，则完成更新并删除预期状态，否则返回预期状态
   * @param deviceId
   * @param state
   * @param handler
   * @return
   */
  @Fluent
  ManagerService updateState(String deviceId,JsonObject state,Handler<AsyncResult<JsonObject>> handler);
}
