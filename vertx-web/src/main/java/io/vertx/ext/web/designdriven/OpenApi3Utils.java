package io.vertx.ext.web.designdriven;

import com.reprezen.kaizen.oasparser.model3.Parameter;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenApi3Utils {

  public static boolean isParameterStyle(Parameter parameter, String style) {
    return parameter.getStyle().equals(style);
  }

  public static boolean isParameterArrayType(Parameter parameter) {
    return parameter.getSchema().getType().equals("array");
  }

  public static boolean isParameterObjectType(Parameter parameter) {
    return parameter.getSchema().getType().equals("object");
  }

}
