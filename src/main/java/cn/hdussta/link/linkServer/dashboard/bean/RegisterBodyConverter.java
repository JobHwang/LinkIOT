package cn.hdussta.link.linkServer.dashboard.bean;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link cn.hdussta.link.linkServer.dashboard.bean.RegisterBody}.
 * NOTE: This class has been automatically generated from the {@link cn.hdussta.link.linkServer.dashboard.bean.RegisterBody} original class using Vert.x codegen.
 */
public class RegisterBodyConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, RegisterBody obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
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

  public static void toJson(RegisterBody obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(RegisterBody obj, java.util.Map<String, Object> json) {
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
