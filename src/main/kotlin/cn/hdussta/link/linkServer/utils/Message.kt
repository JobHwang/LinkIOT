package cn.hdussta.link.linkServer.utils

import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.JsonObject

fun message(status:Int,msg:String):Buffer = JsonObject("status" to status,"msg" to msg).toBuffer()

fun message(status:Int,msg:String,data:JsonObject):Buffer = JsonObject(
  "status" to status,"msg" to msg,"data" to data
).toBuffer()

fun messageToken(status:Int, msg: String, token:String):Buffer = JsonObject("status" to status,"msg" to msg,"data" to JsonObject("token" to token)).toBuffer()

fun messageState(status: Int,msg:String,state:String):Buffer = JsonObject(
  "status" to status,"msg" to msg,"state" to JsonObject(state)).toBuffer()


