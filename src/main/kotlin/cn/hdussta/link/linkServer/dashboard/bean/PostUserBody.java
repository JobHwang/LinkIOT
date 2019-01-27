package cn.hdussta.link.linkServer.dashboard.bean;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
public class PostUserBody {
  private Integer uid;
  private String password;
  private Integer level;
  private String avatar;
  private String nickname;

  public PostUserBody(JsonObject json){
    PostUserBodyConverter.fromJson(json,this);
  }

  public PostUserBody(String json){
    PostUserBodyConverter.fromJson(new JsonObject(json),this);
  }

  public JsonObject toJson(){
    JsonObject json = new JsonObject();
    PostUserBodyConverter.toJson(this,json);
    return json;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getAvatar() {
    return avatar;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
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

  public Integer getUid() {
    return uid;
  }

  public void setUid(Integer uid) {
    this.uid = uid;
  }
}
