package io.vertx.ext.web.openapi;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;

/**
 * Main class for router builder exceptions
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
public class RouterBuilderException extends RuntimeException {

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
     * You specified an interface not annotated with {@link io.vertx.ext.web.api.service.WebApiServiceGen} while
     * calling {@link RouterBuilder#mountServiceInterface(Class, String)}
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

  public RouterBuilderException(String message, ErrorType type, Throwable cause) {
    super(message, cause);
    this.type = type;
  }

  public ErrorType type() {
    return type;
  }

  public static RouterBuilderException cannotFindParameterProcessorGenerator(JsonPointer pointer,
                                                                             JsonObject parameter) {
    return new RouterBuilderException(
      String.format("Cannot find a ParameterProcessorGenerator for %s: %s", pointer.toString(), parameter.encode()),
      ErrorType.UNSUPPORTED_SPEC,
      null
    );
  }

  public static RouterBuilderException createBodyNotSupported(JsonPointer pointer) {
    return new RouterBuilderException(
      String.format("Cannot find a BodyProcessorGenerator for %s", pointer.toString()),
      ErrorType.UNSUPPORTED_SPEC,
      null
    );
  }

  public static RouterBuilderException createInvalidSpecException(Throwable cause) {
    return new RouterBuilderException("Spec is invalid", ErrorType.INVALID_SPEC, cause);
  }

  public static RouterBuilderException createInvalidFileSpec(String path, Throwable cause) {
    return new RouterBuilderException("Cannot load the spec in path " + path, ErrorType.INVALID_FILE, cause);
  }

  public static RouterBuilderException createMissingSecurityHandler(String securitySchema) {
    return new RouterBuilderException("Missing handler for security requirement: " + securitySchema, ErrorType
      .MISSING_SECURITY_HANDLER, null);
  }

  public static RouterBuilderException createMissingSecurityHandler(String securitySchema, String securityScope) {
    return new RouterBuilderException("Missing handler for security requirement: " + securitySchema + ":" +
      securityScope, ErrorType.MISSING_SECURITY_HANDLER, null);
  }

  public static RouterBuilderException createUnsupportedSpecFeature(String message) {
    return new RouterBuilderException(message, ErrorType.UNSUPPORTED_SPEC, null);
  }

  public static RouterBuilderException createWrongExtension(String message) {
    return new RouterBuilderException(message, ErrorType.WRONG_SERVICE_EXTENSION, null);
  }

  public static RouterBuilderException createRouterBuilderInstantiationError(Throwable e, String url) {
    return new RouterBuilderException("Cannot instantiate Router builder for openapi " + url,
      ErrorType.UNSUPPORTED_SPEC, e);
  }

  public static RouterBuilderException createErrorWhileGeneratingValidationHandler(String message, Throwable cause) {
    return new RouterBuilderException(message, ErrorType.CANNOT_GENERATE_VALIDATION_HANDLER, cause);
  }

}
