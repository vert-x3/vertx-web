package io.vertx.ext.web.validation.impl;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.*;
import io.vertx.ext.web.validation.impl.body.BodyProcessor;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessor;
import io.vertx.json.schema.SchemaException;
import io.vertx.json.schema.ValidationException;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class ValidationHandlerImpl implements ValidationHandler {

  private final ParameterProcessor[] queryParameters;
  private final ParameterProcessor[] pathParameters;
  private final ParameterProcessor[] cookieParameters;
  private final ParameterProcessor[] headerParameters;
  private final BodyProcessor[] bodyProcessors;
  private final Function<RoutingContext, RequestPredicateResult>[] predicates;

  @SuppressWarnings("unchecked")
  public ValidationHandlerImpl(Map<ParameterLocation, List<ParameterProcessor>> parameterProcessors, List<BodyProcessor> bodyProcessors, List<Function<RoutingContext, RequestPredicateResult>> predicates) {
    this.queryParameters =
      parameterProcessors.get(ParameterLocation.QUERY) == null || parameterProcessors.get(ParameterLocation.QUERY).isEmpty() ?
        null :
        parameterProcessors.get(ParameterLocation.QUERY).toArray(new ParameterProcessor[0]);
    if (this.queryParameters != null)
      Arrays.sort(this.queryParameters);
    this.pathParameters =
      parameterProcessors.get(ParameterLocation.PATH) == null || parameterProcessors.get(ParameterLocation.PATH).isEmpty() ?
        null :
        parameterProcessors.get(ParameterLocation.PATH).toArray(new ParameterProcessor[0]);
    if (this.pathParameters != null)
      Arrays.sort(this.pathParameters);
    this.cookieParameters =
      parameterProcessors.get(ParameterLocation.COOKIE) == null || parameterProcessors.get(ParameterLocation.COOKIE).isEmpty() ?
        null :
        parameterProcessors.get(ParameterLocation.COOKIE).toArray(new ParameterProcessor[0]);
    if (this.cookieParameters != null)
      Arrays.sort(this.cookieParameters);
    this.headerParameters =
      parameterProcessors.get(ParameterLocation.HEADER) == null || parameterProcessors.get(ParameterLocation.HEADER).isEmpty() ?
        null :
        parameterProcessors.get(ParameterLocation.HEADER).toArray(new ParameterProcessor[0]);
    if (this.headerParameters != null)
      Arrays.sort(this.headerParameters);
    this.bodyProcessors =
      bodyProcessors == null || bodyProcessors.isEmpty() ?
        null :
        bodyProcessors.toArray(new BodyProcessor[0]);
    this.predicates =
      predicates == null || predicates.isEmpty() ?
        null :
        predicates.toArray(new Function[0]);
  }

  @Override
  public void handle(RoutingContext routingContext) {
    try {
      if (predicates != null) {
        runPredicates(routingContext);
      }

      RequestParametersImpl requestParameters = new RequestParametersImpl();

      if (pathParameters != null) {
        requestParameters.setPathParameters(validatePathParams(routingContext));
      }

      if (cookieParameters != null) {
        requestParameters.setCookieParameters(validateCookieParams(routingContext));
      }

      if (queryParameters != null) {
        requestParameters.setQueryParameters(validateQueryParams(routingContext));
      }

      if (headerParameters != null) {
        requestParameters.setHeaderParameters(validateHeaderParams(routingContext));
      }

      if (bodyProcessors != null && routingContext.request().headers().contains("content-type")) {
        requestParameters.setBody(validateBody(routingContext));
      }

      if (routingContext.data().containsKey("parsedParameters")) {
        routingContext.<RequestParametersImpl>get("parsedParameters").merge(requestParameters);
      } else {
        routingContext.put("parsedParameters", requestParameters);
        routingContext.put("requestParameters", requestParameters);
      }

      routingContext.next();
    } catch (BadRequestException | SchemaException | ValidationException e) {
      routingContext.fail(400, e);
    }
  }

  @Override
  public boolean isBodyRequired() {
    if (predicates == null) return false;
    return Arrays.stream(predicates).anyMatch(p -> p == RequestPredicate.BODY_REQUIRED);
  }

  private void runPredicates(RoutingContext context) throws BadRequestException {
    for (Function<RoutingContext, RequestPredicateResult> p : predicates) {
      RequestPredicateResult res = p.apply(context);
      if (!res.succeeded()) throw new RequestPredicateException(res.getErrorMessage());
    }
  }

  private Map<String, RequestParameter> validatePathParams(RoutingContext routingContext) {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    Map<String, List<String>> pathParams = routingContext
      .pathParams()
      .entrySet()
      .stream()
      .map(e -> new SimpleImmutableEntry<>(e.getKey(), Collections.singletonList(e.getValue())))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    Map<String, RequestParameter> parsedParams = new HashMap<>();
    return processParams(parsedParams, pathParams, pathParameters, false);
  }

  private Map<String, RequestParameter> validateCookieParams(RoutingContext routingContext) {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    Map<String, List<String>> cookies = new HashMap<>();
    if (routingContext.request().headers().contains("Cookie")) {
      // Some hack to reuse QueryStringDecoder
      QueryStringDecoder decoder = new QueryStringDecoder("/?" + routingContext.request().getHeader("Cookie"));
      // QueryStringDecoder doesn't trim whitespaces!

      for (Map.Entry<String, List<String>> entry : decoder.parameters().entrySet()) {
        cookies.merge(entry.getKey().trim(), entry.getValue(), (oldValue, newValue) -> {
          oldValue.addAll(newValue);
          return oldValue;
        });
      }
    }
    Map<String, RequestParameter> parsedParams = new HashMap<>();
    return processParams(parsedParams, cookies, cookieParameters, false);
  }

  private Map<String, RequestParameter> validateQueryParams(RoutingContext routingContext) {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    Map<String, RequestParameter> parsedParams = new HashMap<>();
    Map<String, List<String>> queryParams = new HashMap<>();
    routingContext.queryParams().forEach((e) -> queryParams.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).add(e.getValue()));
    return processParams(parsedParams, queryParams, queryParameters, false);
  }

  private Map<String, RequestParameter> validateHeaderParams(RoutingContext routingContext) {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    Map<String, RequestParameter> parsedParams = new HashMap<>();

    // We must force lowercase because parameters are recognized by their lowercase value for headers
    Map<String, List<String>> headers = new HashMap<>();
    routingContext
      .request()
      .headers()
      .forEach((e) -> headers.computeIfAbsent(e.getKey().toLowerCase(), k -> new ArrayList<>()).add(e.getValue()));

    return processParams(parsedParams, headers, headerParameters, true);
  }

  private RequestParameter validateBody(RoutingContext routingContext) {
    for (BodyProcessor processor : bodyProcessors) {
      if (processor.canProcess(routingContext.parsedHeaders().contentType().value())) {
        return processor.process(routingContext);
      }
    }
    throw BodyProcessorException.createMissingMatchingBodyProcessor(routingContext.parsedHeaders().contentType().value());
  }

  private Map<String, RequestParameter> processParams(Map<String, RequestParameter> parsedParams, Map<String, List<String>> params, ParameterProcessor[] processors, boolean forceLowercase) {
    for (ParameterProcessor processor : processors) {
      parsedParams.put(forceLowercase ? processor.getName().toLowerCase() : processor.getName(), processor.process(params));
    }

    return parsedParams;
  }

}
