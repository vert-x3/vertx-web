package io.vertx.ext.web.openapi.impl;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.openapi.OpenAPIHolder;
import io.vertx.ext.web.openapi.RouterBuilderException;

import java.lang.reflect.Method;
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
    if (listContentTypes.contains(",")) {
      StringBuilder stringBuilder = new StringBuilder();
      String[] contentTypes = listContentTypes.split(",");
      for (String contentType : contentTypes)
        stringBuilder.append(Pattern.quote(contentType.trim())).append("|");
      stringBuilder.deleteCharAt(stringBuilder.length() - 1);
      return stringBuilder.toString();
    } else if (listContentTypes.trim().endsWith("/*")) {
      return listContentTypes.trim().substring(0, listContentTypes.indexOf("/*")) + "\\/.*";
    }
    return Pattern.quote(listContentTypes);
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
    return parameters[parameters.length - 2].getType().equals(ServiceRequest.class);
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

  protected static JsonObject generateFakeSchema(JsonObject schema, OpenAPIHolder holder) {
    JsonObject fakeSchema = holder.solveIfNeeded(schema).copy();
    String combinatorKeyword = fakeSchema.containsKey("allOf") ? "allOf" : fakeSchema.containsKey("anyOf") ? "anyOf"
      : fakeSchema.containsKey("oneOf") ? "oneOf" : null;
    if (combinatorKeyword != null) {
      JsonArray schemasArray = fakeSchema.getJsonArray(combinatorKeyword);
      JsonArray processedSchemas = new JsonArray();
      for (int i = 0; i < schemasArray.size(); i++) {
        JsonObject innerSchema = holder.solveIfNeeded(schemasArray.getJsonObject(i));
        processedSchemas.add(innerSchema.copy());
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
        propsObj.put(key, holder.solveIfNeeded(propsObj.getJsonObject(key)));
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

  // Hardcoded FTW
  protected static String openapiSchemaJson = "{\"$id\":\"https://spec.openapis.org/oas/3.0/schema/2019-04-02\"," +
    "\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"description\":\"Validation schema for OpenAPI " +
    "Specification 3.0.X.\",\"type\":\"object\",\"required\":[\"openapi\",\"info\",\"paths\"]," +
    "\"properties\":{\"openapi\":{\"type\":\"string\",\"pattern\":\"^3\\\\.0\\\\.\\\\d(-.+)?$\"}," +
    "\"info\":{\"$ref\":\"#/definitions/Info\"},\"externalDocs\":{\"$ref\":\"#/definitions/ExternalDocumentation\"}," +
    "\"servers\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/definitions/Server\"}},\"security\":{\"type\":\"array\"," +
    "\"items\":{\"$ref\":\"#/definitions/SecurityRequirement\"}},\"tags\":{\"type\":\"array\"," +
    "\"items\":{\"$ref\":\"#/definitions/Tag\"},\"uniqueItems\":true},\"paths\":{\"$ref\":\"#/definitions/Paths\"}," +
    "\"components\":{\"$ref\":\"#/definitions/Components\"}},\"patternProperties\":{\"^x-\":{}}," +
    "\"additionalProperties\":false,\"definitions\":{\"Reference\":{\"type\":\"object\",\"required\":[\"$ref\"]," +
    "\"patternProperties\":{\"^\\\\$ref$\":{\"type\":\"string\",\"format\":\"uri-reference\"}}}," +
    "\"Info\":{\"type\":\"object\",\"required\":[\"title\",\"version\"]," +
    "\"properties\":{\"title\":{\"type\":\"string\"},\"description\":{\"type\":\"string\"}," +
    "\"termsOfService\":{\"type\":\"string\",\"format\":\"uri-reference\"}," +
    "\"contact\":{\"$ref\":\"#/definitions/Contact\"},\"license\":{\"$ref\":\"#/definitions/License\"}," +
    "\"version\":{\"type\":\"string\"}},\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false}," +
    "\"Contact\":{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"},\"url\":{\"type\":\"string\"," +
    "\"format\":\"uri-reference\"},\"email\":{\"type\":\"string\",\"format\":\"email\"}}," +
    "\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false},\"License\":{\"type\":\"object\"," +
    "\"required\":[\"name\"],\"properties\":{\"name\":{\"type\":\"string\"},\"url\":{\"type\":\"string\"," +
    "\"format\":\"uri-reference\"}},\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false}," +
    "\"Server\":{\"type\":\"object\",\"required\":[\"url\"],\"properties\":{\"url\":{\"type\":\"string\"}," +
    "\"description\":{\"type\":\"string\"},\"variables\":{\"type\":\"object\"," +
    "\"additionalProperties\":{\"$ref\":\"#/definitions/ServerVariable\"}}},\"patternProperties\":{\"^x-\":{}}," +
    "\"additionalProperties\":false},\"ServerVariable\":{\"type\":\"object\",\"required\":[\"default\"]," +
    "\"properties\":{\"enum\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"default\":{\"type\":\"string\"}," +
    "\"description\":{\"type\":\"string\"}},\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false}," +
    "\"Components\":{\"type\":\"object\",\"properties\":{\"schemas\":{\"type\":\"object\"," +
    "\"patternProperties\":{\"^[a-zA-Z0-9\\\\.\\\\-_]+$\":{\"oneOf\":[{\"$ref\":\"#/definitions/Schema\"}," +
    "{\"$ref\":\"#/definitions/Reference\"}]}}},\"responses\":{\"type\":\"object\"," +
    "\"patternProperties\":{\"^[a-zA-Z0-9\\\\.\\\\-_]+$\":{\"oneOf\":[{\"$ref\":\"#/definitions/Reference\"}," +
    "{\"$ref\":\"#/definitions/Response\"}]}}},\"parameters\":{\"type\":\"object\"," +
    "\"patternProperties\":{\"^[a-zA-Z0-9\\\\.\\\\-_]+$\":{\"oneOf\":[{\"$ref\":\"#/definitions/Reference\"}," +
    "{\"$ref\":\"#/definitions/Parameter\"}]}}},\"examples\":{\"type\":\"object\"," +
    "\"patternProperties\":{\"^[a-zA-Z0-9\\\\.\\\\-_]+$\":{\"oneOf\":[{\"$ref\":\"#/definitions/Reference\"}," +
    "{\"$ref\":\"#/definitions/Example\"}]}}},\"requestBodies\":{\"type\":\"object\"," +
    "\"patternProperties\":{\"^[a-zA-Z0-9\\\\.\\\\-_]+$\":{\"oneOf\":[{\"$ref\":\"#/definitions/Reference\"}," +
    "{\"$ref\":\"#/definitions/RequestBody\"}]}}},\"headers\":{\"type\":\"object\"," +
    "\"patternProperties\":{\"^[a-zA-Z0-9\\\\.\\\\-_]+$\":{\"oneOf\":[{\"$ref\":\"#/definitions/Reference\"}," +
    "{\"$ref\":\"#/definitions/Header\"}]}}},\"securitySchemes\":{\"type\":\"object\"," +
    "\"patternProperties\":{\"^[a-zA-Z0-9\\\\.\\\\-_]+$\":{\"oneOf\":[{\"$ref\":\"#/definitions/Reference\"}," +
    "{\"$ref\":\"#/definitions/SecurityScheme\"}]}}},\"links\":{\"type\":\"object\"," +
    "\"patternProperties\":{\"^[a-zA-Z0-9\\\\.\\\\-_]+$\":{\"oneOf\":[{\"$ref\":\"#/definitions/Reference\"}," +
    "{\"$ref\":\"#/definitions/Link\"}]}}},\"callbacks\":{\"type\":\"object\"," +
    "\"patternProperties\":{\"^[a-zA-Z0-9\\\\.\\\\-_]+$\":{\"oneOf\":[{\"$ref\":\"#/definitions/Reference\"}," +
    "{\"$ref\":\"#/definitions/Callback\"}]}}}},\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false}," +
    "\"Schema\":{\"type\":\"object\",\"properties\":{\"title\":{\"type\":\"string\"}," +
    "\"multipleOf\":{\"type\":\"number\",\"exclusiveMinimum\":0},\"maximum\":{\"type\":\"number\"}," +
    "\"exclusiveMaximum\":{\"type\":\"boolean\",\"default\":false},\"minimum\":{\"type\":\"number\"}," +
    "\"exclusiveMinimum\":{\"type\":\"boolean\",\"default\":false},\"maxLength\":{\"type\":\"integer\"," +
    "\"minimum\":0},\"minLength\":{\"type\":\"integer\",\"minimum\":0,\"default\":0}," +
    "\"pattern\":{\"type\":\"string\",\"format\":\"regex\"},\"maxItems\":{\"type\":\"integer\",\"minimum\":0}," +
    "\"minItems\":{\"type\":\"integer\",\"minimum\":0,\"default\":0},\"uniqueItems\":{\"type\":\"boolean\"," +
    "\"default\":false},\"maxProperties\":{\"type\":\"integer\",\"minimum\":0}," +
    "\"minProperties\":{\"type\":\"integer\",\"minimum\":0,\"default\":0},\"required\":{\"type\":\"array\"," +
    "\"items\":{\"type\":\"string\"},\"minItems\":1,\"uniqueItems\":true},\"enum\":{\"type\":\"array\",\"items\":{}," +
    "\"minItems\":1,\"uniqueItems\":false},\"type\":{\"type\":\"string\",\"enum\":[\"array\",\"boolean\",\"integer\"," +
    "\"number\",\"object\",\"string\"]},\"not\":{\"oneOf\":[{\"$ref\":\"#/definitions/Schema\"}," +
    "{\"$ref\":\"#/definitions/Reference\"}]},\"allOf\":{\"type\":\"array\"," +
    "\"items\":{\"oneOf\":[{\"$ref\":\"#/definitions/Schema\"},{\"$ref\":\"#/definitions/Reference\"}]}}," +
    "\"oneOf\":{\"type\":\"array\",\"items\":{\"oneOf\":[{\"$ref\":\"#/definitions/Schema\"}," +
    "{\"$ref\":\"#/definitions/Reference\"}]}},\"anyOf\":{\"type\":\"array\"," +
    "\"items\":{\"oneOf\":[{\"$ref\":\"#/definitions/Schema\"},{\"$ref\":\"#/definitions/Reference\"}]}}," +
    "\"items\":{\"oneOf\":[{\"$ref\":\"#/definitions/Schema\"},{\"$ref\":\"#/definitions/Reference\"}]}," +
    "\"properties\":{\"type\":\"object\",\"additionalProperties\":{\"oneOf\":[{\"$ref\":\"#/definitions/Schema\"}," +
    "{\"$ref\":\"#/definitions/Reference\"}]}},\"additionalProperties\":{\"oneOf\":[{\"$ref\":\"#/definitions/Schema" +
    "\"},{\"$ref\":\"#/definitions/Reference\"},{\"type\":\"boolean\"}],\"default\":true}," +
    "\"description\":{\"type\":\"string\"},\"format\":{\"type\":\"string\"},\"default\":{}," +
    "\"nullable\":{\"type\":\"boolean\",\"default\":false}," +
    "\"discriminator\":{\"$ref\":\"#/definitions/Discriminator\"},\"readOnly\":{\"type\":\"boolean\"," +
    "\"default\":false},\"writeOnly\":{\"type\":\"boolean\",\"default\":false},\"example\":{}," +
    "\"externalDocs\":{\"$ref\":\"#/definitions/ExternalDocumentation\"},\"deprecated\":{\"type\":\"boolean\"," +
    "\"default\":false},\"xml\":{\"$ref\":\"#/definitions/XML\"}},\"patternProperties\":{\"^x-\":{}}," +
    "\"additionalProperties\":false},\"Discriminator\":{\"type\":\"object\",\"required\":[\"propertyName\"]," +
    "\"properties\":{\"propertyName\":{\"type\":\"string\"},\"mapping\":{\"type\":\"object\"," +
    "\"additionalProperties\":{\"type\":\"string\"}}}},\"XML\":{\"type\":\"object\"," +
    "\"properties\":{\"name\":{\"type\":\"string\"},\"namespace\":{\"type\":\"string\",\"format\":\"uri\"}," +
    "\"prefix\":{\"type\":\"string\"},\"attribute\":{\"type\":\"boolean\",\"default\":false}," +
    "\"wrapped\":{\"type\":\"boolean\",\"default\":false}},\"patternProperties\":{\"^x-\":{}}," +
    "\"additionalProperties\":false},\"Response\":{\"type\":\"object\",\"required\":[\"description\"]," +
    "\"properties\":{\"description\":{\"type\":\"string\"},\"headers\":{\"type\":\"object\"," +
    "\"additionalProperties\":{\"oneOf\":[{\"$ref\":\"#/definitions/Header\"}," +
    "{\"$ref\":\"#/definitions/Reference\"}]}},\"content\":{\"type\":\"object\"," +
    "\"additionalProperties\":{\"$ref\":\"#/definitions/MediaType\"}},\"links\":{\"type\":\"object\"," +
    "\"additionalProperties\":{\"oneOf\":[{\"$ref\":\"#/definitions/Link\"}," +
    "{\"$ref\":\"#/definitions/Reference\"}]}}},\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false}," +
    "\"MediaType\":{\"type\":\"object\",\"properties\":{\"schema\":{\"oneOf\":[{\"$ref\":\"#/definitions/Schema\"}," +
    "{\"$ref\":\"#/definitions/Reference\"}]},\"example\":{},\"examples\":{\"type\":\"object\"," +
    "\"additionalProperties\":{\"oneOf\":[{\"$ref\":\"#/definitions/Example\"}," +
    "{\"$ref\":\"#/definitions/Reference\"}]}},\"encoding\":{\"type\":\"object\"," +
    "\"additionalProperties\":{\"$ref\":\"#/definitions/Encoding\"}}},\"patternProperties\":{\"^x-\":{}}," +
    "\"additionalProperties\":false,\"allOf\":[{\"$ref\":\"#/definitions/ExampleXORExamples\"}]}," +
    "\"Example\":{\"type\":\"object\",\"properties\":{\"summary\":{\"type\":\"string\"}," +
    "\"description\":{\"type\":\"string\"},\"value\":{},\"externalValue\":{\"type\":\"string\"," +
    "\"format\":\"uri-reference\"}},\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false}," +
    "\"Header\":{\"type\":\"object\",\"properties\":{\"description\":{\"type\":\"string\"}," +
    "\"required\":{\"type\":\"boolean\",\"default\":false},\"deprecated\":{\"type\":\"boolean\",\"default\":false}," +
    "\"allowEmptyValue\":{\"type\":\"boolean\",\"default\":false},\"style\":{\"type\":\"string\"," +
    "\"enum\":[\"simple\"],\"default\":\"simple\"},\"explode\":{\"type\":\"boolean\"}," +
    "\"allowReserved\":{\"type\":\"boolean\",\"default\":false}," +
    "\"schema\":{\"oneOf\":[{\"$ref\":\"#/definitions/Schema\"},{\"$ref\":\"#/definitions/Reference\"}]}," +
    "\"content\":{\"type\":\"object\",\"additionalProperties\":{\"$ref\":\"#/definitions/MediaType\"}," +
    "\"minProperties\":1,\"maxProperties\":1},\"example\":{},\"examples\":{\"type\":\"object\"," +
    "\"additionalProperties\":{\"oneOf\":[{\"$ref\":\"#/definitions/Example\"}," +
    "{\"$ref\":\"#/definitions/Reference\"}]}}},\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false," +
    "\"allOf\":[{\"$ref\":\"#/definitions/ExampleXORExamples\"},{\"$ref\":\"#/definitions/SchemaXORContent\"}]}," +
    "\"Paths\":{\"type\":\"object\",\"patternProperties\":{\"^\\\\/\":{\"$ref\":\"#/definitions/PathItem\"}," +
    "\"^x-\":{}},\"additionalProperties\":false},\"PathItem\":{\"type\":\"object\"," +
    "\"properties\":{\"$ref\":{\"type\":\"string\"},\"summary\":{\"type\":\"string\"}," +
    "\"description\":{\"type\":\"string\"},\"servers\":{\"type\":\"array\"," +
    "\"items\":{\"$ref\":\"#/definitions/Server\"}},\"parameters\":{\"type\":\"array\"," +
    "\"items\":{\"oneOf\":[{\"$ref\":\"#/definitions/Parameter\"},{\"$ref\":\"#/definitions/Reference\"}]}," +
    "\"uniqueItems\":true}},\"patternProperties\":{\"^(get|put|post|delete|options|head|patch|trace)" +
    "$\":{\"$ref\":\"#/definitions/Operation\"},\"^x-\":{}},\"additionalProperties\":false}," +
    "\"Operation\":{\"type\":\"object\",\"required\":[\"responses\"],\"properties\":{\"tags\":{\"type\":\"array\"," +
    "\"items\":{\"type\":\"string\"}},\"summary\":{\"type\":\"string\"},\"description\":{\"type\":\"string\"}," +
    "\"externalDocs\":{\"$ref\":\"#/definitions/ExternalDocumentation\"},\"operationId\":{\"type\":\"string\"}," +
    "\"parameters\":{\"type\":\"array\",\"items\":{\"oneOf\":[{\"$ref\":\"#/definitions/Parameter\"}," +
    "{\"$ref\":\"#/definitions/Reference\"}]},\"uniqueItems\":true}," +
    "\"requestBody\":{\"oneOf\":[{\"$ref\":\"#/definitions/RequestBody\"},{\"$ref\":\"#/definitions/Reference\"}]}," +
    "\"responses\":{\"$ref\":\"#/definitions/Responses\"},\"callbacks\":{\"type\":\"object\"," +
    "\"additionalProperties\":{\"oneOf\":[{\"$ref\":\"#/definitions/Callback\"}," +
    "{\"$ref\":\"#/definitions/Reference\"}]}},\"deprecated\":{\"type\":\"boolean\",\"default\":false}," +
    "\"security\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/definitions/SecurityRequirement\"}}," +
    "\"servers\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/definitions/Server\"}}}," +
    "\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false},\"Responses\":{\"type\":\"object\"," +
    "\"properties\":{\"default\":{\"oneOf\":[{\"$ref\":\"#/definitions/Response\"}," +
    "{\"$ref\":\"#/definitions/Reference\"}]}},\"patternProperties\":{\"^[1-5](?:\\\\d{2}|XX)" +
    "$\":{\"oneOf\":[{\"$ref\":\"#/definitions/Response\"},{\"$ref\":\"#/definitions/Reference\"}]},\"^x-\":{}}," +
    "\"minProperties\":1,\"additionalProperties\":false},\"SecurityRequirement\":{\"type\":\"object\"," +
    "\"additionalProperties\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}}},\"Tag\":{\"type\":\"object\"," +
    "\"required\":[\"name\"],\"properties\":{\"name\":{\"type\":\"string\"},\"description\":{\"type\":\"string\"}," +
    "\"externalDocs\":{\"$ref\":\"#/definitions/ExternalDocumentation\"}},\"patternProperties\":{\"^x-\":{}}," +
    "\"additionalProperties\":false},\"ExternalDocumentation\":{\"type\":\"object\",\"required\":[\"url\"]," +
    "\"properties\":{\"description\":{\"type\":\"string\"},\"url\":{\"type\":\"string\"," +
    "\"format\":\"uri-reference\"}},\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false}," +
    "\"ExampleXORExamples\":{\"description\":\"Example and examples are mutually exclusive\"," +
    "\"not\":{\"required\":[\"example\",\"examples\"]}},\"SchemaXORContent\":{\"description\":\"Schema and content " +
    "are mutually exclusive, at least one is required\",\"not\":{\"required\":[\"schema\",\"content\"]}," +
    "\"oneOf\":[{\"required\":[\"schema\"]},{\"required\":[\"content\"],\"description\":\"Some properties are not " +
    "allowed if content is present\",\"allOf\":[{\"not\":{\"required\":[\"style\"]}}," +
    "{\"not\":{\"required\":[\"explode\"]}},{\"not\":{\"required\":[\"allowReserved\"]}}," +
    "{\"not\":{\"required\":[\"example\"]}},{\"not\":{\"required\":[\"examples\"]}}]}]}," +
    "\"Parameter\":{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"},\"in\":{\"type\":\"string\"}," +
    "\"description\":{\"type\":\"string\"},\"required\":{\"type\":\"boolean\",\"default\":false}," +
    "\"deprecated\":{\"type\":\"boolean\",\"default\":false},\"allowEmptyValue\":{\"type\":\"boolean\"," +
    "\"default\":false},\"style\":{\"type\":\"string\"},\"explode\":{\"type\":\"boolean\"}," +
    "\"allowReserved\":{\"type\":\"boolean\",\"default\":false}," +
    "\"schema\":{\"oneOf\":[{\"$ref\":\"#/definitions/Schema\"},{\"$ref\":\"#/definitions/Reference\"}]}," +
    "\"content\":{\"type\":\"object\",\"additionalProperties\":{\"$ref\":\"#/definitions/MediaType\"}," +
    "\"minProperties\":1,\"maxProperties\":1},\"example\":{},\"examples\":{\"type\":\"object\"," +
    "\"additionalProperties\":{\"oneOf\":[{\"$ref\":\"#/definitions/Example\"}," +
    "{\"$ref\":\"#/definitions/Reference\"}]}}},\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false," +
    "\"required\":[\"name\",\"in\"],\"allOf\":[{\"$ref\":\"#/definitions/ExampleXORExamples\"}," +
    "{\"$ref\":\"#/definitions/SchemaXORContent\"},{\"$ref\":\"#/definitions/ParameterLocation\"}]}," +
    "\"ParameterLocation\":{\"description\":\"Parameter location\",\"oneOf\":[{\"description\":\"Parameter in path\"," +
    "\"required\":[\"required\"],\"properties\":{\"in\":{\"enum\":[\"path\"]},\"style\":{\"enum\":[\"matrix\"," +
    "\"label\",\"simple\"],\"default\":\"simple\"},\"required\":{\"enum\":[true]}}},{\"description\":\"Parameter in " +
    "query\",\"properties\":{\"in\":{\"enum\":[\"query\"]},\"style\":{\"enum\":[\"form\",\"spaceDelimited\"," +
    "\"pipeDelimited\",\"deepObject\"],\"default\":\"form\"}}},{\"description\":\"Parameter in header\"," +
    "\"properties\":{\"in\":{\"enum\":[\"header\"]},\"style\":{\"enum\":[\"simple\"],\"default\":\"simple\"}}}," +
    "{\"description\":\"Parameter in cookie\",\"properties\":{\"in\":{\"enum\":[\"cookie\"]}," +
    "\"style\":{\"enum\":[\"form\"],\"default\":\"form\"}}}]},\"RequestBody\":{\"type\":\"object\"," +
    "\"required\":[\"content\"],\"properties\":{\"description\":{\"type\":\"string\"}," +
    "\"content\":{\"type\":\"object\",\"additionalProperties\":{\"$ref\":\"#/definitions/MediaType\"}}," +
    "\"required\":{\"type\":\"boolean\",\"default\":false}},\"patternProperties\":{\"^x-\":{}}," +
    "\"additionalProperties\":false},\"SecurityScheme\":{\"oneOf\":[{\"$ref\":\"#/definitions/APIKeySecurityScheme" +
    "\"},{\"$ref\":\"#/definitions/HTTPSecurityScheme\"},{\"$ref\":\"#/definitions/OAuth2SecurityScheme\"}," +
    "{\"$ref\":\"#/definitions/OpenIdConnectSecurityScheme\"}]},\"APIKeySecurityScheme\":{\"type\":\"object\"," +
    "\"required\":[\"type\",\"name\",\"in\"],\"properties\":{\"type\":{\"type\":\"string\",\"enum\":[\"apiKey\"]}," +
    "\"name\":{\"type\":\"string\"},\"in\":{\"type\":\"string\",\"enum\":[\"header\",\"query\",\"cookie\"]}," +
    "\"description\":{\"type\":\"string\"}},\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false}," +
    "\"HTTPSecurityScheme\":{\"type\":\"object\",\"required\":[\"scheme\",\"type\"]," +
    "\"properties\":{\"scheme\":{\"type\":\"string\"},\"bearerFormat\":{\"type\":\"string\"}," +
    "\"description\":{\"type\":\"string\"},\"type\":{\"type\":\"string\",\"enum\":[\"http\"]}}," +
    "\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false,\"oneOf\":[{\"description\":\"Bearer\"," +
    "\"properties\":{\"scheme\":{\"enum\":[\"bearer\"]}}},{\"description\":\"Non Bearer\"," +
    "\"not\":{\"required\":[\"bearerFormat\"]},\"properties\":{\"scheme\":{\"not\":{\"enum\":[\"bearer\"]}}}}]}," +
    "\"OAuth2SecurityScheme\":{\"type\":\"object\",\"required\":[\"type\",\"flows\"]," +
    "\"properties\":{\"type\":{\"type\":\"string\",\"enum\":[\"oauth2\"]}," +
    "\"flows\":{\"$ref\":\"#/definitions/OAuthFlows\"},\"description\":{\"type\":\"string\"}}," +
    "\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false}," +
    "\"OpenIdConnectSecurityScheme\":{\"type\":\"object\",\"required\":[\"type\",\"openIdConnectUrl\"]," +
    "\"properties\":{\"type\":{\"type\":\"string\",\"enum\":[\"openIdConnect\"]}," +
    "\"openIdConnectUrl\":{\"type\":\"string\",\"format\":\"uri-reference\"},\"description\":{\"type\":\"string\"}}," +
    "\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false},\"OAuthFlows\":{\"type\":\"object\"," +
    "\"properties\":{\"implicit\":{\"$ref\":\"#/definitions/ImplicitOAuthFlow\"}," +
    "\"password\":{\"$ref\":\"#/definitions/PasswordOAuthFlow\"}," +
    "\"clientCredentials\":{\"$ref\":\"#/definitions/ClientCredentialsFlow\"}," +
    "\"authorizationCode\":{\"$ref\":\"#/definitions/AuthorizationCodeOAuthFlow\"}}," +
    "\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false},\"ImplicitOAuthFlow\":{\"type\":\"object\"," +
    "\"required\":[\"authorizationUrl\",\"scopes\"],\"properties\":{\"authorizationUrl\":{\"type\":\"string\"," +
    "\"format\":\"uri-reference\"},\"refreshUrl\":{\"type\":\"string\",\"format\":\"uri-reference\"}," +
    "\"scopes\":{\"type\":\"object\",\"additionalProperties\":{\"type\":\"string\"}}}," +
    "\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false},\"PasswordOAuthFlow\":{\"type\":\"object\"," +
    "\"required\":[\"tokenUrl\"],\"properties\":{\"tokenUrl\":{\"type\":\"string\",\"format\":\"uri-reference\"}," +
    "\"refreshUrl\":{\"type\":\"string\",\"format\":\"uri-reference\"},\"scopes\":{\"type\":\"object\"," +
    "\"additionalProperties\":{\"type\":\"string\"}}},\"patternProperties\":{\"^x-\":{}}," +
    "\"additionalProperties\":false},\"ClientCredentialsFlow\":{\"type\":\"object\",\"required\":[\"tokenUrl\"]," +
    "\"properties\":{\"tokenUrl\":{\"type\":\"string\",\"format\":\"uri-reference\"}," +
    "\"refreshUrl\":{\"type\":\"string\",\"format\":\"uri-reference\"},\"scopes\":{\"type\":\"object\"," +
    "\"additionalProperties\":{\"type\":\"string\"}}},\"patternProperties\":{\"^x-\":{}}," +
    "\"additionalProperties\":false},\"AuthorizationCodeOAuthFlow\":{\"type\":\"object\"," +
    "\"required\":[\"authorizationUrl\",\"tokenUrl\"],\"properties\":{\"authorizationUrl\":{\"type\":\"string\"," +
    "\"format\":\"uri-reference\"},\"tokenUrl\":{\"type\":\"string\",\"format\":\"uri-reference\"}," +
    "\"refreshUrl\":{\"type\":\"string\",\"format\":\"uri-reference\"},\"scopes\":{\"type\":\"object\"," +
    "\"additionalProperties\":{\"type\":\"string\"}}},\"patternProperties\":{\"^x-\":{}}," +
    "\"additionalProperties\":false},\"Link\":{\"type\":\"object\"," +
    "\"properties\":{\"operationId\":{\"type\":\"string\"},\"operationRef\":{\"type\":\"string\"," +
    "\"format\":\"uri-reference\"},\"parameters\":{\"type\":\"object\",\"additionalProperties\":{}}," +
    "\"requestBody\":{},\"description\":{\"type\":\"string\"},\"server\":{\"$ref\":\"#/definitions/Server\"}}," +
    "\"patternProperties\":{\"^x-\":{}},\"additionalProperties\":false,\"not\":{\"description\":\"Operation Id and " +
    "Operation Ref are mutually exclusive\",\"required\":[\"operationId\",\"operationRef\"]}}," +
    "\"Callback\":{\"type\":\"object\",\"additionalProperties\":{\"$ref\":\"#/definitions/PathItem\"}," +
    "\"patternProperties\":{\"^x-\":{}}},\"Encoding\":{\"type\":\"object\"," +
    "\"properties\":{\"contentType\":{\"type\":\"string\"},\"headers\":{\"type\":\"object\"," +
    "\"additionalProperties\":{\"$ref\":\"#/definitions/Header\"}},\"style\":{\"type\":\"string\",\"enum\":[\"form\"," +
    "\"spaceDelimited\",\"pipeDelimited\",\"deepObject\"]},\"explode\":{\"type\":\"boolean\"}," +
    "\"allowReserved\":{\"type\":\"boolean\",\"default\":false}},\"additionalProperties\":false}}}";

}
