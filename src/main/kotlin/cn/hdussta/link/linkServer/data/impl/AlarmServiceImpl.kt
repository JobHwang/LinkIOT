package cn.hdussta.link.linkServer.data.impl

import cn.hdussta.link.linkServer.common.ALARM_LOG_TABLE
import cn.hdussta.link.linkServer.common.DEVICE_LOG_TABLE
import cn.hdussta.link.linkServer.data.AbstractDataHandleService
import cn.hdussta.link.linkServer.data.LOG
import cn.hdussta.link.linkServer.manager.DeviceInfo
import cn.hdussta.link.linkServer.utils.jsonArray
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.mail.MailClient
import io.vertx.ext.mail.MailConfig
import io.vertx.ext.mail.MailMessage
import io.vertx.ext.sql.SQLClient
import io.vertx.kotlin.ext.mail.sendMailAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait


class AlarmServiceImpl:AbstractDataHandleService(){
  override val address: String
    get() = "service.data.alarm"
  override val name: String
    get() = "Alarm"
  private val mailClient by lazy { MailClient.createShared(vertx
    , MailConfig().setHostname(HOST).setUsername(username).setPassword(password)) }
  override suspend fun handle(info: DeviceInfo, data: JsonObject,param:JsonObject) {
    val level = param.getInteger("level")
    param.getString("email")?.let{ email->
      val message = MailMessage()
      message.from = "867653608@qq.com"
      message.to = listOf(email)
      message.subject = "设备${info.name}报警！"
      message.text = "设备本次上传数据:\n$data"
      mailClient.sendMailAwait(message)
    }
    param.getString("phone")?.let {
      TODO("缺少短信平台")
    }
    param.getString("url")?.let {
      TODO("发送报警信息到指定地址")
    }
    val result = sqlClient.updateWithParamsAwait("INSERT INTO $ALARM_LOG_TABLE (device_id,data,level) VALUES (?,?,?)"
      , jsonArray(info.id,data.toString(),level))
    if(result.updated!=1) throw Exception("未知原因，插入报警历史失败")
  }

  companion object {
    private const val HOST = "smtp.qq.com"
    private const val username = "867653608@qq.com"
    private const val password = "bhqdlzovtapdbbhb"
  }
}
