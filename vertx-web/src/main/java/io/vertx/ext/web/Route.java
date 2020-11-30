/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.impl.HttpStatusException;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * A route is a holder for a set of criteria which determine whether an HTTP request or failure should be routed
 * to a handler.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface Route {

  /**
   * Add an HTTP method for this route. By default a route will match all HTTP methods. If any are specified then the route
   * will only match any of the specified methods
   *
   * @param method the HTTP method to add
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route method(HttpMethod method);

  /**
   * Set the path prefix for this route. If set then this route will only match request URI paths which start with this
   * path prefix. Only a single path or path regex can be set for a route.
   *
   * @param path the path prefix
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route path(String path);

  /**
   * Set the path prefix as a regular expression. If set then this route will only match request URI paths, the beginning
   * of which match the regex. Only a single path or path regex can be set for a route.
   *
   * @param path the path regex
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route pathRegex(String path);

  /**
   * Add a content type produced by this route. Used for content based routing.
   *
   * @param contentType the content type
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route produces(String contentType);

  /**
   * Add a content type consumed by this route. Used for content based routing.
   *
   * @param contentType the content type
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route consumes(String contentType);

  /**
   * Add a virtual host filter for this route.
   *
   * @param hostnamePattern the hostname pattern that should match {@code Host} header of the requests
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route virtualHost(String hostnamePattern);

  /**
   * Specify the order for this route. The router tests routes in that order.
   *
   * @param order the order
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route order(int order);

  /**
   * Specify this is the last route for the router.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route last();

  /**
   * Append a request handler to the route handlers list. The router routes requests to handlers depending on whether the various
   * criteria such as method, path, etc match. When method, path, etc are the same for different routes, You should add multiple
   * handlers to the same route object rather than creating two different routes objects with one handler for route
   *
   * @param requestHandler the request handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route handler(Handler<RoutingContext> requestHandler);


  /**
   * Like {@link io.vertx.ext.web.Route#blockingHandler(Handler, boolean)} called with ordered = true
   */
  @Fluent
  Route blockingHandler(Handler<RoutingContext> requestHandler);

  /**
   * Use a (sub) {@link Router} as a handler. There are several requirements to be fulfilled for this
   * to be accepted.
   *
   * <ul>
   *     <li>The route path must end with a wild card</li>
   *     <li>Parameters are allowed but full regex patterns not</li>
   *     <li>No other handler can be registered before or after this call (but they can on a new route object for the same path)</li>
   *     <li>Only 1 router per path object</li>
   * </ul>
   *
   * @param subRouter the router to add
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route subRouter(Router subRouter);

  /**
   * Specify a blocking request handler for the route.
   * This method works just like {@link #handler(Handler)} excepted that it will run the blocking handler on a worker thread
   * so that it won't block the event loop. Note that it's safe to call context.next() from the
   * blocking handler as it will be executed on the event loop context (and not on the worker thread.
   * <p>
   * If the blocking handler is ordered it means that any blocking handlers for the same context are never executed
   * concurrently but always in the order they were called. The default value of ordered is true. If you do not want this
   * behaviour and don't mind if your blocking handlers are executed in parallel you can set ordered to false.
   *
   * @param requestHandler the blocking request handler
   * @param ordered        if true handlers are executed in sequence, otherwise are run in parallel
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route blockingHandler(Handler<RoutingContext> requestHandler, boolean ordered);

  /**
   * Append a failure handler to the route failure handlers list. The router routes failures to failurehandlers depending on whether the various
   * criteria such as method, path, etc match. When method, path, etc are the same for different routes, You should add multiple
   * failure handlers to the same route object rather than creating two different routes objects with one failure handler for route
   *
   * @param failureHandler the request handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route failureHandler(Handler<RoutingContext> failureHandler);

  /**
   * Remove this route from the router
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route remove();

  /**
   * Disable this route. While disabled the router will not route any requests or failures to it.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route disable();

  /**
   * Enable this route.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route enable();

  /**
   * Use {@link #useNormalizedPath(boolean)} instead
   */
  @Fluent
  @Deprecated
  default Route useNormalisedPath(boolean useNormalizedPath) {
    return this.useNormalizedPath(useNormalizedPath);
  }

  /**
   * If true then the normalized request path will be used when routing (e.g. removing duplicate /)
   * Default is true
   *
   * @param useNormalizedPath use normalized path for routing?
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route useNormalizedPath(boolean useNormalizedPath);

  /**
   * @return the path prefix (if any) for this route
   */
  @Nullable
  String getPath();

  /**
   * Returns true of the path is a regular expression, this includes expression paths.
   *
   * @return true if backed by a pattern.
   */
  boolean isRegexPath();

  /**
   * @return the http methods accepted by this route
   */
  Set<HttpMethod> methods();

  /**
   * When you add a new route with a regular expression, you can add named capture groups for parameters. <br/>
   * However, if you need more complex parameters names (like "param_name"), you can add parameters names with
   * this function. You have to name capture groups in regex with names: "p0", "p1", "p2", ... <br/>
   * <br/>
   * For example: If you declare route with regex \/(?<p0>[a-z]*)\/(?<p1>[a-z]*) and group names ["param_a", "param-b"]
   * for uri /hello/world you receive inside pathParams() the parameter param_a = "hello"
   *
   * @param groups group names
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route setRegexGroupsNames(List<String> groups);

  /**
   * Giving a name to a route will provide this name as metadata to requests matching this route.
   * This metadata is used by metrics and is meant to group requests with different URI paths (due
   * to parameters) by a common identifier, for example "/resource/:resourceID"
   * common name
   *
   * @param name The name of the route.
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Route setName(String name);

  /**
   * @return the name of the route. If not given explicitly, the path or the pattern or
   * null is returned (in that order)
   */
  String getName();

  /**
   * Append a function request handler to the route handlers list. The function expects to receive the routing context
   * and users are expected to return a {@link Future}. The use of this functional interface allows users to quickly
   * link the responses from other vert.x APIs or clients directly to a handler. If the context response has been ended,
   * for example, {@link RoutingContext#end()} has been called, then nothing shall happen. For the remaining cases, the
   * following rules apply:
   *
   * <ol>
   *   <li>When {@code body} is {@code null} then the status code of the response shall be 204 (NO CONTENT)</li>
   *   <li>When {@code body} is of type {@link Buffer} and the {@code Content-Type} isn't set then the {@code Content-Type} shall be {@code application/octet-stream}</li>
   *   <li>When {@code body} is of type {@link String} and the {@code Content-Type} isn't set then the {@code Content-Type} shall be {@code text/html}</li>
   *   <li>Otherwise the response of the future is then passed to the method {@link RoutingContext#json(Object)} to perform a JSON serialization of the result</li>
   * </ol>
   *
   * Internally the function is wrapped as a handler that handles error cases for the user too. For example, if the
   * function throws an exception the error will be catched and a proper error will be propagated throw the router.
   *
   * Also if the same happens while encoding the response, errors are catched and propagated to the router.
   *
   * @param <T>      a generic type to allow type safe API
   * @param function the request handler function
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default <T> Route respond(Function<RoutingContext, Future<@Nullable T>> function) {
    return handler(ctx -> {
      try {
        function.apply(ctx)
          .onFailure(ctx::fail)
          .onSuccess(body -> {
            if (!ctx.response().headWritten()) {
              if (body == null) {
                ctx
                  .response()
                  .setStatusCode(204)
                  .end();
              } else {
                final boolean hasContentType = ctx.response().headers().contains(HttpHeaders.CONTENT_TYPE);
                if (body instanceof Buffer) {
                  if (!hasContentType) {
                    ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
                  }
                  ctx.end((Buffer) body);
                } else if (body instanceof String) {
                  if (!hasContentType) {
                    ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html");
                  }
                  ctx.end((String) body);
                } else {
                  ctx.json(body);
                }
              }
            } else {
              if (body == null) {
                if (!ctx.response().ended()) {
                  ctx.end();
                }
              } else {
                ctx.fail(new HttpStatusException(500, "Response already written"));
              }
            }
          });
      } catch (RuntimeException e) {
        ctx.fail(e);
      }
    });
  }
}


