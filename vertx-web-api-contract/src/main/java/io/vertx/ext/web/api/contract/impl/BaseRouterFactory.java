package io.vertx.ext.web.api.contract.impl;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.RouterFactory;
import io.vertx.ext.web.api.contract.RouterFactoryException;
import io.vertx.ext.web.api.contract.RouterFactoryOptions;
import io.vertx.ext.web.api.validation.ValidationException;
import io.vertx.ext.web.handler.BodyHandler;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
abstract public class BaseRouterFactory<Specification> implements RouterFactory<Specification> {

  /**
   * Default validation failure handler. When ValidationException occurs, It sends a response
   * with status code 400, status message "Bad Request" and error message as body
   */
  public final static Handler<RoutingContext> DEFAULT_VALIDATION_FAILURE_HANDLER = (routingContext -> {
    if (routingContext.failure() instanceof ValidationException) {
      routingContext
        .response()
        .setStatusCode(400)
        .setStatusMessage("Bad Request")
        .end(routingContext.failure().getMessage());
    } else routingContext.next();
  });

  /**
   * Default not implemented handler. It sends a response with status code 501,
   * status message "Not Implemented" and empty body
   */
  public final static Handler<RoutingContext> DEFAULT_NOT_IMPLEMENTED_HANDLER = (routingContext) -> routingContext.response().setStatusCode(501).setStatusMessage("Not Implemented").end();

  /** Default extra payload mapper for {@link io.vertx.ext.web.api.OperationRequest}. By default, no mapper is specified
   *
   */
  public final static Function<RoutingContext, JsonObject> DEFAULT_EXTRA_OPERATION_CONTEXT_PAYLOAD_MAPPER = null;

  protected Vertx vertx;
  protected Specification spec;
  protected RouterFactoryOptions options;

  private Handler<RoutingContext> validationFailureHandler;
  private Handler<RoutingContext> notImplementedFailureHandler;
  private BodyHandler bodyHandler;
  private List<Handler<RoutingContext>> globalHandlers;
  private Function<RoutingContext, JsonObject> extraOperationContextPayloadMapper;

  public BaseRouterFactory(Vertx vertx, Specification spec, RouterFactoryOptions options) {
    this.vertx = vertx;
    this.spec = spec;
    this.options = options;

    this.validationFailureHandler = DEFAULT_VALIDATION_FAILURE_HANDLER;
    this.notImplementedFailureHandler = DEFAULT_NOT_IMPLEMENTED_HANDLER;
    this.bodyHandler = BodyHandler.create();
    this.globalHandlers = new ArrayList<>();
    this.extraOperationContextPayloadMapper = DEFAULT_EXTRA_OPERATION_CONTEXT_PAYLOAD_MAPPER;
  }

  public BaseRouterFactory(Vertx vertx, Specification spec) {
    this(vertx, spec, new RouterFactoryOptions());
  }

  @Override
  public RouterFactory setOptions(RouterFactoryOptions options) {
    this.options = options;
    return this;
  }

  @Override
  public RouterFactoryOptions getOptions() {
    return options;
  }

  @Override
  public Handler<RoutingContext> getValidationFailureHandler() {
    return validationFailureHandler;
  }

  @Override
  @Fluent
  public RouterFactory setValidationFailureHandler(Handler<RoutingContext> validationFailureHandler) {
    this.validationFailureHandler = validationFailureHandler;
    return this;
  }

  protected Handler<RoutingContext> getNotImplementedFailureHandler() {
    return notImplementedFailureHandler;
  }

  @Override
  @Fluent
  public RouterFactory setNotImplementedFailureHandler(Handler<RoutingContext> notImplementedFailureHandler) {
    this.notImplementedFailureHandler = notImplementedFailureHandler;
    return this;
  }

  protected BodyHandler getBodyHandler() {
    return bodyHandler;
  }

  @Override
  @Fluent
  public RouterFactory setBodyHandler(BodyHandler bodyHandler) {
    this.bodyHandler = bodyHandler;
    return this;
  }

  protected List<Handler<RoutingContext>> getGlobalHandlers() {
    return globalHandlers;
  }

  @Override
  @Fluent
  public RouterFactory addGlobalHandler(Handler<RoutingContext> globalHandler) {
    this.globalHandlers.add(globalHandler);
    return this;
  }

  protected Function<RoutingContext, JsonObject> getExtraOperationContextPayloadMapper() {
    return extraOperationContextPayloadMapper;
  }

  @Override
  @Fluent
  public RouterFactory setExtraOperationContextPayloadMapper(Function<RoutingContext, JsonObject> extraOperationContextPayloadMapper) {
    this.extraOperationContextPayloadMapper = extraOperationContextPayloadMapper;
    return this;
  }
}
