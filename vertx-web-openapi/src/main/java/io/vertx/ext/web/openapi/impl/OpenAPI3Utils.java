package io.vertx.ext.web.openapi.impl;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.openapi.OpenAPIHolder;
import io.vertx.ext.web.openapi.RouterBuilderException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
class OpenAPI3Utils {

  protected static boolean isSchemaArray(JsonObject schema) {
    return "array".equals(schema.getString("type")) || schema.containsKey("items");
  }

  protected static boolean isSchemaObjectOrAllOfType(JsonObject schema) {
    return isSchemaObject(schema) || schema.containsKey("allOf");
  }

  protected static boolean isSchemaObjectOrCombinators(JsonObject schema) {
    return isSchemaObject(schema) || schema.containsKey("allOf") || schema.containsKey("oneOf") || schema.containsKey("anyOf");
  }

  protected static boolean isSchemaObject(JsonObject schema) {
    return "object".equals(schema.getString("type")) || schema.containsKey("properties");
  }

  protected static String resolveStyle(JsonObject param) {
    if (param.containsKey("style")) return param.getString("style");
    else switch (param.getString("in")) {
      case "query":
      case "cookie":
        return "form";
      case "path":
      case "header":
        return "simple";
    }
    return null; //TODO error reporting here?
  }

  protected static boolean resolveExplode(JsonObject param) {
    String style = resolveStyle(param);
    return param.getBoolean("explode", "form".equals(style));
  }

  protected static JsonArray mergeSecurityRequirements(JsonArray globalSecurityRequirements,
                                                       JsonArray localSecurityRequirements) {
    return localSecurityRequirements != null ? localSecurityRequirements : globalSecurityRequirements;
  }

  protected static String resolveContentTypeRegex(String listContentTypes) {
    // Check if it's list
    return Arrays.stream(listContentTypes.split(","))
      .map(String::trim)
      .map(pattern -> {
        int wildcardIndex = pattern.indexOf("/*");
        if (wildcardIndex != -1) {
          return Pattern.quote(pattern.substring(0, wildcardIndex + 1)) + ".*";
        }
        return Pattern.quote(pattern);
      })
      .collect(Collectors.joining("|"));
  }

  /* This function resolve all properties inside an allOf array of schemas */
  protected static Map<String, JsonObject> resolveAllOfArrays(List<JsonObject> allOfSchemas) {
    Map<String, JsonObject> properties = new HashMap<>();
    for (JsonObject schema : allOfSchemas) {
      if ("object".equals(schema.getString("type")))
        throw RouterBuilderException.createUnsupportedSpecFeature("allOf allows only inner object types in parameters" +
          ". Schema: " + schema.encode());
      schema.forEach(e -> properties.put(e.getKey(), (JsonObject) e.getValue()));
    }
    return properties;
  }

