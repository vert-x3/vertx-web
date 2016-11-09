/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.groovy.ext.web;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.vertx.groovy.core.http.HttpServerRequest
import io.vertx.groovy.core.Vertx
import java.util.Set
import io.vertx.core.json.JsonArray
import java.util.List
import io.vertx.groovy.ext.auth.User
import io.vertx.groovy.core.buffer.Buffer
import io.vertx.groovy.core.http.HttpServerResponse
import io.vertx.core.http.HttpMethod
import java.util.Map
import io.vertx.core.json.JsonObject
import io.vertx.core.Handler
/**
 * Represents the context for the handling of a request in Vert.x-Web.
 * <p>
 * A new instance is created for each HTTP request that is received in the
 * {@link io.vertx.groovy.ext.web.Router#accept} of the router.
 * <p>
 * The same instance is passed to any matching request or failure handlers during the routing of the request or
 * failure.
 * <p>
 * The context provides access to the  and 
 * and allows you to maintain arbitrary data that lives for the lifetime of the context. Contexts are discarded once they
 * have been routed to the handler for the request.
 * <p>
 * The context also provides access to the {@link io.vertx.groovy.ext.web.Session}, cookies and body for the request, given the correct handlers
 * in the application.
*/
@CompileStatic
public class RoutingContext {
  private final def io.vertx.ext.web.RoutingContext delegate;
  public RoutingContext(Object delegate) {
    this.delegate = (io.vertx.ext.web.RoutingContext) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * @return the HTTP request object
   */
  public HttpServerRequest request() {
    if (cached_0 != null) {
      return cached_0;
    }
    def ret = InternalHelper.safeCreate(delegate.request(), io.vertx.groovy.core.http.HttpServerRequest.class);
    cached_0 = ret;
    return ret;
  }
  /**
   * @return the HTTP response object
   */
  public HttpServerResponse response() {
    if (cached_1 != null) {
      return cached_1;
    }
    def ret = InternalHelper.safeCreate(delegate.response(), io.vertx.groovy.core.http.HttpServerResponse.class);
    cached_1 = ret;
    return ret;
  }
  /**
   * Tell the router to route this context to the next matching route (if any).
   * This method, if called, does not need to be called during the execution of the handler, it can be called
   * some arbitrary time later, if required.
   * <p>
   * If next is not called for a handler then the handler should make sure it ends the response or no response
   * will be sent.
   */
  public void next() {
    delegate.next();
  }
  /**
   * Fail the context with the specified status code.
   * <p>
   * This will cause the router to route the context to any matching failure handlers for the request. If no failure handlers
   * match a default failure response will be sent.
   * @param statusCode the HTTP status code
   */
  public void fail(int statusCode) {
    delegate.fail(statusCode);
  }
  /**
   * Fail the context with the specified throwable.
   * <p>
   * This will cause the router to route the context to any matching failure handlers for the request. If no failure handlers
   * match a default failure response with status code 500 will be sent.
   * @param throwable a throwable representing the failure
   */
  public void fail(Throwable throwable) {
    delegate.fail(throwable);
  }
  /**
   * Put some arbitrary data in the context. This will be available in any handlers that receive the context.
   * @param key the key for the data
   * @param obj the data
   * @return a reference to this, so the API can be used fluently
   */
  public RoutingContext put(String key, Object obj) {
    delegate.put(key, obj != null ? InternalHelper.unwrapObject(obj) : null);
    return this;
  }
  /**
   * Get some data from the context. The data is available in any handlers that receive the context.
   * @param key the key for the data
   * @return the data
   */
  public <T> T get(String key) {
    def ret = (T) InternalHelper.wrapObject(delegate.get(key));
    return ret;
  }
  /**
   * Remove some data from the context. The data is available in any handlers that receive the context.
   * @param key the key for the data
   * @return the previous data associated with the key
   */
  public <T> T remove(String key) {
    def ret = (T) InternalHelper.wrapObject(delegate.remove(key));
    return ret;
  }
  /**
   * @return the Vert.x instance associated to the initiating {@link io.vertx.groovy.ext.web.Router} for this context
   */
  public Vertx vertx() {
    def ret = InternalHelper.safeCreate(delegate.vertx(), io.vertx.groovy.core.Vertx.class);
    return ret;
  }
  /**
   * @return the mount point for this router. It will be null for a top level router. For a sub-router it will be the path at which the subrouter was mounted.
   */
  public String mountPoint() {
    def ret = delegate.mountPoint();
    return ret;
  }
  /**
   * @return the current route this context is being routed through.
   */
  public Route currentRoute() {
    def ret = InternalHelper.safeCreate(delegate.currentRoute(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
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
   * It's recommended to always use the normalised path as opposed to 
   * if accessing server resources requested by a client.
   * @return the normalised path
   */
  public String normalisedPath() {
    def ret = delegate.normalisedPath();
    return ret;
  }
  /**
   * Get the cookie with the specified name. The context must have first been routed to a {@link io.vertx.groovy.ext.web.handler.CookieHandler}
   * for this to work.
   * @param name the cookie name
   * @return the cookie
   */
  public Cookie getCookie(String name) {
    def ret = InternalHelper.safeCreate(delegate.getCookie(name), io.vertx.groovy.ext.web.Cookie.class);
    return ret;
  }
  /**
   * Add a cookie. This will be sent back to the client in the response. The context must have first been routed
   * to a {@link io.vertx.groovy.ext.web.handler.CookieHandler} for this to work.
   * @param cookie the cookie
   * @return a reference to this, so the API can be used fluently
   */
  public RoutingContext addCookie(Cookie cookie) {
    delegate.addCookie(cookie != null ? (io.vertx.ext.web.Cookie)cookie.getDelegate() : null);
    return this;
  }
  /**
   * Remove a cookie. The context must have first been routed to a {@link io.vertx.groovy.ext.web.handler.CookieHandler}
   * for this to work.
   * @param name the name of the cookie
   * @return the cookie, if it existed, or null
   */
  public Cookie removeCookie(String name) {
    def ret = InternalHelper.safeCreate(delegate.removeCookie(name), io.vertx.groovy.ext.web.Cookie.class);
    return ret;
  }
  /**
   * @return the number of cookies. The context must have first been routed to a {@link io.vertx.groovy.ext.web.handler.CookieHandler} for this to work.
   */
  public int cookieCount() {
    def ret = delegate.cookieCount();
    return ret;
  }
  /**
   * @return a set of all the cookies. The context must have first been routed to a {@link io.vertx.groovy.ext.web.handler.CookieHandler} for this to be populated.
   */
  public Set<Cookie> cookies() {
    def ret = (Set)delegate.cookies()?.collect({InternalHelper.safeCreate(it, io.vertx.groovy.ext.web.Cookie.class)}) as Set;
    return ret;
  }
  /**
   * @return the entire HTTP request body as a string, assuming UTF-8 encoding. The context must have first been routed to a {@link io.vertx.groovy.ext.web.handler.BodyHandler} for this to be populated.
   */
  public String getBodyAsString() {
    def ret = delegate.getBodyAsString();
    return ret;
  }
  /**
   * Get the entire HTTP request body as a string, assuming the specified encoding. The context must have first been routed to a
   * {@link io.vertx.groovy.ext.web.handler.BodyHandler} for this to be populated.
   * @param encoding the encoding, e.g. "UTF-16"
   * @return the body
   */
  public String getBodyAsString(String encoding) {
    def ret = delegate.getBodyAsString(encoding);
    return ret;
  }
  /**
   * @return Get the entire HTTP request body as a . The context must have first been routed to a {@link io.vertx.groovy.ext.web.handler.BodyHandler} for this to be populated.
   */
  public Map<String, Object> getBodyAsJson() {
    def ret = (Map<String, Object>)InternalHelper.wrapObject(delegate.getBodyAsJson());
    return ret;
  }
  /**
   * @return Get the entire HTTP request body as a . The context must have first been routed to a {@link io.vertx.groovy.ext.web.handler.BodyHandler} for this to be populated.
   */
  public List<Object> getBodyAsJsonArray() {
    def ret = (List<Object>)InternalHelper.wrapObject(delegate.getBodyAsJsonArray());
    return ret;
  }
  /**
   * @return Get the entire HTTP request body as a . The context must have first been routed to a {@link io.vertx.groovy.ext.web.handler.BodyHandler} for this to be populated.
   */
  public Buffer getBody() {
    def ret = InternalHelper.safeCreate(delegate.getBody(), io.vertx.groovy.core.buffer.Buffer.class);
    return ret;
  }
  /**
   * @return a set of fileuploads (if any) for the request. The context must have first been routed to a {@link io.vertx.groovy.ext.web.handler.BodyHandler} for this to work.
   */
  public Set<FileUpload> fileUploads() {
    def ret = (Set)delegate.fileUploads()?.collect({InternalHelper.safeCreate(it, io.vertx.groovy.ext.web.FileUpload.class)}) as Set;
    return ret;
  }
  /**
   * Get the session. The context must have first been routed to a {@link io.vertx.groovy.ext.web.handler.SessionHandler}
   * for this to be populated.
   * Sessions live for a browser session, and are maintained by session cookies.
   * @return the session.
   */
  public Session session() {
    def ret = InternalHelper.safeCreate(delegate.session(), io.vertx.groovy.ext.web.Session.class);
    return ret;
  }
  /**
   * Get the authenticated user (if any). This will usually be injected by an auth handler if authentication if successful.
   * @return the user, or null if the current user is not authenticated.
   */
  public User user() {
    def ret = InternalHelper.safeCreate(delegate.user(), io.vertx.groovy.ext.auth.User.class);
    return ret;
  }
  /**
   * If the context is being routed to failure handlers after a failure has been triggered by calling
   * {@link io.vertx.groovy.ext.web.RoutingContext#fail} then this will return that throwable. It can be used by failure handlers to render a response,
   * e.g. create a failure response page.
   * @return the throwable used when signalling failure
   */
  public Throwable failure() {
    if (cached_2 != null) {
      return cached_2;
    }
    def ret = delegate.failure();
    cached_2 = ret;
    return ret;
  }
  /**
   * If the context is being routed to failure handlers after a failure has been triggered by calling
   * {@link io.vertx.groovy.ext.web.RoutingContext#fail}  then this will return that status code.  It can be used by failure handlers to render a response,
   * e.g. create a failure response page.
   *
   * When the status code has not been set yet (it is undefined) its value will be -1.
   * @return the status code used when signalling failure
   */
  public int statusCode() {
    if (cached_3 != null) {
      return cached_3;
    }
    def ret = delegate.statusCode();
    cached_3 = ret;
    return ret;
  }
  /**
   * If the route specifies produces matches, e.g. produces `text/html` and `text/plain`, and the `accept` header
   * matches one or more of these then this returns the most acceptable match.
   * @return the most acceptable content type.
   */
  public String getAcceptableContentType() {
    def ret = delegate.getAcceptableContentType();
    return ret;
  }
  /**
   * The headers:
   * <ol>
   * <li>Accept</li>
   * <li>Accept-Charset</li>
   * <li>Accept-Encoding</li>
   * <li>Accept-Language</li>
   * <li>Content-Type</li>
   * </ol>
   * Parsed into {@link io.vertx.groovy.ext.web.ParsedHeaderValue}
   * @return A container with the parsed headers.
   */
  public ParsedHeaderValues parsedHeaders() {
    if (cached_4 != null) {
      return cached_4;
    }
    def ret = InternalHelper.safeCreate(delegate.parsedHeaders(), io.vertx.groovy.ext.web.ParsedHeaderValues.class);
    cached_4 = ret;
    return ret;
  }
  /**
   * Add a handler that will be called just before headers are written to the response. This gives you a hook where
   * you can write any extra headers before the response has been written when it will be too late.
   * @param handler the handler
   * @return the id of the handler. This can be used if you later want to remove the handler.
   */
  public int addHeadersEndHandler(Handler<Void> handler) {
    def ret = delegate.addHeadersEndHandler(handler);
    return ret;
  }
  /**
   * Remove a headers end handler
   * @param handlerID the id as returned from {@link io.vertx.groovy.ext.web.RoutingContext#addHeadersEndHandler}.
   * @return true if the handler existed and was removed, false otherwise
   */
  public boolean removeHeadersEndHandler(int handlerID) {
    def ret = delegate.removeHeadersEndHandler(handlerID);
    return ret;
  }
  /**
   * Provides a handler that will be called after the last part of the body is written to the wire.
   * The handler is called asynchronously of when the response has been received by the client.
   * This provides a hook allowing you to do more operations once the request has been sent over the wire
   * such as resource cleanup.
   * @param handler the handler
   * @return the id of the handler. This can be used if you later want to remove the handler.
   */
  public int addBodyEndHandler(Handler<Void> handler) {
    def ret = delegate.addBodyEndHandler(handler);
    return ret;
  }
  /**
   * Remove a body end handler
   * @param handlerID the id as returned from {@link io.vertx.groovy.ext.web.RoutingContext#addBodyEndHandler}.
   * @return true if the handler existed and was removed, false otherwise
   */
  public boolean removeBodyEndHandler(int handlerID) {
    def ret = delegate.removeBodyEndHandler(handlerID);
    return ret;
  }
  /**
   * @return true if the context is being routed to failure handlers.
   */
  public boolean failed() {
    def ret = delegate.failed();
    return ret;
  }
  /**
   * Set the body. Used by the {@link io.vertx.groovy.ext.web.handler.BodyHandler}. You will not normally call this method.
   * @param body the body
   */
  public void setBody(Buffer body) {
    delegate.setBody(body != null ? (io.vertx.core.buffer.Buffer)body.getDelegate() : null);
  }
  /**
   * Set the session. Used by the {@link io.vertx.groovy.ext.web.handler.SessionHandler}. You will not normally call this method.
   * @param session the session
   */
  public void setSession(Session session) {
    delegate.setSession(session != null ? (io.vertx.ext.web.Session)session.getDelegate() : null);
  }
  /**
   * Set the user. Usually used by auth handlers to inject a User. You will not normally call this method.
   * @param user the user
   */
  public void setUser(User user) {
    delegate.setUser(user != null ? (io.vertx.ext.auth.User)user.getDelegate() : null);
  }
  /**
   * Clear the current user object in the context. This usually is used for implementing a log out feature, since the
   * current user is unbounded from the routing context.
   */
  public void clearUser() {
    delegate.clearUser();
  }
  /**
   * Set the acceptable content type. Used by
   * @param contentType the content type
   */
  public void setAcceptableContentType(String contentType) {
    delegate.setAcceptableContentType(contentType);
  }
  /**
   * Restarts the current router with a new path and reusing the original method. All path parameters are then parsed
   * and available on the params list.
   * @param path the new http path.
   */
  public void reroute(String path) {
    delegate.reroute(path);
  }
  /**
   * Restarts the current router with a new method and path. All path parameters are then parsed and available on the
   * params list.
   * @param method the new http request
   * @param path the new http path.
   */
  public void reroute(HttpMethod method, String path) {
    delegate.reroute(method, path);
  }
  /**
   * Returns the locales for the current request. The locales are determined from the `accept-languages` header and
   * sorted on quality.
   *
   * When 2 or more entries have the same quality then the order used to return the best match is based on the lowest
   * index on the original list. For example if a user has en-US and en-GB with same quality and this order the best
   * match will be en-US because it was declared as first entry by the client.
   * @return the best matched locale for the request
   */
  public List<Locale> acceptableLocales() {
    if (cached_5 != null) {
      return cached_5;
    }
    def ret = (List)delegate.acceptableLocales()?.collect({InternalHelper.safeCreate(it, io.vertx.groovy.ext.web.Locale.class)});
    cached_5 = ret;
    return ret;
  }
  /**
   * Returns the languages for the current request. The languages are determined from the <code>Accept-Language</code>
   * header and sorted on quality.
   *
   * When 2 or more entries have the same quality then the order used to return the best match is based on the lowest
   * index on the original list. For example if a user has en-US and en-GB with same quality and this order the best
   * match will be en-US because it was declared as first entry by the client.
   * @return The best matched language for the request
   */
  public List<LanguageHeader> acceptableLanguages() {
    if (cached_6 != null) {
      return cached_6;
    }
    def ret = (List)delegate.acceptableLanguages()?.collect({InternalHelper.safeCreate(it, io.vertx.groovy.ext.web.LanguageHeader.class)});
    cached_6 = ret;
    return ret;
  }
  /**
   * Helper to return the user preferred locale. It is the same action as returning the first element of the acceptable
   * locales.
   * @return the users preferred locale.
   */
  public Locale preferredLocale() {
    if (cached_7 != null) {
      return cached_7;
    }
    def ret = InternalHelper.safeCreate(delegate.preferredLocale(), io.vertx.groovy.ext.web.Locale.class);
    cached_7 = ret;
    return ret;
  }
  /**
   * Helper to return the user preferred language.
   * It is the same action as returning the first element of the acceptable languages.
   * @return the users preferred locale.
   */
  public LanguageHeader preferredLanguage() {
    if (cached_8 != null) {
      return cached_8;
    }
    def ret = InternalHelper.safeCreate(delegate.preferredLanguage(), io.vertx.groovy.ext.web.LanguageHeader.class);
    cached_8 = ret;
    return ret;
  }
  /**
   * Returns a map of named parameters as defined in path declaration with their actual values
   * @return the map of named parameters
   */
  public Map<String, String> pathParams() {
    def ret = delegate.pathParams();
    return ret;
  }
  /**
   * Gets the value of a single path parameter
   * @param name the name of parameter as defined in path declaration
   * @return the actual value of the parameter or null if it doesn't exist
   */
  public String pathParam(String name) {
    def ret = delegate.pathParam(name);
    return ret;
  }
  private HttpServerRequest cached_0;
  private HttpServerResponse cached_1;
  private Throwable cached_2;
  private Integer cached_3;
  private ParsedHeaderValues cached_4;
  private List<Locale> cached_5;
  private List<LanguageHeader> cached_6;
  private Locale cached_7;
  private LanguageHeader cached_8;
}
