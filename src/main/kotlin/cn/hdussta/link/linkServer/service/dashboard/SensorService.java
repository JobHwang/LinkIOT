package cn.hdussta.link.linkServer.service.dashboard;

import cn.hdussta.link.linkServer.dashboard.bean.PostSensorBody;
import cn.hdussta.link.linkServer.dashboard.bean.PutSensorBody;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
public interface SensorService {

  void getSensors(String deviceId, Integer offset, Integer limit, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void putSensor(String deviceId, PutSensorBody body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void postSensor(String deviceId, PostSensorBody body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void deleteSensor(String deviceId,Integer sensorId,OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void getData(String deviceId,Integer sensorId,Integer offset,Integer limit,OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

}
