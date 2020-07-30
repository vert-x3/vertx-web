package io.vertx.ext.web.openapi;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;

/**
 * Main class for router factory exceptions
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
public class RouterFactoryException extends RuntimeException {

  @VertxGen
  public enum ErrorType {
    /**
     * You are trying to mount an operation with operation_id not defined in specification
     */
    OPERATION_ID_NOT_FOUND,
    /**
     * Error while loading contract. The path is wrong or the spec is an invalid json/yaml file
     */
    INVALID_FILE,
    /**
     * Provided file is not a valid OpenAPI contract
     */
    INVALID_SPEC,
    /**
     * Missing security handler during construction of router
     */
    MISSING_SECURITY_HANDLER,
    /**
     * You are trying to use a spec feature not supported by this package.
     * Most likely you you have defined in you contract
     * two or more path parameters with a combination of parameters/name/styles/explode not supported
     */
    UNSUPPORTED_SPEC,
    /**
     * You specified an interface not annotated with {@link io.vertx.ext.web.api.service.WebApiServiceGen} while calling {@link RouterFactory#mountServiceInterface(Class, String)}
     */
    WRONG_INTERFACE,
    /**
     * You specified a wrong service extension
     */
    WRONG_SERVICE_EXTENSION,
    /**
     * Error while generating the {@link io.vertx.ext.web.validation.ValidationHandler}
     */
    CANNOT_GENERATE_VALIDATION_HANDLER
  }

  private ErrorType type;

  public RouterFactoryException(String message, ErrorType type, Throwable cause) {
    super(message, cause);
    this.type = type;
  }

  public ErrorType type() {
    return type;
  }

//  public static RouterFactoryException createPathNotFoundException(String pathName) {
//    return new RouterFactoryException(pathName + " not found inside specification", ErrorType.PATH_NOT_FOUND);
//  }
//
//  public static RouterFactoryException createOperationIdNotFoundException(String operationId) {
//    return new RouterFactoryException(operationId + " not found inside specification", ErrorType
//      .OPERATION_ID_NOT_FOUND);
//  }

  public static RouterFactoryException cannotFindParameterProcessorGenerator(JsonPointer pointer, JsonObject parameter) {
    return new RouterFactoryException(
      String.format("Cannot find a ParameterProcessorGenerator for %s: %s", pointer.toString(), parameter.encode()),
      ErrorType.UNSUPPORTED_SPEC,
      null
    );
  }

  public static RouterFactoryException createBodyNotSupported(JsonPointer pointer) {
    return new RouterFactoryException(
      String.format("Cannot find a BodyProcessorGenerator for %s", pointer.toString()),
      ErrorType.UNSUPPORTED_SPEC,
      null
    );
  }

  public static RouterFactoryException createInvalidSpecException(Throwable cause) {
    return new RouterFactoryException("Spec is invalid", ErrorType.INVALID_SPEC, cause);
  }

  public static RouterFactoryException createInvalidFileSpec(String path, Throwable cause) {
    return new RouterFactoryException("Cannot load the spec in path " + path, ErrorType.INVALID_FILE, cause);
  }

  public static RouterFactoryException createMissingSecurityHandler(String securitySchema) {
    return new RouterFactoryException("Missing handler for security requirement: " + securitySchema, ErrorType
      .MISSING_SECURITY_HANDLER, null);
  }

  public static RouterFactoryException createMissingSecurityHandler(String securitySchema, String securityScope) {
    return new RouterFactoryException("Missing handler for security requirement: " + securitySchema + ":" +
      securityScope, ErrorType.MISSING_SECURITY_HANDLER, null);
  }

//  public static RouterFactoryException createWrongInterface(Class i) {
//    return new RouterFactoryException("Interface " + i.getName() + " is not annotated with @WebApiServiceProxy", ErrorType.WRONG_INTERFACE);
//  }

  public static RouterFactoryException createUnsupportedSpecFeature(String message) {
    return new RouterFactoryException(message, ErrorType.UNSUPPORTED_SPEC, null);
  }

  public static RouterFactoryException createWrongExtension(String message) {
    return new RouterFactoryException(message, ErrorType.WRONG_SERVICE_EXTENSION, null);
  }

  public static RouterFactoryException createRouterFactoryInstantiationError(Throwable e, String url) {
    return new RouterFactoryException("Cannot instantiate Router Factory for openapi " + url, ErrorType.UNSUPPORTED_SPEC, e);
  }

  public static RouterFactoryException createErrorWhileGeneratingValidationHandler(String message, Throwable cause) {
    return new RouterFactoryException(message, ErrorType.CANNOT_GENERATE_VALIDATION_HANDLER, cause);
  }

}
