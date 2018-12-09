package cn.hdussta.link.linkServer.manager

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

@DataObject
class SensorInfo(){
  /**
   * 传感器ID
   */
  var id:Int = 0
  /**
   * 传感器数据类型
   */
  lateinit var type: DataType
  constructor(jsonArray: JsonArray):this(){
    id = jsonArray.getInteger(0)
    type = when(jsonArray.getInteger(1)){
      DataType.NUMBER.ordinal-> DataType.NUMBER
      DataType.TEXT.ordinal-> DataType.TEXT
      DataType.POINT.ordinal-> DataType.POINT
      DataType.IMAGE.ordinal-> DataType.IMAGE
      DataType.BOOLEAN.ordinal-> DataType.BOOLEAN
      DataType.OBJECT.ordinal-> DataType.OBJECT
      else-> DataType.UNKNOWN
    }
  }

  constructor(json:JsonObject):this(){
    id = json.getInteger("id")
    type = when(json.getInteger("type")){
      DataType.NUMBER.ordinal-> DataType.NUMBER
      DataType.TEXT.ordinal-> DataType.TEXT
      DataType.POINT.ordinal-> DataType.POINT
      DataType.IMAGE.ordinal-> DataType.IMAGE
      DataType.BOOLEAN.ordinal-> DataType.BOOLEAN
      DataType.OBJECT.ordinal-> DataType.OBJECT
      else-> DataType.UNKNOWN
    }
  }

  fun toJson():JsonObject{
    return JsonObject(mapOf("id" to id,"type" to type.ordinal))
  }
}
