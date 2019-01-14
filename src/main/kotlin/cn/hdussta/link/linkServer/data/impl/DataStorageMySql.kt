package cn.hdussta.link.linkServer.data.impl

import cn.hdussta.link.linkServer.common.DataType
import cn.hdussta.link.linkServer.manager.DeviceInfo
import cn.hdussta.link.linkServer.service.DataHandleService
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.sql.SQLClient
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import io.vertx.servicediscovery.ServiceDiscovery
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @name DataStorageMySqlService
 * @description 存储设备上传数据至Mysql中
 * @author Wooyme
 */
class DataStorageMySql(private val vertx: Vertx, override val discovery: ServiceDiscovery, private val sqlClient: SQLClient) : AbstractDataHandleService() {
  override val ruleName: String = RULE_NAME
  override val logger = LoggerFactory.getLogger(DataStorageMySql::class.java)!!
  override fun handle(device: DeviceInfo, data: JsonObject, handler: Handler<AsyncResult<JsonObject>>): DataHandleService {
    val rawData = data.getJsonObject("data")

    if (!rawData.all { device.sensors.containsKey(it.key) }) {
      fail(SENSOR_NOT_FOUND, data, handler)
      return this
    }

    val checkResult = typeCheck(device, rawData)
    if (checkResult != null) {
      fail("Sensor $checkResult data type is wrong", data, handler)
      return this
    }

    GlobalScope.launch(vertx.dispatcher()) {
      rawData.forEach {
        val sensor = device.sensors[it.key]!!
        val table = sensor.type.name.toLowerCase()
        val input = it.value
        sqlClient.updateWithParamsAwait("INSERT INTO sstalink_data_$table (deviceid,sensorid,data) VALUES (?,?,?)"
          , JsonArray(listOf(device.id, sensor.id, input)))
      }
    }.invokeOnCompletion {
      if (it != null) {
        fail(it.localizedMessage, data, handler)
      } else {
        next(UPDATE_SUCCESS, device, data, handler)
      }
    }
    return this
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
    private const val UPDATE_SUCCESS = "上传成功"
    private const val SENSOR_NOT_FOUND = "没有找到对应传感器"
    private const val RULE_NAME = "mysql"
    const val SERVICE_ADDRESS = "service.data.storage"
    const val SERVICE_NAME = "data-storage-service"
  }
}
