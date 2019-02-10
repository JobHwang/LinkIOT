package cn.hdussta.link.linkServer.dashboard.bean;


import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
@DataObject(generateConverter = true)
public class PostAlarmLogBody {
  private Integer logId;
  private String handle;

  public PostAlarmLogBody(JsonObject json){
    PostAlarmLogBodyConverter.fromJson(json,this);
  }

  public PostAlarmLogBody(String json){
    PostAlarmLogBodyConverter.fromJson(new JsonObject(json),this);
  }

  public JsonObject toJson(){
    JsonObject json = new JsonObject();
    PostAlarmLogBodyConverter.toJson(this,json);
    return json;
  }

  public Integer getLogId() {
    return logId;
  }

  public void setLogId(Integer logId) {
    this.logId = logId;
  }

  public String getHandle() {
    return handle;
  }

  public void setHandle(String handle) {
    this.handle = handle;
  }
}
