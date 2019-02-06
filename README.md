Linkserver
==

image:https://img.shields.io/badge/vert.x-3.6.0-purple.svg[link="https://vertx.io"]

This application was generated using http://start.vertx.io

Building
==

To cn.hdussta.link.linkServer.launch your tests:
```
./mvnw clean test
```

To package your application:
```
./mvnw clean package
```

To run your application:
```
./mvnw clean exec:java
```

Documentation
==
HTTP
-----
* 设备登录
  ```
  GET http://host:28080/api/auth/login/{设备ID}/{设备秘钥}
  ```
  返回
  ```
  {"status":1,"msg":"成功","token":"随机字符串"}
  ```

* 设备登出
  ```
  GET http://host:28080/api/auth/logout/{token}
  ```
* 更新状态
  ```
  POST http://host:28080/api/state/{token}
  状态为任意JSON
  ```
  **如果没有预期状态或预期状态与上传状态相同，则返回更新成功**  
  **如果预期状态与上传状态不服，则更新失败并返回预期状态**
* 上传数据
  ```
  POST http://host:28080/api/data/{token}
  数据格式
  {"传感器ID1":数据,"传感器ID2":数据,.......}
  ```

MQTT
-----
* **mqtt://host:1883**
  ******
* 设备登录
  ```
  连接服务器并设置username=设备ID，password=设备秘钥
  subscribe("devie-state")
  用于接收服务器返回预期状态
  subscribe("device-data")
  用于接收服务器返回数据处理结果
  ```
* 设备登出
  ```
  断开连接默认下线
  ```
* 更新状态
  ```
  publish("device-state",Payload，AT_LEAST_ONCE)
  Payload为任意JSON
  ```
  **如果预期状态为空或预期状态与上传状态相同，则上传成功。**  
  **如果预期状态与上传状态不同，则服务器publish预期状态到device-state**
* 上传数据
  ```
  publish("device-data",Payload,AT_LEAST_ONCE)
  ```
  *payload*
  ```
  {"data":{"传感器ID1":数据,"传感器ID2":数据,.................}}
  ```

设备管理
----
* 状态控制
  ```
  POST http://host:28081/api/manage/state/{设备ID}
  状态为任意JSON
  ```
* 状态查询
  ```
  GET http://host:28081/api/manage/state/{设备ID}
  ```

Help
==

* https://vertx.io/docs/[Vert.x Documentation]
* https://stackoverflow.com/questions/tagged/vert.x?sort=newest&pageSize=15[Vert.x Stack Overflow]
* https://groups.google.com/forum/?fromgroups#!forum/vertx[Vert.x User Group]
* https://gitter.im/eclipse-vertx/vertx-users[Vert.x Gitter]


