package cn.hdussta.link.linkServer.service.dashboard;

import cn.hdussta.link.linkServer.dashboard.bean.PostUserBody;
import cn.hdussta.link.linkServer.dashboard.bean.RegisterBody;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
public interface UserService {
  void register(RegisterBody body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
  void getUser(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
  void postUser(PostUserBody body,OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
}
