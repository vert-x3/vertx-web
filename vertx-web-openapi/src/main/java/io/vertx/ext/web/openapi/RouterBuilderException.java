package io.vertx.ext.web.openapi;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;

/**
 * Main class for router builder exceptions
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
public class RouterBuilderException extends RuntimeException {

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

  public static RouterBuilderException createInvalidSpec(String message, Throwable cause) {
    return new RouterBuilderException(message, ErrorType.INVALID_SPEC, cause);
  }

  public static RouterBuilderException createInvalidSpec(Throwable cause) {
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
