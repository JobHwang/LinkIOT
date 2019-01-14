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

package cn.hdussta.link.linkServer.service.dashboard;

import cn.hdussta.link.linkServer.service.dashboard.SensorService;
import io.vertx.core.Vertx;
import io.vertx.core.Handler;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.serviceproxy.ProxyHandler;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;
import io.vertx.serviceproxy.HelperUtils;

import cn.hdussta.link.linkServer.dashboard.bean.PutSensorBody;
import cn.hdussta.link.linkServer.dashboard.bean.PostSensorBody;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.core.AsyncResult;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.generator.ApiHandlerUtils;
/*
  Generated Proxy code - DO NOT EDIT
  @author Roger the Robot
*/

@SuppressWarnings({"unchecked", "rawtypes"})
public class SensorServiceVertxProxyHandler extends ProxyHandler {

  public static final long DEFAULT_CONNECTION_TIMEOUT = 5 * 60; // 5 minutes 
  private final Vertx vertx;
  private final SensorService service;
  private final long timerID;
  private long lastAccessed;
  private final long timeoutSeconds;

  public SensorServiceVertxProxyHandler(Vertx vertx, SensorService service){
    this(vertx, service, DEFAULT_CONNECTION_TIMEOUT);
  }

  public SensorServiceVertxProxyHandler(Vertx vertx, SensorService service, long timeoutInSecond){
    this(vertx, service, true, timeoutInSecond);
  }

  public SensorServiceVertxProxyHandler(Vertx vertx, SensorService service, boolean topLevel, long timeoutSeconds) {
      this.vertx = vertx;
      this.service = service;
      this.timeoutSeconds = timeoutSeconds;
      try {
        this.vertx.eventBus().registerDefaultCodec(ServiceException.class,
            new ServiceExceptionMessageCodec());
      } catch (IllegalStateException ex) {}
      if (timeoutSeconds != -1 && !topLevel) {
        long period = timeoutSeconds * 1000 / 2;
        if (period > 10000) {
          period = 10000;
        }
        this.timerID = vertx.setPeriodic(period, this::checkTimedOut);
      } else {
        this.timerID = -1;
      }
      accessed();
    }


  private void checkTimedOut(long id) {
    long now = System.nanoTime();
    if (now - lastAccessed > timeoutSeconds * 1000000000) {
      close();
    }
  }

    @Override
    public void close() {
      if (timerID != -1) {
        vertx.cancelTimer(timerID);
      }
      super.close();
    }

    private void accessed() {
      this.lastAccessed = System.nanoTime();
    }

