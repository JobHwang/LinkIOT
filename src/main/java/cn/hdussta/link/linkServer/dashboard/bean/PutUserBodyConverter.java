package cn.hdussta.link.linkServer.dashboard.bean;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link cn.hdussta.link.linkServer.dashboard.bean.PutUserBody}.
 * NOTE: This class has been automatically generated from the {@link cn.hdussta.link.linkServer.dashboard.bean.PutUserBody} original class using Vert.x codegen.
 */
public class PutUserBodyConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, PutUserBody obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "adminId":
          if (member.getValue() instanceof Number) {
            obj.setAdminId(((Number)member.getValue()).intValue());
          }
          break;
        case "level":
          if (member.getValue() instanceof Number) {
            obj.setLevel(((Number)member.getValue()).intValue());
          }
          break;
        case "nickname":
          if (member.getValue() instanceof String) {
            obj.setNickname((String)member.getValue());
          }
          break;
        case "password":
          if (member.getValue() instanceof String) {
            obj.setPassword((String)member.getValue());
          }
          break;
        case "username":
          if (member.getValue() instanceof String) {
            obj.setUsername((String)member.getValue());
          }
          break;
      }
    }
  }

  public static void toJson(PutUserBody obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(PutUserBody obj, java.util.Map<String, Object> json) {
    if (obj.getAdminId() != null) {
      json.put("adminId", obj.getAdminId());
    }
    if (obj.getLevel() != null) {
      json.put("level", obj.getLevel());
    }
    if (obj.getNickname() != null) {
      json.put("nickname", obj.getNickname());
    }
    if (obj.getPassword() != null) {
      json.put("password", obj.getPassword());
    }
    if (obj.getUsername() != null) {
      json.put("username", obj.getUsername());
    }
  }
}
