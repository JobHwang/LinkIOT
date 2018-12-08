package cn.hdussta.link.linkServer.manager

import cn.hdussta.link.linkServer.device.DeviceInfo
import io.vertx.core.json.JsonObject
import java.io.Serializable

class ManageableDeviceInfo():Serializable{
  lateinit var shadow:JsonObject
  lateinit var info:DeviceInfo
  lateinit var token:String
  constructor(info: DeviceInfo,token:String):this(){
    this.info = info
    this.token = token
    this.shadow = JsonObject()
  }

  constructor(json:JsonObject):this(){
    this.info = DeviceInfo(json.getJsonObject("info"))
    this.shadow = json.getJsonObject("shadow")
    this.token = json.getString("token")
  }

  fun toJson():JsonObject{
    return JsonObject().put("shadow", shadow).put("token", token).put("info", info.toJson())
  }
}
