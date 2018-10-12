package io.vertx.ext.web.api.param_extraction;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.router_factory_integration.FilterData;
import io.vertx.ext.web.api.router_factory_integration.SomeEnum;

import java.util.*;
import java.util.stream.Collectors;

public class ParamsTestServiceImpl implements ParamsTestService {

  @Override
  public void basicTypes(String str, byte b, short s, int i, long l, float f, double d, char c, boolean bool, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithPlainText(Buffer.buffer(
      "" + str + b + s + i + l + f + d + c + bool
    ))));
  }

  @Override
  public void basicBoxedTypes(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c, Boolean bool, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithPlainText(Buffer.buffer(
      "" + str + b + s + i + l + f + d + c + bool
    ))));
  }

  @Override
  public void basicBoxedTypesNull(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c, Boolean bool, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithPlainText(Buffer.buffer(
      "" + str + b + s + i + l + f + d + c + bool
    ))));
  }

  @Override
  public void jsonTypes(JsonObject jsonObject, JsonArray jsonArray, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithPlainText(Buffer.buffer(
      "" + jsonObject + jsonArray
    ))));
  }

  @Override
  public void jsonTypesNull(JsonObject jsonObject, JsonArray jsonArray, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithPlainText(Buffer.buffer(
      "" + jsonObject + jsonArray
    ))));
  }

  @Override
  public void enumType(SomeEnum someEnum, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithPlainText(Buffer.buffer(
      "" + someEnum
    ))));
  }

  @Override
  public void enumTypeNull(SomeEnum someEnum, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithPlainText(Buffer.buffer(
      "" + someEnum
    ))));
  }

  @Override
  public void dataObjectType(FilterData options, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithPlainText(Buffer.buffer(
      "" + options
    ))));
  }

  @Override
  public void dataObjectTypeNull(FilterData options, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithPlainText(Buffer.buffer(
      "" + options
    ))));
  }

  @Override
  public void listParams(List<String> listString, List<Byte> listByte, List<Short> listShort, List<Integer> listInt, List<Long> listLong, List<JsonObject> listJsonObject, List<JsonArray> listJsonArray, List<FilterData> listDataObject, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithPlainText(Buffer.buffer(
      "" + new JsonArray(listString) + new JsonArray(listByte) + new JsonArray(listShort) + new JsonArray(listInt) + new JsonArray(listLong) + new JsonArray(listJsonObject) + new JsonArray(listJsonArray) + new JsonArray(listDataObject.stream().map(FilterData::toJson).collect(Collectors.toList()))
    ))));
  }

  @Override
  public void setParams(Set<String> setString, Set<Byte> setByte, Set<Short> setShort, Set<Integer> setInt, Set<Long> setLong, Set<JsonObject> setJsonObject, Set<JsonArray> setJsonArray, Set<FilterData> setDataObject, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithPlainText(Buffer.buffer(
      "" + new JsonArray(new ArrayList<>(setString)) + new JsonArray(new ArrayList<>(setByte)) + new JsonArray(new ArrayList<>(setShort)) + new JsonArray(new ArrayList<>(setInt)) + new JsonArray(new ArrayList<>(setLong)) + new JsonArray(new ArrayList<>(setJsonObject)) + new JsonArray(new ArrayList<>(setJsonArray)) + new JsonArray(setDataObject.stream().map(FilterData::toJson).collect(Collectors.toList()))
    ))));
  }

  @Override
  public void mapParams(Map<String, String> mapString, Map<String, Byte> mapByte, Map<String, Short> mapShort, Map<String, Integer> mapInt, Map<String, Long> mapLong, Map<String, JsonObject> mapJsonObject, Map<String, JsonArray> mapJsonArray, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithPlainText(Buffer.buffer(
      "" +
        new JsonObject().put(mapString.keySet().iterator().next(), mapString.values().iterator().next()) +
        new JsonObject().put(mapByte.keySet().iterator().next(), mapByte.values().iterator().next()) +
        new JsonObject().put(mapShort.keySet().iterator().next(), mapShort.values().iterator().next()) +
        new JsonObject().put(mapInt.keySet().iterator().next(), mapInt.values().iterator().next()) +
        new JsonObject().put(mapLong.keySet().iterator().next(), mapLong.values().iterator().next()) +
        new JsonObject().put(mapJsonObject.keySet().iterator().next(), mapJsonObject.values().iterator().next()) +
        new JsonObject().put(mapJsonArray.keySet().iterator().next(), mapJsonArray.values().iterator().next())
    ))));
  }
}
