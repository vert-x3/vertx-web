package io.vertx.ext.web.openapi;

import io.vertx.codegen.annotations.VertxGen;

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
