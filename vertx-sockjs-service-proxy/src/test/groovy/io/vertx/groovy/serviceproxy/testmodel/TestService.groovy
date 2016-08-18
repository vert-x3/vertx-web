/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.groovy.serviceproxy.testmodel;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.vertx.serviceproxy.testmodel.SomeEnum
import io.vertx.groovy.core.Vertx
import java.util.Set
import io.vertx.core.json.JsonArray
import io.vertx.serviceproxy.testmodel.TestDataObject
import java.util.List
import java.util.Map
import io.vertx.core.json.JsonObject
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
*/
@CompileStatic
public class TestService {
  private final def io.vertx.serviceproxy.testmodel.TestService delegate;
  public TestService(Object delegate) {
    this.delegate = (io.vertx.serviceproxy.testmodel.TestService) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static TestService create(Vertx vertx) {
    def ret = InternalHelper.safeCreate(io.vertx.serviceproxy.testmodel.TestService.create(vertx != null ? (io.vertx.core.Vertx)vertx.getDelegate() : null), io.vertx.groovy.serviceproxy.testmodel.TestService.class);
    return ret;
  }
  public static TestService createProxy(Vertx vertx, String address) {
    def ret = InternalHelper.safeCreate(io.vertx.serviceproxy.testmodel.TestService.createProxy(vertx != null ? (io.vertx.core.Vertx)vertx.getDelegate() : null, address), io.vertx.groovy.serviceproxy.testmodel.TestService.class);
    return ret;
  }
  public static TestService createProxyLongDelivery(Vertx vertx, String address) {
    def ret = InternalHelper.safeCreate(io.vertx.serviceproxy.testmodel.TestService.createProxyLongDelivery(vertx != null ? (io.vertx.core.Vertx)vertx.getDelegate() : null, address), io.vertx.groovy.serviceproxy.testmodel.TestService.class);
    return ret;
  }
  public void longDeliverySuccess(Handler<AsyncResult<String>> resultHandler) {
    delegate.longDeliverySuccess(resultHandler);
  }
  public void longDeliveryFailed(Handler<AsyncResult<String>> resultHandler) {
    delegate.longDeliveryFailed(resultHandler);
  }
  public void createConnection(String str, Handler<AsyncResult<TestConnection>> resultHandler) {
    delegate.createConnection(str, resultHandler != null ? new Handler<AsyncResult<io.vertx.serviceproxy.testmodel.TestConnection>>() {
      public void handle(AsyncResult<io.vertx.serviceproxy.testmodel.TestConnection> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(InternalHelper.safeCreate(ar.result(), io.vertx.groovy.serviceproxy.testmodel.TestConnection.class)));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void createConnectionWithCloseFuture(Handler<AsyncResult<TestConnectionWithCloseFuture>> resultHandler) {
    delegate.createConnectionWithCloseFuture(resultHandler != null ? new Handler<AsyncResult<io.vertx.serviceproxy.testmodel.TestConnectionWithCloseFuture>>() {
      public void handle(AsyncResult<io.vertx.serviceproxy.testmodel.TestConnectionWithCloseFuture> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(InternalHelper.safeCreate(ar.result(), io.vertx.groovy.serviceproxy.testmodel.TestConnectionWithCloseFuture.class)));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void noParams() {
    delegate.noParams();
  }
  public void basicTypes(String str, byte b, short s, int i, long l, float f, double d, char c, boolean bool) {
    delegate.basicTypes(str, b, s, i, l, f, d, c, bool);
  }
  public void basicBoxedTypes(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c, Boolean bool) {
    delegate.basicBoxedTypes(str, b, s, i, l, f, d, c, bool);
  }
  public void basicBoxedTypesNull(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c, Boolean bool) {
    delegate.basicBoxedTypesNull(str, b, s, i, l, f, d, c, bool);
  }
  public void jsonTypes(Map<String, Object> jsonObject, List<Object> jsonArray) {
    delegate.jsonTypes(jsonObject != null ? new io.vertx.core.json.JsonObject(jsonObject) : null, jsonArray != null ? new io.vertx.core.json.JsonArray(jsonArray) : null);
  }
  public void jsonTypesNull(Map<String, Object> jsonObject, List<Object> jsonArray) {
    delegate.jsonTypesNull(jsonObject != null ? new io.vertx.core.json.JsonObject(jsonObject) : null, jsonArray != null ? new io.vertx.core.json.JsonArray(jsonArray) : null);
  }
  public void enumType(SomeEnum someEnum) {
    delegate.enumType(someEnum);
  }
  public void enumTypeNull(SomeEnum someEnum) {
    delegate.enumTypeNull(someEnum);
  }
  public void enumTypeAsResult(Handler<AsyncResult<SomeEnum>> someEnum) {
    delegate.enumTypeAsResult(someEnum);
  }
  public void enumTypeAsResultNull(Handler<AsyncResult<SomeEnum>> someEnum) {
    delegate.enumTypeAsResultNull(someEnum);
  }
  public void dataObjectType(Map<String, Object> options = [:]) {
    delegate.dataObjectType(options != null ? new io.vertx.serviceproxy.testmodel.TestDataObject(io.vertx.lang.groovy.InternalHelper.toJsonObject(options)) : null);
  }
  public void dataObjectTypeNull(Map<String, Object> options = [:]) {
    delegate.dataObjectTypeNull(options != null ? new io.vertx.serviceproxy.testmodel.TestDataObject(io.vertx.lang.groovy.InternalHelper.toJsonObject(options)) : null);
  }
  public void listParams(List<String> listString, List<Byte> listByte, List<Short> listShort, List<Integer> listInt, List<Long> listLong, List<Map<String, Object>> listJsonObject, List<List<Object>> listJsonArray, List<Map<String, Object>> listDataObject) {
    delegate.listParams(listString != null ? (List)listString.collect({it}) : null, listByte != null ? (List)listByte.collect({it}) : null, listShort != null ? (List)listShort.collect({it}) : null, listInt != null ? (List)listInt.collect({it}) : null, listLong != null ? (List)listLong.collect({it}) : null, listJsonObject != null ? (List)listJsonObject.collect({new io.vertx.core.json.JsonObject(it)}) : null, listJsonArray != null ? (List)listJsonArray.collect({new io.vertx.core.json.JsonArray(it)}) : null, listDataObject != null ? (List)listDataObject.collect({new io.vertx.serviceproxy.testmodel.TestDataObject(io.vertx.lang.groovy.InternalHelper.toJsonObject(it))}) : null);
  }
  public void setParams(Set<String> setString, Set<Byte> setByte, Set<Short> setShort, Set<Integer> setInt, Set<Long> setLong, Set<Map<String, Object>> setJsonObject, Set<List<Object>> setJsonArray, Set<Map<String, Object>> setDataObject) {
    delegate.setParams(setString != null ? (Set)setString.collect({it}) as Set : null, setByte != null ? (Set)setByte.collect({it}) as Set : null, setShort != null ? (Set)setShort.collect({it}) as Set : null, setInt != null ? (Set)setInt.collect({it}) as Set : null, setLong != null ? (Set)setLong.collect({it}) as Set : null, setJsonObject != null ? (Set)setJsonObject.collect({new io.vertx.core.json.JsonObject(it)}) as Set : null, setJsonArray != null ? (Set)setJsonArray.collect({new io.vertx.core.json.JsonArray(it)}) as Set : null, setDataObject != null ? (Set)setDataObject.collect({new io.vertx.serviceproxy.testmodel.TestDataObject(io.vertx.lang.groovy.InternalHelper.toJsonObject(it))}) as Set : null);
  }
  public void mapParams(Map<String, String> mapString, Map<String, Byte> mapByte, Map<String, Short> mapShort, Map<String, Integer> mapInt, Map<String, Long> mapLong, Map<String, Map<String, Object>> mapJsonObject, Map<String, List<Object>> mapJsonArray) {
    delegate.mapParams(mapString != null ? (Map)mapString.collectEntries({[it.key,it.value]}) : null, mapByte != null ? (Map)mapByte.collectEntries({[it.key,it.value]}) : null, mapShort != null ? (Map)mapShort.collectEntries({[it.key,it.value]}) : null, mapInt != null ? (Map)mapInt.collectEntries({[it.key,it.value]}) : null, mapLong != null ? (Map)mapLong.collectEntries({[it.key,it.value]}) : null, mapJsonObject != null ? (Map)mapJsonObject.collectEntries({[it.key,new io.vertx.core.json.JsonObject(it.value)]}) : null, mapJsonArray != null ? (Map)mapJsonArray.collectEntries({[it.key,new io.vertx.core.json.JsonArray(it.value)]}) : null);
  }
  public void stringHandler(Handler<AsyncResult<String>> resultHandler) {
    delegate.stringHandler(resultHandler);
  }
  public void stringNullHandler(Handler<AsyncResult<String>> resultHandler) {
    delegate.stringNullHandler(resultHandler);
  }
  public void byteHandler(Handler<AsyncResult<Byte>> resultHandler) {
    delegate.byteHandler(resultHandler);
  }
  public void byteNullHandler(Handler<AsyncResult<Byte>> resultHandler) {
    delegate.byteNullHandler(resultHandler);
  }
  public void shortHandler(Handler<AsyncResult<Short>> resultHandler) {
    delegate.shortHandler(resultHandler);
  }
  public void shortNullHandler(Handler<AsyncResult<Short>> resultHandler) {
    delegate.shortNullHandler(resultHandler);
  }
  public void intHandler(Handler<AsyncResult<Integer>> resultHandler) {
    delegate.intHandler(resultHandler);
  }
  public void intNullHandler(Handler<AsyncResult<Integer>> resultHandler) {
    delegate.intNullHandler(resultHandler);
  }
  public void longHandler(Handler<AsyncResult<Long>> resultHandler) {
    delegate.longHandler(resultHandler);
  }
  public void longNullHandler(Handler<AsyncResult<Long>> resultHandler) {
    delegate.longNullHandler(resultHandler);
  }
  public void floatHandler(Handler<AsyncResult<Float>> resultHandler) {
    delegate.floatHandler(resultHandler);
  }
  public void floatNullHandler(Handler<AsyncResult<Float>> resultHandler) {
    delegate.floatNullHandler(resultHandler);
  }
  public void doubleHandler(Handler<AsyncResult<Double>> resultHandler) {
    delegate.doubleHandler(resultHandler);
  }
  public void doubleNullHandler(Handler<AsyncResult<Double>> resultHandler) {
    delegate.doubleNullHandler(resultHandler);
  }
  public void charHandler(Handler<AsyncResult<Character>> resultHandler) {
    delegate.charHandler(resultHandler);
  }
  public void charNullHandler(Handler<AsyncResult<Character>> resultHandler) {
    delegate.charNullHandler(resultHandler);
  }
  public void booleanHandler(Handler<AsyncResult<Boolean>> resultHandler) {
    delegate.booleanHandler(resultHandler);
  }
  public void booleanNullHandler(Handler<AsyncResult<Boolean>> resultHandler) {
    delegate.booleanNullHandler(resultHandler);
  }
  public void jsonObjectHandler(Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    delegate.jsonObjectHandler(resultHandler != null ? new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture((Map<String, Object>)InternalHelper.wrapObject(ar.result())));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void jsonObjectNullHandler(Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    delegate.jsonObjectNullHandler(resultHandler != null ? new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture((Map<String, Object>)InternalHelper.wrapObject(ar.result())));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void jsonArrayHandler(Handler<AsyncResult<List<Object>>> resultHandler) {
    delegate.jsonArrayHandler(resultHandler != null ? new Handler<AsyncResult<io.vertx.core.json.JsonArray>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonArray> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture((List<Object>)InternalHelper.wrapObject(ar.result())));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void jsonArrayNullHandler(Handler<AsyncResult<List<Object>>> resultHandler) {
    delegate.jsonArrayNullHandler(resultHandler != null ? new Handler<AsyncResult<io.vertx.core.json.JsonArray>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonArray> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture((List<Object>)InternalHelper.wrapObject(ar.result())));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void dataObjectHandler(Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    delegate.dataObjectHandler(resultHandler != null ? new Handler<AsyncResult<io.vertx.serviceproxy.testmodel.TestDataObject>>() {
      public void handle(AsyncResult<io.vertx.serviceproxy.testmodel.TestDataObject> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture((Map<String, Object>)InternalHelper.wrapObject(ar.result()?.toJson())));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void dataObjectNullHandler(Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    delegate.dataObjectNullHandler(resultHandler != null ? new Handler<AsyncResult<io.vertx.serviceproxy.testmodel.TestDataObject>>() {
      public void handle(AsyncResult<io.vertx.serviceproxy.testmodel.TestDataObject> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture((Map<String, Object>)InternalHelper.wrapObject(ar.result()?.toJson())));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void voidHandler(Handler<AsyncResult<Void>> resultHandler) {
    delegate.voidHandler(resultHandler);
  }
  public TestService fluentMethod(String str, Handler<AsyncResult<String>> resultHandler) {
    delegate.fluentMethod(str, resultHandler);
    return this;
  }
  public TestService fluentNoParams() {
    delegate.fluentNoParams();
    return this;
  }
  public void failingMethod(Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    delegate.failingMethod(resultHandler != null ? new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture((Map<String, Object>)InternalHelper.wrapObject(ar.result())));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void invokeWithMessage(Map<String, Object> object, String str, int i, char chr, SomeEnum senum, Handler<AsyncResult<String>> resultHandler) {
    delegate.invokeWithMessage(object != null ? new io.vertx.core.json.JsonObject(object) : null, str, i, chr, senum, resultHandler);
  }
  public void listStringHandler(Handler<AsyncResult<List<String>>> resultHandler) {
    delegate.listStringHandler(resultHandler != null ? new Handler<AsyncResult<java.util.List<java.lang.String>>>() {
      public void handle(AsyncResult<java.util.List<java.lang.String>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void listByteHandler(Handler<AsyncResult<List<Byte>>> resultHandler) {
    delegate.listByteHandler(resultHandler != null ? new Handler<AsyncResult<java.util.List<java.lang.Byte>>>() {
      public void handle(AsyncResult<java.util.List<java.lang.Byte>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void listShortHandler(Handler<AsyncResult<List<Short>>> resultHandler) {
    delegate.listShortHandler(resultHandler != null ? new Handler<AsyncResult<java.util.List<java.lang.Short>>>() {
      public void handle(AsyncResult<java.util.List<java.lang.Short>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void listIntHandler(Handler<AsyncResult<List<Integer>>> resultHandler) {
    delegate.listIntHandler(resultHandler != null ? new Handler<AsyncResult<java.util.List<java.lang.Integer>>>() {
      public void handle(AsyncResult<java.util.List<java.lang.Integer>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void listLongHandler(Handler<AsyncResult<List<Long>>> resultHandler) {
    delegate.listLongHandler(resultHandler != null ? new Handler<AsyncResult<java.util.List<java.lang.Long>>>() {
      public void handle(AsyncResult<java.util.List<java.lang.Long>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void listFloatHandler(Handler<AsyncResult<List<Float>>> resultHandler) {
    delegate.listFloatHandler(resultHandler != null ? new Handler<AsyncResult<java.util.List<java.lang.Float>>>() {
      public void handle(AsyncResult<java.util.List<java.lang.Float>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void listDoubleHandler(Handler<AsyncResult<List<Double>>> resultHandler) {
    delegate.listDoubleHandler(resultHandler != null ? new Handler<AsyncResult<java.util.List<java.lang.Double>>>() {
      public void handle(AsyncResult<java.util.List<java.lang.Double>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void listCharHandler(Handler<AsyncResult<List<Character>>> resultHandler) {
    delegate.listCharHandler(resultHandler != null ? new Handler<AsyncResult<java.util.List<java.lang.Character>>>() {
      public void handle(AsyncResult<java.util.List<java.lang.Character>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void listBoolHandler(Handler<AsyncResult<List<Boolean>>> resultHandler) {
    delegate.listBoolHandler(resultHandler != null ? new Handler<AsyncResult<java.util.List<java.lang.Boolean>>>() {
      public void handle(AsyncResult<java.util.List<java.lang.Boolean>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void listJsonObjectHandler(Handler<AsyncResult<List<Map<String, Object>>>> resultHandler) {
    delegate.listJsonObjectHandler(resultHandler != null ? new Handler<AsyncResult<java.util.List<io.vertx.core.json.JsonObject>>>() {
      public void handle(AsyncResult<java.util.List<io.vertx.core.json.JsonObject>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture((List)ar.result()?.collect({(Map<String, Object>)InternalHelper.wrapObject(it)})));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void listJsonArrayHandler(Handler<AsyncResult<List<List<Object>>>> resultHandler) {
    delegate.listJsonArrayHandler(resultHandler != null ? new Handler<AsyncResult<java.util.List<io.vertx.core.json.JsonArray>>>() {
      public void handle(AsyncResult<java.util.List<io.vertx.core.json.JsonArray>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture((List)ar.result()?.collect({(List<Object>)InternalHelper.wrapObject(it)})));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void listDataObjectHandler(Handler<AsyncResult<List<Map<String, Object>>>> resultHandler) {
    delegate.listDataObjectHandler(resultHandler != null ? new Handler<AsyncResult<java.util.List<io.vertx.serviceproxy.testmodel.TestDataObject>>>() {
      public void handle(AsyncResult<java.util.List<io.vertx.serviceproxy.testmodel.TestDataObject>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture((List)ar.result()?.collect({(Map<String, Object>)InternalHelper.wrapObject(it?.toJson())})));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void setStringHandler(Handler<AsyncResult<Set<String>>> resultHandler) {
    delegate.setStringHandler(resultHandler != null ? new Handler<AsyncResult<java.util.Set<java.lang.String>>>() {
      public void handle(AsyncResult<java.util.Set<java.lang.String>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void setByteHandler(Handler<AsyncResult<Set<Byte>>> resultHandler) {
    delegate.setByteHandler(resultHandler != null ? new Handler<AsyncResult<java.util.Set<java.lang.Byte>>>() {
      public void handle(AsyncResult<java.util.Set<java.lang.Byte>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void setShortHandler(Handler<AsyncResult<Set<Short>>> resultHandler) {
    delegate.setShortHandler(resultHandler != null ? new Handler<AsyncResult<java.util.Set<java.lang.Short>>>() {
      public void handle(AsyncResult<java.util.Set<java.lang.Short>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void setIntHandler(Handler<AsyncResult<Set<Integer>>> resultHandler) {
    delegate.setIntHandler(resultHandler != null ? new Handler<AsyncResult<java.util.Set<java.lang.Integer>>>() {
      public void handle(AsyncResult<java.util.Set<java.lang.Integer>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void setLongHandler(Handler<AsyncResult<Set<Long>>> resultHandler) {
    delegate.setLongHandler(resultHandler != null ? new Handler<AsyncResult<java.util.Set<java.lang.Long>>>() {
      public void handle(AsyncResult<java.util.Set<java.lang.Long>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void setFloatHandler(Handler<AsyncResult<Set<Float>>> resultHandler) {
    delegate.setFloatHandler(resultHandler != null ? new Handler<AsyncResult<java.util.Set<java.lang.Float>>>() {
      public void handle(AsyncResult<java.util.Set<java.lang.Float>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void setDoubleHandler(Handler<AsyncResult<Set<Double>>> resultHandler) {
    delegate.setDoubleHandler(resultHandler != null ? new Handler<AsyncResult<java.util.Set<java.lang.Double>>>() {
      public void handle(AsyncResult<java.util.Set<java.lang.Double>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void setCharHandler(Handler<AsyncResult<Set<Character>>> resultHandler) {
    delegate.setCharHandler(resultHandler != null ? new Handler<AsyncResult<java.util.Set<java.lang.Character>>>() {
      public void handle(AsyncResult<java.util.Set<java.lang.Character>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void setBoolHandler(Handler<AsyncResult<Set<Boolean>>> resultHandler) {
    delegate.setBoolHandler(resultHandler != null ? new Handler<AsyncResult<java.util.Set<java.lang.Boolean>>>() {
      public void handle(AsyncResult<java.util.Set<java.lang.Boolean>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void setJsonObjectHandler(Handler<AsyncResult<Set<Map<String, Object>>>> resultHandler) {
    delegate.setJsonObjectHandler(resultHandler != null ? new Handler<AsyncResult<java.util.Set<io.vertx.core.json.JsonObject>>>() {
      public void handle(AsyncResult<java.util.Set<io.vertx.core.json.JsonObject>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture((Set)ar.result()?.collect({(Map<String, Object>)InternalHelper.wrapObject(it)}) as Set));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void setJsonArrayHandler(Handler<AsyncResult<Set<List<Object>>>> resultHandler) {
    delegate.setJsonArrayHandler(resultHandler != null ? new Handler<AsyncResult<java.util.Set<io.vertx.core.json.JsonArray>>>() {
      public void handle(AsyncResult<java.util.Set<io.vertx.core.json.JsonArray>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture((Set)ar.result()?.collect({(List<Object>)InternalHelper.wrapObject(it)}) as Set));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void setDataObjectHandler(Handler<AsyncResult<Set<Map<String, Object>>>> resultHandler) {
    delegate.setDataObjectHandler(resultHandler != null ? new Handler<AsyncResult<java.util.Set<io.vertx.serviceproxy.testmodel.TestDataObject>>>() {
      public void handle(AsyncResult<java.util.Set<io.vertx.serviceproxy.testmodel.TestDataObject>> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture((Set)ar.result()?.collect({(Map<String, Object>)InternalHelper.wrapObject(it?.toJson())}) as Set));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  public void ignoredMethod() {
    delegate.ignoredMethod();
  }
}
