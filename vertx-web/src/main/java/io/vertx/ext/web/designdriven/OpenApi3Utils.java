package io.vertx.ext.web.designdriven;

import com.reprezen.kaizen.oasparser.model3.Parameter;
import com.reprezen.kaizen.oasparser.model3.Schema;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenApi3Utils {

  public static boolean isParameterStyle(Parameter parameter, String style) {
    if (parameter.getStyle() != null)
      return parameter.getStyle().equals(style);
    else
      return false;
  }

  public static boolean isParameterArrayType(Parameter parameter) {
    if (parameter.getSchema() != null && parameter.getSchema().getType() != null)
      return parameter.getSchema().getType().equals("array");
    else
      return false;
  }

  public static boolean isParameterObjectType(Parameter parameter) {
    if (parameter.getSchema() != null && parameter.getSchema().getType() != null)
      return parameter.getSchema().getType().equals("object");
    else
      return false;
  }

  public static boolean isRequiredParam(Schema schema, String parameterName) {
    return (schema != null && schema.getRequiredFields() != null) ? schema.getRequiredFields().contains(parameterName) : false;
  }

  public static boolean isRequiredParam(Parameter param) {
    return (param != null) ? param.getRequired() : false;
  }

  public static String resolveStyle(Parameter param) {
    if (param.getStyle() != null)
      return null;
    else
      switch (param.getIn()) {
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

}
