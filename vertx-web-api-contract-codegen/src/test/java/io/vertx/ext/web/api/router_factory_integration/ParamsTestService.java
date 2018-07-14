package io.vertx.ext.web.api.router_factory_integration;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.codegen.testmodel.TestDataObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;
import io.vertx.ext.web.api.generator.WebApiProxyGen;

import java.util.List;
import java.util.Map;
import java.util.Set;

@WebApiProxyGen
@VertxGen
public interface ParamsTestService {

  void basicTypes(String str, byte b, short s, int i, long l, float f, double d, char c, boolean bool, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void basicBoxedTypes(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c,
                       Boolean bool, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void basicBoxedTypesNull(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c,
                           Boolean bool, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void jsonTypes(JsonObject jsonObject, JsonArray jsonArray, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void jsonTypesNull(JsonObject jsonObject, JsonArray jsonArray, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void enumType(SomeEnum someEnum, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void enumTypeNull(SomeEnum someEnum, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void dataObjectType(TestDataObject options, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void dataObjectTypeNull(TestDataObject options, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void listParams(List<String> listString, List<Byte> listByte, List<Short> listShort, List<Integer> listInt, List<Long> listLong, List<JsonObject> listJsonObject, List<JsonArray> listJsonArray, List<TestDataObject> listDataObject, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void setParams(Set<String> setString, Set<Byte> setByte, Set<Short> setShort, Set<Integer> setInt, Set<Long> setLong, Set<JsonObject> setJsonObject, Set<JsonArray> setJsonArray, Set<TestDataObject> setDataObject, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void mapParams(Map<String, String> mapString, Map<String, Byte> mapByte, Map<String, Short> mapShort, Map<String, Integer> mapInt, Map<String, Long> mapLong, Map<String, JsonObject> mapJsonObject, Map<String, JsonArray> mapJsonArray, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

}
