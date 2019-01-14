package cn.hdussta.link.linkServer.service.dashboard;

import cn.hdussta.link.linkServer.dashboard.bean.PostDeviceBody;
import cn.hdussta.link.linkServer.dashboard.bean.PutDeviceBody;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
public interface DeviceService {
  void listDevices(int offset, int limit, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void putDevice(PutDeviceBody body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void postDevice(PostDeviceBody body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void deleteDevice(String deviceId,OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void getState(String deviceId,OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void postState(String deviceId, JsonObject body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void forceClose(String deviceId,OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

}
