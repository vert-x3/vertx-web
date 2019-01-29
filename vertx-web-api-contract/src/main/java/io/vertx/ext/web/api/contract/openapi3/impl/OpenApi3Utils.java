package io.vertx.ext.web.api.contract.openapi3.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.ObjectMapperFactory;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.contract.RouterFactoryException;
import io.vertx.ext.web.api.validation.SpecFeatureNotSupportedException;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenApi3Utils {

  public static ParseOptions getParseOptions() {
    ParseOptions options = new ParseOptions();
    options.setResolve(true);
    options.setResolveCombinators(false);
    options.setResolveFully(true);
    return options;
  }

  public static boolean isParameterArrayType(Parameter parameter) {
    if (parameter.getSchema() != null && parameter.getSchema().getType() != null)
      return parameter.getSchema().getType().equals("array");
    else return false;
  }

  public static boolean isParameterObjectOrAllOfType(Parameter parameter) {
    return isSchemaObjectOrAllOfType(parameter.getSchema());
  }

  public static boolean isSchemaObjectOrAllOfType(Schema schema) {
    return isSchemaObject(schema) || isAllOfSchema(schema);
  }

  public static boolean isSchemaObject(Schema schema) {
    return schema != null && ("object".equals(schema.getType()) || schema.getProperties() != null);
  }

  public static boolean isRequiredParam(Schema schema, String parameterName) {
    return schema != null && schema.getRequired() != null && schema.getRequired().contains(parameterName);
  }

  public static boolean isRequiredParam(Parameter param) {
    return (param != null) ? param.getRequired() : false;
  }

  public static String resolveStyle(Parameter param) {
    if (param.getStyle() != null) return param.getStyle().toString();
    else switch (param.getIn()) {
      case "query":
        return "form";
      case "path":
        return "simple";
      case "header":
        return "simple";
      case "cookie":
        return "form";
      default:
        return null;
    }
  }

  public static boolean isOneOfSchema(Schema schema) {
    if (!(schema instanceof ComposedSchema)) return false;
    ComposedSchema composedSchema = (ComposedSchema) schema;
    return (composedSchema.getOneOf() != null && composedSchema.getOneOf().size() != 0);
  }

  public static boolean isAnyOfSchema(Schema schema) {
    if (!(schema instanceof ComposedSchema)) return false;
    ComposedSchema composedSchema = (ComposedSchema) schema;
    return (composedSchema.getAnyOf() != null && composedSchema.getAnyOf().size() != 0);
  }

  public static boolean isAllOfSchema(Schema schema) {
    if (!(schema instanceof ComposedSchema)) return false;
    ComposedSchema composedSchema = (ComposedSchema) schema;
    return (composedSchema.getAllOf() != null && composedSchema.getAllOf().size() != 0);
  }

  public static boolean resolveAllowEmptyValue(Parameter parameter) {
    if (parameter.getAllowEmptyValue() != null) {
      // As OAS says: This is valid only for query parameters and allows sending a parameter with an empty value. Default value is false. If style is used, and if behavior is n/a (cannot be serialized), the value of allowEmptyValue SHALL be ignored
      if (!"form".equals(resolveStyle(parameter)))
        return false;
      else
        return parameter.getAllowEmptyValue();
    } else return false;
  }

  // Thank you StackOverflow :) https://stackoverflow
  // .com/questions/28332924/case-insensitive-matching-of-a-string-to-a-java-enum :)
  public static <T extends Enum<?>> T searchEnum(Class<T> enumeration, String search) {
    for (T each : enumeration.getEnumConstants()) {
      if (each.name().compareToIgnoreCase(search) == 0) {
        return each;
      }
    }
    return null;
  }

  public static String resolveContentTypeRegex(String listContentTypes) {
    // Check if it's list
    if (listContentTypes.contains(",")) {
      StringBuilder stringBuilder = new StringBuilder();
      String[] contentTypes = listContentTypes.split(",");
      for (String contentType : contentTypes)
        stringBuilder.append(Pattern.quote(contentType.trim()) + "|");
      stringBuilder.deleteCharAt(stringBuilder.length() - 1);
      return stringBuilder.toString();
    } else return Pattern.quote(listContentTypes);
  }

  public static List<Parameter> mergeParameters(List<Parameter> operationParameters, List<Parameter> parentParameters) {
    if (parentParameters == null && operationParameters == null) {
      return new ArrayList<>();
    } else if (operationParameters == null) {
      return new ArrayList<>(parentParameters);
    } else if (parentParameters == null) {
      return new ArrayList<>(operationParameters);
    } else {
      List<Parameter> result = new ArrayList<>(operationParameters);
      List<Parameter> actualParams = new ArrayList<>(operationParameters);
      for (Parameter parentParameter : parentParameters) {
        for (Parameter actualParam1 : actualParams) {
          Parameter parentParam = parentParameter;
          Parameter actualParam = actualParam1;
          if (!(parentParam.getIn().equalsIgnoreCase(actualParam.getIn()) && parentParam.getName().equals(actualParam
            .getName())))
            result.add(parentParam);
        }
      }
      return result;
    }
  }

  protected static class ObjectField {
    Schema schema;
    boolean required;

    public ObjectField(Schema schema, String name, Schema superSchema) {
      this.schema = schema;
      this.required = superSchema.getRequired() != null && superSchema.getRequired().contains(name);
    }

    public Schema getSchema() {
      return schema;
    }

    public boolean isRequired() {
      return required;
    }
  }

  /* This function resolve all properties inside an allOf array of schemas */
  public static Map<String, ObjectField> resolveAllOfArrays(List<Schema> allOfSchemas) {
    Map<String, ObjectField> properties = new HashMap<>();
    for (Schema schema : allOfSchemas) {
      if (schema.getType() != null && !schema.getType().equals("object"))
        throw new SpecFeatureNotSupportedException("allOf only allows inner object types");
      for (Map.Entry<String, ? extends Schema> entry : ((Map<String, Schema>) schema.getProperties()).entrySet()) {
        properties.put(entry.getKey(), new OpenApi3Utils.ObjectField(entry.getValue(), entry.getKey(), schema));
      }
    }
    return properties;
  }

  /* This function check if schema is an allOf array or an object and returns a map of properties */
  public static Map<String, ObjectField> solveObjectParameters(Schema schema) {
    if (OpenApi3Utils.isSchemaObjectOrAllOfType(schema)) {
      if (OpenApi3Utils.isAllOfSchema(schema)) {
        // allOf case
        ComposedSchema composedSchema = (ComposedSchema) schema;
        return resolveAllOfArrays(new ArrayList<>(composedSchema.getAllOf()));
      } else {
        // type object case
        Map<String, ObjectField> properties = new HashMap<>();
        if (schema.getProperties() == null) return new HashMap<>();
        for (Map.Entry<String, ? extends Schema> entry : ((Map<String, Schema>) schema.getProperties()).entrySet()) {
          properties.put(entry.getKey(), new OpenApi3Utils.ObjectField(entry.getValue(), entry.getKey(), schema));
        }
        return properties;
      }
    } else return null;
  }

  private final static Pattern COMPONENTS_REFS_MATCHER = Pattern.compile("^\\#\\/components\\/schemas\\/(.+)$");
  private final static String COMPONENTS_REFS_SUBSTITUTION = "\\#\\/definitions\\/$1";

  public static JsonNode generateSanitizedJsonSchemaNode(Schema s, OpenAPI oas) {
    ObjectNode node = ObjectMapperFactory.createJson().convertValue(s, ObjectNode.class);
    walkAndSolve(node, node, oas);
    return node;
  }

  private static void walkAndSolve(ObjectNode n, ObjectNode root, OpenAPI oas) {
    if (n.has("$ref")) {
      replaceRef(n, root, oas);
    } else if (n.has("allOf")) {
      for (JsonNode jsonNode : n.get("allOf")) {
        // We assert that parser validated allOf as array of objects
        walkAndSolve((ObjectNode) jsonNode, root, oas);
      }
    } else if (n.has("anyOf")) {
      for (JsonNode jsonNode : n.get("anyOf")) {
        walkAndSolve((ObjectNode) jsonNode, root, oas);
      }
    } else if (n.has("oneOf")) {
      for (JsonNode jsonNode : n.get("oneOf")) {
        walkAndSolve((ObjectNode) jsonNode, root, oas);
      }
    } else if (n.has("properties")) {
      ObjectNode properties = (ObjectNode) n.get("properties");
      Iterator<String> it = properties.fieldNames();
      while (it.hasNext()) {
        walkAndSolve((ObjectNode) properties.get(it.next()), root, oas);
      }
    } else if (n.has("items")) {
      walkAndSolve((ObjectNode) n.get("items"), root, oas);
    } else if (n.has("additionalProperties")) {
      JsonNode jsonNode = n.get("additionalProperties");
      if (jsonNode.getNodeType().equals(JsonNodeType.OBJECT)) {
        walkAndSolve((ObjectNode) n.get("additionalProperties"), root, oas);
      }
    }
  }

  private static void replaceRef(ObjectNode n, ObjectNode root, OpenAPI oas) {
    /**
     * If a ref is found, the structure of the schema is circular. The oas parser don't solve circular refs.
     * So I bundle the schema:
     * 1. I update the ref field with a #/definitions/schema_name uri
     * 2. If #/definitions/schema_name is empty, I solve it
     */
    String oldRef = n.get("$ref").asText();
    Matcher m = COMPONENTS_REFS_MATCHER.matcher(oldRef);
    if (m.lookingAt()) {
      String schemaName = m.group(1);
      String newRef = m.replaceAll(COMPONENTS_REFS_SUBSTITUTION);
      n.remove("$ref");
      n.put("$ref", newRef);
      if (!root.has("definitions") || !root.get("definitions").has(schemaName)) {
        Schema s = oas.getComponents().getSchemas().get(schemaName);
        ObjectNode schema = ObjectMapperFactory.createJson().convertValue(s, ObjectNode.class);
        // We need to search inside for other refs
        if (!root.has("definitions")) {
          ObjectNode definitions = root.putObject("definitions");
          definitions.set(schemaName, schema);
        } else {
          ((ObjectNode)root.get("definitions")).set(schemaName, schema);
        }
        walkAndSolve(schema, root, oas);
      }
    } else throw new RuntimeException("Wrong ref! " + oldRef);
  }

  public static List<MediaType> extractTypesFromMediaTypesMap(Map<String, MediaType> types, Predicate<String> matchingFunction) {
    return types
      .entrySet().stream()
      .filter(e -> matchingFunction.test(e.getKey()))
      .map(Map.Entry::getValue).collect(Collectors.toList());
  }

  public final static List<Class> SERVICE_PROXY_METHOD_PARAMETERS = Arrays.asList(new Class[]{OperationRequest.class, Handler.class});

  public static boolean serviceProxyMethodIsCompatibleHandler(Method method) {
    java.lang.reflect.Parameter[] parameters = method.getParameters();
    if (parameters.length < 2) return false;
    if (!parameters[parameters.length - 1].getType().equals(Handler.class)) return false;
    if (!parameters[parameters.length - 2].getType().equals(OperationRequest.class)) return false;
    return true;
  }

  public static JsonObject sanitizeDeliveryOptionsExtension(JsonObject jsonObject) {
    JsonObject newObj = new JsonObject();
    if (jsonObject.containsKey("timeout")) newObj.put("timeout", jsonObject.getValue("timeout"));
    if (jsonObject.containsKey("headers")) newObj.put("headers", jsonObject.getValue("headers"));
    return newObj;
  }

  public static String sanitizeOperationId(String operationId) {
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < operationId.length(); i++) {
      char c = operationId.charAt(i);
      if (c == '-' || c == ' ' || c == '_') {
        try {
          while (c == '-' || c == ' ' || c == '_') {
            i++;
            c = operationId.charAt(i);
          }
          result.append(Character.toUpperCase(operationId.charAt(i)));
        } catch (StringIndexOutOfBoundsException e) {}
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }
  
  public static Object getAndMergeServiceExtension(String extensionKey, String addressKey, String methodKey, PathItem pathModel, Operation operationModel) {
    Object pathExtension = pathModel.getExtensions() != null ? pathModel.getExtensions().get(extensionKey) : null;
    Object operationExtension = operationModel.getExtensions() != null ? operationModel.getExtensions().get(extensionKey) : null;

    // Cases:
    // 1. both strings or path extension null: operation extension overrides all
    // 2. path extension map and operation extension string: path extension interpreted as delivery options and operation extension as address
    // 3. path extension string and operation extension map: path extension interpreted as address
    // 4. both maps: extension map overrides path map elements
    // 5. operation extension null: path extension overrides all

    if ((operationExtension instanceof String && pathExtension instanceof String) || pathExtension == null) return operationExtension;
    if (operationExtension instanceof String && pathExtension instanceof Map) {
      Map<String, Object> result = new HashMap<>();
      result.put(addressKey, operationExtension);
      Map<String, Object> pathExtensionMap = (Map<String, Object>) pathExtension;
      if (pathExtensionMap.containsKey(methodKey)) throw RouterFactoryException.createWrongExtension("Extension " + extensionKey + " in path declaration must not contain " + methodKey);
      pathExtensionMap.forEach(result::putIfAbsent);
      return result;
    }
    if (operationExtension instanceof Map && pathExtension instanceof String) {
      Map<String, Object> result = (Map<String, Object>) operationExtension;
      result.putIfAbsent(addressKey, pathExtension);
      return result;
    }
    if (operationExtension instanceof Map && pathExtension instanceof Map) {
      Map<String, Object> result = (Map<String, Object>) operationExtension;
      ((Map<String, Object>)pathExtension).forEach(result::putIfAbsent);
      return result;
    }
    if (operationExtension == null) return pathExtension;
    return null;
  }

}
