/*
* Copyright 2014 Red Hat, Inc.
*
* Red Hat licenses this file to you under the Apache License, version 2.0
* (the "License"); you may not use this file except in compliance with the
* License. You may obtain a copy of the License at:
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations
* under the License.
*/

package cn.hdussta.link.linkServer.service;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.Vertx;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Function;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;
import io.vertx.serviceproxy.ProxyUtils;

import cn.hdussta.link.linkServer.manager.DeviceInfo;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import cn.hdussta.link.linkServer.service.ScriptService;
/*
  Generated Proxy code - DO NOT EDIT
  @author Roger the Robot
*/

@SuppressWarnings({"unchecked", "rawtypes"})
public class ScriptServiceVertxEBProxy implements ScriptService {
  private Vertx _vertx;
  private String _address;
  private DeliveryOptions _options;
  private boolean closed;

  public ScriptServiceVertxEBProxy(Vertx vertx, String address) {
    this(vertx, address, null);
  }

  public ScriptServiceVertxEBProxy(Vertx vertx, String address, DeliveryOptions options) {
    this._vertx = vertx;
    this._address = address;
    this._options = options;
    try{
      this._vertx.eventBus().registerDefaultCodec(ServiceException.class, new ServiceExceptionMessageCodec());
    } catch (IllegalStateException ex) {}
  }

  @Override
  public  ScriptService handle(DeviceInfo device, JsonObject data, Handler<AsyncResult<JsonObject>> handler){
    if (closed) {
      handler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return this;
    }
    JsonObject _json = new JsonObject();
    _json.put("device", device == null ? null : device.toJson());
    _json.put("data", data);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "handle");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        handler.handle(Future.failedFuture(res.cause()));
      } else {
        handler.handle(Future.succeededFuture(res.result().body()));
      }
    });
    return this;
  }
  @Override
  public  ScriptService updateScript(String deviceId, Handler<AsyncResult<Void>> handler){
    if (closed) {
      handler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return this;
    }
    JsonObject _json = new JsonObject();
    _json.put("deviceId", deviceId);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "updateScript");
    _vertx.eventBus().<Void>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        handler.handle(Future.failedFuture(res.cause()));
      } else {
        handler.handle(Future.succeededFuture(res.result().body()));
      }
    });
    return this;
  }
  @Override
  public  ScriptService withdrawScript(String deviceId, Handler<AsyncResult<Void>> handler){
    if (closed) {
      handler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return this;
    }
    JsonObject _json = new JsonObject();
    _json.put("deviceId", deviceId);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "withdrawScript");
    _vertx.eventBus().<Void>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        handler.handle(Future.failedFuture(res.cause()));
      } else {
        handler.handle(Future.succeededFuture(res.result().body()));
      }
    });
    return this;
  }
}
