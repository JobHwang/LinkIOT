package cn.hdussta.link.linkServer.dashboard.bean;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link cn.hdussta.link.linkServer.dashboard.bean.PutDeviceBody}.
 * NOTE: This class has been automatically generated from the {@link cn.hdussta.link.linkServer.dashboard.bean.PutDeviceBody} original class using Vert.x codegen.
 */
public class PutDeviceBodyConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, PutDeviceBody obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "description":
          if (member.getValue() instanceof String) {
            obj.setDescription((String)member.getValue());
          }
          break;
        case "name":
          if (member.getValue() instanceof String) {
            obj.setName((String)member.getValue());
          }
          break;
        case "script":
          if (member.getValue() instanceof String) {
            obj.setScript((String)member.getValue());
          }
          break;
      }
    }
  }

  public static void toJson(PutDeviceBody obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(PutDeviceBody obj, java.util.Map<String, Object> json) {
    if (obj.getDescription() != null) {
      json.put("description", obj.getDescription());
    }
    if (obj.getName() != null) {
      json.put("name", obj.getName());
    }
    if (obj.getScript() != null) {
      json.put("script", obj.getScript());
    }
  }
}
