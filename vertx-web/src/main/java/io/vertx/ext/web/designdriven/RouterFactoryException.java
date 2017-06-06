package io.vertx.ext.web.designdriven;

import io.vertx.codegen.annotations.VertxGen;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class RouterFactoryException extends RuntimeException {

  @VertxGen
  public enum ErrorType {
    PATH_NOT_FOUND,
    OPERATION_ID_NOT_FOUND,
    SPEC_INVALID,
    MISSING_SECURITY_HANDLER
  }

  private ErrorType type;

  public RouterFactoryException(String message, ErrorType type) {
    super(message);
    this.type = type;
  }

  public ErrorType getType() {
    return type;
  }

  public static RouterFactoryException createPathNotFoundException(String pathName) {
    return new RouterFactoryException(pathName + " not found inside specification", ErrorType.PATH_NOT_FOUND);
  }

  public static RouterFactoryException createOperationIdNotFoundException(String operationId) {
    return new RouterFactoryException(operationId + " not found inside specification", ErrorType.OPERATION_ID_NOT_FOUND);
  }

  public static RouterFactoryException createSpecInvalidException(String message) {
    return new RouterFactoryException(message, ErrorType.SPEC_INVALID);
  }

  public static RouterFactoryException createMissingSecurityHandler(String securitySchema) {
    return new RouterFactoryException("Missing handler for security requirement: " + securitySchema, ErrorType.MISSING_SECURITY_HANDLER);
  }

  public static RouterFactoryException createMissingSecurityHandler(String securitySchema, String securityScope) {
    return new RouterFactoryException("Missing handler for security requirement: " + securitySchema + ":" + securityScope, ErrorType.MISSING_SECURITY_HANDLER);
  }
}
