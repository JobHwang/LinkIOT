package cn.hdussta.link.linkServer.dashboard.bean;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
public class RegisterBody {
  private String username;
  private String password;
  private String nickname;
  public RegisterBody(JsonObject json){
    RegisterBodyConverter.fromJson(json,this);
  }
  public RegisterBody(String json){
    RegisterBodyConverter.fromJson(new JsonObject(json),this);
  }

  public JsonObject toJson(){
    JsonObject json = new JsonObject();
    RegisterBodyConverter.toJson(this,json);
    return json;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }
}
