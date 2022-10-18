package io.vertx.ext.web.openapi.impl;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthorizationHandler;
import io.vertx.ext.web.openapi.OpenAPIHolder;
import io.vertx.ext.web.openapi.Operation;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OperationImpl implements Operation {

  private String operationId;
  private HttpMethod method;
  private String path;
  private JsonObject pathModel;
  private JsonObject operationModel;
  private JsonPointer operationPointer;

  private Map<JsonPointer, JsonObject> parameters;
  private List<String> tags;
  private List<AuthorizationHandler> authzHandlers;
  private List<Handler<RoutingContext>> userHandlers;
  private List<Handler<RoutingContext>> userFailureHandlers;

  private String ebServiceAddress;
  private String ebServiceMethodName;
  private DeliveryOptions ebServiceDeliveryOptions;

  protected OperationImpl(String operationId, HttpMethod method, String path, JsonObject operationModel, JsonObject pathModel, URI specScope, OpenAPIHolder holder) {
    this.operationId = operationId;
    this.method = method;
    this.path = path;
    this.pathModel = pathModel;
    this.operationModel = operationModel;
    this.tags = operationModel.getJsonArray("tags", new JsonArray()).stream().map(Object::toString).collect(Collectors.toList());

    JsonPointer pathPointer = JsonPointer.fromURI(specScope).append("paths").append(path);
    this.operationPointer = pathPointer.copy().append(method.name().toLowerCase());

    // Merge parameters
    this.parameters = new HashMap<>();

    JsonArray operationParameters = operationModel.getJsonArray("parameters", new JsonArray());
    JsonArray pathParameters = pathModel.getJsonArray("parameters", new JsonArray());

    for (int i = 0; i < operationParameters.size(); i++) {
      JsonObject parameterModel = holder.solveIfNeeded(operationParameters.getJsonObject(i));
      JsonPointer parameterPointer = operationPointer.copy().append("parameters").append(i);
      this.parameters.put(parameterPointer, parameterModel);
    }

    for (int i = 0; i < pathParameters.size(); i++) {
      JsonObject parameterModel = holder.solveIfNeeded(pathParameters.getJsonObject(i));
      String paramName = parameterModel.getString("name");
      String paramIn = parameterModel.getString("in");
      // A parameter is uniquely identified by a tuple (name, in)
      if (this.parameters
        .values()
        .stream()
        .noneMatch(j -> j.getString("in").equalsIgnoreCase(paramIn) && j.getString("name").equals(paramName)))
        this.parameters.put(pathPointer.copy().append(i), parameterModel);
    }
    this.authzHandlers = new ArrayList<>();
    this.userHandlers = new ArrayList<>();
    this.userFailureHandlers = new ArrayList<>();
  }

  @Override
  public Operation authorizationHandler(AuthorizationHandler handler) {
    this.authzHandlers.add(handler);
    return this;
  }

  @Override
  public Operation handler(Handler<RoutingContext> handler) {
    this.userHandlers.add(handler);
    return this;
  }

  @Override
  public Operation failureHandler(Handler<RoutingContext> handler) {
    this.userFailureHandlers.add(handler);
    return this;
  }

  @Override
  public Operation routeToEventBus(String address) {
    mountRouteToService(address);
    return this;
  }

  @Override
  public Operation routeToEventBus(String address, DeliveryOptions options) {
    mountRouteToService(address, options);
    return this;
  }

  @Override
  public String getOperationId() {
    return operationId;
  }

  @Override
  public JsonObject getOperationModel() {
    return operationModel;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return method;
  }

  @Override
  public String getOpenAPIPath() {
    return path;
  }

  protected JsonPointer getPointer() {
    return this.operationPointer.copy();
  }

  protected Map<JsonPointer, JsonObject> getParameters() {
    return parameters;
  }

  protected JsonObject getPathModel() {
    return pathModel;
  }

  protected List<AuthorizationHandler> getAuthorizationHandlers() {
    return authzHandlers;
  }

  protected List<Handler<RoutingContext>> getUserHandlers() {
    return userHandlers;
  }

  protected List<Handler<RoutingContext>> getUserFailureHandlers() {
    return userFailureHandlers;
  }

  protected boolean isConfigured() {
    return userHandlers.size() != 0 || mustMountRouteToService();
  }

  protected List<String> getTags() {
    return tags;
  }

  protected boolean hasTag(String tag) { return tags != null && tags.contains(tag); }

  protected void mountRouteToService(String address) {
    this.ebServiceAddress = address;
    this.ebServiceMethodName = OpenAPI3Utils.sanitizeOperationId(operationId);
  }

  protected void mountRouteToService(String address, String methodName) {
    this.ebServiceAddress = address;
    this.ebServiceMethodName = OpenAPI3Utils.sanitizeOperationId(methodName);
  }

  protected void mountRouteToService(String address, DeliveryOptions deliveryOptions) {
    this.ebServiceAddress = address;
    this.ebServiceMethodName = OpenAPI3Utils.sanitizeOperationId(operationId);
    this.ebServiceDeliveryOptions = deliveryOptions;
  }

  protected void mountRouteToService(String address, String methodName, DeliveryOptions deliveryOptions) {
    this.ebServiceAddress = address;
    this.ebServiceMethodName = OpenAPI3Utils.sanitizeOperationId(methodName);
    this.ebServiceDeliveryOptions = deliveryOptions;
  }

  protected boolean mustMountRouteToService() {
    return this.ebServiceAddress != null;
  }

  protected String getEbServiceAddress() {
    return ebServiceAddress;
  }

  protected String getEbServiceMethodName() {
    return ebServiceMethodName;
  }

  protected DeliveryOptions getEbServiceDeliveryOptions() {
    return ebServiceDeliveryOptions;
  }
}
