package io.vertx.ext.web.designdriven.openapi3.impl;

import com.reprezen.kaizen.oasparser.model3.*;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.designdriven.RouterFactoryException;
import io.vertx.ext.web.designdriven.impl.BaseDesignDrivenRouterFactory;
import io.vertx.ext.web.designdriven.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.impl.RouteImpl;

import java.util.*;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenAPI3RouterFactoryImpl extends BaseDesignDrivenRouterFactory<OpenApi3> implements
  OpenAPI3RouterFactory {

  private final Handler<RoutingContext> NOT_IMPLEMENTED_HANDLER = (routingContext) -> {
    routingContext.response().setStatusCode(501).setStatusMessage("Not Implemented").end();
  };

  // This map is fullfilled when spec is loaded in memory
  Map<String, OperationValue> operations;

  Map<SecurityRequirementKey, Handler> securityHandlers;

  private class SecurityRequirementKey {
    private String name;
    private String oauth2Scope;

    public SecurityRequirementKey(String name, String oauth2Scope) {
      this.name = name;
      this.oauth2Scope = oauth2Scope;
    }

    public SecurityRequirementKey(String name) {
      this(name, null);
    }

    public String getName() {
      return name;
    }

    public String getOauth2Scope() {
      return oauth2Scope;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      SecurityRequirementKey that = (SecurityRequirementKey) o;

      if (!name.equals(that.name)) return false;
      return oauth2Scope != null ? oauth2Scope.equals(that.oauth2Scope) : that.oauth2Scope == null;
    }

    @Override
    public int hashCode() {
      int result = name.hashCode();
      result = 31 * result + (oauth2Scope != null ? oauth2Scope.hashCode() : 0);
      return result;
    }
  }

  private class Handlers {
    private List<Handler> handlers;
    private List<Handler> failureHandlers;

    public Handlers(List<Handler> handlers, List<Handler> failureHandlers) {
      this.handlers = handlers;
      this.failureHandlers = failureHandlers;
    }

    public List<Handler> getHandlers() {
      return handlers;
    }

    public List<Handler> getFailureHandlers() {
      return failureHandlers;
    }
  }

  private class OperationValue {
    private HttpMethod method;
    private String path;
    private Operation operationModel;

    private List<Parameter> parameters;

    private List<Handler<RoutingContext>> userHandlers;
    private List<Handler<RoutingContext>> userFailureHandlers;

    private OperationValue(HttpMethod method, String path, Operation operationModel, Collection<? extends
      Parameter> parentParameters) {
      this.method = method;
      this.path = path;
      this.operationModel = operationModel;
      // Merge parameters
      this.parameters = OpenApi3Utils.mergeParameters(new ArrayList<Parameter>(operationModel.getParameters()), new ArrayList<Parameter>(parentParameters));
      this.userHandlers = new ArrayList<>();
      this.userFailureHandlers = new ArrayList<>();
    }

    public HttpMethod getMethod() {
      return method;
    }

    public Operation getOperationModel() {
      return operationModel;
    }

    public List<Parameter> getParameters() {
      return parameters;
    }

    public String getPath() {
      return path;
    }

    public void addUserHandler(Handler<RoutingContext> userHandler) {
      this.userHandlers.add(userHandler);
    }

    public void addUserFailureHandler(Handler<RoutingContext> userFailureHandler) {
      this.userFailureHandlers.add(userFailureHandler);
    }

    public List<Handler<RoutingContext>> getUserHandlers() {
      return userHandlers;
    }

    public List<Handler<RoutingContext>> getUserFailureHandlers() {
      return userFailureHandlers;
    }

    public boolean isConfigured() {
      return userHandlers.size() != 0;
    }
  }

  public OpenAPI3RouterFactoryImpl(Vertx vertx, OpenApi3 spec) {
    super(vertx, spec);
    this.operations = new HashMap<>();
    this.securityHandlers = new HashMap<>();

    /* --- Initialization of all arrays and maps --- */
    for (Map.Entry<String, ? extends Path> pathEntry : spec.getPaths().entrySet()) {
      for (Map.Entry<String, ? extends Operation> opEntry : pathEntry.getValue().getOperations().entrySet()) {
        this.operations.put(opEntry.getValue().getOperationId(), new OperationValue(OpenApi3Utils
          .searchEnum(HttpMethod.class, opEntry.getKey()), pathEntry.getKey(), opEntry.getValue(), pathEntry.getValue()
          .getParameters()));
      }
    }
  }

  @Override
  public OpenAPI3RouterFactory addSecuritySchemaScopeValidator(String securitySchemaName, String scopeName, Handler
    handler) {
    SecurityRequirementKey key = new SecurityRequirementKey(securitySchemaName, scopeName);
    securityHandlers.put(key, handler);
    return this;
  }

  @Override
  public OpenAPI3RouterFactory addHandlerByOperationId(String operationId, Handler<RoutingContext> handler) {
    if (handler != null) {
      OperationValue op = operations.get(operationId);
      if (op == null) throw RouterFactoryException.createOperationIdNotFoundException(operationId);
      op.addUserHandler(handler);
    }
    return this;
  }

  @Override
  public OpenAPI3RouterFactory addFailureHandlerByOperationId(String operationId, Handler<RoutingContext> failureHandler) {
    if (failureHandler != null) {
      OperationValue op = operations.get(operationId);
      if (op == null) throw RouterFactoryException.createOperationIdNotFoundException(operationId);
      op.addUserFailureHandler(failureHandler);
    }
    return this;
  }

  @Override
  public OpenAPI3RouterFactory addSecurityHandler(String securitySchemaName, Handler handler) {
    SecurityRequirementKey key = new SecurityRequirementKey(securitySchemaName);
    securityHandlers.put(key, handler);
    return this;
  }

  private String resolveOperationId(HttpMethod method, String path) {
    // I assume the user give path in openapi style
    Path pathObject = this.spec.getPath(path);
    if (pathObject == null) {
      throw RouterFactoryException.createPathNotFoundException(path);
    }
    Operation operation;
    switch (method) {
      case GET:
        operation = pathObject.getGet();
        break;
      case PUT:
        operation = pathObject.getPut();
        break;
      case HEAD:
        operation = pathObject.getHead();
        break;
      case DELETE:
        operation = pathObject.getDelete();
        break;
      case PATCH:
        operation = pathObject.getPatch();
        break;
      case POST:
        operation = pathObject.getPost();
        break;
      case OPTIONS:
        operation = pathObject.getOptions();
        break;
      case TRACE:
        operation = pathObject.getTrace();
        break;
      case OTHER:
      case CONNECT:
      default:
        throw RouterFactoryException.createPathNotFoundException(path);
    }
    return operation.getOperationId();
  }

  @Override
  public OpenAPI3RouterFactory addHandler(HttpMethod method, String path, Handler handler) {
    addHandlerByOperationId(resolveOperationId(method, path), handler);
    return this;
  }

  @Override
  public OpenAPI3RouterFactory addFailureHandler(HttpMethod method, String path, Handler failureHandler) {
    addFailureHandlerByOperationId(resolveOperationId(method, path), failureHandler);
    return this;
  }

  @Override
  public Router getRouter() {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    for (OperationValue operation : operations.values()) {
      // If user don't want 501 handlers and the operation is not configured, skip it
      if (!mount501handlers && !operation.isConfigured())
        continue;

      List<Handler> handlersToLoad = new ArrayList<>();
      List<Handler> failureHandlersToLoad = new ArrayList<>();

      // Resolve security handlers
      if (operation.getOperationModel().hasSecurityRequirements()) {
        for (SecurityRequirement securityRequirement : operation.getOperationModel().getSecurityRequirements()) {
          for (Map.Entry<String, ? extends SecurityParameter> securityValue : securityRequirement.getRequirements()
            .entrySet()) {
            if (securityValue.getValue().getParameters() != null && securityValue.getValue().getParameters().size()
              != 0) {
              // It's a multiscope security requirement
              for (String scope : securityValue.getValue().getParameters()) {
                Handler securityHandlerToLoad = this.securityHandlers.get(new SecurityRequirementKey(securityValue
                  .getKey(), scope));
                if (securityHandlerToLoad == null) {
                  // Maybe there's only one security handler for all scopes of this security schema
                  securityHandlerToLoad = this.securityHandlers.get(new SecurityRequirementKey(securityValue.getKey()));
                  if (securityHandlerToLoad == null)
                    throw RouterFactoryException.createMissingSecurityHandler(securityValue.getKey(), scope);
                  else handlersToLoad.add(securityHandlerToLoad);
                } else handlersToLoad.add(securityHandlerToLoad);
              }
            } else {
              Handler securityHandlerToLoad = this.securityHandlers.get(new SecurityRequirementKey(securityValue
                .getKey()));
              if (securityHandlerToLoad == null)
                throw RouterFactoryException.createMissingSecurityHandler(securityValue.getKey());
              else handlersToLoad.add(securityHandlerToLoad);
            }
          }
        }
      }

      // Generate ValidationHandler
      Handler<RoutingContext> validationHandler = new OpenAPI3RequestValidationHandlerImpl(operation
        .getOperationModel(), operation.getParameters());
      handlersToLoad.add(validationHandler);

      // Check validation failure handler
      if (this.enableValidationFailureHandler) failureHandlersToLoad.add(this.failureHandler);

      // Check if path is set by user
      if (operation.isConfigured()) {
        handlersToLoad.addAll(operation.getUserHandlers());
        handlersToLoad.addAll(operation.getUserFailureHandlers());
      } else {
        handlersToLoad.add(this.NOT_IMPLEMENTED_HANDLER);
      }

      // Now add all handlers to router
      OpenAPI3PathResolver pathResolver = new OpenAPI3PathResolver(operation.getPath(), operation.getParameters());
      Route route = router.routeWithRegex(operation.getMethod(), pathResolver.solve().toString());
      route.setRegexGroupsNames(new ArrayList<>(pathResolver.getMappedGroups().values()));
      for (Handler handler : handlersToLoad)
        route.handler(handler);
      for (Handler failureHandler : failureHandlersToLoad)
        route.failureHandler(failureHandler);
    }
    return router;
  }

}
