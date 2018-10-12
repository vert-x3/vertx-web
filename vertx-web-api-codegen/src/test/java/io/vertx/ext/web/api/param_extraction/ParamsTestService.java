package io.vertx.ext.web.api.param_extraction;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;
import io.vertx.ext.web.api.router_factory_integration.FilterData;
import io.vertx.ext.web.api.router_factory_integration.SomeEnum;

import java.util.List;
import java.util.Map;
import java.util.Set;

@WebApiServiceGen
@VertxGen
public interface ParamsTestService {

  void basicTypes(String str, byte b, short s, int i, long l, float f, double d, char c, boolean bool, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void basicBoxedTypes(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c,
                       Boolean bool, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void basicBoxedTypesNull(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c,
                           Boolean bool, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void jsonTypes(JsonObject jsonObject, JsonArray jsonArray, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void jsonTypesNull(JsonObject jsonObject, JsonArray jsonArray, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void enumType(SomeEnum someEnum, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void enumTypeNull(SomeEnum someEnum, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void dataObjectType(FilterData options, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void dataObjectTypeNull(FilterData options, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void listParams(List<String> listString, List<Byte> listByte, List<Short> listShort, List<Integer> listInt, List<Long> listLong, List<JsonObject> listJsonObject, List<JsonArray> listJsonArray, List<FilterData> listDataObject, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void setParams(Set<String> setString, Set<Byte> setByte, Set<Short> setShort, Set<Integer> setInt, Set<Long> setLong, Set<JsonObject> setJsonObject, Set<JsonArray> setJsonArray, Set<FilterData> setDataObject, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void mapParams(Map<String, String> mapString, Map<String, Byte> mapByte, Map<String, Short> mapShort, Map<String, Integer> mapInt, Map<String, Long> mapLong, Map<String, JsonObject> mapJsonObject, Map<String, JsonArray> mapJsonArray, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

}
