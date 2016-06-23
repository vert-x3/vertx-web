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

package io.vertx.rxjava.serviceproxy.testmodel;

import java.util.Map;
import rx.Observable;
import io.vertx.serviceproxy.testmodel.SomeEnum;
import io.vertx.rxjava.core.Vertx;
import java.util.Set;
import io.vertx.core.json.JsonArray;
import io.vertx.serviceproxy.testmodel.TestDataObject;
import java.util.List;
import java.util.Map;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.serviceproxy.testmodel.TestService original} non RX-ified interface using Vert.x codegen.
 */

public class TestService {

  final io.vertx.serviceproxy.testmodel.TestService delegate;

  public TestService(io.vertx.serviceproxy.testmodel.TestService delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public static TestService create(Vertx vertx) { 
    TestService ret = TestService.newInstance(io.vertx.serviceproxy.testmodel.TestService.create((io.vertx.core.Vertx)vertx.getDelegate()));
    return ret;
  }

  public static TestService createProxy(Vertx vertx, String address) { 
    TestService ret = TestService.newInstance(io.vertx.serviceproxy.testmodel.TestService.createProxy((io.vertx.core.Vertx)vertx.getDelegate(), address));
    return ret;
  }

  public static TestService createProxyLongDelivery(Vertx vertx, String address) { 
    TestService ret = TestService.newInstance(io.vertx.serviceproxy.testmodel.TestService.createProxyLongDelivery((io.vertx.core.Vertx)vertx.getDelegate(), address));
    return ret;
  }

  public void longDeliverySuccess(Handler<AsyncResult<String>> resultHandler) { 
    delegate.longDeliverySuccess(resultHandler);
  }

  public Observable<String> longDeliverySuccessObservable() { 
    io.vertx.rx.java.ObservableFuture<String> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    longDeliverySuccess(resultHandler.toHandler());
    return resultHandler;
  }

  public void longDeliveryFailed(Handler<AsyncResult<String>> resultHandler) { 
    delegate.longDeliveryFailed(resultHandler);
  }

  public Observable<String> longDeliveryFailedObservable() { 
    io.vertx.rx.java.ObservableFuture<String> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    longDeliveryFailed(resultHandler.toHandler());
    return resultHandler;
  }

  public void createConnection(String str, Handler<AsyncResult<TestConnection>> resultHandler) { 
    delegate.createConnection(str, new Handler<AsyncResult<io.vertx.serviceproxy.testmodel.TestConnection>>() {
      public void handle(AsyncResult<io.vertx.serviceproxy.testmodel.TestConnection> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(TestConnection.newInstance(ar.result())));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    });
  }

  public Observable<TestConnection> createConnectionObservable(String str) { 
    io.vertx.rx.java.ObservableFuture<TestConnection> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    createConnection(str, resultHandler.toHandler());
    return resultHandler;
  }

  public void createConnectionWithCloseFuture(Handler<AsyncResult<TestConnectionWithCloseFuture>> resultHandler) { 
    delegate.createConnectionWithCloseFuture(new Handler<AsyncResult<io.vertx.serviceproxy.testmodel.TestConnectionWithCloseFuture>>() {
      public void handle(AsyncResult<io.vertx.serviceproxy.testmodel.TestConnectionWithCloseFuture> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(TestConnectionWithCloseFuture.newInstance(ar.result())));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    });
  }

  public Observable<TestConnectionWithCloseFuture> createConnectionWithCloseFutureObservable() { 
    io.vertx.rx.java.ObservableFuture<TestConnectionWithCloseFuture> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    createConnectionWithCloseFuture(resultHandler.toHandler());
    return resultHandler;
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

  public void jsonTypes(JsonObject jsonObject, JsonArray jsonArray) { 
    delegate.jsonTypes(jsonObject, jsonArray);
  }

  public void jsonTypesNull(JsonObject jsonObject, JsonArray jsonArray) { 
    delegate.jsonTypesNull(jsonObject, jsonArray);
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

  public Observable<SomeEnum> enumTypeAsResultObservable() { 
    io.vertx.rx.java.ObservableFuture<SomeEnum> someEnum = io.vertx.rx.java.RxHelper.observableFuture();
    enumTypeAsResult(someEnum.toHandler());
    return someEnum;
  }

  public void enumTypeAsResultNull(Handler<AsyncResult<SomeEnum>> someEnum) { 
    delegate.enumTypeAsResultNull(someEnum);
  }

  public Observable<SomeEnum> enumTypeAsResultNullObservable() { 
    io.vertx.rx.java.ObservableFuture<SomeEnum> someEnum = io.vertx.rx.java.RxHelper.observableFuture();
    enumTypeAsResultNull(someEnum.toHandler());
    return someEnum;
  }

  public void dataObjectType(TestDataObject options) { 
    delegate.dataObjectType(options);
  }

  public void dataObjectTypeNull(TestDataObject options) { 
    delegate.dataObjectTypeNull(options);
  }

  public void listParams(List<String> listString, List<Byte> listByte, List<Short> listShort, List<Integer> listInt, List<Long> listLong, List<JsonObject> listJsonObject, List<JsonArray> listJsonArray, List<TestDataObject> listDataObject) { 
    delegate.listParams(listString, listByte, listShort, listInt, listLong, listJsonObject, listJsonArray, listDataObject);
  }

  public void setParams(Set<String> setString, Set<Byte> setByte, Set<Short> setShort, Set<Integer> setInt, Set<Long> setLong, Set<JsonObject> setJsonObject, Set<JsonArray> setJsonArray, Set<TestDataObject> setDataObject) { 
    delegate.setParams(setString, setByte, setShort, setInt, setLong, setJsonObject, setJsonArray, setDataObject);
  }

  public void mapParams(Map<String,String> mapString, Map<String,Byte> mapByte, Map<String,Short> mapShort, Map<String,Integer> mapInt, Map<String,Long> mapLong, Map<String,JsonObject> mapJsonObject, Map<String,JsonArray> mapJsonArray) { 
    delegate.mapParams(mapString, mapByte, mapShort, mapInt, mapLong, mapJsonObject, mapJsonArray);
  }

  public void stringHandler(Handler<AsyncResult<String>> resultHandler) { 
    delegate.stringHandler(resultHandler);
  }

  public Observable<String> stringHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<String> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    stringHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void stringNullHandler(Handler<AsyncResult<String>> resultHandler) { 
    delegate.stringNullHandler(resultHandler);
  }

  public Observable<String> stringNullHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<String> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    stringNullHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void byteHandler(Handler<AsyncResult<Byte>> resultHandler) { 
    delegate.byteHandler(resultHandler);
  }

  public Observable<Byte> byteHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Byte> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    byteHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void byteNullHandler(Handler<AsyncResult<Byte>> resultHandler) { 
    delegate.byteNullHandler(resultHandler);
  }

  public Observable<Byte> byteNullHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Byte> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    byteNullHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void shortHandler(Handler<AsyncResult<Short>> resultHandler) { 
    delegate.shortHandler(resultHandler);
  }

  public Observable<Short> shortHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Short> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    shortHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void shortNullHandler(Handler<AsyncResult<Short>> resultHandler) { 
    delegate.shortNullHandler(resultHandler);
  }

  public Observable<Short> shortNullHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Short> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    shortNullHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void intHandler(Handler<AsyncResult<Integer>> resultHandler) { 
    delegate.intHandler(resultHandler);
  }

  public Observable<Integer> intHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Integer> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    intHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void intNullHandler(Handler<AsyncResult<Integer>> resultHandler) { 
    delegate.intNullHandler(resultHandler);
  }

  public Observable<Integer> intNullHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Integer> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    intNullHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void longHandler(Handler<AsyncResult<Long>> resultHandler) { 
    delegate.longHandler(resultHandler);
  }

  public Observable<Long> longHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Long> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    longHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void longNullHandler(Handler<AsyncResult<Long>> resultHandler) { 
    delegate.longNullHandler(resultHandler);
  }

  public Observable<Long> longNullHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Long> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    longNullHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void floatHandler(Handler<AsyncResult<Float>> resultHandler) { 
    delegate.floatHandler(resultHandler);
  }

  public Observable<Float> floatHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Float> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    floatHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void floatNullHandler(Handler<AsyncResult<Float>> resultHandler) { 
    delegate.floatNullHandler(resultHandler);
  }

  public Observable<Float> floatNullHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Float> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    floatNullHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void doubleHandler(Handler<AsyncResult<Double>> resultHandler) { 
    delegate.doubleHandler(resultHandler);
  }

  public Observable<Double> doubleHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Double> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    doubleHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void doubleNullHandler(Handler<AsyncResult<Double>> resultHandler) { 
    delegate.doubleNullHandler(resultHandler);
  }

  public Observable<Double> doubleNullHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Double> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    doubleNullHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void charHandler(Handler<AsyncResult<Character>> resultHandler) { 
    delegate.charHandler(resultHandler);
  }

  public Observable<Character> charHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Character> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    charHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void charNullHandler(Handler<AsyncResult<Character>> resultHandler) { 
    delegate.charNullHandler(resultHandler);
  }

  public Observable<Character> charNullHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Character> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    charNullHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void booleanHandler(Handler<AsyncResult<Boolean>> resultHandler) { 
    delegate.booleanHandler(resultHandler);
  }

  public Observable<Boolean> booleanHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Boolean> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    booleanHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void booleanNullHandler(Handler<AsyncResult<Boolean>> resultHandler) { 
    delegate.booleanNullHandler(resultHandler);
  }

  public Observable<Boolean> booleanNullHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Boolean> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    booleanNullHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void jsonObjectHandler(Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.jsonObjectHandler(resultHandler);
  }

  public Observable<JsonObject> jsonObjectHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    jsonObjectHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void jsonObjectNullHandler(Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.jsonObjectNullHandler(resultHandler);
  }

  public Observable<JsonObject> jsonObjectNullHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    jsonObjectNullHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void jsonArrayHandler(Handler<AsyncResult<JsonArray>> resultHandler) { 
    delegate.jsonArrayHandler(resultHandler);
  }

  public Observable<JsonArray> jsonArrayHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<JsonArray> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    jsonArrayHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void jsonArrayNullHandler(Handler<AsyncResult<JsonArray>> resultHandler) { 
    delegate.jsonArrayNullHandler(resultHandler);
  }

  public Observable<JsonArray> jsonArrayNullHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<JsonArray> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    jsonArrayNullHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void dataObjectHandler(Handler<AsyncResult<TestDataObject>> resultHandler) { 
    delegate.dataObjectHandler(resultHandler);
  }

  public Observable<TestDataObject> dataObjectHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<TestDataObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    dataObjectHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void dataObjectNullHandler(Handler<AsyncResult<TestDataObject>> resultHandler) { 
    delegate.dataObjectNullHandler(resultHandler);
  }

  public Observable<TestDataObject> dataObjectNullHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<TestDataObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    dataObjectNullHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void voidHandler(Handler<AsyncResult<Void>> resultHandler) { 
    delegate.voidHandler(resultHandler);
  }

  public Observable<Void> voidHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Void> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    voidHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public TestService fluentMethod(String str, Handler<AsyncResult<String>> resultHandler) { 
    delegate.fluentMethod(str, resultHandler);
    return this;
  }

  public Observable<String> fluentMethodObservable(String str) { 
    io.vertx.rx.java.ObservableFuture<String> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    fluentMethod(str, resultHandler.toHandler());
    return resultHandler;
  }

  public TestService fluentNoParams() { 
    delegate.fluentNoParams();
    return this;
  }

  public void failingMethod(Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.failingMethod(resultHandler);
  }

  public Observable<JsonObject> failingMethodObservable() { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    failingMethod(resultHandler.toHandler());
    return resultHandler;
  }

  public void invokeWithMessage(JsonObject object, String str, int i, char chr, SomeEnum senum, Handler<AsyncResult<String>> resultHandler) { 
    delegate.invokeWithMessage(object, str, i, chr, senum, resultHandler);
  }

  public Observable<String> invokeWithMessageObservable(JsonObject object, String str, int i, char chr, SomeEnum senum) { 
    io.vertx.rx.java.ObservableFuture<String> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    invokeWithMessage(object, str, i, chr, senum, resultHandler.toHandler());
    return resultHandler;
  }

  public void listStringHandler(Handler<AsyncResult<List<String>>> resultHandler) { 
    delegate.listStringHandler(resultHandler);
  }

  public Observable<List<String>> listStringHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<List<String>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    listStringHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void listByteHandler(Handler<AsyncResult<List<Byte>>> resultHandler) { 
    delegate.listByteHandler(resultHandler);
  }

  public Observable<List<Byte>> listByteHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<List<Byte>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    listByteHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void listShortHandler(Handler<AsyncResult<List<Short>>> resultHandler) { 
    delegate.listShortHandler(resultHandler);
  }

  public Observable<List<Short>> listShortHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<List<Short>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    listShortHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void listIntHandler(Handler<AsyncResult<List<Integer>>> resultHandler) { 
    delegate.listIntHandler(resultHandler);
  }

  public Observable<List<Integer>> listIntHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<List<Integer>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    listIntHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void listLongHandler(Handler<AsyncResult<List<Long>>> resultHandler) { 
    delegate.listLongHandler(resultHandler);
  }

  public Observable<List<Long>> listLongHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<List<Long>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    listLongHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void listFloatHandler(Handler<AsyncResult<List<Float>>> resultHandler) { 
    delegate.listFloatHandler(resultHandler);
  }

  public Observable<List<Float>> listFloatHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<List<Float>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    listFloatHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void listDoubleHandler(Handler<AsyncResult<List<Double>>> resultHandler) { 
    delegate.listDoubleHandler(resultHandler);
  }

  public Observable<List<Double>> listDoubleHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<List<Double>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    listDoubleHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void listCharHandler(Handler<AsyncResult<List<Character>>> resultHandler) { 
    delegate.listCharHandler(resultHandler);
  }

  public Observable<List<Character>> listCharHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<List<Character>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    listCharHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void listBoolHandler(Handler<AsyncResult<List<Boolean>>> resultHandler) { 
    delegate.listBoolHandler(resultHandler);
  }

  public Observable<List<Boolean>> listBoolHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<List<Boolean>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    listBoolHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void listJsonObjectHandler(Handler<AsyncResult<List<JsonObject>>> resultHandler) { 
    delegate.listJsonObjectHandler(resultHandler);
  }

  public Observable<List<JsonObject>> listJsonObjectHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<List<JsonObject>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    listJsonObjectHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void listJsonArrayHandler(Handler<AsyncResult<List<JsonArray>>> resultHandler) { 
    delegate.listJsonArrayHandler(resultHandler);
  }

  public Observable<List<JsonArray>> listJsonArrayHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<List<JsonArray>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    listJsonArrayHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void listDataObjectHandler(Handler<AsyncResult<List<TestDataObject>>> resultHandler) { 
    delegate.listDataObjectHandler(resultHandler);
  }

  public Observable<List<TestDataObject>> listDataObjectHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<List<TestDataObject>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    listDataObjectHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void setStringHandler(Handler<AsyncResult<Set<String>>> resultHandler) { 
    delegate.setStringHandler(resultHandler);
  }

  public Observable<Set<String>> setStringHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Set<String>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    setStringHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void setByteHandler(Handler<AsyncResult<Set<Byte>>> resultHandler) { 
    delegate.setByteHandler(resultHandler);
  }

  public Observable<Set<Byte>> setByteHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Set<Byte>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    setByteHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void setShortHandler(Handler<AsyncResult<Set<Short>>> resultHandler) { 
    delegate.setShortHandler(resultHandler);
  }

  public Observable<Set<Short>> setShortHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Set<Short>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    setShortHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void setIntHandler(Handler<AsyncResult<Set<Integer>>> resultHandler) { 
    delegate.setIntHandler(resultHandler);
  }

  public Observable<Set<Integer>> setIntHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Set<Integer>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    setIntHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void setLongHandler(Handler<AsyncResult<Set<Long>>> resultHandler) { 
    delegate.setLongHandler(resultHandler);
  }

  public Observable<Set<Long>> setLongHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Set<Long>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    setLongHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void setFloatHandler(Handler<AsyncResult<Set<Float>>> resultHandler) { 
    delegate.setFloatHandler(resultHandler);
  }

  public Observable<Set<Float>> setFloatHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Set<Float>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    setFloatHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void setDoubleHandler(Handler<AsyncResult<Set<Double>>> resultHandler) { 
    delegate.setDoubleHandler(resultHandler);
  }

  public Observable<Set<Double>> setDoubleHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Set<Double>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    setDoubleHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void setCharHandler(Handler<AsyncResult<Set<Character>>> resultHandler) { 
    delegate.setCharHandler(resultHandler);
  }

  public Observable<Set<Character>> setCharHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Set<Character>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    setCharHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void setBoolHandler(Handler<AsyncResult<Set<Boolean>>> resultHandler) { 
    delegate.setBoolHandler(resultHandler);
  }

  public Observable<Set<Boolean>> setBoolHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Set<Boolean>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    setBoolHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void setJsonObjectHandler(Handler<AsyncResult<Set<JsonObject>>> resultHandler) { 
    delegate.setJsonObjectHandler(resultHandler);
  }

  public Observable<Set<JsonObject>> setJsonObjectHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Set<JsonObject>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    setJsonObjectHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void setJsonArrayHandler(Handler<AsyncResult<Set<JsonArray>>> resultHandler) { 
    delegate.setJsonArrayHandler(resultHandler);
  }

  public Observable<Set<JsonArray>> setJsonArrayHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Set<JsonArray>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    setJsonArrayHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void setDataObjectHandler(Handler<AsyncResult<Set<TestDataObject>>> resultHandler) { 
    delegate.setDataObjectHandler(resultHandler);
  }

  public Observable<Set<TestDataObject>> setDataObjectHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Set<TestDataObject>> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    setDataObjectHandler(resultHandler.toHandler());
    return resultHandler;
  }

  public void ignoredMethod() { 
    delegate.ignoredMethod();
  }


  public static TestService newInstance(io.vertx.serviceproxy.testmodel.TestService arg) {
    return arg != null ? new TestService(arg) : null;
  }
}
