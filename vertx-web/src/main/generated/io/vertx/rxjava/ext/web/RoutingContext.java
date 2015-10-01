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

package io.vertx.rxjava.ext.web;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.core.Vertx;
import java.util.Set;
import io.vertx.rxjava.core.Future;
import io.vertx.rxjava.ext.auth.User;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Handler;

/**
 * Represents the context for the handling of a request in Vert.x-Web.
 * <p>
 * A new instance is created for each HTTP request that is received in the
 * {@link io.vertx.rxjava.ext.web.Router#accept} of the router.
 * <p>
 * The same instance is passed to any matching request or failure handlers during the routing of the request or
 * failure.
 * <p>
 * The context provides access to the  and 
 * and allows you to maintain arbitrary data that lives for the lifetime of the context. Contexts are discarded once they
 * have been routed to the handler for the request.
 * <p>
 * The context also provides access to the {@link io.vertx.rxjava.ext.web.Session}, cookies and body for the request, given the correct handlers
 * in the application.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.RoutingContext original} non RX-ified interface using Vert.x codegen.
 */

public class RoutingContext {

  final io.vertx.ext.web.RoutingContext delegate;

  public RoutingContext(io.vertx.ext.web.RoutingContext delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  /**
   * @return the HTTP request object
   * @return 
   */
  public HttpServerRequest request() { 
    if (cached_0 != null) {
      return cached_0;
    }
    HttpServerRequest ret= HttpServerRequest.newInstance(this.delegate.request());
    cached_0 = ret;
    return ret;
  }

  /**
   * @return the HTTP response object
   * @return 
   */
  public HttpServerResponse response() { 
    if (cached_1 != null) {
      return cached_1;
    }
    HttpServerResponse ret= HttpServerResponse.newInstance(this.delegate.response());
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
    this.delegate.next();
  }

  /**
   * Fail the context with the specified status code.
   * <p>
   * This will cause the router to route the context to any matching failure handlers for the request. If no failure handlers
   * match a default failure response will be sent.
   * @param statusCode the HTTP status code
   */
  public void fail(int statusCode) { 
    this.delegate.fail(statusCode);
  }

  /**
   * Fail the context with the specified throwable.
   * <p>
   * This will cause the router to route the context to any matching failure handlers for the request. If no failure handlers
   * match a default failure response with status code 500 will be sent.
   * @param throwable a throwable representing the failure
   */
  public void fail(Throwable throwable) { 
    this.delegate.fail(throwable);
  }

  /**
   * Put some arbitrary data in the context. This will be available in any handlers that receive the context.
   * @param key the key for the data
   * @param obj the data
   * @return a reference to this, so the API can be used fluently
   */
  public RoutingContext put(String key, Object obj) { 
    this.delegate.put(key, obj);
    return this;
  }

  /**
   * Get some data from the context. The data is available in any handlers that receive the context.
   * @param key the key for the data
   * @return the data
   */
  public <T> T get(String key) { 
    T ret = (T) this.delegate.get(key);
    return ret;
  }

  /**
   * @return the Vert.x instance associated to the initiating {@link io.vertx.ext.web.Router} for this context
   * @return 
   */
  public Vertx vertx() { 
    Vertx ret= Vertx.newInstance(this.delegate.vertx());
    return ret;
  }

  /**
   * @return the mount point for this router. It will be null for a top level router. For a sub-router it will be the path
   * at which the subrouter was mounted.
   * @return 
   */
  public String mountPoint() { 
    String ret = this.delegate.mountPoint();
    return ret;
  }

  /**
   * @return the current route this context is being routed through.
   * @return 
   */
  public Route currentRoute() { 
    Route ret= Route.newInstance(this.delegate.currentRoute());
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
    String ret = this.delegate.normalisedPath();
    return ret;
  }

  /**
   * Get the cookie with the specified name. The context must have first been routed to a {@link io.vertx.ext.web.handler.CookieHandler}
   * for this to work.
   * @param name the cookie name
   * @return the cookie
   */
  public Cookie getCookie(String name) { 
    Cookie ret= Cookie.newInstance(this.delegate.getCookie(name));
    return ret;
  }

  /**
   * Add a cookie. This will be sent back to the client in the response. The context must have first been routed
   * to a {@link io.vertx.rxjava.ext.web.handler.CookieHandler} for this to work.
   * @param cookie the cookie
   * @return a reference to this, so the API can be used fluently
   */
  public RoutingContext addCookie(Cookie cookie) { 
    this.delegate.addCookie((io.vertx.ext.web.Cookie) cookie.getDelegate());
    return this;
  }

  /**
   * Remove a cookie. The context must have first been routed to a {@link io.vertx.rxjava.ext.web.handler.CookieHandler}
   * for this to work.
   * @param name the name of the cookie
   * @return the cookie, if it existed, or null
   */
  public Cookie removeCookie(String name) { 
    Cookie ret= Cookie.newInstance(this.delegate.removeCookie(name));
    return ret;
  }

  /**
   * @return the number of cookies. The context must have first been routed to a {@link io.vertx.rxjava.ext.web.handler.CookieHandler}
   * for this to work.
   * @return 
   */
  public int cookieCount() { 
    int ret = this.delegate.cookieCount();
    return ret;
  }

  /**
   * @return a set of all the cookies. The context must have first been routed to a {@link io.vertx.ext.web.handler.CookieHandler}
   * for this to be populated.
   * @return 
   */
  public Set<Cookie> cookies() { 
    Set<Cookie> ret = this.delegate.cookies().stream().map(Cookie::newInstance).collect(java.util.stream.Collectors.toSet());
    return ret;
  }

  /**
   * @return  the entire HTTP request body as a string, assuming UTF-8 encoding. The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   * @return 
   */
  public String getBodyAsString() { 
    String ret = this.delegate.getBodyAsString();
    return ret;
  }

  /**
   * Get the entire HTTP request body as a string, assuming the specified encoding. The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   * @param encoding the encoding, e.g. "UTF-16"
   * @return the body
   */
  public String getBodyAsString(String encoding) { 
    String ret = this.delegate.getBodyAsString(encoding);
    return ret;
  }

  /**
   * @return Get the entire HTTP request body as a . The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   * @return 
   */
  public JsonObject getBodyAsJson() { 
    JsonObject ret = this.delegate.getBodyAsJson();
    return ret;
  }

  /**
   * @return Get the entire HTTP request body as a . The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   * @return 
   */
  public Buffer getBody() { 
    Buffer ret= Buffer.newInstance(this.delegate.getBody());
    return ret;
  }

  /**
   * @return a set of fileuploads (if any) for the request. The context must have first been routed to a
   * {@link io.vertx.rxjava.ext.web.handler.BodyHandler} for this to work.
   * @return 
   */
  public Set<FileUpload> fileUploads() { 
    Set<FileUpload> ret = this.delegate.fileUploads().stream().map(FileUpload::newInstance).collect(java.util.stream.Collectors.toSet());
    return ret;
  }

  /**
   * Get the session. The context must have first been routed to a {@link io.vertx.ext.web.handler.SessionHandler}
   * for this to be populated.
   * Sessions live for a browser session, and are maintained by session cookies.
   * @return the session.
   */
  public Session session() { 
    Session ret= Session.newInstance(this.delegate.session());
    return ret;
  }

  /**
   * Get the authenticated user (if any). This will usually be injected by an auth handler if authentication if successful.
   * @return the user, or null if the current user is not authenticated.
   */
  public User user() { 
    User ret= User.newInstance(this.delegate.user());
    return ret;
  }

  /**
   * If the context is being routed to failure handlers after a failure has been triggered by calling
   * {@link io.vertx.rxjava.ext.web.RoutingContext#fail}  then this will return that status code.  It can be used by failure handlers to render a response,
   * e.g. create a failure response page.
   * @return the status code used when signalling failure
   */
  public int statusCode() { 
    if (cached_2 != null) {
      return cached_2;
    }
    int ret = this.delegate.statusCode();
    cached_2 = ret;
    return ret;
  }

  /**
   * If the route specifies produces matches, e.g. produces `text/html` and `text/plain`, and the `accept` header
   * matches one or more of these then this returns the most acceptable match.
   * @return the most acceptable content type.
   */
  public String getAcceptableContentType() { 
    String ret = this.delegate.getAcceptableContentType();
    return ret;
  }

  /**
   * Add a handler that will be called just before headers are written to the response. This gives you a hook where
   * you can write any extra headers before the response has been written when it will be too late.
   * The handler will be passed a future, when you've completed the work you want to do you should complete (or fail)
   * the future. This can be done after the handler has returned.
   * @param handler the handler
   * @return the id of the handler. This can be used if you later want to remove the handler.
   */
  public int addHeadersEndHandler(Handler<Future> handler) { 
    int ret = this.delegate.addHeadersEndHandler(new Handler<io.vertx.core.Future>() {
      public void handle(io.vertx.core.Future event) {
        handler.handle(new Future(event));
      }
    });
    return ret;
  }

  /**
   * Remove a headers end handler
   * @param handlerID the id as returned from {@link io.vertx.ext.web.RoutingContext}.
   * @return true if the handler existed and was removed, false otherwise
   */
  public boolean removeHeadersEndHandler(int handlerID) { 
    boolean ret = this.delegate.removeHeadersEndHandler(handlerID);
    return ret;
  }

  /**
   * Add a handler that will be called just before the response body has been completely written.
   * This gives you a hook where you can write any extra data to the response before it has ended when it will be too late.
   * @param handler the handler
   * @return the id of the handler. This can be used if you later want to remove the handler.
   */
  public int addBodyEndHandler(Handler<Void> handler) { 
    int ret = this.delegate.addBodyEndHandler(handler);
    return ret;
  }

  /**
   * Remove a body end handler
   * @param handlerID the id as returned from {@link io.vertx.ext.web.RoutingContext}.
   * @return true if the handler existed and was removed, false otherwise
   */
  public boolean removeBodyEndHandler(int handlerID) { 
    boolean ret = this.delegate.removeBodyEndHandler(handlerID);
    return ret;
  }

  /**
   * @return true if the context is being routed to failure handlers.
   * @return 
   */
  public boolean failed() { 
    boolean ret = this.delegate.failed();
    return ret;
  }

  /**
   * Set the body. Used by the {@link io.vertx.ext.web.handler.BodyHandler}. You will not normally call this method.
   * @param body the body
   */
  public void setBody(Buffer body) { 
    this.delegate.setBody((io.vertx.core.buffer.Buffer) body.getDelegate());
  }

  /**
   * Set the session. Used by the {@link io.vertx.ext.web.handler.SessionHandler}. You will not normally call this method.
   * @param session the session
   */
  public void setSession(Session session) { 
    this.delegate.setSession((io.vertx.ext.web.Session) session.getDelegate());
  }

  /**
   * Set the user. Usually used by auth handlers to inject a User. You will not normally call this method.
   * @param user the user
   */
  public void setUser(User user) { 
    this.delegate.setUser((io.vertx.ext.auth.User) user.getDelegate());
  }

  /**
   * Clear the current user object in the context. This usually is used for implementing a log out feature, since the
   * current user is unbounded from the routing context.
   */
  public void clearUser() { 
    this.delegate.clearUser();
  }

  /**
   * Set the acceptable content type. Used by
   * @param contentType the content type
   */
  public void setAcceptableContentType(String contentType) { 
    this.delegate.setAcceptableContentType(contentType);
  }

  /**
   * Restarts the current router with a new path and reusing the original method. All path parameters are then parsed
   * and available on the params list.
   * @param path the new http path.
   */
  public void reroute(String path) { 
    this.delegate.reroute(path);
  }

  /**
   * Restarts the current router with a new method and path. All path parameters are then parsed and available on the
   * params list.
   * @param method the new http request
   * @param path the new http path.
   */
  public void reroute(HttpMethod method, String path) { 
    this.delegate.reroute(method, path);
  }

  private HttpServerRequest cached_0;
  private HttpServerResponse cached_1;
  private java.lang.Integer cached_2;

  public static RoutingContext newInstance(io.vertx.ext.web.RoutingContext arg) {
    return arg != null ? new RoutingContext(arg) : null;
  }
}
