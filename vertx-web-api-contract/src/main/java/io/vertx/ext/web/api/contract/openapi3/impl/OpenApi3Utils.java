package io.vertx.ext.web.api.contract.openapi3.impl;

import io.swagger.oas.models.media.ComposedSchema;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.parameters.Parameter;
import io.vertx.ext.web.api.validation.SpecFeatureNotSupportedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenApi3Utils {

  public static boolean isParameterArrayType(Parameter parameter) {
    if (parameter.getSchema() != null && parameter.getSchema().getType() != null)
      return parameter.getSchema().getType().equals("array");
    else return false;
  }

  public static boolean isParameterObjectOrAllOfType(Parameter parameter) {
    return isSchemaObjectOrAllOfType(parameter.getSchema());
  }

  public static boolean isSchemaObjectOrAllOfType(Schema schema) {
    return schema != null && (isAllOfSchema(schema) || "object".equals(schema.getType()));
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
    if(!(schema instanceof ComposedSchema)) return false;
    ComposedSchema composedSchema = (ComposedSchema) schema;
    return (composedSchema.getOneOf() != null && composedSchema.getOneOf().size() != 0);
  }

  public static boolean isAnyOfSchema(Schema schema) {
    if(!(schema instanceof ComposedSchema)) return false;
    ComposedSchema composedSchema = (ComposedSchema) schema;
    return (composedSchema.getAnyOf() != null && composedSchema.getAnyOf().size() != 0);
  }

  public static boolean isAllOfSchema(Schema schema) {
    if(!(schema instanceof ComposedSchema)) return false;
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
      for (int i = 0; i < parentParameters.size(); i++) {
        for (int j = 0; j < actualParams.size(); j++) {
          Parameter parentParam = parentParameters.get(i);
          Parameter actualParam = actualParams.get(j);
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
      for (Map.Entry<String, ? extends Schema> entry : ((Map<String, Schema>)schema.getProperties()).entrySet()) {
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
        for (Map.Entry<String, ? extends Schema> entry : ((Map<String, Schema>)schema.getProperties()).entrySet()) {
          properties.put(entry.getKey(), new OpenApi3Utils.ObjectField(entry.getValue(), entry.getKey(), schema));
        }
        return properties;
      }
    } else return null;
  }

}
