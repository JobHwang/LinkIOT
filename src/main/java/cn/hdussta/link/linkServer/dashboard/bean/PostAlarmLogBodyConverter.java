package cn.hdussta.link.linkServer.dashboard.bean;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link cn.hdussta.link.linkServer.dashboard.bean.PostAlarmLogBody}.
 * NOTE: This class has been automatically generated from the {@link cn.hdussta.link.linkServer.dashboard.bean.PostAlarmLogBody} original class using Vert.x codegen.
 */
public class PostAlarmLogBodyConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, PostAlarmLogBody obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "handle":
          if (member.getValue() instanceof String) {
            obj.setHandle((String)member.getValue());
          }
          break;
        case "logId":
          if (member.getValue() instanceof Number) {
            obj.setLogId(((Number)member.getValue()).intValue());
          }
          break;
      }
    }
  }

  public static void toJson(PostAlarmLogBody obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(PostAlarmLogBody obj, java.util.Map<String, Object> json) {
    if (obj.getHandle() != null) {
      json.put("handle", obj.getHandle());
    }
    if (obj.getLogId() != null) {
      json.put("logId", obj.getLogId());
    }
  }
}
