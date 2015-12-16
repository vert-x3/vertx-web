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

package io.vertx.serviceproxy.testmodel;

import io.vertx.serviceproxy.testmodel.TestService;
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
import io.vertx.serviceproxy.testmodel.TestService;
import io.vertx.serviceproxy.testmodel.SomeEnum;
import io.vertx.core.Vertx;
import java.util.Set;
import io.vertx.serviceproxy.testmodel.TestConnection;
import io.vertx.core.json.JsonArray;
import io.vertx.serviceproxy.testmodel.TestDataObject;
import java.util.List;
import java.util.Map;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.serviceproxy.testmodel.TestConnectionWithCloseFuture;
import io.vertx.core.Handler;

/*
  Generated Proxy code - DO NOT EDIT
  @author Roger the Robot
*/
public class TestServiceVertxProxyHandler extends ProxyHandler {

  public static final long DEFAULT_CONNECTION_TIMEOUT = 5 * 60; // 5 minutes 

  private final Vertx vertx;
  private final TestService service;
  private final long timerID;
  private long lastAccessed;
  private final long timeoutSeconds;

  public TestServiceVertxProxyHandler(Vertx vertx, TestService service) {
    this(vertx, service, DEFAULT_CONNECTION_TIMEOUT);
  }

  public TestServiceVertxProxyHandler(Vertx vertx, TestService service, long timeoutInSecond) {
    this(vertx, service, true, timeoutInSecond);
  }

