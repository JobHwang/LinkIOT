package cn.hdussta.link.linkServer.manager

import cn.hdussta.link.linkServer.common.DeviceStatus
import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.io.Serializable

@DataObject
class DeviceInfo():Serializable {
  /**
   * 设备ID
   */
  lateinit var id:String
  /**
   * 设备名称
   */
  lateinit var name:String
  /**
   * 设备秘钥
   */
  lateinit var secret:String
  /**
   * 设备状态(指连接情况和权限等)
   */
  lateinit var status: DeviceStatus
  /**
   * 设备下所有传感器
   */
  lateinit var sensors:Map<String, SensorInfo>

  /**
   * 设备登录后获得的token
   */
  lateinit var token:String


  constructor(json:JsonObject):this(){
    id = json.getString("id")
    name = json.getString("name")
    secret = json.getString("secret")
    status = when(json.getInteger("status")){
      0-> DeviceStatus.OFF
      1-> DeviceStatus.ON
      2-> DeviceStatus.BANNED
      3-> DeviceStatus.BANNED
      else-> DeviceStatus.UNKNOWN
    }
    sensors = json.getJsonArray("sensors").map {
      it as JsonObject
      val sensorInfo = SensorInfo(it)
      sensorInfo.id to sensorInfo
    }.toMap()
    token = json.getString("messageToken")
  }

  constructor(jsonArray: JsonArray,sensorArray:List<JsonArray>):this(){
    id = jsonArray.getString(0)
    name = jsonArray.getString(1)
    secret = jsonArray.getString(2)
    status = when(jsonArray.getInteger(3)){
      0-> DeviceStatus.OFF
      1-> DeviceStatus.ON
      2-> DeviceStatus.BANNED
      3-> DeviceStatus.LOST
      else-> DeviceStatus.UNKNOWN
    }
    sensors = sensorArray.map {
      val sensorInfo = SensorInfo(it)
      sensorInfo.id to sensorInfo
    }.toMap()
  }

  fun toJson():JsonObject{
    return JsonObject(mapOf("id" to id
      ,"name" to name
      ,"secret" to secret
      ,"status" to status.ordinal
      ,"sensors" to JsonArray(sensors.map { it.value.toJson() })
      ,"messageToken" to token))
  }
}