  /* This function check if schema is an allOf array or an object and returns a map of properties */
  protected static Map<String, JsonObject> solveObjectParameters(JsonObject schema) {
    if (schema.containsKey("allOf")) {
      return resolveAllOfArrays(schema.getJsonArray("allOf").stream().map(s -> (JsonObject) s).collect(Collectors.toList()));
    } else {
      return schema
        .getJsonObject("properties", new JsonObject())
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> (JsonObject) e.getValue()));
    }
  }

  protected static boolean serviceProxyMethodIsCompatibleHandler(Method method) {
    java.lang.reflect.Parameter[] parameters = method.getParameters();
    if (parameters.length < 2) return false;
    if (!parameters[parameters.length - 1].getType().equals(Handler.class)) return false;
    return parameters[parameters.length - 2].getType().getName().equals("io.vertx.ext.web.api.service.ServiceRequest");
  }

  protected static JsonObject sanitizeDeliveryOptionsExtension(JsonObject jsonObject) {
    JsonObject newObj = new JsonObject();
    if (jsonObject.containsKey("timeout")) newObj.put("timeout", jsonObject.getValue("timeout"));
    if (jsonObject.containsKey("headers")) newObj.put("headers", jsonObject.getValue("headers"));
    return newObj;
  }

  protected static String sanitizeOperationId(String operationId) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < operationId.length(); i++) {
      char c = operationId.charAt(i);
      if (c == '-' || c == ' ' || c == '_') {
        try {
          while (c == '-' || c == ' ' || c == '_') {
            i++;
            c = operationId.charAt(i);
          }
          result.append(Character.toUpperCase(operationId.charAt(i)));
        } catch (StringIndexOutOfBoundsException e) {
        }
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }

  protected static Object getAndMergeServiceExtension(String extensionKey, String addressKey, String methodKey,
                                                      JsonObject pathModel, JsonObject operationModel) {
    Object pathExtension = pathModel.getValue(extensionKey);
    Object operationExtension = operationModel.getValue(extensionKey);

    // Cases:
    // 1. both strings or path extension null: operation extension overrides all
    // 2. path extension map and operation extension string: path extension interpreted as delivery options and
    // operation extension as address
    // 3. path extension string and operation extension map: path extension interpreted as address
    // 4. both maps: extension map overrides path map elements
    // 5. operation extension null: path extension overrides all

    if ((operationExtension instanceof String && pathExtension instanceof String) || pathExtension == null)
      return operationExtension;
    if (operationExtension instanceof String && pathExtension instanceof JsonObject) {
      JsonObject result = new JsonObject();
      result.put(addressKey, operationExtension);
      JsonObject pathExtensionMap = (JsonObject) pathExtension;
      if (pathExtensionMap.containsKey(methodKey))
        throw RouterBuilderException.createWrongExtension("Extension " + extensionKey + " in path declaration must " +
          "not contain " + methodKey);
      result.mergeIn(pathExtensionMap);
      return result;
    }
    if (operationExtension instanceof JsonObject && pathExtension instanceof String) {
      JsonObject result = ((JsonObject) operationExtension).copy();
      if (!result.containsKey(addressKey))
        result.put(addressKey, pathExtension);
      return result;
    }
    if (operationExtension instanceof JsonObject && pathExtension instanceof JsonObject) {
      return ((JsonObject) pathExtension).copy().mergeIn((JsonObject) operationExtension);
    }
    if (operationExtension == null) return pathExtension;
    return null;
  }

  // /definitions/hello/properties/a - /definitions/hello = /properties/a
  protected static JsonPointer pointerDifference(JsonPointer pointer1, JsonPointer pointer2) {
    String firstPointer = pointer1.toString();
    String secondPointer = pointer2.toString();

    return JsonPointer.from(
      firstPointer.substring(secondPointer.length())
    );
  }

  protected static JsonObject generateFakeSchema(JsonObject originalSchema, OpenAPIHolder holder) {
    JsonObject fakeSchema = holder.solveIfNeeded(originalSchema).copy();
    String combinatorKeyword = fakeSchema.containsKey("allOf") ? "allOf" : fakeSchema.containsKey("anyOf") ? "anyOf"
      : fakeSchema.containsKey("oneOf") ? "oneOf" : null;
    if (combinatorKeyword != null) {
      JsonArray schemasArray = fakeSchema.getJsonArray(combinatorKeyword);
      JsonArray processedSchemas = new JsonArray();
      for (int i = 0; i < schemasArray.size(); i++) {
        JsonObject innerSchema = holder.solveIfNeeded(schemasArray.getJsonObject(i)).copy();
        processedSchemas.add(innerSchema);
        schemasArray.getJsonObject(i).mergeIn(innerSchema);
        if ("object".equals(innerSchema.getString("type")) || innerSchema.containsKey("properties"))
          fakeSchema = fakeSchema.mergeIn(innerSchema, true);
      }
      fakeSchema.remove(combinatorKeyword);
      fakeSchema.put("x-" + combinatorKeyword, processedSchemas);
    }
    if (fakeSchema.containsKey("properties")) {
      JsonObject propsObj = fakeSchema.getJsonObject("properties");
      for (String key : propsObj.fieldNames()) {
        propsObj.put(key, holder.solveIfNeeded(propsObj.getJsonObject(key)).copy());
      }
    }
    if (fakeSchema.containsKey("items")) {
      fakeSchema.put("items", holder.solveIfNeeded(fakeSchema.getJsonObject("items")));
    }

    return fakeSchema;

  }

  protected static boolean isFakeSchemaAnyOfOrOneOf(JsonObject fakeSchema) {
    return fakeSchema.containsKey("x-anyOf") || fakeSchema.containsKey("x-oneOf");
  }
}
