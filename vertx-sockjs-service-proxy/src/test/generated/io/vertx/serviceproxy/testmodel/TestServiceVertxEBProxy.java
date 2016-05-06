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
public class TestServiceVertxEBProxy implements TestService {

  private Vertx _vertx;
  private String _address;
  private DeliveryOptions _options;
  private boolean closed;

  public TestServiceVertxEBProxy(Vertx vertx, String address) {
    this(vertx, address, null);
  }

  public TestServiceVertxEBProxy(Vertx vertx, String address, DeliveryOptions options) {
    this._vertx = vertx;
    this._address = address;
    this._options = options;
    try {
      this._vertx.eventBus().registerDefaultCodec(ServiceException.class,
          new ServiceExceptionMessageCodec());
    } catch (IllegalStateException ex) {}
  }

  public void longDeliverySuccess(Handler<AsyncResult<String>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "longDeliverySuccess");
    _vertx.eventBus().<String>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void longDeliveryFailed(Handler<AsyncResult<String>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "longDeliveryFailed");
    _vertx.eventBus().<String>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void createConnection(String str, Handler<AsyncResult<TestConnection>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("str", str);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "createConnection");
    _vertx.eventBus().<TestConnection>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        String addr = res.result().headers().get("proxyaddr");
        resultHandler.handle(Future.succeededFuture(ProxyHelper.createProxy(TestConnection.class, _vertx, addr)));
      }
    });
  }

  public void createConnectionWithCloseFuture(Handler<AsyncResult<TestConnectionWithCloseFuture>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "createConnectionWithCloseFuture");
    _vertx.eventBus().<TestConnectionWithCloseFuture>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        String addr = res.result().headers().get("proxyaddr");
        resultHandler.handle(Future.succeededFuture(ProxyHelper.createProxy(TestConnectionWithCloseFuture.class, _vertx, addr)));
      }
    });
  }

  public void noParams() {
    if (closed) {
      throw new IllegalStateException("Proxy is closed");
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "noParams");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void basicTypes(String str, byte b, short s, int i, long l, float f, double d, char c, boolean bool) {
    if (closed) {
      throw new IllegalStateException("Proxy is closed");
    }
    JsonObject _json = new JsonObject();
    _json.put("str", str);
    _json.put("b", b);
    _json.put("s", s);
    _json.put("i", i);
    _json.put("l", l);
    _json.put("f", f);
    _json.put("d", d);
    _json.put("c", (int)c);
    _json.put("bool", bool);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "basicTypes");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void basicBoxedTypes(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c, Boolean bool) {
    if (closed) {
      throw new IllegalStateException("Proxy is closed");
    }
    JsonObject _json = new JsonObject();
    _json.put("str", str);
    _json.put("b", b);
    _json.put("s", s);
    _json.put("i", i);
    _json.put("l", l);
    _json.put("f", f);
    _json.put("d", d);
    _json.put("c", c == null ? null : (int)c);
    _json.put("bool", bool);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "basicBoxedTypes");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void basicBoxedTypesNull(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c, Boolean bool) {
    if (closed) {
      throw new IllegalStateException("Proxy is closed");
    }
    JsonObject _json = new JsonObject();
    _json.put("str", str);
    _json.put("b", b);
    _json.put("s", s);
    _json.put("i", i);
    _json.put("l", l);
    _json.put("f", f);
    _json.put("d", d);
    _json.put("c", c == null ? null : (int)c);
    _json.put("bool", bool);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "basicBoxedTypesNull");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void jsonTypes(JsonObject jsonObject, JsonArray jsonArray) {
    if (closed) {
      throw new IllegalStateException("Proxy is closed");
    }
    JsonObject _json = new JsonObject();
    _json.put("jsonObject", jsonObject);
    _json.put("jsonArray", jsonArray);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "jsonTypes");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void jsonTypesNull(JsonObject jsonObject, JsonArray jsonArray) {
    if (closed) {
      throw new IllegalStateException("Proxy is closed");
    }
    JsonObject _json = new JsonObject();
    _json.put("jsonObject", jsonObject);
    _json.put("jsonArray", jsonArray);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "jsonTypesNull");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void enumType(SomeEnum someEnum) {
    if (closed) {
      throw new IllegalStateException("Proxy is closed");
    }
    JsonObject _json = new JsonObject();
    _json.put("someEnum", someEnum == null ? null : someEnum.toString());
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "enumType");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void enumTypeNull(SomeEnum someEnum) {
    if (closed) {
      throw new IllegalStateException("Proxy is closed");
    }
    JsonObject _json = new JsonObject();
    _json.put("someEnum", someEnum == null ? null : someEnum.toString());
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "enumTypeNull");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void enumTypeAsResult(Handler<AsyncResult<SomeEnum>> someEnum) {
    if (closed) {
      someEnum.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "enumTypeAsResult");
    _vertx.eventBus().<String>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        someEnum.handle(Future.failedFuture(res.cause()));
      } else {
        someEnum.handle(Future.succeededFuture(res.result().body() == null ? null : SomeEnum.valueOf(res.result().body())));
      }
    });
  }

  public void enumTypeAsResultNull(Handler<AsyncResult<SomeEnum>> someEnum) {
    if (closed) {
      someEnum.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "enumTypeAsResultNull");
    _vertx.eventBus().<String>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        someEnum.handle(Future.failedFuture(res.cause()));
      } else {
        someEnum.handle(Future.succeededFuture(res.result().body() == null ? null : SomeEnum.valueOf(res.result().body())));
      }
    });
  }

  public void dataObjectType(TestDataObject options) {
    if (closed) {
      throw new IllegalStateException("Proxy is closed");
    }
    JsonObject _json = new JsonObject();
    _json.put("options", options == null ? null : options.toJson());
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "dataObjectType");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void dataObjectTypeNull(TestDataObject options) {
    if (closed) {
      throw new IllegalStateException("Proxy is closed");
    }
    JsonObject _json = new JsonObject();
    _json.put("options", options == null ? null : options.toJson());
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "dataObjectTypeNull");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void listParams(List<String> listString, List<Byte> listByte, List<Short> listShort, List<Integer> listInt, List<Long> listLong, List<JsonObject> listJsonObject, List<JsonArray> listJsonArray, List<TestDataObject> listDataObject) {
    if (closed) {
      throw new IllegalStateException("Proxy is closed");
    }
    JsonObject _json = new JsonObject();
    _json.put("listString", new JsonArray(listString));
    _json.put("listByte", new JsonArray(listByte));
    _json.put("listShort", new JsonArray(listShort));
    _json.put("listInt", new JsonArray(listInt));
    _json.put("listLong", new JsonArray(listLong));
    _json.put("listJsonObject", new JsonArray(listJsonObject));
    _json.put("listJsonArray", new JsonArray(listJsonArray));
    _json.put("listDataObject", new JsonArray(listDataObject.stream().map(TestDataObject::toJson).collect(Collectors.toList())));
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listParams");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void setParams(Set<String> setString, Set<Byte> setByte, Set<Short> setShort, Set<Integer> setInt, Set<Long> setLong, Set<JsonObject> setJsonObject, Set<JsonArray> setJsonArray, Set<TestDataObject> setDataObject) {
    if (closed) {
      throw new IllegalStateException("Proxy is closed");
    }
    JsonObject _json = new JsonObject();
    _json.put("setString", new JsonArray(new ArrayList<>(setString)));
    _json.put("setByte", new JsonArray(new ArrayList<>(setByte)));
    _json.put("setShort", new JsonArray(new ArrayList<>(setShort)));
    _json.put("setInt", new JsonArray(new ArrayList<>(setInt)));
    _json.put("setLong", new JsonArray(new ArrayList<>(setLong)));
    _json.put("setJsonObject", new JsonArray(new ArrayList<>(setJsonObject)));
    _json.put("setJsonArray", new JsonArray(new ArrayList<>(setJsonArray)));
    _json.put("setDataObject", new JsonArray(setDataObject.stream().map(TestDataObject::toJson).collect(Collectors.toList())));
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "setParams");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void mapParams(Map<String,String> mapString, Map<String,Byte> mapByte, Map<String,Short> mapShort, Map<String,Integer> mapInt, Map<String,Long> mapLong, Map<String,JsonObject> mapJsonObject, Map<String,JsonArray> mapJsonArray) {
    if (closed) {
      throw new IllegalStateException("Proxy is closed");
    }
    JsonObject _json = new JsonObject();
    _json.put("mapString", new JsonObject(convertMap(mapString)));
    _json.put("mapByte", new JsonObject(convertMap(mapByte)));
    _json.put("mapShort", new JsonObject(convertMap(mapShort)));
    _json.put("mapInt", new JsonObject(convertMap(mapInt)));
    _json.put("mapLong", new JsonObject(convertMap(mapLong)));
    _json.put("mapJsonObject", new JsonObject(convertMap(mapJsonObject)));
    _json.put("mapJsonArray", new JsonObject(convertMap(mapJsonArray)));
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "mapParams");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
  }

  public void stringHandler(Handler<AsyncResult<String>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "stringHandler");
    _vertx.eventBus().<String>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void stringNullHandler(Handler<AsyncResult<String>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "stringNullHandler");
    _vertx.eventBus().<String>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void byteHandler(Handler<AsyncResult<Byte>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "byteHandler");
    _vertx.eventBus().<Byte>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void byteNullHandler(Handler<AsyncResult<Byte>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "byteNullHandler");
    _vertx.eventBus().<Byte>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void shortHandler(Handler<AsyncResult<Short>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "shortHandler");
    _vertx.eventBus().<Short>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void shortNullHandler(Handler<AsyncResult<Short>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "shortNullHandler");
    _vertx.eventBus().<Short>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void intHandler(Handler<AsyncResult<Integer>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "intHandler");
    _vertx.eventBus().<Integer>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void intNullHandler(Handler<AsyncResult<Integer>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "intNullHandler");
    _vertx.eventBus().<Integer>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void longHandler(Handler<AsyncResult<Long>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "longHandler");
    _vertx.eventBus().<Long>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void longNullHandler(Handler<AsyncResult<Long>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "longNullHandler");
    _vertx.eventBus().<Long>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void floatHandler(Handler<AsyncResult<Float>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "floatHandler");
    _vertx.eventBus().<Float>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void floatNullHandler(Handler<AsyncResult<Float>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "floatNullHandler");
    _vertx.eventBus().<Float>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void doubleHandler(Handler<AsyncResult<Double>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "doubleHandler");
    _vertx.eventBus().<Double>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void doubleNullHandler(Handler<AsyncResult<Double>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "doubleNullHandler");
    _vertx.eventBus().<Double>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void charHandler(Handler<AsyncResult<Character>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "charHandler");
    _vertx.eventBus().<Character>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void charNullHandler(Handler<AsyncResult<Character>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "charNullHandler");
    _vertx.eventBus().<Character>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void booleanHandler(Handler<AsyncResult<Boolean>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "booleanHandler");
    _vertx.eventBus().<Boolean>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void booleanNullHandler(Handler<AsyncResult<Boolean>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "booleanNullHandler");
    _vertx.eventBus().<Boolean>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void jsonObjectHandler(Handler<AsyncResult<JsonObject>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "jsonObjectHandler");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void jsonObjectNullHandler(Handler<AsyncResult<JsonObject>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "jsonObjectNullHandler");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void jsonArrayHandler(Handler<AsyncResult<JsonArray>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "jsonArrayHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void jsonArrayNullHandler(Handler<AsyncResult<JsonArray>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "jsonArrayNullHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void dataObjectHandler(Handler<AsyncResult<TestDataObject>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "dataObjectHandler");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body() == null ? null : new TestDataObject(res.result().body())));
                      }
    });
  }

  public void dataObjectNullHandler(Handler<AsyncResult<TestDataObject>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "dataObjectNullHandler");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body() == null ? null : new TestDataObject(res.result().body())));
                      }
    });
  }

  public void voidHandler(Handler<AsyncResult<Void>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "voidHandler");
    _vertx.eventBus().<Void>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public TestService fluentMethod(String str, Handler<AsyncResult<String>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return this;
    }
    JsonObject _json = new JsonObject();
    _json.put("str", str);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "fluentMethod");
    _vertx.eventBus().<String>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
    return this;
  }

  public TestService fluentNoParams() {
    if (closed) {
      throw new IllegalStateException("Proxy is closed");
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "fluentNoParams");
    _vertx.eventBus().send(_address, _json, _deliveryOptions);
    return this;
  }

  public void failingMethod(Handler<AsyncResult<JsonObject>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "failingMethod");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void invokeWithMessage(JsonObject object, String str, int i, char chr, SomeEnum senum, Handler<AsyncResult<String>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("object", object);
    _json.put("str", str);
    _json.put("i", i);
    _json.put("chr", (int)chr);
    _json.put("senum", senum == null ? null : senum.toString());
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "invokeWithMessage");
    _vertx.eventBus().<String>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void listStringHandler(Handler<AsyncResult<List<String>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listStringHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  public void listByteHandler(Handler<AsyncResult<List<Byte>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listByteHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  public void listShortHandler(Handler<AsyncResult<List<Short>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listShortHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  public void listIntHandler(Handler<AsyncResult<List<Integer>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listIntHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  public void listLongHandler(Handler<AsyncResult<List<Long>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listLongHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  public void listFloatHandler(Handler<AsyncResult<List<Float>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listFloatHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  public void listDoubleHandler(Handler<AsyncResult<List<Double>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listDoubleHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  public void listCharHandler(Handler<AsyncResult<List<Character>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listCharHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertToListChar(res.result().body())));
      }
    });
  }

  public void listBoolHandler(Handler<AsyncResult<List<Boolean>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listBoolHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  public void listJsonObjectHandler(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listJsonObjectHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  public void listJsonArrayHandler(Handler<AsyncResult<List<JsonArray>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listJsonArrayHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
  }

  public void listDataObjectHandler(Handler<AsyncResult<List<TestDataObject>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "listDataObjectHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body().stream().map(o -> o instanceof Map ? new TestDataObject(new JsonObject((Map) o)) : new TestDataObject((JsonObject) o)).collect(Collectors.toList())));
      }
    });
  }

  public void setStringHandler(Handler<AsyncResult<Set<String>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "setStringHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  public void setByteHandler(Handler<AsyncResult<Set<Byte>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "setByteHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  public void setShortHandler(Handler<AsyncResult<Set<Short>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "setShortHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  public void setIntHandler(Handler<AsyncResult<Set<Integer>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "setIntHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  public void setLongHandler(Handler<AsyncResult<Set<Long>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "setLongHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  public void setFloatHandler(Handler<AsyncResult<Set<Float>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "setFloatHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  public void setDoubleHandler(Handler<AsyncResult<Set<Double>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "setDoubleHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  public void setCharHandler(Handler<AsyncResult<Set<Character>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "setCharHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertToSetChar(res.result().body())));
      }
    });
  }

  public void setBoolHandler(Handler<AsyncResult<Set<Boolean>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "setBoolHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  public void setJsonObjectHandler(Handler<AsyncResult<Set<JsonObject>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "setJsonObjectHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  public void setJsonArrayHandler(Handler<AsyncResult<Set<JsonArray>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "setJsonArrayHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(convertSet(res.result().body().getList())));
      }
    });
  }

  public void setDataObjectHandler(Handler<AsyncResult<Set<TestDataObject>>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "setDataObjectHandler");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body().stream().map(o -> o instanceof Map ? new TestDataObject(new JsonObject((Map) o)) : new TestDataObject((JsonObject) o)).collect(Collectors.toSet())));
      }
    });
  }

  public void ignoredMethod() {
  }


  private List<Character> convertToListChar(JsonArray arr) {
    List<Character> list = new ArrayList<>();
    for (Object obj: arr) {
      Integer jobj = (Integer)obj;
      list.add((char)(int)jobj);
    }
    return list;
  }

  private Set<Character> convertToSetChar(JsonArray arr) {
    Set<Character> set = new HashSet<>();
    for (Object obj: arr) {
      Integer jobj = (Integer)obj;
      set.add((char)(int)jobj);
    }
    return set;
  }

  private <T> Map<String, T> convertMap(Map map) {
    if (map.isEmpty()) { 
      return (Map<String, T>) map; 
    } 
     
    Object elem = map.values().stream().findFirst().get(); 
    if (!(elem instanceof Map) && !(elem instanceof List)) { 
      return (Map<String, T>) map; 
    } else { 
      Function<Object, T> converter; 
      if (elem instanceof List) { 
        converter = object -> (T) new JsonArray((List) object); 
      } else { 
        converter = object -> (T) new JsonObject((Map) object); 
      } 
      return ((Map<String, T>) map).entrySet() 
       .stream() 
       .collect(Collectors.toMap(Map.Entry::getKey, converter::apply)); 
    } 
  }
  private <T> List<T> convertList(List list) {
    if (list.isEmpty()) { 
          return (List<T>) list; 
        } 
     
    Object elem = list.get(0); 
    if (!(elem instanceof Map) && !(elem instanceof List)) { 
      return (List<T>) list; 
    } else { 
      Function<Object, T> converter; 
      if (elem instanceof List) { 
        converter = object -> (T) new JsonArray((List) object); 
      } else { 
        converter = object -> (T) new JsonObject((Map) object); 
      } 
      return (List<T>) list.stream().map(converter).collect(Collectors.toList()); 
    } 
  }
  private <T> Set<T> convertSet(List list) {
    return new HashSet<T>(convertList(list));
  }
}