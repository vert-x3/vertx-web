package io.vertx.ext.web.api.contract.openapi3.impl;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.RouterFactoryException;
import io.vertx.ext.web.api.contract.impl.BaseRouterFactory;
import io.vertx.ext.web.api.contract.impl.RouteToEBServiceHandler;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenAPI3RouterFactoryImpl extends BaseRouterFactory<OpenAPI> implements
  OpenAPI3RouterFactory {

  private final static String OPENAPI_EXTENSION = "x-vertx-event-bus";
  private final static String OPENAPI_EXTENSION_ADDRESS = "address";
  private final static String OPENAPI_EXTENSION_METHOD_NAME = "method";

  // This map is fullfilled when spec is loaded in memory
  Map<String, OperationValue> operations;

  SecurityHandlersStore securityHandlers;

  private class OperationValue {
    private HttpMethod method;
    private String path;
    private PathItem pathModel;
    private Operation operationModel;

    private List<Parameter> parameters;
    private List<String> tags;
    private List<Handler<RoutingContext>> userHandlers;
    private List<Handler<RoutingContext>> userFailureHandlers;

    private String ebServiceAddress;
    private String ebServiceMethodName;
    private JsonObject ebServiceDeliveryOptions;

    private OperationValue(HttpMethod method, String path, Operation operationModel, PathItem pathModel) {
      this.method = method;
      this.path = path;
      this.pathModel = pathModel;
      this.operationModel = operationModel;
      this.tags = operationModel.getTags();
      // Merge parameters
      List<Parameter> opParams = operationModel.getParameters()==null ? new ArrayList<>() : operationModel.getParameters();
      List<Parameter> parentParams = pathModel.getParameters() == null ? new ArrayList<>() : pathModel.getParameters();
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

    public PathItem getPathModel() {
      return pathModel;
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
      return userHandlers.size() != 0 || mustMountRouteToService();
    }

    public List<String> getTags() {
      return tags;
    }

    public boolean hasTag(String tag) { return tags != null && tags.contains(tag); }

    public void mountRouteToService(String address) {
      this.ebServiceAddress = address;
      this.ebServiceMethodName = OpenApi3Utils.sanitizeOperationId(operationModel.getOperationId());
    }

    public void mountRouteToService(String address, String methodName) {
      this.ebServiceAddress = address;
      this.ebServiceMethodName = OpenApi3Utils.sanitizeOperationId(methodName);
    }

    public void mountRouteToService(String address, String methodName, JsonObject deliveryOptions) {
      this.ebServiceAddress = address;
      this.ebServiceMethodName = OpenApi3Utils.sanitizeOperationId(methodName);
      this.ebServiceDeliveryOptions = deliveryOptions;
    }

    public boolean mustMountRouteToService() {
      return this.ebServiceAddress != null;
    }

    public String getEbServiceAddress() {
      return ebServiceAddress;
    }

    public String getEbServiceMethodName() {
      return ebServiceMethodName;
    }

    public JsonObject getEbServiceDeliveryOptions() {
      return ebServiceDeliveryOptions;
    }
  }

  public OpenAPI3RouterFactoryImpl(Vertx vertx, OpenAPI spec) {
    super(vertx, spec);
    this.operations = new LinkedHashMap<>();
    this.securityHandlers = new SecurityHandlersStore();

    /* --- Initialization of all arrays and maps --- */
    for (Map.Entry<String, ? extends PathItem> pathEntry : spec.getPaths().entrySet()) {
      for (Map.Entry<PathItem.HttpMethod, ? extends Operation> opEntry : pathEntry.getValue().readOperationsMap().entrySet()) {
        this.operations.put(opEntry.getValue().getOperationId(), new OperationValue(
          HttpMethod.valueOf(opEntry.getKey().name()),
          pathEntry.getKey(),
          opEntry.getValue(),
          pathEntry.getValue()
        ));
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
  public OpenAPI3RouterFactory mountServiceFromTag(String tag, String address) {
    for (Map.Entry<String, OperationValue> op : operations.entrySet()) {
      if (op.getValue().hasTag(tag))
        op.getValue().mountRouteToService(address);
    }
    return this;
  }

  @Override
  public OpenAPI3RouterFactory mountServiceInterface(Class interfaceClass, String address) {
    for (Method m : interfaceClass.getMethods()) {
      if (OpenApi3Utils.serviceProxyMethodIsCompatibleHandler(m)) {
        String methodName = m.getName();
        OperationValue op = Optional
          .ofNullable(this.operations.get(methodName))
          .orElseGet(() ->
            this.operations.entrySet().stream().filter(e -> OpenApi3Utils.sanitizeOperationId(e.getKey()).equals(methodName)).map(Map.Entry::getValue).findFirst().orElseGet(() -> null)
          );
        if (op != null) {
          op.mountRouteToService(address, methodName);
        }
      }
    }
    return this;
  }

  @Override
  public OpenAPI3RouterFactory mountOperationToEventBus(String operationId, String address) {
    OperationValue op = operations.get(operationId);
    if (op == null) throw RouterFactoryException.createOperationIdNotFoundException(operationId);
    op.mountRouteToService(address, operationId);
    return this;
  }

  @Override
  public OpenAPI3RouterFactory mountServicesFromExtensions() {
    for (Map.Entry<String, OperationValue> opEntry : operations.entrySet()) {
      OperationValue operation = opEntry.getValue();
      Object extensionVal = OpenApi3Utils.getAndMergeServiceExtension(OPENAPI_EXTENSION, OPENAPI_EXTENSION_ADDRESS, OPENAPI_EXTENSION_METHOD_NAME, operation.pathModel, operation.operationModel);

      if (extensionVal != null) {
        if (extensionVal instanceof String) {
          operation.mountRouteToService((String) extensionVal, opEntry.getKey());
        } else if (extensionVal instanceof Map) {
          JsonObject extensionMap = new JsonObject((Map<String, Object>) extensionVal);
          String address = extensionMap.getString(OPENAPI_EXTENSION_ADDRESS);
          String methodName = extensionMap.getString(OPENAPI_EXTENSION_METHOD_NAME);
          JsonObject sanitizedMap = OpenApi3Utils.sanitizeDeliveryOptionsExtension(extensionMap);
          if (address == null)
            throw RouterFactoryException.createWrongExtension("Extension " + OPENAPI_EXTENSION + " must define " + OPENAPI_EXTENSION_ADDRESS);
          if (methodName == null)
            operation.mountRouteToService(address, opEntry.getKey());
          else
            operation.mountRouteToService(address, methodName, sanitizedMap);
        } else {
          throw RouterFactoryException.createWrongExtension("Extension " + OPENAPI_EXTENSION + " must be or string or a JsonObject");
        }
      }
    }
    return this;
  }

  @Override
  public OpenAPI3RouterFactory addSecurityHandler(String securitySchemaName, Handler handler) {
    securityHandlers.addSecurityRequirement(securitySchemaName, handler);
    return this;
  }

  @Override
  public Router getRouter() {
    Router router = Router.router(vertx);
    Route globalRoute = router.route();
    globalRoute.handler(this.getBodyHandler());

    List<Handler<RoutingContext>> globalHandlers = this.getGlobalHandlers();
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
      if (this.options.isMountValidationFailureHandler()) failureHandlersToLoad.add(this.getValidationFailureHandler());

      // Check if path is set by user
      if (operation.isConfigured()) {
        handlersToLoad.addAll(operation.getUserHandlers());
        failureHandlersToLoad.addAll(operation.getUserFailureHandlers());
        if (operation.mustMountRouteToService()) {
          handlersToLoad.add(
            (operation.getEbServiceDeliveryOptions() != null) ? RouteToEBServiceHandler.build(
              vertx.eventBus(),
              operation.getEbServiceAddress(),
              operation.getEbServiceMethodName(),
              operation.getEbServiceDeliveryOptions(),
              this.getExtraOperationContextPayloadMapper()
            ) : RouteToEBServiceHandler.build(
              vertx.eventBus(),
              operation.getEbServiceAddress(),
              operation.getEbServiceMethodName(),
              this.getExtraOperationContextPayloadMapper()
            )
          );
        }
      } else {
        handlersToLoad.add(this.getNotImplementedFailureHandler());
      }

      // Now add all handlers to route
      OpenAPI3PathResolver pathResolver = new OpenAPI3PathResolver(operation.getPath(), operation.getParameters());
      Route route = router.routeWithRegex(operation.getMethod(), pathResolver.solve().toString());

      String exposeConfigurationKey = this.getOptions().getOperationModelKey();
      if (exposeConfigurationKey != null)
        route.handler(context -> context.put(exposeConfigurationKey, operation.getOperationModel()).next());

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
