package cn.hdussta.link.linkServer.service.dashboard;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
public interface UserLogService {
  void getUserLogs(Integer offset, Integer limit, @Nullable String username,@Nullable Integer type, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
  void countUserLogs(@Nullable String username,@Nullable Integer type, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
}
