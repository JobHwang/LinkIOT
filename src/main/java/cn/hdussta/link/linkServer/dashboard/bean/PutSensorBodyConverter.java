package cn.hdussta.link.linkServer.dashboard.bean;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link cn.hdussta.link.linkServer.dashboard.bean.PutSensorBody}.
 * NOTE: This class has been automatically generated from the {@link cn.hdussta.link.linkServer.dashboard.bean.PutSensorBody} original class using Vert.x codegen.
 */
public class PutSensorBodyConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, PutSensorBody obj) {
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
        case "showType":
          if (member.getValue() instanceof String) {
            obj.setShowType((String)member.getValue());
          }
          break;
      }
    }
  }

  public static void toJson(PutSensorBody obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(PutSensorBody obj, java.util.Map<String, Object> json) {
    if (obj.getDataType() != null) {
      json.put("dataType", obj.getDataType());
    }
    if (obj.getDescription() != null) {
      json.put("description", obj.getDescription());
    }
    if (obj.getName() != null) {
      json.put("name", obj.getName());
    }
    if (obj.getShowType() != null) {
      json.put("showType", obj.getShowType());
    }
  }
}
