package cn.hdussta.link.linkServer.manager

import cn.hdussta.link.linkServer.device.DeviceInfo
import io.vertx.core.json.JsonObject

class ManageableDeviceInfo(val info:DeviceInfo, val token:String){
  val shadow = JsonObject()
}
