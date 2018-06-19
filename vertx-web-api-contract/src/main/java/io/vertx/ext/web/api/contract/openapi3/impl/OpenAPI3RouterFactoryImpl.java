package io.vertx.ext.web.api.contract.openapi3.impl;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.RouterFactoryException;
import io.vertx.ext.web.api.contract.impl.BaseRouterFactory;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;

import java.util.*;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenAPI3RouterFactoryImpl extends BaseRouterFactory<OpenAPI> implements
  OpenAPI3RouterFactory {

  // This map is fullfilled when spec is loaded in memory
  Map<String, OperationValue> operations;

  SecurityHandlersStore securityHandlers;

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
      List<Parameter> opParams = operationModel.getParameters()==null?new ArrayList<>():new ArrayList<>(operationModel.getParameters());
      List<Parameter> parentParams = parentParameters==null?new ArrayList<>():new ArrayList<>(parentParameters);
      this.parameters = OpenApi3Utils.mergeParameters(opParams, parentParams);
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

  public OpenAPI3RouterFactoryImpl(Vertx vertx, OpenAPI spec) {
    super(vertx, spec);
    this.operations = new LinkedHashMap<>();
    this.securityHandlers = new SecurityHandlersStore();

    /* --- Initialization of all arrays and maps --- */
    for (Map.Entry<String, ? extends PathItem> pathEntry : spec.getPaths().entrySet()) {
      for (Map.Entry<PathItem.HttpMethod, ? extends Operation> opEntry : pathEntry.getValue().readOperationsMap().entrySet()) {
        this.operations.put(opEntry.getValue().getOperationId(), new OperationValue(HttpMethod.valueOf(opEntry.getKey().name()), pathEntry.getKey(), opEntry.getValue(), pathEntry.getValue()
          .getParameters()));
      }
    }
  }

  @Override
  public OpenAPI3RouterFactory addSecuritySchemaScopeValidator(String securitySchemaName, String scopeName, Handler
    handler) {
    securityHandlers.addSecurityRequirement(securitySchemaName, scopeName, handler);
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
    securityHandlers.addSecurityRequirement(securitySchemaName, handler);
    return this;
  }

  private String resolveOperationId(HttpMethod method, String path) {
    // I assume the user give path in openapi style
    PathItem pathObject = this.spec.getPaths().get(path);
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
    Route globalRoute = router.route();
    globalRoute.handler(options.getBodyHandler());

    List<Handler<RoutingContext>> globalHandlers = this.getOptions().getGlobalHandlers();
    for (Handler<RoutingContext> globalHandler: globalHandlers) {
      globalRoute.handler(globalHandler);
    }

    List<Handler<RoutingContext>> globalSecurityHandlers = securityHandlers
      .solveSecurityHandlers(spec.getSecurity(), this.getOptions().isRequireSecurityHandlers());
    for (OperationValue operation : operations.values()) {
      // If user don't want 501 handlers and the operation is not configured, skip it
      if (!options.isMountNotImplementedHandler() && !operation.isConfigured())
        continue;

      List<Handler> handlersToLoad = new ArrayList<>();
      List<Handler> failureHandlersToLoad = new ArrayList<>();

      // Resolve security handlers
      // As https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#fixed-fields-8 says:
      // Operation specific security requirement overrides global security requirement, even if local security requirement is an empty array
      if (operation.getOperationModel().getSecurity() != null) {
        handlersToLoad.addAll(securityHandlers.solveSecurityHandlers(
          operation.getOperationModel().getSecurity(),
          this.getOptions().isRequireSecurityHandlers()
        ));
      } else {
        handlersToLoad.addAll(globalSecurityHandlers);
      }

      // Generate ValidationHandler
      Handler<RoutingContext> validationHandler = new OpenAPI3RequestValidationHandlerImpl(operation
        .getOperationModel(), operation.getParameters(), this.spec);
      handlersToLoad.add(validationHandler);

      // Check validation failure handler
      if (this.options.isMountValidationFailureHandler()) failureHandlersToLoad.add(this.options.getValidationFailureHandler());

      // Check if path is set by user
      if (operation.isConfigured()) {
        handlersToLoad.addAll(operation.getUserHandlers());
        failureHandlersToLoad.addAll(operation.getUserFailureHandlers());
      } else {
        handlersToLoad.add(this.options.getNotImplementedFailureHandler());
      }

      // Now add all handlers to route
      OpenAPI3PathResolver pathResolver = new OpenAPI3PathResolver(operation.getPath(), operation.getParameters());
      Route route = router.routeWithRegex(operation.getMethod(), pathResolver.solve().toString());

      // Set produces/consumes
      Set<String> consumes = new HashSet<>();
      Set<String> produces = new HashSet<>();
      if (operation.getOperationModel().getRequestBody() != null &&
        operation.getOperationModel().getRequestBody().getContent() != null)
        consumes.addAll(operation.getOperationModel().getRequestBody().getContent().keySet());

      if (operation.getOperationModel().getResponses() != null)
        for (ApiResponse response : operation.getOperationModel().getResponses().values())
          if (response.getContent() != null)
            produces.addAll(response.getContent().keySet());

      for (String ct : consumes)
        route.consumes(ct);

      for (String ct : produces)
        route.produces(ct);

      if (options.isMountResponseContentTypeHandler() && produces.size() != 0)
        route.handler(ResponseContentTypeHandler.create());

      route.setRegexGroupsNames(new ArrayList<>(pathResolver.getMappedGroups().values()));
      for (Handler handler : handlersToLoad)
        route.handler(handler);
      for (Handler failureHandler : failureHandlersToLoad)
        route.failureHandler(failureHandler);
    }
    return router;
  }

}
