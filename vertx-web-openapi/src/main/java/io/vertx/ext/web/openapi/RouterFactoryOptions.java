package io.vertx.ext.web.openapi;

import java.util.function.Function;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
@DataObject(generateConverter = true, publicConverter = false)
public class RouterFactoryOptions {

  /**
   * By default, RouterFactory mounts Not Implemented/Method Not Allowed handler
   */
  public final static boolean DEFAULT_MOUNT_NOT_IMPLEMENTED_HANDLER = true;

  /**
   * By default, RouterFactory requires security handlers
   * to be defined while calling getRouter() or it will throw an Exception
   */
  public final static boolean DEFAULT_REQUIRE_SECURITY_HANDLERS = true;

  /**
   * By default, RouterFactory will mount ResponseContentTypeHandler when required
   */
  public final static boolean DEFAULT_MOUNT_RESPONSE_CONTENT_TYPE_HANDLER = true;

  /**
   * By default, RouterFactory will not expose operation configuration in the the routing context
   */
  public final static String DEFAULT_OPERATION_MODEL_KEY = null;

  /**
   * By default, RouterFactory will name routes by open api path.
   */
  public final static RouteNamingStrategy DEFAULT_ROUTE_NAMING_STRATEGY = RouteNamingStrategy.OPERATION_OPENAPI_PATH;

  private boolean mountNotImplementedHandler;
  private boolean requireSecurityHandlers;
  private boolean mountResponseContentTypeHandler;
  private String operationModelKey;
  private RouteNamingStrategy routeNamingStrategy;

  public RouterFactoryOptions() {
    init();
  }

  public RouterFactoryOptions(JsonObject json) {
    init();
    RouterFactoryOptionsConverter.fromJson(json, this);
  }

  public RouterFactoryOptions(RouterFactoryOptions other) {
    this.mountNotImplementedHandler = other.isMountNotImplementedHandler();
    this.requireSecurityHandlers = other.isRequireSecurityHandlers();
    this.mountResponseContentTypeHandler = other.isMountResponseContentTypeHandler();
    this.operationModelKey = other.getOperationModelKey();
    this.routeNamingStrategy = other.getRouteNamingStrategy();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    RouterFactoryOptionsConverter.toJson(this, json);
    return json;
  }

  private void init() {
    this.mountNotImplementedHandler = DEFAULT_MOUNT_NOT_IMPLEMENTED_HANDLER;
    this.requireSecurityHandlers = DEFAULT_REQUIRE_SECURITY_HANDLERS;
    this.mountResponseContentTypeHandler = DEFAULT_MOUNT_RESPONSE_CONTENT_TYPE_HANDLER;
    this.operationModelKey = DEFAULT_OPERATION_MODEL_KEY;
    this.routeNamingStrategy = DEFAULT_ROUTE_NAMING_STRATEGY;
  }

  public boolean isMountNotImplementedHandler() {
    return mountNotImplementedHandler;
  }

  /**
   * If true, Router Factory will automatically mount an handler that return HTTP 405/501 status code for each operation where you didn't specify an handler.
   * You can customize the response with {@link io.vertx.ext.web.Router#errorHandler(int, Handler)}
   *
   * @param mountOperationsWithoutHandler
   * @return this object
   */
  @Fluent
  public RouterFactoryOptions setMountNotImplementedHandler(boolean mountOperationsWithoutHandler) {
    this.mountNotImplementedHandler = mountOperationsWithoutHandler;
    return this;
  }

  public boolean isRequireSecurityHandlers() {
    return requireSecurityHandlers;
  }

  /**
   * If true, when you call {@link RouterFactory#createRouter()} ()} the factory will mount for every path
   * the required security handlers and, if a security handler is not defined, it throws an {@link RouterFactoryException}
   *
   * @param requireSecurityHandlers
   * @return this object
   */
  @Fluent
  public RouterFactoryOptions setRequireSecurityHandlers(boolean requireSecurityHandlers) {
    this.requireSecurityHandlers = requireSecurityHandlers;
    return this;
  }

  public boolean isMountResponseContentTypeHandler() {
    return mountResponseContentTypeHandler;
  }

  /**
   * If true, when required, the factory will mount a {@link io.vertx.ext.web.handler.ResponseContentTypeHandler}
   * @param mountResponseContentTypeHandler
   * @return
   */
  @Fluent
  public RouterFactoryOptions setMountResponseContentTypeHandler(boolean mountResponseContentTypeHandler) {
    this.mountResponseContentTypeHandler = mountResponseContentTypeHandler;
    return this;
  }

  public String getOperationModelKey() {
    return operationModelKey;
  }

  /**
   * When set, an additional handler will be created to expose the operation model in the routing
   * context under the given key. When the key is null, the handler is not added.
   * @param operationModelKey
   * @return
   */
  @Fluent
  public RouterFactoryOptions setOperationModelKey(String operationModelKey) {
    this.operationModelKey = operationModelKey;
    return this;
  }

  public RouteNamingStrategy getRouteNamingStrategy() {
    return routeNamingStrategy;
  }

  /**
   * The strategy to follow when naming the generated routes.
   * @param routeNamingStrategy
   * @return this object
   */
  @Fluent
  public RouterFactoryOptions setRouteNamingStrategy(RouteNamingStrategy routeNamingStrategy) {
    this.routeNamingStrategy = routeNamingStrategy;
    return this;
  }

  /**
   * The way an OpenAPI operation is transformed into a route name.
   */
  @VertxGen
  public enum RouteNamingStrategy implements Function<Operation, String> {
    OPERATION_ID(Operation::getOperationId),
    OPERATION_OPENAPI_PATH(Operation::getOpenAPIPath);

    private final Function<Operation, String> impl;

    RouteNamingStrategy(Function<Operation, String> impl) {
      this.impl = impl;
    }

    @Override
    public String apply(Operation operation) {
      return impl.apply(operation);
    }
  }
}
