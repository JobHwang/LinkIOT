package cn.hdussta.link.linkServer.data;

import java.util.Map;

@FunctionalInterface
public interface JsFunction {
  void accept(Map<String,Object> things,Map<String,Object> params);
}
