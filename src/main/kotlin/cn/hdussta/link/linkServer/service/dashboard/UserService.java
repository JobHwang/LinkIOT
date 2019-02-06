package cn.hdussta.link.linkServer.service.dashboard;

import cn.hdussta.link.linkServer.dashboard.bean.PostUserBody;
import cn.hdussta.link.linkServer.dashboard.bean.PutUserBody;
import com.sun.org.apache.xpath.internal.operations.Bool;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
public interface UserService {
  void putUser(PutUserBody body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
  void getUser(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
  void postUser(PostUserBody body,OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
  void delUser(String username,OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
  void listUser(@Nullable Integer adminId, Integer offset, Integer limit, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
}