  public TestServiceVertxProxyHandler(Vertx vertx, TestService service, boolean topLevel, long timeoutSeconds) {
    this.vertx = vertx;
    this.service = service;
    this.timeoutSeconds = timeoutSeconds;
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

  public MessageConsumer<JsonObject> registerHandler(String address) {
    MessageConsumer<JsonObject> consumer = vertx.eventBus().<JsonObject>consumer(address).handler(this);
    this.setConsumer(consumer);
    return consumer;
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
    try {
      JsonObject json = msg.body();
      String action = msg.headers().get("action");
      if (action == null) {
        throw new IllegalStateException("action not specified");
      }
      accessed();
      switch (action) {



        case "longDeliverySuccess": {
          service.longDeliverySuccess(createHandler(msg));
          break;
        }
        case "longDeliveryFailed": {
          service.longDeliveryFailed(createHandler(msg));
          break;
        }
        case "createConnection": {
          service.createConnection((java.lang.String)json.getValue("str"), res -> {
            if (res.failed()) {
              msg.fail(-1, res.cause().getMessage());
            } else {
              String proxyAddress = UUID.randomUUID().toString();
              ProxyHelper.registerService(TestConnection.class, vertx, res.result(), proxyAddress, false, timeoutSeconds);
              msg.reply(null, new DeliveryOptions().addHeader("proxyaddr", proxyAddress));
            }
          });
          break;
        }
        case "createConnectionWithCloseFuture": {
          service.createConnectionWithCloseFuture(res -> {
            if (res.failed()) {
              msg.fail(-1, res.cause().getMessage());
            } else {
              String proxyAddress = UUID.randomUUID().toString();
              ProxyHelper.registerService(TestConnectionWithCloseFuture.class, vertx, res.result(), proxyAddress, false, timeoutSeconds);
              msg.reply(null, new DeliveryOptions().addHeader("proxyaddr", proxyAddress));
            }
          });
          break;
        }
        case "noParams": {
          service.noParams();
          break;
        }
        case "basicTypes": {
          service.basicTypes((java.lang.String)json.getValue("str"), json.getValue("b") == null ? null : (json.getLong("b").byteValue()), json.getValue("s") == null ? null : (json.getLong("s").shortValue()), json.getValue("i") == null ? null : (json.getLong("i").intValue()), json.getValue("l") == null ? null : (json.getLong("l").longValue()), json.getValue("f") == null ? null : (json.getDouble("f").floatValue()), json.getValue("d") == null ? null : (json.getDouble("d").doubleValue()), json.getInteger("c") == null ? null : (char)(int)(json.getInteger("c")), (boolean)json.getValue("bool"));
          break;
        }
        case "basicBoxedTypes": {
          service.basicBoxedTypes((java.lang.String)json.getValue("str"), json.getValue("b") == null ? null : (json.getLong("b").byteValue()), json.getValue("s") == null ? null : (json.getLong("s").shortValue()), json.getValue("i") == null ? null : (json.getLong("i").intValue()), json.getValue("l") == null ? null : (json.getLong("l").longValue()), json.getValue("f") == null ? null : (json.getDouble("f").floatValue()), json.getValue("d") == null ? null : (json.getDouble("d").doubleValue()), json.getInteger("c") == null ? null : (char)(int)(json.getInteger("c")), (java.lang.Boolean)json.getValue("bool"));
          break;
        }
        case "basicBoxedTypesNull": {
          service.basicBoxedTypesNull((java.lang.String)json.getValue("str"), json.getValue("b") == null ? null : (json.getLong("b").byteValue()), json.getValue("s") == null ? null : (json.getLong("s").shortValue()), json.getValue("i") == null ? null : (json.getLong("i").intValue()), json.getValue("l") == null ? null : (json.getLong("l").longValue()), json.getValue("f") == null ? null : (json.getDouble("f").floatValue()), json.getValue("d") == null ? null : (json.getDouble("d").doubleValue()), json.getInteger("c") == null ? null : (char)(int)(json.getInteger("c")), (java.lang.Boolean)json.getValue("bool"));
          break;
        }
        case "jsonTypes": {
          service.jsonTypes((io.vertx.core.json.JsonObject)json.getValue("jsonObject"), (io.vertx.core.json.JsonArray)json.getValue("jsonArray"));
          break;
        }
        case "jsonTypesNull": {
          service.jsonTypesNull((io.vertx.core.json.JsonObject)json.getValue("jsonObject"), (io.vertx.core.json.JsonArray)json.getValue("jsonArray"));
          break;
        }
        case "enumType": {
          service.enumType(json.getString("someEnum") == null ? null : io.vertx.serviceproxy.testmodel.SomeEnum.valueOf(json.getString("someEnum")));
          break;
        }
        case "enumTypeNull": {
          service.enumTypeNull(json.getString("someEnum") == null ? null : io.vertx.serviceproxy.testmodel.SomeEnum.valueOf(json.getString("someEnum")));
          break;
        }
        case "enumTypeAsResult": {
          service.enumTypeAsResult(createHandler(msg));
          break;
        }
        case "enumTypeAsResultNull": {
          service.enumTypeAsResultNull(createHandler(msg));
          break;
        }
        case "dataObjectType": {
          service.dataObjectType(json.getJsonObject("options") == null ? null : new io.vertx.serviceproxy.testmodel.TestDataObject(json.getJsonObject("options")));
          break;
        }
        case "dataObjectTypeNull": {
          service.dataObjectTypeNull(json.getJsonObject("options") == null ? null : new io.vertx.serviceproxy.testmodel.TestDataObject(json.getJsonObject("options")));
          break;
        }
        case "listParams": {
          service.listParams(convertList(json.getJsonArray("listString").getList()), json.getJsonArray("listByte").stream().map(o -> ((Number)o).byteValue()).collect(Collectors.toList()), json.getJsonArray("listShort").stream().map(o -> ((Number)o).shortValue()).collect(Collectors.toList()), json.getJsonArray("listInt").stream().map(o -> ((Number)o).intValue()).collect(Collectors.toList()), json.getJsonArray("listLong").stream().map(o -> ((Number)o).longValue()).collect(Collectors.toList()), convertList(json.getJsonArray("listJsonObject").getList()), convertList(json.getJsonArray("listJsonArray").getList()), json.getJsonArray("listDataObject").stream().map(o -> new TestDataObject((JsonObject)o)).collect(Collectors.toList()));
          break;
        }
        case "setParams": {
          service.setParams(convertSet(json.getJsonArray("setString").getList()), json.getJsonArray("setByte").stream().map(o -> ((Number)o).byteValue()).collect(Collectors.toSet()), json.getJsonArray("setShort").stream().map(o -> ((Number)o).shortValue()).collect(Collectors.toSet()), json.getJsonArray("setInt").stream().map(o -> ((Number)o).intValue()).collect(Collectors.toSet()), json.getJsonArray("setLong").stream().map(o -> ((Number)o).longValue()).collect(Collectors.toSet()), convertSet(json.getJsonArray("setJsonObject").getList()), convertSet(json.getJsonArray("setJsonArray").getList()), json.getJsonArray("setDataObject").stream().map(o -> new TestDataObject((JsonObject)o)).collect(Collectors.toSet()));
          break;
        }
        case "mapParams": {
          service.mapParams(convertMap(json.getJsonObject("mapString").getMap()), json.getJsonObject("mapByte").getMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> ((java.lang.Number)entry.getValue()).byteValue())), json.getJsonObject("mapShort").getMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> ((java.lang.Number)entry.getValue()).shortValue())), json.getJsonObject("mapInt").getMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> ((java.lang.Number)entry.getValue()).intValue())), json.getJsonObject("mapLong").getMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> ((java.lang.Number)entry.getValue()).longValue())), convertMap(json.getJsonObject("mapJsonObject").getMap()), convertMap(json.getJsonObject("mapJsonArray").getMap()));
          break;
        }
        case "stringHandler": {
          service.stringHandler(createHandler(msg));
          break;
        }
        case "stringNullHandler": {
          service.stringNullHandler(createHandler(msg));
          break;
        }
        case "byteHandler": {
          service.byteHandler(createHandler(msg));
          break;
        }
        case "byteNullHandler": {
          service.byteNullHandler(createHandler(msg));
          break;
        }
        case "shortHandler": {
          service.shortHandler(createHandler(msg));
          break;
        }
        case "shortNullHandler": {
          service.shortNullHandler(createHandler(msg));
          break;
        }
        case "intHandler": {
          service.intHandler(createHandler(msg));
          break;
        }
        case "intNullHandler": {
          service.intNullHandler(createHandler(msg));
          break;
        }
        case "longHandler": {
          service.longHandler(createHandler(msg));
          break;
        }
        case "longNullHandler": {
          service.longNullHandler(createHandler(msg));
          break;
        }
        case "floatHandler": {
          service.floatHandler(createHandler(msg));
          break;
        }
        case "floatNullHandler": {
          service.floatNullHandler(createHandler(msg));
          break;
        }
        case "doubleHandler": {
          service.doubleHandler(createHandler(msg));
          break;
        }
        case "doubleNullHandler": {
          service.doubleNullHandler(createHandler(msg));
          break;
        }
        case "charHandler": {
          service.charHandler(createHandler(msg));
          break;
        }
        case "charNullHandler": {
          service.charNullHandler(createHandler(msg));
          break;
        }
        case "booleanHandler": {
          service.booleanHandler(createHandler(msg));
          break;
        }
        case "booleanNullHandler": {
          service.booleanNullHandler(createHandler(msg));
          break;
        }
        case "jsonObjectHandler": {
          service.jsonObjectHandler(createHandler(msg));
          break;
        }
        case "jsonObjectNullHandler": {
          service.jsonObjectNullHandler(createHandler(msg));
          break;
        }
        case "jsonArrayHandler": {
          service.jsonArrayHandler(createHandler(msg));
          break;
        }
        case "jsonArrayNullHandler": {
          service.jsonArrayNullHandler(createHandler(msg));
          break;
        }
        case "dataObjectHandler": {
          service.dataObjectHandler(res -> {
            if (res.failed()) {
              msg.fail(-1, res.cause().getMessage());
            } else {
              msg.reply(res.result() == null ? null : res.result().toJson());
            }
         });
          break;
        }
        case "dataObjectNullHandler": {
          service.dataObjectNullHandler(res -> {
            if (res.failed()) {
              msg.fail(-1, res.cause().getMessage());
            } else {
              msg.reply(res.result() == null ? null : res.result().toJson());
            }
         });
          break;
        }
        case "voidHandler": {
          service.voidHandler(createHandler(msg));
          break;
        }
        case "fluentMethod": {
          service.fluentMethod((java.lang.String)json.getValue("str"), createHandler(msg));
          break;
        }
        case "fluentNoParams": {
          service.fluentNoParams();
          break;
        }
        case "failingMethod": {
          service.failingMethod(createHandler(msg));
          break;
        }
        case "invokeWithMessage": {
          service.invokeWithMessage((io.vertx.core.json.JsonObject)json.getValue("object"), (java.lang.String)json.getValue("str"), json.getValue("i") == null ? null : (json.getLong("i").intValue()), json.getInteger("chr") == null ? null : (char)(int)(json.getInteger("chr")), json.getString("senum") == null ? null : io.vertx.serviceproxy.testmodel.SomeEnum.valueOf(json.getString("senum")), createHandler(msg));
          break;
        }
        case "listStringHandler": {
          service.listStringHandler(createListHandler(msg));
          break;
        }
        case "listByteHandler": {
          service.listByteHandler(createListHandler(msg));
          break;
        }
        case "listShortHandler": {
          service.listShortHandler(createListHandler(msg));
          break;
        }
        case "listIntHandler": {
          service.listIntHandler(createListHandler(msg));
          break;
        }
        case "listLongHandler": {
          service.listLongHandler(createListHandler(msg));
          break;
        }
        case "listFloatHandler": {
          service.listFloatHandler(createListHandler(msg));
          break;
        }
        case "listDoubleHandler": {
          service.listDoubleHandler(createListHandler(msg));
          break;
        }
        case "listCharHandler": {
          service.listCharHandler(createListCharHandler(msg));
          break;
        }
        case "listBoolHandler": {
          service.listBoolHandler(createListHandler(msg));
          break;
        }
        case "listJsonObjectHandler": {
          service.listJsonObjectHandler(createListHandler(msg));
          break;
        }
        case "listJsonArrayHandler": {
          service.listJsonArrayHandler(createListHandler(msg));
          break;
        }
        case "listDataObjectHandler": {
          service.listDataObjectHandler(res -> {
            if (res.failed()) {
              msg.fail(-1, res.cause().getMessage());
            } else {
              msg.reply(new JsonArray(res.result().stream().map(TestDataObject::toJson).collect(Collectors.toList())));
            }
         });
          break;
        }
        case "setStringHandler": {
          service.setStringHandler(createSetHandler(msg));
          break;
        }
        case "setByteHandler": {
          service.setByteHandler(createSetHandler(msg));
          break;
        }
        case "setShortHandler": {
          service.setShortHandler(createSetHandler(msg));
          break;
        }
        case "setIntHandler": {
          service.setIntHandler(createSetHandler(msg));
          break;
        }
        case "setLongHandler": {
          service.setLongHandler(createSetHandler(msg));
          break;
        }
        case "setFloatHandler": {
          service.setFloatHandler(createSetHandler(msg));
          break;
        }
        case "setDoubleHandler": {
          service.setDoubleHandler(createSetHandler(msg));
          break;
        }
        case "setCharHandler": {
          service.setCharHandler(createSetCharHandler(msg));
          break;
        }
        case "setBoolHandler": {
          service.setBoolHandler(createSetHandler(msg));
          break;
        }
        case "setJsonObjectHandler": {
          service.setJsonObjectHandler(createSetHandler(msg));
          break;
        }
        case "setJsonArrayHandler": {
          service.setJsonArrayHandler(createSetHandler(msg));
          break;
        }
        case "setDataObjectHandler": {
          service.setDataObjectHandler(res -> {
            if (res.failed()) {
              msg.fail(-1, res.cause().getMessage());
            } else {
              msg.reply(new JsonArray(res.result().stream().map(TestDataObject::toJson).collect(Collectors.toList())));
            }
         });
          break;
        }
        case "ignoredMethod": {
          service.ignoredMethod();
          break;
        }
        default: {
          throw new IllegalStateException("Invalid action: " + action);
        }
      }
    } catch (Throwable t) {
      msg.fail(-1, t.getMessage());
      throw t;
    }
  }

  private <T> Handler<AsyncResult<T>> createHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        msg.fail(-1, res.cause().getMessage());
      } else {
        if (res.result() != null  && res.result().getClass().isEnum()) {          msg.reply(((Enum) res.result()).name());        } else {          msg.reply(res.result());        }      }
    };
  }

  private <T> Handler<AsyncResult<List<T>>> createListHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        msg.fail(-1, res.cause().getMessage());
      } else {
        msg.reply(new JsonArray(res.result()));
      }
    };
  }

  private <T> Handler<AsyncResult<Set<T>>> createSetHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        msg.fail(-1, res.cause().getMessage());
      } else {
        msg.reply(new JsonArray(new ArrayList<>(res.result())));
      }
    };
  }

  private Handler<AsyncResult<List<Character>>> createListCharHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        msg.fail(-1, res.cause().getMessage());
      } else {
        JsonArray arr = new JsonArray();
        for (Character chr: res.result()) {
          arr.add((int) chr);
        }
        msg.reply(arr);
      }
    };
  }

  private Handler<AsyncResult<Set<Character>>> createSetCharHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        msg.fail(-1, res.cause().getMessage());
      } else {
        JsonArray arr = new JsonArray();
        for (Character chr: res.result()) {
          arr.add((int) chr);
        }
        msg.reply(arr);
      }
    };
  }

  private <T> Map<String, T> convertMap(Map map) {
    return (Map<String, T>)map;
  }

  private <T> List<T> convertList(List list) {
    return (List<T>)list;
  }

  private <T> Set<T> convertSet(List list) {
    return new HashSet<T>((List<T>)list);
  }
}