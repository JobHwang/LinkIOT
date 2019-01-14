package cn.hdussta.link.linkServer.dashboard.bean;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link cn.hdussta.link.linkServer.dashboard.bean.PostSensorBody}.
 * NOTE: This class has been automatically generated from the {@link cn.hdussta.link.linkServer.dashboard.bean.PostSensorBody} original class using Vert.x codegen.
 */
public class PostSensorBodyConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, PostSensorBody obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "dataType":
          if (member.getValue() instanceof Number) {
            obj.setDataType(((Number)member.getValue()).intValue());
          }
          break;
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
        case "sensorId":
          if (member.getValue() instanceof Number) {
            obj.setSensorId(((Number)member.getValue()).intValue());
          }
          break;
        case "showType":
          if (member.getValue() instanceof String) {
            obj.setShowType((String)member.getValue());
          }
          break;
      }
    }
  }

  public static void toJson(PostSensorBody obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(PostSensorBody obj, java.util.Map<String, Object> json) {
    if (obj.getDataType() != null) {
      json.put("dataType", obj.getDataType());
    }
    if (obj.getDescription() != null) {
      json.put("description", obj.getDescription());
    }
    if (obj.getName() != null) {
      json.put("name", obj.getName());
    }
    if (obj.getSensorId() != null) {
      json.put("sensorId", obj.getSensorId());
    }
    if (obj.getShowType() != null) {
      json.put("showType", obj.getShowType());
    }
  }
}
