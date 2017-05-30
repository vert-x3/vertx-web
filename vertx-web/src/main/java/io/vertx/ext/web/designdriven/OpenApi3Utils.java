package io.vertx.ext.web.designdriven;

import com.reprezen.kaizen.oasparser.model3.Parameter;

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

}
