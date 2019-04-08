package io.vertx.ext.web.validation.impl;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.*;
import io.vertx.ext.web.validation.impl.body.BodyProcessor;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessor;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class ValidationHandlerImpl implements ValidationHandler {

  private ParameterProcessor[] queryParameters;
  private ParameterProcessor[] pathParameters;
  private ParameterProcessor[] cookieParameters;
  private ParameterProcessor[] headerParameters;
  private BodyProcessor[] bodyProcessors;
  private Function<RoutingContext, RequestPredicateResult>[] predicates;

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
      if (predicates != null)
        runPredicates(routingContext);

      // Hacky algorithm, it should create less futures and use less locks than CompositeFuture
      RequestParametersImpl requestParameters = new RequestParametersImpl();
      Future<RequestParametersImpl> resultFut = Future.succeededFuture(requestParameters);
      Future<RequestParametersImpl> waitingFut = Future.succeededFuture();

      if (pathParameters != null) {
        Future<Map<String, RequestParameter>> f = validatePathParams(routingContext);
        if (f.isComplete()) {
          if (f.succeeded()) {
            requestParameters.setPathParameters(f.result());
          } else {
            routingContext.fail(400, f.cause());
            return;
          }
        } else {
          waitingFut = f.compose(res -> {
            requestParameters.setPathParameters(res);
            return resultFut;
          });
        }
      }

      if (cookieParameters != null) {
        Future<Map<String, RequestParameter>> f = validateCookieParams(routingContext);
        if (f.isComplete()) {
          if (f.succeeded()) {
            requestParameters.setCookieParameters(f.result());
          } else {
            routingContext.fail(400, f.cause());
            return;
          }
        } else {
          waitingFut = f.compose(res -> {
            requestParameters.setCookieParameters(res);
            return resultFut;
          });
        }
      }

      if (queryParameters != null) {
        Future<Map<String, RequestParameter>> f = validateQueryParams(routingContext);
        if (f.isComplete()) {
          if (f.succeeded()) {
            requestParameters.setQueryParameters(f.result());
          } else {
            routingContext.fail(400, f.cause());
            return;
          }
        } else {
          waitingFut = f.compose(res -> {
            requestParameters.setQueryParameters(res);
            return resultFut;
          });
        }
      }

      if (headerParameters != null) {
        Future<Map<String, RequestParameter>> f = validateHeaderParams(routingContext);
        if (f.isComplete()) {
          if (f.succeeded()) {
            requestParameters.setHeaderParameters(f.result());
          } else {
            routingContext.fail(400, f.cause());
            return;
          }
        } else {
          waitingFut = f.compose(res -> {
            requestParameters.setHeaderParameters(res);
            return resultFut;
          });
        }
      }

      if (bodyProcessors != null && routingContext.request().headers().contains("content-type")) {
        Future<RequestParameter> f = validateBody(routingContext);
        if (f.isComplete()) {
          if (f.succeeded()) {
            requestParameters.setBody(f.result());
          } else {
            routingContext.fail(400, f.cause());
            return;
          }
        } else {
          waitingFut = f.compose(res -> {
            requestParameters.setBody(res);
            return resultFut;
          });
        }
      }

      waitingFut.setHandler(ar -> {
        if (ar.failed()) routingContext.fail(400, ar.cause());
        else {
          if (routingContext.data().containsKey("parsedParameters")) {
            ((RequestParametersImpl)routingContext.get("parsedParameters")).merge(requestParameters);
          } else {
            routingContext.put("parsedParameters", requestParameters);
            routingContext.put("requestParameters", requestParameters);
          }
          routingContext.next();
        }
      });
    } catch (BadRequestException e) {
      routingContext.fail(400, e);
    }
  }

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

  private Future<Map<String, RequestParameter>> validatePathParams(RoutingContext routingContext) {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    Map<String, List<String>> pathParams = routingContext
      .pathParams()
      .entrySet()
      .stream()
      .map(e -> new SimpleImmutableEntry<>(e.getKey(), Collections.singletonList(e.getValue())))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    Map<String, RequestParameter> parsedParams = new HashMap<>();

    return processParams(parsedParams, pathParams, pathParameters);
  }

  private Future<Map<String, RequestParameter>> validateCookieParams(RoutingContext routingContext) {
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

    return processParams(parsedParams, cookies, cookieParameters);
  }

  private Future<Map<String, RequestParameter>> validateQueryParams(RoutingContext routingContext) {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    Map<String, RequestParameter> parsedParams = new HashMap<>();
    return processParams(parsedParams, routingContext.queryParams(), queryParameters);
  }

  private Future<Map<String, RequestParameter>> validateHeaderParams(RoutingContext routingContext) {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    Map<String, RequestParameter> parsedParams = new HashMap<>();
    return processParams(parsedParams, routingContext.request().headers(), headerParameters);
  }

  private Future<RequestParameter> validateBody(RoutingContext routingContext) {
    for (BodyProcessor processor : bodyProcessors) {
      if (processor.canProcess(routingContext.parsedHeaders().contentType().value()))
        return processor.process(routingContext);
    }
    throw BodyProcessorException.createMissingMatchingBodyProcessor(routingContext.parsedHeaders().contentType().value());
  }

  private Map<String, List<String>> copyMultiMapInMap(MultiMap multiMap) {
    Map<String, List<String>> map = new HashMap<>();
    multiMap.forEach((e) -> map.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).add(e.getValue()));
    return map;
  }

  private Future<Map<String, RequestParameter>> processParams(Map<String, RequestParameter> parsedParams, MultiMap params, ParameterProcessor[] processors) {
    return processParams(parsedParams, copyMultiMapInMap(params), processors);
  }

  private Future<Map<String, RequestParameter>> processParams(Map<String, RequestParameter> parsedParams, Map<String, List<String>> params, ParameterProcessor[] processors) {
    Future<Map<String, RequestParameter>> waitingFutureChain = Future.succeededFuture(parsedParams);

    for (ParameterProcessor processor : processors) {
      try {
        Future<RequestParameter> fut = processor.process(params);
        if (fut.isComplete()) {
          if (fut.succeeded()) {
            parsedParams.put(processor.getName(), fut.result());
          } else if (fut.failed()) {
            return Future.failedFuture(fut.cause());
          }
        } else {
          waitingFutureChain = waitingFutureChain.compose(m -> fut.map(rp -> {
            parsedParams.put(processor.getName(), rp);
            return parsedParams;
          }));
        }
      } catch (BadRequestException e) {
        return Future.failedFuture(e);
      }
    }

    return waitingFutureChain;
  }

}
