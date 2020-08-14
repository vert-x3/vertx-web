package io.vertx.ext.web.openapi.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.SchemaRouter;
import io.vertx.ext.json.schema.openapi3.OpenAPI3SchemaParser;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.service.RouteToEBServiceHandler;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.impl.RouteImpl;
import io.vertx.ext.web.openapi.*;
import io.vertx.ext.web.validation.impl.ValidationHandlerImpl;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenAPI3RouterFactoryImpl implements RouterFactory {

  private final static String OPENAPI_EXTENSION = "x-vertx-event-bus";
  private final static String OPENAPI_EXTENSION_ADDRESS = "address";
  private final static String OPENAPI_EXTENSION_METHOD_NAME = "method";

  private final static Handler<RoutingContext> NOT_IMPLEMENTED_HANDLER = rc -> rc.fail(501);

  private static Handler<RoutingContext> generateNotAllowedHandler(List<HttpMethod> allowedMethods) {
    return rc -> {
      rc.addHeadersEndHandler(v ->
          rc.response().headers().add("Allow", String.join(", ",
            allowedMethods.stream().map(HttpMethod::toString).collect(Collectors.toList())
          ))
        );
      rc.fail(405);
    };
  }

  private Vertx vertx;
  private OpenAPIHolder openapi;
  private RouterFactoryOptions options;
  private Map<String, OperationImpl> operations;
  private BodyHandler bodyHandler;
  private AuthenticationHandlersStore securityHandlers;
  private List<Handler<RoutingContext>> globalHandlers;
  private Function<RoutingContext, JsonObject> serviceExtraPayloadMapper;
  private SchemaRouter schemaRouter;
  private OpenAPI3SchemaParser schemaParser;
  private OpenAPI3ValidationHandlerGenerator validationHandlerGenerator;

  public OpenAPI3RouterFactoryImpl(Vertx vertx, OpenAPIHolderImpl spec, OpenAPILoaderOptions options) {
    this.vertx = vertx;
    this.openapi = spec;
    this.options = new RouterFactoryOptions();
    this.bodyHandler = BodyHandler.create();
    this.globalHandlers = new ArrayList<>();
    this.schemaRouter = SchemaRouter.create(vertx, options.toSchemaRouterOptions());
    this.schemaParser = OpenAPI3SchemaParser.create(schemaRouter);
    this.validationHandlerGenerator = new OpenAPI3ValidationHandlerGenerator(spec, schemaParser);

    spec.getAbsolutePaths().forEach((u, jo) -> schemaRouter.addJson(u, jo));

    // Load default generators
    this.validationHandlerGenerator
      .addParameterProcessorGenerator(new DeepObjectParameterProcessorGenerator())
      .addParameterProcessorGenerator(new ExplodedArrayParameterProcessorGenerator())
      .addParameterProcessorGenerator(new ExplodedMatrixArrayParameterProcessorGenerator())
      .addParameterProcessorGenerator(new ExplodedObjectParameterProcessorGenerator())
      .addParameterProcessorGenerator(new ExplodedSimpleObjectParameterProcessorGenerator())
      .addParameterProcessorGenerator(new JsonParameterProcessorGenerator())
      .addParameterProcessorGenerator(new DefaultParameterProcessorGenerator());

    this.validationHandlerGenerator
      .addBodyProcessorGenerator(new JsonBodyProcessorGenerator())
      .addBodyProcessorGenerator(new UrlEncodedFormBodyProcessorGenerator())
      .addBodyProcessorGenerator(new MultipartFormBodyProcessorGenerator());

    this.operations = new LinkedHashMap<>();
    this.securityHandlers = new AuthenticationHandlersStore();

    /* --- Initialization of operations --- */
    spec.solveIfNeeded(spec.getOpenAPI().getJsonObject("paths")).forEach(pathEntry -> {
      if (pathEntry.getKey().startsWith("x-")) return;
      JsonObject pathModel = spec.solveIfNeeded((JsonObject) pathEntry.getValue());
      Stream.of(
        "get", "put", "post", "delete", "options", "head", "patch", "trace"
      )
        .filter(pathModel::containsKey)
        .forEach(verb -> {
          JsonObject operationModel = spec.solveIfNeeded(pathModel.getJsonObject(verb));
          this.operations.put(
            operationModel.getString("operationId"),
            new OperationImpl(
              operationModel.getString("operationId"),
              HttpMethod.valueOf(verb.toUpperCase()),
              pathEntry.getKey(),
              operationModel,
              pathModel,
              spec.getInitialScope(),
              openapi
            )
          );
        });
    });
  }

  @Override
  public RouterFactory setOptions(RouterFactoryOptions options) {
    Objects.requireNonNull(options);
    this.options = options;
    return this;
  }

  @Override
  public RouterFactoryOptions getOptions() {
    return options;
  }

  @Override
  public OpenAPIHolder getOpenAPI() {
    return openapi;
  }

  @Override
  public SchemaRouter getSchemaRouter() {
    return schemaRouter;
  }

  @Override
  public SchemaParser getSchemaParser() {
    return schemaParser;
  }

  @Override
  public RouterFactory serviceExtraPayloadMapper(Function<RoutingContext, JsonObject> serviceExtraPayloadMapper) {
    this.serviceExtraPayloadMapper = serviceExtraPayloadMapper;
    return this;
  }

  @Override
  public RouterFactory securityHandler(String securitySchemaName, AuthenticationHandler handler) {
    Objects.requireNonNull(securitySchemaName);
    Objects.requireNonNull(handler);
    securityHandlers.addAuthnRequirement(securitySchemaName, handler);
    return this;
  }

  @Override
  public List<Operation> operations() {
    return this.operations.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
  }

  @Override
  public Operation operation(String operationId) {
    Objects.requireNonNull(operationId);
    return this.operations.get(operationId);
  }

  @Override
  public RouterFactory bodyHandler(BodyHandler bodyHandler) {
    Objects.requireNonNull(bodyHandler);
    this.bodyHandler = bodyHandler;
    return this;
  }

  @Override
  public RouterFactory rootHandler(Handler<RoutingContext> rootHandler) {
    this.globalHandlers.add(rootHandler);
    return this;
  }

  @Override
  public RouterFactory mountServiceInterface(Class interfaceClass, String address) {
    for (Method m : interfaceClass.getMethods()) {
      if (OpenAPI3Utils.serviceProxyMethodIsCompatibleHandler(m)) {
        String methodName = m.getName();
        OperationImpl op = Optional
          .ofNullable(this.operations.get(methodName))
          .orElseGet(() ->
            this.operations.entrySet().stream().filter(e -> OpenAPI3Utils.sanitizeOperationId(e.getKey()).equals(methodName)).map(Map.Entry::getValue).findFirst().orElse(null)
          );
        if (op != null) {
          op.mountRouteToService(address, methodName);
        }
      }
    }
    return this;
  }

  @Override
  public RouterFactory mountServicesFromExtensions() {
    for (Map.Entry<String, OperationImpl> opEntry : operations.entrySet()) {
      OperationImpl operation = opEntry.getValue();
      Object extensionVal = OpenAPI3Utils.getAndMergeServiceExtension(OPENAPI_EXTENSION, OPENAPI_EXTENSION_ADDRESS, OPENAPI_EXTENSION_METHOD_NAME, operation.getPathModel(), operation.getOperationModel());

      if (extensionVal != null) {
        if (extensionVal instanceof String) {
          operation.mountRouteToService((String) extensionVal, opEntry.getKey());
        } else if (extensionVal instanceof JsonObject) {
          JsonObject extensionMap = (JsonObject) extensionVal;
          String address = extensionMap.getString(OPENAPI_EXTENSION_ADDRESS);
          String methodName = extensionMap.getString(OPENAPI_EXTENSION_METHOD_NAME);
          JsonObject sanitizedMap = OpenAPI3Utils.sanitizeDeliveryOptionsExtension(extensionMap);
          if (address == null)
            throw RouterFactoryException.createWrongExtension("Extension " + OPENAPI_EXTENSION + " must define " + OPENAPI_EXTENSION_ADDRESS); //TODO specify where
          if (methodName == null)
            operation.mountRouteToService(address, opEntry.getKey());
          else
            operation.mountRouteToService(address, methodName, new DeliveryOptions(sanitizedMap));
        } else {
          throw RouterFactoryException.createWrongExtension("Extension " + OPENAPI_EXTENSION + " must be or string or a JsonObject"); //TODO specify where
        }
      }
    }
    return this;
  }

  @Override
  public Router createRouter() {
    Router router = Router.router(vertx);
    Route globalRoute = router.route();
    globalRoute.handler(bodyHandler);
    globalHandlers.forEach(globalRoute::handler);

    for (OperationImpl operation : operations.values()) {
      // If user don't want 501 handlers and the operation is not configured, skip it
      if (!options.isMountNotImplementedHandler() && !operation.isConfigured())
        continue;

      List<Handler<RoutingContext>> handlersToLoad = new ArrayList<>();
      List<Handler<RoutingContext>> failureHandlersToLoad = new ArrayList<>();

      // Authentication Handler
      AuthenticationHandler authnHandler = this.securityHandlers.solveAuthenticationHandler(
        OpenAPI3Utils.mergeSecurityRequirements(
          this.openapi.getOpenAPI().getJsonArray("security"),
          operation.getOperationModel().getJsonArray("security")
        ),
        this.options.isRequireSecurityHandlers()
      );
      if (authnHandler != null) {
        handlersToLoad.add(authnHandler);
      }

      // Generate ValidationHandler
      ValidationHandlerImpl validationHandler = validationHandlerGenerator.create(operation);
      handlersToLoad.add(validationHandler);

      // Check if path is set by user
      if (operation.isConfigured()) {
        handlersToLoad.addAll(operation.getUserHandlers());
        failureHandlersToLoad.addAll(operation.getUserFailureHandlers());
        if (operation.mustMountRouteToService()) {
          RouteToEBServiceHandler routeToEBServiceHandler =
            (operation.getEbServiceDeliveryOptions() != null) ? RouteToEBServiceHandler.build(
              vertx.eventBus(),
              operation.getEbServiceAddress(),
              operation.getEbServiceMethodName(),
              operation.getEbServiceDeliveryOptions()
            ) : RouteToEBServiceHandler.build(
              vertx.eventBus(),
              operation.getEbServiceAddress(),
              operation.getEbServiceMethodName()
            );
          routeToEBServiceHandler.extraPayloadMapper(serviceExtraPayloadMapper);
          handlersToLoad.add(routeToEBServiceHandler);
        }
      } else {
        // Check if not implemented or method not allowed
        List<HttpMethod> configuredMethodsForThisPath = operations
          .values()
          .stream()
          .filter(ov -> operation.getOpenAPIPath().equals(ov.getOpenAPIPath()))
          .filter(OperationImpl::isConfigured)
          .map(OperationImpl::getHttpMethod)
          .collect(Collectors.toList());

        if (!configuredMethodsForThisPath.isEmpty())
          handlersToLoad.add(generateNotAllowedHandler(configuredMethodsForThisPath));
        else
          handlersToLoad.add(NOT_IMPLEMENTED_HANDLER);
      }

      // Now add all handlers to route
      OpenAPI3PathResolver pathResolver = new OpenAPI3PathResolver(operation.getOpenAPIPath(), new ArrayList<>(operation.getParameters().values()), openapi);
      Route route = pathResolver
        .solve() // If this optional is empty, this route doesn't need regex
        .map(solvedRegex -> router.routeWithRegex(operation.getHttpMethod(), solvedRegex.toString()))
        .orElseGet(() -> router.route(operation.getHttpMethod(), operation.getOpenAPIPath()))
        .setName(options.getRouteNamingStrategy().apply(operation));

      String exposeConfigurationKey = this.getOptions().getOperationModelKey();
      if (exposeConfigurationKey != null)
        route.handler(context -> context.put(exposeConfigurationKey, operation.getOperationModel()).next());

      // Set produces/consumes
      Set<String> consumes = ((JsonObject) JsonPointer.from("/requestBody/content")
        .queryJsonOrDefault(operation.getOperationModel(), new JsonObject()))
        .fieldNames();

      Set<String> produces = operation.getOperationModel()
        .getJsonObject("responses", new JsonObject())
        .stream()
        .map(Map.Entry::getValue)
        .map(j -> (JsonObject)j)
        .flatMap(j -> j.getJsonObject("content", new JsonObject()).fieldNames().stream())
        .collect(Collectors.toSet());

      // for (String ct : consumes)
        // route.consumes(ct);
      // TODO Do we really need this?

      for (String ct : produces)
        route.produces(ct);

      if (!consumes.isEmpty())
        ((RouteImpl)route).setEmptyBodyPermittedWithConsumes(!validationHandler.isBodyRequired());

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
