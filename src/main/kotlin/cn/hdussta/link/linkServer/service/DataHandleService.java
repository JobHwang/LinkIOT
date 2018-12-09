package cn.hdussta.link.linkServer.service;


import cn.hdussta.link.linkServer.manager.DeviceInfo;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface DataHandleService {


  /**
   * 处理设备上传的数据
   * @param device
   * @param data 设备上传格式{"data":{"传感器ID":值}}
   * @param handler
   * @return
   */
  @Fluent
  DataHandleService handle(DeviceInfo device, JsonObject data, Handler<AsyncResult<JsonObject>> handler);
}
