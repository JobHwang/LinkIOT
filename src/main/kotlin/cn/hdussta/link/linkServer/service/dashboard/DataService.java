package cn.hdussta.link.linkServer.service.dashboard;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
public interface DataService {
  void listData(String deviceId,Integer sensorId,Integer offset, Integer limit, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void countData(String deviceId,Integer sensorId, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
}
