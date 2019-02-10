package cn.hdussta.link.linkServer.service.dashboard;

import cn.hdussta.link.linkServer.dashboard.bean.PostAlarmLogBody;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
public interface AlarmLogService {
  void getAlarmLogs(Integer offset, Integer limit, @Nullable Integer level,@Nullable String deviceId, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
  void deleteAlarmLog(Integer logId, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
  void postAlarmLog(PostAlarmLogBody body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
  void countAlarmLogs(@Nullable Integer level,@Nullable String deviceId, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
}
