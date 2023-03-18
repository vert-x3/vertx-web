package io.vertx.ext.web.api.service.generator.param_extraction;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.FilterData;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.SomeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ParamsTestServiceImpl implements ParamsTestService {

  @Override
  public Future<ServiceResponse> basicTypes(String str, byte b, short s, int i, long l, float f, double d, char c, boolean bool, ServiceRequest context) {
    return Future.succeededFuture(ServiceResponse.completedWithPlainText(Buffer.buffer(
      "" + str + b + s + i + l + f + d + c + bool
    )));
  }

  @Override
  public Future<ServiceResponse> basicBoxedTypes(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c, Boolean bool, ServiceRequest context) {
    return Future.succeededFuture(ServiceResponse.completedWithPlainText(Buffer.buffer(
      "" + str + b + s + i + l + f + d + c + bool
    )));
  }

  @Override
  public Future<ServiceResponse> basicBoxedTypesNull(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c, Boolean bool, ServiceRequest context) {
    return Future.succeededFuture(ServiceResponse.completedWithPlainText(Buffer.buffer(
      "" + str + b + s + i + l + f + d + c + bool
    )));
  }

  @Override
  public Future<ServiceResponse> jsonTypes(JsonObject jsonObject, JsonArray jsonArray, ServiceRequest context) {
    return Future.succeededFuture(ServiceResponse.completedWithPlainText(Buffer.buffer(
      "" + jsonObject + jsonArray
    )));
  }

  @Override
  public Future<ServiceResponse> jsonTypesNull(JsonObject jsonObject, JsonArray jsonArray, ServiceRequest context) {
    return Future.succeededFuture(ServiceResponse.completedWithPlainText(Buffer.buffer(
      "" + jsonObject + jsonArray
    )));
  }

  @Override
  public Future<ServiceResponse> enumType(SomeEnum someEnum, ServiceRequest context) {
    return Future.succeededFuture(ServiceResponse.completedWithPlainText(Buffer.buffer(
      "" + someEnum
    )));
  }

  @Override
  public Future<ServiceResponse> enumTypeNull(SomeEnum someEnum, ServiceRequest context) {
    return Future.succeededFuture(ServiceResponse.completedWithPlainText(Buffer.buffer(
      "" + someEnum
    )));
  }

  @Override
  public Future<ServiceResponse> dataObjectType(FilterData options, ServiceRequest context) {
    return Future.succeededFuture(ServiceResponse.completedWithPlainText(Buffer.buffer(
      "" + options
    )));
  }

  @Override
  public Future<ServiceResponse> dataObjectTypeNull(FilterData options, ServiceRequest context) {
    return Future.succeededFuture(ServiceResponse.completedWithPlainText(Buffer.buffer(
      "" + options
    )));
  }

  @Override
  public Future<ServiceResponse> listParams(List<String> listString, List<Byte> listByte, List<Short> listShort, List<Integer> listInt, List<Long> listLong, List<JsonObject> listJsonObject, List<JsonArray> listJsonArray, List<FilterData> listDataObject, ServiceRequest context) {
    return Future.succeededFuture(ServiceResponse.completedWithPlainText(Buffer.buffer(
      "" + new JsonArray(listString) + new JsonArray(listByte) + new JsonArray(listShort) + new JsonArray(listInt) + new JsonArray(listLong) + new JsonArray(listJsonObject) + new JsonArray(listJsonArray) + new JsonArray(listDataObject.stream().map(FilterData::toJson).collect(Collectors.toList()))
    )));
  }

  @Override
  public Future<ServiceResponse> setParams(Set<String> setString, Set<Byte> setByte, Set<Short> setShort, Set<Integer> setInt, Set<Long> setLong, Set<JsonObject> setJsonObject, Set<JsonArray> setJsonArray, Set<FilterData> setDataObject, ServiceRequest context) {
    return Future.succeededFuture(ServiceResponse.completedWithPlainText(Buffer.buffer(
      "" + new JsonArray(new ArrayList<>(setString)) + new JsonArray(new ArrayList<>(setByte)) + new JsonArray(new ArrayList<>(setShort)) + new JsonArray(new ArrayList<>(setInt)) + new JsonArray(new ArrayList<>(setLong)) + new JsonArray(new ArrayList<>(setJsonObject)) + new JsonArray(new ArrayList<>(setJsonArray)) + new JsonArray(setDataObject.stream().map(FilterData::toJson).collect(Collectors.toList()))
    )));
  }

  @Override
  public Future<ServiceResponse> mapParams(Map<String, String> mapString, Map<String, Byte> mapByte, Map<String, Short> mapShort, Map<String, Integer> mapInt, Map<String, Long> mapLong, Map<String, JsonObject> mapJsonObject, Map<String, JsonArray> mapJsonArray, ServiceRequest context) {
    return Future.succeededFuture(ServiceResponse.completedWithPlainText(Buffer.buffer(
      "" +
        new JsonObject().put(mapString.keySet().iterator().next(), mapString.values().iterator().next()) +
        new JsonObject().put(mapByte.keySet().iterator().next(), mapByte.values().iterator().next()) +
        new JsonObject().put(mapShort.keySet().iterator().next(), mapShort.values().iterator().next()) +
        new JsonObject().put(mapInt.keySet().iterator().next(), mapInt.values().iterator().next()) +
        new JsonObject().put(mapLong.keySet().iterator().next(), mapLong.values().iterator().next()) +
        new JsonObject().put(mapJsonObject.keySet().iterator().next(), mapJsonObject.values().iterator().next()) +
        new JsonObject().put(mapJsonArray.keySet().iterator().next(), mapJsonArray.values().iterator().next())
    )));
  }
}
