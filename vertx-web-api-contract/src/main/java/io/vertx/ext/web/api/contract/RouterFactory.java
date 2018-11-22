package io.vertx.ext.web.api.contract;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.List;
import java.util.function.Function;

/**
 * Main interface for Design Driven Router factory
 * Author: Francesco Guardiani @slinkydeveloper
 */
@VertxGen(concrete = false)
public interface RouterFactory<Specification> {

  /**
   * Mount to paths that have to follow a security schema a security handler
   *
   * @param securitySchemaName
   * @param handler
   * @return
   */
  @Fluent
  RouterFactory addSecurityHandler(String securitySchemaName, Handler<RoutingContext> handler);

  /**
   * Set options of router factory. For more info {@link RouterFactoryOptions}
   *
   * @param options
   * @return
   */
  @Fluent
  RouterFactory setOptions(RouterFactoryOptions options);

  /**
   * Get options of router factory. For more info {@link RouterFactoryOptions}
   *
   * @return
   */
  RouterFactoryOptions getOptions();

  /**
   * Construct a new router based on spec. It will fail if you are trying to mount a spec with security schemes
   * without assigned handlers<br/>
   * <b>Note:</b> Router is constructed in this function, so it will be respected the path definition ordering.
   *
   * @return
   */
  Router getRouter();


  Handler<RoutingContext> getValidationFailureHandler();

  /**
   * Set default validation failure handler. You can enable/disable this feature from
   * {@link RouterFactoryOptions#setMountValidationFailureHandler(boolean)}
   *
   * @param validationFailureHandler
   * @return this object
   */
  @Fluent
  RouterFactory setValidationFailureHandler(Handler<RoutingContext> validationFailureHandler);

  /**
   * Set not implemented failure handler. It's called when you don't define an handler for a
   * specific operation. You can enable/disable this feature from
   * {@link RouterFactoryOptions#setMountNotImplementedHandler(boolean)}
   *
   * @param notImplementedFailureHandler
   * @return this object
   */
  @Fluent
  RouterFactory setNotImplementedFailureHandler(Handler<RoutingContext> notImplementedFailureHandler);

  /**
   * Supply your own BodyHandler if you would like to control body limit, uploads directory and deletion of uploaded files
   * @param bodyHandler
   * @return self
   */
  @Fluent
  RouterFactory setBodyHandler(BodyHandler bodyHandler);

  /**
   * Add global handler to be applied prior to {@link io.vertx.ext.web.Router} being generated. <br/>
   * Please note that you should not add a body handler inside that list. If you want to modify the body handler, please use {@link RouterFactory#setBodyHandler(BodyHandler)}
   *
   * @param globalHandler
   * @return this object
   */
  @Fluent
  RouterFactory addGlobalHandler(Handler<RoutingContext> globalHandler);

  /**
   * When set, this function is called while creating the payload of {@link io.vertx.ext.web.api.OperationRequest}
   * @param extraOperationContextPayloadMapper
   * @return
   */
  @Fluent
  RouterFactory setExtraOperationContextPayloadMapper(Function<RoutingContext, JsonObject> extraOperationContextPayloadMapper);
}
