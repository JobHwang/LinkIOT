package cn.hdussta.link.linkServer.transport

import cn.hdussta.link.linkServer.device.DeviceInfoVerticle
import cn.hdussta.link.linkServer.transport.http.HTTPVerticle
import io.vertx.core.Vertx

fun main(args:Array<String>){
  val vertx = Vertx.vertx()
  vertx.deployVerticle(DeviceInfoVerticle()){
    if(it.failed()){
      it.cause().printStackTrace()
    }else{
      vertx.deployVerticle(HTTPVerticle()){
        if(it.failed()){
          it.cause().printStackTrace()
        }
      }
    }
  }

}
