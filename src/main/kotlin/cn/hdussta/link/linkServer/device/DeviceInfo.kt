package cn.hdussta.link.linkServer.device

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

@DataObject
class DeviceInfo() {
  lateinit var id:String
  lateinit var name:String
  lateinit var secret:String
  lateinit var status:DeviceStatus
  lateinit var rules:Array<String>

  constructor(json:JsonObject):this(){
    id = json.getString("id")
    name = json.getString("name")
    secret = json.getString("secret")
    status = when(json.getInteger("status")){
      0->DeviceStatus.OFF
      1->DeviceStatus.ON
      2->DeviceStatus.BANNED
      3->DeviceStatus.BANNED
      else->DeviceStatus.UNKNOWN
    }
    rules = json.getString("rules").split("|").toTypedArray()
  }

  constructor(jsonArray: JsonArray):this(){
    id = jsonArray.getString(0)
    name = jsonArray.getString(1)
    secret = jsonArray.getString(2)
    status = when(jsonArray.getInteger(3)){
      0->DeviceStatus.OFF
      1->DeviceStatus.ON
      2->DeviceStatus.BANNED
      3->DeviceStatus.LOST
      else->DeviceStatus.UNKNOWN
    }
    rules = jsonArray.getString(4).split("|").toTypedArray()
  }

  fun toJson():JsonObject{
    return JsonObject(mapOf("id" to id
      ,"name" to name,"secret" to secret
      ,"status" to status.ordinal
      ,"rules" to rules.joinToString { "|" }))
  }
}
