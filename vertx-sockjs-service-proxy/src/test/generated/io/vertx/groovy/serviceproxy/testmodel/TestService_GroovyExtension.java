package io.vertx.groovy.serviceproxy.testmodel;
public class TestService_GroovyExtension {
  public static void jsonTypes(io.vertx.serviceproxy.testmodel.TestService j_receiver, java.util.Map<String, Object> jsonObject, java.util.List<Object> jsonArray) {
    j_receiver.jsonTypes(jsonObject != null ? io.vertx.lang.groovy.ConversionHelper.toJsonObject(jsonObject) : null,
      jsonArray != null ? io.vertx.lang.groovy.ConversionHelper.toJsonArray(jsonArray) : null);
  }
  public static void jsonTypesNull(io.vertx.serviceproxy.testmodel.TestService j_receiver, java.util.Map<String, Object> jsonObject, java.util.List<Object> jsonArray) {
    j_receiver.jsonTypesNull(jsonObject != null ? io.vertx.lang.groovy.ConversionHelper.toJsonObject(jsonObject) : null,
      jsonArray != null ? io.vertx.lang.groovy.ConversionHelper.toJsonArray(jsonArray) : null);
  }
  public static void dataObjectType(io.vertx.serviceproxy.testmodel.TestService j_receiver, java.util.Map<String, Object> options) {
    j_receiver.dataObjectType(options != null ? new io.vertx.serviceproxy.testmodel.TestDataObject(io.vertx.lang.groovy.ConversionHelper.toJsonObject(options)) : null);
  }
  public static void dataObjectTypeNull(io.vertx.serviceproxy.testmodel.TestService j_receiver, java.util.Map<String, Object> options) {
    j_receiver.dataObjectTypeNull(options != null ? new io.vertx.serviceproxy.testmodel.TestDataObject(io.vertx.lang.groovy.ConversionHelper.toJsonObject(options)) : null);
  }
  public static void listParams(io.vertx.serviceproxy.testmodel.TestService j_receiver, java.util.List<java.lang.String> listString, java.util.List<java.lang.Byte> listByte, java.util.List<java.lang.Short> listShort, java.util.List<java.lang.Integer> listInt, java.util.List<java.lang.Long> listLong, java.util.List<java.util.Map<String, Object>> listJsonObject, java.util.List<java.util.List<Object>> listJsonArray, java.util.List<java.util.Map<String, Object>> listDataObject) {
    j_receiver.listParams(listString != null ? listString.stream().map(elt -> elt).collect(java.util.stream.Collectors.toList()) : null,
      listByte != null ? listByte.stream().map(elt -> elt).collect(java.util.stream.Collectors.toList()) : null,
      listShort != null ? listShort.stream().map(elt -> elt).collect(java.util.stream.Collectors.toList()) : null,
      listInt != null ? listInt.stream().map(elt -> elt).collect(java.util.stream.Collectors.toList()) : null,
      listLong != null ? listLong.stream().map(elt -> elt).collect(java.util.stream.Collectors.toList()) : null,
      listJsonObject != null ? listJsonObject.stream().map(elt -> elt != null ? io.vertx.lang.groovy.ConversionHelper.toJsonObject(elt) : null).collect(java.util.stream.Collectors.toList()) : null,
      listJsonArray != null ? listJsonArray.stream().map(elt -> elt != null ? io.vertx.lang.groovy.ConversionHelper.toJsonArray(elt) : null).collect(java.util.stream.Collectors.toList()) : null,
      listDataObject != null ? listDataObject.stream().map(elt -> elt != null ? new io.vertx.serviceproxy.testmodel.TestDataObject(io.vertx.lang.groovy.ConversionHelper.toJsonObject(elt)) : null).collect(java.util.stream.Collectors.toList()) : null);
  }
  public static void setParams(io.vertx.serviceproxy.testmodel.TestService j_receiver, java.util.Set<java.lang.String> setString, java.util.Set<java.lang.Byte> setByte, java.util.Set<java.lang.Short> setShort, java.util.Set<java.lang.Integer> setInt, java.util.Set<java.lang.Long> setLong, java.util.Set<java.util.Map<String, Object>> setJsonObject, java.util.Set<java.util.List<Object>> setJsonArray, java.util.Set<java.util.Map<String, Object>> setDataObject) {
    j_receiver.setParams(setString != null ? setString.stream().map(elt -> elt).collect(java.util.stream.Collectors.toSet()) : null,
      setByte != null ? setByte.stream().map(elt -> elt).collect(java.util.stream.Collectors.toSet()) : null,
      setShort != null ? setShort.stream().map(elt -> elt).collect(java.util.stream.Collectors.toSet()) : null,
      setInt != null ? setInt.stream().map(elt -> elt).collect(java.util.stream.Collectors.toSet()) : null,
      setLong != null ? setLong.stream().map(elt -> elt).collect(java.util.stream.Collectors.toSet()) : null,
      setJsonObject != null ? setJsonObject.stream().map(elt -> elt != null ? io.vertx.lang.groovy.ConversionHelper.toJsonObject(elt) : null).collect(java.util.stream.Collectors.toSet()) : null,
      setJsonArray != null ? setJsonArray.stream().map(elt -> elt != null ? io.vertx.lang.groovy.ConversionHelper.toJsonArray(elt) : null).collect(java.util.stream.Collectors.toSet()) : null,
      setDataObject != null ? setDataObject.stream().map(elt -> elt != null ? new io.vertx.serviceproxy.testmodel.TestDataObject(io.vertx.lang.groovy.ConversionHelper.toJsonObject(elt)) : null).collect(java.util.stream.Collectors.toSet()) : null);
  }
  public static void mapParams(io.vertx.serviceproxy.testmodel.TestService j_receiver, java.util.Map<String, java.lang.String> mapString, java.util.Map<String, java.lang.Byte> mapByte, java.util.Map<String, java.lang.Short> mapShort, java.util.Map<String, java.lang.Integer> mapInt, java.util.Map<String, java.lang.Long> mapLong, java.util.Map<String, java.util.Map<String, Object>> mapJsonObject, java.util.Map<String, java.util.List<Object>> mapJsonArray) {
    j_receiver.mapParams(mapString != null ? mapString.entrySet().stream().collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey, entry -> entry.getValue())) : null,
      mapByte != null ? mapByte.entrySet().stream().collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey, entry -> entry.getValue())) : null,
      mapShort != null ? mapShort.entrySet().stream().collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey, entry -> entry.getValue())) : null,
      mapInt != null ? mapInt.entrySet().stream().collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey, entry -> entry.getValue())) : null,
      mapLong != null ? mapLong.entrySet().stream().collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey, entry -> entry.getValue())) : null,
      mapJsonObject != null ? mapJsonObject.entrySet().stream().collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey, entry -> entry.getValue() != null ? io.vertx.lang.groovy.ConversionHelper.toJsonObject(entry.getValue()) : null)) : null,
      mapJsonArray != null ? mapJsonArray.entrySet().stream().collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey, entry -> entry.getValue() != null ? io.vertx.lang.groovy.ConversionHelper.toJsonArray(entry.getValue()) : null)) : null);
  }
  public static void jsonObjectHandler(io.vertx.serviceproxy.testmodel.TestService j_receiver, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.Map<String, Object>>> resultHandler) {
    j_receiver.jsonObjectHandler(resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.lang.groovy.ConversionHelper.fromJsonObject(event)));
      }
    } : null);
  }
  public static void jsonObjectNullHandler(io.vertx.serviceproxy.testmodel.TestService j_receiver, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.Map<String, Object>>> resultHandler) {
    j_receiver.jsonObjectNullHandler(resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.lang.groovy.ConversionHelper.fromJsonObject(event)));
      }
    } : null);
  }
  public static void jsonArrayHandler(io.vertx.serviceproxy.testmodel.TestService j_receiver, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.List<Object>>> resultHandler) {
    j_receiver.jsonArrayHandler(resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonArray>>() {
      public void handle(io.vertx.core.AsyncResult<io.vertx.core.json.JsonArray> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.lang.groovy.ConversionHelper.fromJsonArray(event)));
      }
    } : null);
  }
  public static void jsonArrayNullHandler(io.vertx.serviceproxy.testmodel.TestService j_receiver, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.List<Object>>> resultHandler) {
    j_receiver.jsonArrayNullHandler(resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonArray>>() {
      public void handle(io.vertx.core.AsyncResult<io.vertx.core.json.JsonArray> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.lang.groovy.ConversionHelper.fromJsonArray(event)));
      }
    } : null);
  }
  public static void dataObjectHandler(io.vertx.serviceproxy.testmodel.TestService j_receiver, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.Map<String, Object>>> resultHandler) {
    j_receiver.dataObjectHandler(resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.serviceproxy.testmodel.TestDataObject>>() {
      public void handle(io.vertx.core.AsyncResult<io.vertx.serviceproxy.testmodel.TestDataObject> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.lang.groovy.ConversionHelper.applyIfNotNull(event, a -> io.vertx.lang.groovy.ConversionHelper.fromJsonObject(a.toJson()))));
      }
    } : null);
  }
  public static void dataObjectNullHandler(io.vertx.serviceproxy.testmodel.TestService j_receiver, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.Map<String, Object>>> resultHandler) {
    j_receiver.dataObjectNullHandler(resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.serviceproxy.testmodel.TestDataObject>>() {
      public void handle(io.vertx.core.AsyncResult<io.vertx.serviceproxy.testmodel.TestDataObject> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.lang.groovy.ConversionHelper.applyIfNotNull(event, a -> io.vertx.lang.groovy.ConversionHelper.fromJsonObject(a.toJson()))));
      }
    } : null);
  }
  public static void failingMethod(io.vertx.serviceproxy.testmodel.TestService j_receiver, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.Map<String, Object>>> resultHandler) {
    j_receiver.failingMethod(resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.lang.groovy.ConversionHelper.fromJsonObject(event)));
      }
    } : null);
  }
  public static void invokeWithMessage(io.vertx.serviceproxy.testmodel.TestService j_receiver, java.util.Map<String, Object> object, java.lang.String str, int i, char chr, io.vertx.serviceproxy.testmodel.SomeEnum senum, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.String>> resultHandler) {
    j_receiver.invokeWithMessage(object != null ? io.vertx.lang.groovy.ConversionHelper.toJsonObject(object) : null,
      str,
      i,
      chr,
      senum,
      resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.String>>() {
      public void handle(io.vertx.core.AsyncResult<java.lang.String> ar) {
        resultHandler.handle(ar.map(event -> event));
      }
    } : null);
  }
  public static void listJsonObjectHandler(io.vertx.serviceproxy.testmodel.TestService j_receiver, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.List<java.util.Map<String, Object>>>> resultHandler) {
    j_receiver.listJsonObjectHandler(resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.List<io.vertx.core.json.JsonObject>>>() {
      public void handle(io.vertx.core.AsyncResult<java.util.List<io.vertx.core.json.JsonObject>> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.lang.groovy.ConversionHelper.applyIfNotNull(event, list -> list.stream().map(elt -> io.vertx.lang.groovy.ConversionHelper.fromJsonObject(elt)).collect(java.util.stream.Collectors.toList()))));
      }
    } : null);
  }
  public static void listJsonArrayHandler(io.vertx.serviceproxy.testmodel.TestService j_receiver, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.List<java.util.List<Object>>>> resultHandler) {
    j_receiver.listJsonArrayHandler(resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.List<io.vertx.core.json.JsonArray>>>() {
      public void handle(io.vertx.core.AsyncResult<java.util.List<io.vertx.core.json.JsonArray>> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.lang.groovy.ConversionHelper.applyIfNotNull(event, list -> list.stream().map(elt -> io.vertx.lang.groovy.ConversionHelper.fromJsonArray(elt)).collect(java.util.stream.Collectors.toList()))));
      }
    } : null);
  }
  public static void listDataObjectHandler(io.vertx.serviceproxy.testmodel.TestService j_receiver, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.List<java.util.Map<String, Object>>>> resultHandler) {
    j_receiver.listDataObjectHandler(resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.List<io.vertx.serviceproxy.testmodel.TestDataObject>>>() {
      public void handle(io.vertx.core.AsyncResult<java.util.List<io.vertx.serviceproxy.testmodel.TestDataObject>> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.lang.groovy.ConversionHelper.applyIfNotNull(event, list -> list.stream().map(elt -> io.vertx.lang.groovy.ConversionHelper.applyIfNotNull(elt, a -> io.vertx.lang.groovy.ConversionHelper.fromJsonObject(a.toJson()))).collect(java.util.stream.Collectors.toList()))));
      }
    } : null);
  }
  public static void setJsonObjectHandler(io.vertx.serviceproxy.testmodel.TestService j_receiver, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.Set<java.util.Map<String, Object>>>> resultHandler) {
    j_receiver.setJsonObjectHandler(resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.Set<io.vertx.core.json.JsonObject>>>() {
      public void handle(io.vertx.core.AsyncResult<java.util.Set<io.vertx.core.json.JsonObject>> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.lang.groovy.ConversionHelper.applyIfNotNull(event, list -> list.stream().map(elt -> io.vertx.lang.groovy.ConversionHelper.fromJsonObject(elt)).collect(java.util.stream.Collectors.toSet()))));
      }
    } : null);
  }
  public static void setJsonArrayHandler(io.vertx.serviceproxy.testmodel.TestService j_receiver, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.Set<java.util.List<Object>>>> resultHandler) {
    j_receiver.setJsonArrayHandler(resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.Set<io.vertx.core.json.JsonArray>>>() {
      public void handle(io.vertx.core.AsyncResult<java.util.Set<io.vertx.core.json.JsonArray>> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.lang.groovy.ConversionHelper.applyIfNotNull(event, list -> list.stream().map(elt -> io.vertx.lang.groovy.ConversionHelper.fromJsonArray(elt)).collect(java.util.stream.Collectors.toSet()))));
      }
    } : null);
  }
  public static void setDataObjectHandler(io.vertx.serviceproxy.testmodel.TestService j_receiver, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.Set<java.util.Map<String, Object>>>> resultHandler) {
    j_receiver.setDataObjectHandler(resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.Set<io.vertx.serviceproxy.testmodel.TestDataObject>>>() {
      public void handle(io.vertx.core.AsyncResult<java.util.Set<io.vertx.serviceproxy.testmodel.TestDataObject>> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.lang.groovy.ConversionHelper.applyIfNotNull(event, list -> list.stream().map(elt -> io.vertx.lang.groovy.ConversionHelper.applyIfNotNull(elt, a -> io.vertx.lang.groovy.ConversionHelper.fromJsonObject(a.toJson()))).collect(java.util.stream.Collectors.toSet()))));
      }
    } : null);
  }
}
