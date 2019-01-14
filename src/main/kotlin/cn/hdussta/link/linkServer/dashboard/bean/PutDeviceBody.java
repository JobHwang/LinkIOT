package cn.hdussta.link.linkServer.dashboard.bean;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
public class PutDeviceBody {
  private String name;
  private String description;
  private String script;

  public PutDeviceBody(JsonObject json){
    PutDeviceBodyConverter.fromJson(json,this);
  }

  public PutDeviceBody(String json){
    PutDeviceBodyConverter.fromJson(new JsonObject(json),this);
  }

  public JsonObject toJson(){
    JsonObject json = new JsonObject();
    PutDeviceBodyConverter.toJson(this,json);
    return json;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
  }
}
