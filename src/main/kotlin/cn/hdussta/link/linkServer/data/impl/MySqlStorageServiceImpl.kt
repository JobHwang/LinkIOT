package cn.hdussta.link.linkServer.data.impl

import cn.hdussta.link.linkServer.common.DataType
import cn.hdussta.link.linkServer.data.AbstractDataHandleService
import cn.hdussta.link.linkServer.manager.DeviceInfo
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.ext.sql.updateWithParamsAwait

/**
 * @name MySqlStorageServiceImpl
 * @description 存储设备上传数据至Mysql中
 * @author Wooyme
 */
class MySqlStorageServiceImpl : AbstractDataHandleService() {
  override val address: String
    get() = "service.data.storage"
  override val name: String
    get() = "Storage"
  override suspend fun handle(info: DeviceInfo, data: JsonObject,param:JsonObject) {

    if (!data.all { info.sensors.containsKey(it.key) }) {
      fail(info, SENSOR_NOT_FOUND)
      return
    }

    val checkResult = typeCheck(info, data)
    if (checkResult != null) {
      fail(info,"Sensor $checkResult data type is wrong")
      return
    }

    data.forEach {
      val sensor = info.sensors[it.key]!!
      val table = sensor.type.name.toLowerCase()
      val input = it.value
      sqlClient.updateWithParamsAwait("INSERT INTO sstalink_data_$table (deviceid,sensorid,data) VALUES (?,?,?)"
        , JsonArray(listOf(info.id, sensor.id, input)))
    }
  }

  private fun typeCheck(device: DeviceInfo, data: JsonObject): String? {
    data.forEach {
      val id = it.key
      val sensorInfo = device.sensors[id]!!
      val input = it.value
      when (sensorInfo.type) {
        DataType.NUMBER -> {
          if (input !is Int && input !is Double && input !is Float && input !is Long) {
            return id
          }
        }
        DataType.TEXT -> {
          if (input !is String) {
            return id
          }
        }
        DataType.POINT -> {
          if (input !is JsonArray) {
            return id
          }
        }
        DataType.BOOLEAN -> {
          if (input !is Boolean) {
            return id
          }
        }
        DataType.IMAGE -> {
          if (input !is String && input !is ByteArray) {
            return id
          }
        }
        DataType.OBJECT -> {
          if (input !is JsonObject) {
            return id
          }
        }
        DataType.UNKNOWN -> {
          return id
        }
      }
    }
    return null
  }

  companion object {
    private const val SENSOR_NOT_FOUND = "没有找到对应传感器"
  }
}
