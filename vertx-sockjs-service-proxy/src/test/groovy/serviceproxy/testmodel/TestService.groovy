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

package serviceproxy.testmodel;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import serviceproxy.testmodel.SomeEnum
import io.vertx.groovy.core.Vertx
import java.util.Set
import serviceproxy.testmodel.TestDataObject
import io.vertx.core.json.JsonArray
import java.util.List
import java.util.Map
import io.vertx.core.json.JsonObject
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
*/
@CompileStatic
public class TestService {
  private final def serviceproxy.testmodel.TestService delegate;
  public TestService(Object delegate) {
    this.delegate = (serviceproxy.testmodel.TestService) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static TestService create(Vertx vertx) {
    def ret= InternalHelper.safeCreate(serviceproxy.testmodel.TestService.create((io.vertx.core.Vertx)vertx.getDelegate()), serviceproxy.testmodel.TestService.class);
    return ret;
  }
  public static TestService createProxy(Vertx vertx, String address) {
    def ret= InternalHelper.safeCreate(serviceproxy.testmodel.TestService.createProxy((io.vertx.core.Vertx)vertx.getDelegate(), address), serviceproxy.testmodel.TestService.class);
    return ret;
  }
  public static TestService createProxyLongDelivery(Vertx vertx, String address) {
    def ret= InternalHelper.safeCreate(serviceproxy.testmodel.TestService.createProxyLongDelivery((io.vertx.core.Vertx)vertx.getDelegate(), address), serviceproxy.testmodel.TestService.class);
    return ret;
  }
  public void longDeliverySuccess(Handler<AsyncResult<String>> resultHandler) {
    this.delegate.longDeliverySuccess(resultHandler);
  }
  public void longDeliveryFailed(Handler<AsyncResult<String>> resultHandler) {
    this.delegate.longDeliveryFailed(resultHandler);
  }
  public void createConnection(String str, Handler<AsyncResult<TestConnection>> resultHandler) {
    this.delegate.createConnection(str, new Handler<AsyncResult<serviceproxy.testmodel.TestConnection>>() {
      public void handle(AsyncResult<serviceproxy.testmodel.TestConnection> event) {
        AsyncResult<TestConnection> f
        if (event.succeeded()) {
          f = InternalHelper.<TestConnection>result(new TestConnection(event.result()))
        } else {
          f = InternalHelper.<TestConnection>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void createConnectionWithCloseFuture(Handler<AsyncResult<TestConnectionWithCloseFuture>> resultHandler) {
    this.delegate.createConnectionWithCloseFuture(new Handler<AsyncResult<serviceproxy.testmodel.TestConnectionWithCloseFuture>>() {
      public void handle(AsyncResult<serviceproxy.testmodel.TestConnectionWithCloseFuture> event) {
        AsyncResult<TestConnectionWithCloseFuture> f
        if (event.succeeded()) {
          f = InternalHelper.<TestConnectionWithCloseFuture>result(new TestConnectionWithCloseFuture(event.result()))
        } else {
          f = InternalHelper.<TestConnectionWithCloseFuture>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void noParams() {
    this.delegate.noParams();
  }
  public void basicTypes(String str, byte b, short s, int i, long l, float f, double d, char c, boolean bool) {
    this.delegate.basicTypes(str, b, s, i, l, f, d, c, bool);
  }
  public void basicBoxedTypes(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c, Boolean bool) {
    this.delegate.basicBoxedTypes(str, b, s, i, l, f, d, c, bool);
  }
  public void basicBoxedTypesNull(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c, Boolean bool) {
    this.delegate.basicBoxedTypesNull(str, b, s, i, l, f, d, c, bool);
  }
  public void jsonTypes(Map<String, Object> jsonObject, List<Object> jsonArray) {
    this.delegate.jsonTypes(jsonObject != null ? new io.vertx.core.json.JsonObject(jsonObject) : null, jsonArray != null ? new io.vertx.core.json.JsonArray(jsonArray) : null);
  }
  public void jsonTypesNull(Map<String, Object> jsonObject, List<Object> jsonArray) {
    this.delegate.jsonTypesNull(jsonObject != null ? new io.vertx.core.json.JsonObject(jsonObject) : null, jsonArray != null ? new io.vertx.core.json.JsonArray(jsonArray) : null);
  }
  public void enumType(SomeEnum someEnum) {
    this.delegate.enumType(someEnum);
  }
  public void enumTypeNull(SomeEnum someEnum) {
    this.delegate.enumTypeNull(someEnum);
  }
  public void enumTypeAsResult(Handler<AsyncResult<SomeEnum>> someEnum) {
    this.delegate.enumTypeAsResult(null /* Handler<AsyncResult<serviceproxy.testmodel.SomeEnum>> with kind ENUM not yet implemented */);
  }
  public void enumTypeAsResultNull(Handler<AsyncResult<SomeEnum>> someEnum) {
    this.delegate.enumTypeAsResultNull(null /* Handler<AsyncResult<serviceproxy.testmodel.SomeEnum>> with kind ENUM not yet implemented */);
  }
  public void dataObjectType(Map<String, Object> options = [:]) {
    this.delegate.dataObjectType(options != null ? new serviceproxy.testmodel.TestDataObject(new io.vertx.core.json.JsonObject(options)) : null);
  }
  public void dataObjectTypeNull(Map<String, Object> options = [:]) {
    this.delegate.dataObjectTypeNull(options != null ? new serviceproxy.testmodel.TestDataObject(new io.vertx.core.json.JsonObject(options)) : null);
  }
  public void listParams(List<String> listString, List<Byte> listByte, List<Short> listShort, List<Integer> listInt, List<Long> listLong, List<Map<String, Object>> listJsonObject, List<List<Object>> listJsonArray, List<Map<String, Object>> listDataObject) {
    this.delegate.listParams(listString, listByte, listShort, listInt, listLong, listJsonObject.collect({underpants -> new JsonObject(underpants)}), listJsonArray.collect({underpants -> new JsonArray(underpants)}), listDataObject.collect({underpants -> new TestDataObject(new JsonObject(underpants))}));
  }
  public void setParams(Set<String> setString, Set<Byte> setByte, Set<Short> setShort, Set<Integer> setInt, Set<Long> setLong, Set<Map<String, Object>> setJsonObject, Set<List<Object>> setJsonArray, Set<Map<String, Object>> setDataObject) {
    this.delegate.setParams(setString, setByte, setShort, setInt, setLong, setJsonObject.collect({underpants -> new JsonObject(underpants)}) as Set, setJsonArray.collect({underpants -> new JsonArray(underpants)}) as Set, setDataObject.collect({underpants -> new TestDataObject(new JsonObject(underpants))}) as Set);
  }
  public void mapParams(Map<String,String> mapString, Map<String,Byte> mapByte, Map<String,Short> mapShort, Map<String,Integer> mapInt, Map<String,Long> mapLong, Map<String, Map<String, Object>> mapJsonObject, Map<String, List<Object>> mapJsonArray) {
    this.delegate.mapParams(mapString, mapByte, mapShort, mapInt, mapLong, (Map<String, io.vertx.core.json.JsonObject>)(mapJsonObject.collectEntries({k, v -> [k, new JsonObject(v)]})), (Map<String, io.vertx.core.json.JsonArray>)(mapJsonArray.collectEntries({k, v -> [k, new JsonArray(v)]})));
  }
  public void stringHandler(Handler<AsyncResult<String>> resultHandler) {
    this.delegate.stringHandler(resultHandler);
  }
  public void stringNullHandler(Handler<AsyncResult<String>> resultHandler) {
    this.delegate.stringNullHandler(resultHandler);
  }
  public void byteHandler(Handler<AsyncResult<Byte>> resultHandler) {
    this.delegate.byteHandler(resultHandler);
  }
  public void byteNullHandler(Handler<AsyncResult<Byte>> resultHandler) {
    this.delegate.byteNullHandler(resultHandler);
  }
  public void shortHandler(Handler<AsyncResult<Short>> resultHandler) {
    this.delegate.shortHandler(resultHandler);
  }
  public void shortNullHandler(Handler<AsyncResult<Short>> resultHandler) {
    this.delegate.shortNullHandler(resultHandler);
  }
  public void intHandler(Handler<AsyncResult<Integer>> resultHandler) {
    this.delegate.intHandler(resultHandler);
  }
  public void intNullHandler(Handler<AsyncResult<Integer>> resultHandler) {
    this.delegate.intNullHandler(resultHandler);
  }
  public void longHandler(Handler<AsyncResult<Long>> resultHandler) {
    this.delegate.longHandler(resultHandler);
  }
  public void longNullHandler(Handler<AsyncResult<Long>> resultHandler) {
    this.delegate.longNullHandler(resultHandler);
  }
  public void floatHandler(Handler<AsyncResult<Float>> resultHandler) {
    this.delegate.floatHandler(resultHandler);
  }
  public void floatNullHandler(Handler<AsyncResult<Float>> resultHandler) {
    this.delegate.floatNullHandler(resultHandler);
  }
  public void doubleHandler(Handler<AsyncResult<Double>> resultHandler) {
    this.delegate.doubleHandler(resultHandler);
  }
  public void doubleNullHandler(Handler<AsyncResult<Double>> resultHandler) {
    this.delegate.doubleNullHandler(resultHandler);
  }
  public void charHandler(Handler<AsyncResult<Character>> resultHandler) {
    this.delegate.charHandler(resultHandler);
  }
  public void charNullHandler(Handler<AsyncResult<Character>> resultHandler) {
    this.delegate.charNullHandler(resultHandler);
  }
  public void booleanHandler(Handler<AsyncResult<Boolean>> resultHandler) {
    this.delegate.booleanHandler(resultHandler);
  }
  public void booleanNullHandler(Handler<AsyncResult<Boolean>> resultHandler) {
    this.delegate.booleanNullHandler(resultHandler);
  }
  public void jsonObjectHandler(Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.jsonObjectHandler(new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void jsonObjectNullHandler(Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.jsonObjectNullHandler(new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void jsonArrayHandler(Handler<AsyncResult<List<Object>>> resultHandler) {
    this.delegate.jsonArrayHandler(new Handler<AsyncResult<io.vertx.core.json.JsonArray>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonArray> event) {
        AsyncResult<List<Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<List<Object>>result((List<Object>)InternalHelper.wrapObject(event.result()))
        } else {
          f = InternalHelper.<List<Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void jsonArrayNullHandler(Handler<AsyncResult<List<Object>>> resultHandler) {
    this.delegate.jsonArrayNullHandler(new Handler<AsyncResult<io.vertx.core.json.JsonArray>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonArray> event) {
        AsyncResult<List<Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<List<Object>>result((List<Object>)InternalHelper.wrapObject(event.result()))
        } else {
          f = InternalHelper.<List<Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void dataObjectHandler(Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.dataObjectHandler(new Handler<AsyncResult<serviceproxy.testmodel.TestDataObject>>() {
      public void handle(AsyncResult<serviceproxy.testmodel.TestDataObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()?.toJson()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void dataObjectNullHandler(Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.dataObjectNullHandler(new Handler<AsyncResult<serviceproxy.testmodel.TestDataObject>>() {
      public void handle(AsyncResult<serviceproxy.testmodel.TestDataObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()?.toJson()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void voidHandler(Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.voidHandler(resultHandler);
  }
  public TestService fluentMethod(String str, Handler<AsyncResult<String>> resultHandler) {
    this.delegate.fluentMethod(str, resultHandler);
    return this;
  }
  public TestService fluentNoParams() {
    this.delegate.fluentNoParams();
    return this;
  }
  public void failingMethod(Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.failingMethod(new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void invokeWithMessage(Map<String, Object> object, String str, int i, char chr, SomeEnum senum, Handler<AsyncResult<String>> resultHandler) {
    this.delegate.invokeWithMessage(object != null ? new io.vertx.core.json.JsonObject(object) : null, str, i, chr, senum, resultHandler);
  }
  public void listStringHandler(Handler<AsyncResult<List<String>>> resultHandler) {
    this.delegate.listStringHandler(resultHandler);
  }
  public void listByteHandler(Handler<AsyncResult<List<Byte>>> resultHandler) {
    this.delegate.listByteHandler(resultHandler);
  }
  public void listShortHandler(Handler<AsyncResult<List<Short>>> resultHandler) {
    this.delegate.listShortHandler(resultHandler);
  }
  public void listIntHandler(Handler<AsyncResult<List<Integer>>> resultHandler) {
    this.delegate.listIntHandler(resultHandler);
  }
  public void listLongHandler(Handler<AsyncResult<List<Long>>> resultHandler) {
    this.delegate.listLongHandler(resultHandler);
  }
  public void listFloatHandler(Handler<AsyncResult<List<Float>>> resultHandler) {
    this.delegate.listFloatHandler(resultHandler);
  }
  public void listDoubleHandler(Handler<AsyncResult<List<Double>>> resultHandler) {
    this.delegate.listDoubleHandler(resultHandler);
  }
  public void listCharHandler(Handler<AsyncResult<List<Character>>> resultHandler) {
    this.delegate.listCharHandler(resultHandler);
  }
  public void listBoolHandler(Handler<AsyncResult<List<Boolean>>> resultHandler) {
    this.delegate.listBoolHandler(resultHandler);
  }
  public void listJsonObjectHandler(Handler<AsyncResult<List<Map<String, Object>>>> resultHandler) {
    this.delegate.listJsonObjectHandler(new Handler<AsyncResult<List<JsonObject>>>() {
      public void handle(AsyncResult<List<JsonObject>> event) {
        AsyncResult<List<Map<String, Object>>> f
        if (event.succeeded()) {
          f = InternalHelper.<List<Map<String, Object>>>result(event.result().collect({
            io.vertx.core.json.JsonObject element ->
            InternalHelper.wrapObject(element)
          }) as List)
        } else {
          f = InternalHelper.<List<Map<String, Object>>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void listJsonArrayHandler(Handler<AsyncResult<List<List<Object>>>> resultHandler) {
    this.delegate.listJsonArrayHandler(new Handler<AsyncResult<List<JsonArray>>>() {
      public void handle(AsyncResult<List<JsonArray>> event) {
        AsyncResult<List<List<Object>>> f
        if (event.succeeded()) {
          f = InternalHelper.<List<List<Object>>>result(event.result().collect({
            io.vertx.core.json.JsonArray element ->
            InternalHelper.wrapObject(element)
          }) as List)
        } else {
          f = InternalHelper.<List<List<Object>>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void listDataObjectHandler(Handler<AsyncResult<List<Map<String, Object>>>> resultHandler) {
    this.delegate.listDataObjectHandler(new Handler<AsyncResult<List<TestDataObject>>>() {
      public void handle(AsyncResult<List<TestDataObject>> event) {
        AsyncResult<List<Map<String, Object>>> f
        if (event.succeeded()) {
          f = InternalHelper.<List<Map<String, Object>>>result(event.result().collect({
            serviceproxy.testmodel.TestDataObject element ->
            (Map<String, Object>)InternalHelper.wrapObject(element?.toJson())
          }) as List)
        } else {
          f = InternalHelper.<List<Map<String, Object>>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void setStringHandler(Handler<AsyncResult<Set<String>>> resultHandler) {
    this.delegate.setStringHandler(resultHandler);
  }
  public void setByteHandler(Handler<AsyncResult<Set<Byte>>> resultHandler) {
    this.delegate.setByteHandler(resultHandler);
  }
  public void setShortHandler(Handler<AsyncResult<Set<Short>>> resultHandler) {
    this.delegate.setShortHandler(resultHandler);
  }
  public void setIntHandler(Handler<AsyncResult<Set<Integer>>> resultHandler) {
    this.delegate.setIntHandler(resultHandler);
  }
  public void setLongHandler(Handler<AsyncResult<Set<Long>>> resultHandler) {
    this.delegate.setLongHandler(resultHandler);
  }
  public void setFloatHandler(Handler<AsyncResult<Set<Float>>> resultHandler) {
    this.delegate.setFloatHandler(resultHandler);
  }
  public void setDoubleHandler(Handler<AsyncResult<Set<Double>>> resultHandler) {
    this.delegate.setDoubleHandler(resultHandler);
  }
  public void setCharHandler(Handler<AsyncResult<Set<Character>>> resultHandler) {
    this.delegate.setCharHandler(resultHandler);
  }
  public void setBoolHandler(Handler<AsyncResult<Set<Boolean>>> resultHandler) {
    this.delegate.setBoolHandler(resultHandler);
  }
  public void setJsonObjectHandler(Handler<AsyncResult<Set<Map<String, Object>>>> resultHandler) {
    this.delegate.setJsonObjectHandler(new Handler<AsyncResult<Set<JsonObject>>>() {
      public void handle(AsyncResult<Set<JsonObject>> event) {
        AsyncResult<Set<Map<String, Object>>> f
        if (event.succeeded()) {
          f = InternalHelper.<Set<Map<String, Object>>>result(event.result().collect({
            io.vertx.core.json.JsonObject element ->
            InternalHelper.wrapObject(element)
          }) as Set)
        } else {
          f = InternalHelper.<Set<Map<String, Object>>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void setJsonArrayHandler(Handler<AsyncResult<Set<List<Object>>>> resultHandler) {
    this.delegate.setJsonArrayHandler(new Handler<AsyncResult<Set<JsonArray>>>() {
      public void handle(AsyncResult<Set<JsonArray>> event) {
        AsyncResult<Set<List<Object>>> f
        if (event.succeeded()) {
          f = InternalHelper.<Set<List<Object>>>result(event.result().collect({
            io.vertx.core.json.JsonArray element ->
            InternalHelper.wrapObject(element)
          }) as Set)
        } else {
          f = InternalHelper.<Set<List<Object>>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void setDataObjectHandler(Handler<AsyncResult<Set<Map<String, Object>>>> resultHandler) {
    this.delegate.setDataObjectHandler(new Handler<AsyncResult<Set<TestDataObject>>>() {
      public void handle(AsyncResult<Set<TestDataObject>> event) {
        AsyncResult<Set<Map<String, Object>>> f
        if (event.succeeded()) {
          f = InternalHelper.<Set<Map<String, Object>>>result(event.result().collect({
            serviceproxy.testmodel.TestDataObject element ->
            (Map<String, Object>)InternalHelper.wrapObject(element?.toJson())
          }) as Set)
        } else {
          f = InternalHelper.<Set<Map<String, Object>>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void ignoredMethod() {
    this.delegate.ignoredMethod();
  }
}
