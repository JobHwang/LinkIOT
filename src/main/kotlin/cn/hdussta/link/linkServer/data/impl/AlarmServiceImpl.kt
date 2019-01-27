package cn.hdussta.link.linkServer.data.impl

import cn.hdussta.link.linkServer.data.AbstractDataHandleService
import cn.hdussta.link.linkServer.manager.DeviceInfo
import io.vertx.core.json.JsonObject
import io.vertx.ext.mail.MailClient
import io.vertx.ext.mail.MailConfig
import io.vertx.ext.mail.MailMessage
import io.vertx.kotlin.ext.mail.sendMailAwait


class AlarmServiceImpl:AbstractDataHandleService(){
  override val address: String
    get() = "service.data.alarm"
  override val name: String
    get() = "Alarm"
  private val mailClient by lazy { MailClient.createShared(vertx
    , MailConfig().setHostname(HOST).setPort(587).setUsername(username).setPassword(password)) }
  override suspend fun handle(info: DeviceInfo, data: JsonObject,param:JsonObject) {
    param.getString("email")?.let{ email->
      val message = MailMessage()
      message.from = "admin@link.hdussta.cn"
      message.setTo(email)
      message.text = "设备${info.name}"
      mailClient.sendMailAwait(message)
    }
    param.getString("phone")?.let {
      TODO("缺少短信平台")
    }
    param.getString("url")?.let {
      TODO("发送报警信息到指定地址")
    }
  }

  companion object {
    private const val HOST = "link.hdussta.cn"
    private const val username = "admin"
    private const val password = "admin88888"
  }
}
