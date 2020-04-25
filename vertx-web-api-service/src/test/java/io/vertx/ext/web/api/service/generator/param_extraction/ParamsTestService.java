package io.vertx.ext.web.api.service.generator.param_extraction;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@WebApiServiceGen
@VertxGen
public interface ParamsTestService {

  void basicTypes(String str, byte b, short s, int i, long l, float f, double d, char c, boolean bool, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void basicBoxedTypes(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c,
                       Boolean bool, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void basicBoxedTypesNull(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c,
                           Boolean bool, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void jsonTypes(JsonObject jsonObject, JsonArray jsonArray, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void jsonTypesNull(JsonObject jsonObject, JsonArray jsonArray, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void enumType(SomeEnum someEnum, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void enumTypeNull(SomeEnum someEnum, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void dataObjectType(FilterData options, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void dataObjectTypeNull(FilterData options, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void listParams(List<String> listString, List<Byte> listByte, List<Short> listShort, List<Integer> listInt, List<Long> listLong, List<JsonObject> listJsonObject, List<JsonArray> listJsonArray, List<FilterData> listDataObject, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void setParams(Set<String> setString, Set<Byte> setByte, Set<Short> setShort, Set<Integer> setInt, Set<Long> setLong, Set<JsonObject> setJsonObject, Set<JsonArray> setJsonArray, Set<FilterData> setDataObject, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void mapParams(Map<String, String> mapString, Map<String, Byte> mapByte, Map<String, Short> mapShort, Map<String, Integer> mapInt, Map<String, Long> mapLong, Map<String, JsonObject> mapJsonObject, Map<String, JsonArray> mapJsonArray, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  Future<ServiceResponse> futureReturn(String str, ServiceRequest context);

}
