/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.apex;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;

import java.util.Map;
import java.util.Set;

/**
 * Represents the context for the handling of a request in Apex.
 * <p>
 * A new instance is created for each HTTP request that is received in the
 * {@link io.vertx.ext.apex.Router#accept(io.vertx.core.http.HttpServerRequest)} of the router.
 * <p>
 * The same instance is passed to any matching request or failure handlers during the routing of the request or
 * failure.
 * <p>
 * The context provides access to the {@link io.vertx.core.http.HttpServerRequest} and {@link io.vertx.core.http.HttpServerResponse}
 * and allows you to maintain arbitrary data that lives for the lifetime of the context. Contexts are discarded once they
 * have been routed to the handler for the request.
 * <p>
 * The context also provides access to the {@link Session}, cookies and body for the request, given the correct handlers
 * in the application.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface RoutingContext {

  /**
   * @return the HTTP request object
   */
  @CacheReturn
  HttpServerRequest request();

  /**
   * @return the HTTP response object
   */
  @CacheReturn
  HttpServerResponse response();

  /**
   * Tell the router to route this context to the next matching route (if any).
   * This method, if called, does not need to be called during the execution of the handler, it can be called
   * some arbitrary time later, if required.
   * <p>
   * If next is not called for a handler then the handler should make sure it ends the response or no response
   * will be sent.
   */
  void next();

  /**
   * Fail the context with the specified status code.
   * <p>
   * This will cause the router to route the context to any matching failure handlers for the request. If no failure handlers
   * match a default failure response will be sent.
   *
   * @param statusCode  the HTTP status code
   */
  void fail(int statusCode);

  /**
   * Fail the context with the specified throwable.
   * <p>
   * This will cause the router to route the context to any matching failure handlers for the request. If no failure handlers
   * match a default failure response with status code 500 will be sent.
   *
   * @param throwable  a throwable representing the failure
   */
  @GenIgnore
  void fail(Throwable throwable);

  /**
   * Put some arbitrary data in the context. This will be available in any handlers that receive the context.
   *
   * @param key  the key for the data
   * @param obj  the data
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  RoutingContext put(String key, Object obj);

  /**
   * Get some data from the context. The data is available in any handlers that receive the context.
   *
   * @param key  the key for the data
   * @param <T>  the type of the data
   * @return  the data
   * @throws java.lang.ClassCastException if the data is not of the expected type
   */
  <T> T get(String key);

  /**
   * @return all the context data as a map
   */
  @GenIgnore
  Map<String, Object> data();

  /**
   * @return the Vert.x instance associated to the initiating {@link io.vertx.ext.apex.Router} for this context
   */
  Vertx vertx();

  /**
   * @return the mount point for this router. It will be null for a top level router. For a sub-router it will be the path
   * at which the subrouter was mounted.
   */
  String mountPoint();

  /**
   * @return the current route this context is being routed through.
   */
  Route currentRoute();

  /**
   * Return the normalised path for the request.
   * <p>
   * The normalised path is where the URI path has been decoded, i.e. any unicode or other illegal URL characters that
   * were encoded in the original URL with `%` will be returned to their original form. E.g. `%20` will revert to a space.
   * Also `+` reverts to a space in a query.
   * <p>
   * The normalised path will also not contain any `..` character sequences to prevent resources being accessed outside
   * of the permitted area.
   * <p>
   * It's recommended to always use the normalised path as opposed to {@link io.vertx.core.http.HttpServerRequest#path()}
   * if accessing server resources requested by a client.
   *
   * @return the normalised path
   */
  String normalisedPath();

  /**
   * Get the cookie with the specified name. The context must have first been routed to a {@link io.vertx.ext.apex.handler.CookieHandler}
   * for this to work.
   *
   * @param name  the cookie name
   * @return the cookie
   */
  Cookie getCookie(String name);

  /**
   * Add a cookie. This will be sent back to the client in the response. The context must have first been routed
   * to a {@link io.vertx.ext.apex.handler.CookieHandler} for this to work.
   *
   * @param cookie  the cookie
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  RoutingContext addCookie(Cookie cookie);

  /**
   * Remove a cookie. The context must have first been routed to a {@link io.vertx.ext.apex.handler.CookieHandler}
   * for this to work.
   *
   * @param name  the name of the cookie
   * @return the cookie, if it existed, or null
   */
  Cookie removeCookie(String name);

  /**
   * @return the number of cookies. The context must have first been routed to a {@link io.vertx.ext.apex.handler.CookieHandler}
   * for this to work.
   */
  int cookieCount();

  /**
   * @return a set of all the cookies. The context must have first been routed to a {@link io.vertx.ext.apex.handler.CookieHandler}
   * for this to be populated.
   */
  Set<Cookie> cookies();

  /**
   * @return  the entire HTTP request body as a string, assuming UTF-8 encoding. The context must have first been routed to a
   * {@link io.vertx.ext.apex.handler.BodyHandler} for this to be populated.
   */
  String getBodyAsString();

  /**
   * Get the entire HTTP request body as a string, assuming the specified encoding. The context must have first been routed to a
   * {@link io.vertx.ext.apex.handler.BodyHandler} for this to be populated.
   *
   * @param encoding  the encoding, e.g. "UTF-16"
   * @return the body
   */
  String getBodyAsString(String encoding);

  /**
   * @return Get the entire HTTP request body as a {@link io.vertx.core.json.JsonObject}. The context must have first been routed to a
   * {@link io.vertx.ext.apex.handler.BodyHandler} for this to be populated.
   */
  JsonObject getBodyAsJson();

  /**
   * @return Get the entire HTTP request body as a {@link io.vertx.core.buffer.Buffer}. The context must have first been routed to a
   * {@link io.vertx.ext.apex.handler.BodyHandler} for this to be populated.
   */
  Buffer getBody();

  /**
   * @return a set of fileuploads (if any) for the request. The context must have first been routed to a
   * {@link io.vertx.ext.apex.handler.BodyHandler} for this to work.
   */
  Set<FileUpload> fileUploads();

  /**
   * Get the session. The context must have first been routed to a {@link io.vertx.ext.apex.handler.SessionHandler}
   * for this to be populated.
   * Sessions live for a browser session, and are maintained by session cookies.
   * @return  the session.
   */
  Session session();

  /**
   * Get the authenticated user (if any). This will usually be injected by an auth handler if authentication if successful.
   * @return  the user, or null if the current user is not authenticated.
   */
  User user();

  /**
   * If the context is being routed to failure handlers after a failure has been triggered by calling
   * {@link #fail(Throwable)} then this will return that throwable. It can be used by failure handlers to render a response,
   * e.g. create a failure response page.
   *
   * @return  the throwable used when signalling failure
   */
  @GenIgnore
  @CacheReturn
  Throwable failure();

  /**
   * If the context is being routed to failure handlers after a failure has been triggered by calling
   * {@link #fail(int)}  then this will return that status code.  It can be used by failure handlers to render a response,
   * e.g. create a failure response page.
   *
   * @return  the status code used when signalling failure
   */
  @CacheReturn
  int statusCode();

  /**
   * If the route specifies produces matches, e.g. produces `text/html` and `text/plain`, and the `accept` header
   * matches one or more of these then this returns the most acceptable match.
   *
   * @return  the most acceptable content type.
   */
  String getAcceptableContentType();

  /**
   * Add a handler that will be called just before headers are written to the response. This gives you a hook where
   * you can write any extra headers before the response has been written when it will be too late.
   *
   * @param handler  the handler
   * @return  the id of the handler. This can be used if you later want to remove the handler.
   */
  int addHeadersEndHandler(Handler<Void> handler);

  /**
   * Remove a headers end handler
   *
   * @param handlerID  the id as returned from {@link io.vertx.ext.apex.RoutingContext#addHeadersEndHandler(io.vertx.core.Handler)}.
   * @return true if the handler existed and was removed, false otherwise
   */
  boolean removeHeadersEndHandler(int handlerID);

  /**
   * Add a handler that will be called just before the response body has been completely written.
   * This gives you a hook where you can write any extra data to the response before it has ended when it will be too late.
   *
   * @param handler  the handler
   * @return  the id of the handler. This can be used if you later want to remove the handler.
   */
  int addBodyEndHandler(Handler<Void> handler);

  /**
   * Remove a body end handler
   *
   * @param handlerID  the id as returned from {@link io.vertx.ext.apex.RoutingContext#addBodyEndHandler(io.vertx.core.Handler)}.
   * @return true if the handler existed and was removed, false otherwise
   */
  boolean removeBodyEndHandler(int handlerID);

  /**
   * @return true if the context is being routed to failure handlers.
   */
  boolean failed();

  /**
   * Set the body. Used by the {@link io.vertx.ext.apex.handler.BodyHandler}. You will not normally call this method.
   *
   * @param body  the body
   */
  void setBody(Buffer body);

  /**
   * Set the session. Used by the {@link io.vertx.ext.apex.handler.SessionHandler}. You will not normally call this method.
   *
   * @param session  the session
   */
  void setSession(Session session);

  /**
   * Set the user. Usually used by auth handlers to inject a User. You will not normally call this method.
   *
   * @param user  the user
   */
  void setUser(User user);

  /**
   * Set the acceptable content type. Used by
   * @param contentType  the content type
   */
  void setAcceptableContentType(String contentType);

}
