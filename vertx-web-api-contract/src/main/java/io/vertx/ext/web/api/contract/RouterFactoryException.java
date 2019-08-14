package io.vertx.ext.web.api.contract;

import io.vertx.codegen.annotations.VertxGen;

/**
 * Main class for router factory exceptions
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
public class RouterFactoryException extends RuntimeException {

  @VertxGen
  public enum ErrorType {
    /**
     * You are trying to mount an operation (combination of path and method) not defined in specification
     */
    PATH_NOT_FOUND,
    /**
     * You are trying to mount an operation with operation_id not defined in specification
     */
    OPERATION_ID_NOT_FOUND,
    /**
     * Specification is not valid
     */
    SPEC_INVALID,
    /**
     * Missing security handler during construction of router
     */
    MISSING_SECURITY_HANDLER,
    /**
     * You have provided a wrong directory/path to specification file
     */
    INVALID_SPEC_PATH,
    /**
     * You are trying to use two or more path parameters with a combination of parameters/name/styles/explode not supported
     */
    PATH_PARAMETERS_COMBINATION_NOT_SUPPORTED,
    /**
     * You specified an interface not annotated with io.vertx.ext.web.api.generator.WebApiProxyGen while calling {@link RouterFactory#mountServiceProxy(Class, String)}
     */
    WRONG_INTERFACE
  }

  private ErrorType type;

  public RouterFactoryException(String message, ErrorType type) {
    super(message);
    this.type = type;
  }

  public ErrorType type() {
    return type;
  }

  public static RouterFactoryException createPathNotFoundException(String pathName) {
    return new RouterFactoryException(pathName + " not found inside specification", ErrorType.PATH_NOT_FOUND);
  }

  public static RouterFactoryException createOperationIdNotFoundException(String operationId) {
    return new RouterFactoryException(operationId + " not found inside specification", ErrorType
      .OPERATION_ID_NOT_FOUND);
  }

  public static RouterFactoryException createSpecInvalidException(String message) {
    return new RouterFactoryException(message, ErrorType.SPEC_INVALID);
  }

  public static RouterFactoryException createSpecNotExistsException(String path) {
    return new RouterFactoryException("Wrong specification url/path: " + path, ErrorType.INVALID_SPEC_PATH);
  }

  public static RouterFactoryException createMissingSecurityHandler(String securitySchema) {
    return new RouterFactoryException("Missing handler for security requirement: " + securitySchema, ErrorType
      .MISSING_SECURITY_HANDLER);
  }

  public static RouterFactoryException createMissingSecurityHandler(String securitySchema, String securityScope) {
    return new RouterFactoryException("Missing handler for security requirement: " + securitySchema + ":" +
      securityScope, ErrorType.MISSING_SECURITY_HANDLER);
  }

  public static RouterFactoryException createWrongExtension(String message) {
    return new RouterFactoryException(message, ErrorType.SPEC_INVALID);
  }

  public static RouterFactoryException createWrongInterface(Class i) {
    return new RouterFactoryException("Interface " + i.getName() + " is not annotated with @WebApiServiceProxy", ErrorType.WRONG_INTERFACE);
  }

}