  public void handle(Message<JsonObject> msg) {
    try{
      JsonObject json = msg.body();
      String action = msg.headers().get("action");
      if (action == null) throw new IllegalStateException("action not specified");
      accessed();
      switch (action) {
        case "getSensors": {
          JsonObject contextSerialized = json.getJsonObject("context");
          if (contextSerialized == null)
            throw new IllegalStateException("Received action " + action + " without OperationRequest \"context\"");
          OperationRequest context = new OperationRequest(contextSerialized);
          JsonObject params = context.getParams();
          try {
            service.getSensors((java.lang.String)ApiHandlerUtils.searchInJson(params, "deviceId"),
                            ApiHandlerUtils.searchOptionalLongInJson(params, "offset").map(Long::intValue).orElse(null),
                            ApiHandlerUtils.searchOptionalLongInJson(params, "limit").map(Long::intValue).orElse(null),
                            context,
                            res -> {
                            if (res.failed()) {
                              if (res.cause() instanceof ServiceException) {
                                msg.reply(res.cause());
                              } else {
                                msg.reply(new ServiceException(-1, res.cause().getMessage()));
                              }
                            } else {
                              msg.reply(res.result() == null ? null : res.result().toJson());
                            }
                          }
            );
          } catch (Exception e) {
            msg.reply(new ServiceException(-1, e.getMessage()));
          }
          break;
        }
        case "putSensor": {
          JsonObject contextSerialized = json.getJsonObject("context");
          if (contextSerialized == null)
            throw new IllegalStateException("Received action " + action + " without OperationRequest \"context\"");
          OperationRequest context = new OperationRequest(contextSerialized);
          JsonObject params = context.getParams();
          try {
            service.putSensor((java.lang.String)ApiHandlerUtils.searchInJson(params, "deviceId"),
                            ApiHandlerUtils.searchOptionalJsonObjectInJson(params, "body").map(j -> new cn.hdussta.link.linkServer.dashboard.bean.PutSensorBody(j)).orElse(null),
                            context,
                            res -> {
                            if (res.failed()) {
                              if (res.cause() instanceof ServiceException) {
                                msg.reply(res.cause());
                              } else {
                                msg.reply(new ServiceException(-1, res.cause().getMessage()));
                              }
                            } else {
                              msg.reply(res.result() == null ? null : res.result().toJson());
                            }
                          }
            );
          } catch (Exception e) {
            msg.reply(new ServiceException(-1, e.getMessage()));
          }
          break;
        }
        case "postSensor": {
          JsonObject contextSerialized = json.getJsonObject("context");
          if (contextSerialized == null)
            throw new IllegalStateException("Received action " + action + " without OperationRequest \"context\"");
          OperationRequest context = new OperationRequest(contextSerialized);
          JsonObject params = context.getParams();
          try {
            service.postSensor((java.lang.String)ApiHandlerUtils.searchInJson(params, "deviceId"),
                            ApiHandlerUtils.searchOptionalJsonObjectInJson(params, "body").map(j -> new cn.hdussta.link.linkServer.dashboard.bean.PostSensorBody(j)).orElse(null),
                            context,
                            res -> {
                            if (res.failed()) {
                              if (res.cause() instanceof ServiceException) {
                                msg.reply(res.cause());
                              } else {
                                msg.reply(new ServiceException(-1, res.cause().getMessage()));
                              }
                            } else {
                              msg.reply(res.result() == null ? null : res.result().toJson());
                            }
                          }
            );
          } catch (Exception e) {
            msg.reply(new ServiceException(-1, e.getMessage()));
          }
          break;
        }
        case "deleteSensor": {
          JsonObject contextSerialized = json.getJsonObject("context");
          if (contextSerialized == null)
            throw new IllegalStateException("Received action " + action + " without OperationRequest \"context\"");
          OperationRequest context = new OperationRequest(contextSerialized);
          JsonObject params = context.getParams();
          try {
            service.deleteSensor((java.lang.String)ApiHandlerUtils.searchInJson(params, "deviceId"),
                            ApiHandlerUtils.searchOptionalLongInJson(params, "sensorId").map(Long::intValue).orElse(null),
                            context,
                            res -> {
                            if (res.failed()) {
                              if (res.cause() instanceof ServiceException) {
                                msg.reply(res.cause());
                              } else {
                                msg.reply(new ServiceException(-1, res.cause().getMessage()));
                              }
                            } else {
                              msg.reply(res.result() == null ? null : res.result().toJson());
                            }
                          }
            );
          } catch (Exception e) {
            msg.reply(new ServiceException(-1, e.getMessage()));
          }
          break;
        }
        case "getData": {
          JsonObject contextSerialized = json.getJsonObject("context");
          if (contextSerialized == null)
            throw new IllegalStateException("Received action " + action + " without OperationRequest \"context\"");
          OperationRequest context = new OperationRequest(contextSerialized);
          JsonObject params = context.getParams();
          try {
            service.getData((java.lang.String)ApiHandlerUtils.searchInJson(params, "deviceId"),
                            ApiHandlerUtils.searchOptionalLongInJson(params, "sensorId").map(Long::intValue).orElse(null),
                            ApiHandlerUtils.searchOptionalLongInJson(params, "offset").map(Long::intValue).orElse(null),
                            ApiHandlerUtils.searchOptionalLongInJson(params, "limit").map(Long::intValue).orElse(null),
                            context,
                            res -> {
                            if (res.failed()) {
                              if (res.cause() instanceof ServiceException) {
                                msg.reply(res.cause());
                              } else {
                                msg.reply(new ServiceException(-1, res.cause().getMessage()));
                              }
                            } else {
                              msg.reply(res.result() == null ? null : res.result().toJson());
                            }
                          }
            );
          } catch (Exception e) {
            msg.reply(new ServiceException(-1, e.getMessage()));
          }
          break;
        }
        default: throw new IllegalStateException("Invalid action: " + action);
      }
    } catch (Throwable t) {
      msg.reply(new ServiceException(500, t.getMessage()));
      throw t;
    }
  }
}