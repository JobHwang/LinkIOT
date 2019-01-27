package cn.hdussta.link.linkServer.dashboard.bean;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
public class PutUserBody {
  private String username;
  private String password;
  private String nickname;
  private Integer level;
  private Integer adminId;
  public PutUserBody(JsonObject json){
    PutUserBodyConverter.fromJson(json,this);
  }
  public PutUserBody(String json){
    PutUserBodyConverter.fromJson(new JsonObject(json),this);
  }

  public JsonObject toJson(){
    JsonObject json = new JsonObject();
    PutUserBodyConverter.toJson(this,json);
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

  public Integer getLevel() {
    return level;
  }

  public void setLevel(Integer level) {
    this.level = level;
  }

  public Integer getAdminId() {
    return adminId;
  }

  public void setAdminId(Integer adminId) {
    this.adminId = adminId;
  }
}
