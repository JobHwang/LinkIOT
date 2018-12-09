package cn.hdussta.link.linkServer.service;

import cn.hdussta.link.linkServer.manager.DeviceInfo;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

@ProxyGen
@VertxGen
public interface DeviceManagerService {

  String SERVICE_NAME = "device-manager-service";
  String SERVICE_ADDRESS = "service.device.manager";
  /**
   * 设备登录
   * @param id
   * @param secret
   * @param handler
   * @return
   */
  @Fluent
  DeviceManagerService login(String id, String secret, Handler<AsyncResult<String>> handler);

  /**
   * 设备登出
   * @param token
   * @param handler
   * @return
   */
  @Fluent
  DeviceManagerService logout(String token,Handler<AsyncResult<Void>> handler);

  /**
   * 通过token获取设备状态
   * @param token
   * @param handler
   * @return
   */
  @Fluent
  DeviceManagerService getDeviceByToken(String token,Handler<AsyncResult<DeviceInfo>> handler);

  /**
   * 设备更新数据到ManagerService
   * 如果设备更新的状态与ManagerService中存储的预期状态相同，则完成更新并删除预期状态，否则返回预期状态
   * @param token
   * @param state
   * @param handler
   * @return
   */
  @Fluent
  DeviceManagerService updateState(String token, String state, Handler<AsyncResult<String>> handler);

  /**
   * 用户获取设备当前状态
   * @param deviceId
   * @param handler
   * @return
   */
  @Fluent
  DeviceManagerService getState(String deviceId, Handler<AsyncResult<String>> handler);

  /**
   * 用户设置设备预期状态
   * 如果设备在线或可以下发状态，则直接下发，否则将存储在ManagerService中，等到设备下次上传状态时再下发
   * @param deviceId
   * @param desired
   * @param handler
   * @return
   */
  @Fluent
  DeviceManagerService setState(String deviceId, String desired, Handler<AsyncResult<Void>> handler);
}
