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

package io.vertx.groovy.ext.apex;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.groovy.core.http.HttpServerRequest
import io.vertx.groovy.core.Vertx
import java.util.Set
import io.vertx.groovy.core.buffer.Buffer
import io.vertx.groovy.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.core.Handler
/**
 * Represents the context for the handling of a request in Apex.
 * <p>
 * A new instance is created for each HTTP request that is received in the
 * link of the router.
 * <p>
 * The same instance is passed to any matching request or failure handlers during the routing of the request or
 * failure.
 * <p>
 * The context provides access to the link and link
 * and allows you to maintain arbitrary data that lives for the lifetime of the context. Contexts are discarded once they
 * have been routed to the handler for the request.
 * <p>
 * The context also provides access to the link, cookies and body for the request, given the correct handlers
 * in the application.
*/
@CompileStatic
public class RoutingContext {
  final def io.vertx.ext.apex.RoutingContext delegate;
  public RoutingContext(io.vertx.ext.apex.RoutingContext delegate) {
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
    def ret= HttpServerRequest.FACTORY.apply(this.delegate.request());
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
    def ret= HttpServerResponse.FACTORY.apply(this.delegate.response());
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
   * Put some arbitrary data in the context. This will be available in any handlers that receive the context.
   * @param key the key for the data
   * @param obj the data
   * @return a reference to this, so the API can be used fluently
   */
  public RoutingContext put(String key, Object obj) {
    this.delegate.put(key, InternalHelper.unwrapObject(obj));
    return this;
  }
  /**
   * Get some data from the context. The data is available in any handlers that receive the context.
   * @param key the key for the data
   * @return the data
   */
  public <T> T get(String key) {
    // This cast is cleary flawed
    def ret = (T) InternalHelper.wrapObject(this.delegate.get(key));
    return ret;
  }
  /**
   * @return the Vert.x instance associated to the initiating link for this context
   * @return 
   */
  public Vertx vertx() {
    def ret= Vertx.FACTORY.apply(this.delegate.vertx());
    return ret;
  }
  /**
   * @return the mount point for this router. It will be null for a top level router. For a sub-router it will be the path
   * at which the subrouter was mounted.
   * @return 
   */
  public String mountPoint() {
    def ret = this.delegate.mountPoint();
    return ret;
  }
  /**
   * @return the current route this context is being routed through.
   * @return 
   */
  public Route currentRoute() {
    def ret= Route.FACTORY.apply(this.delegate.currentRoute());
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
   * It's recommended to always use the normalised path as opposed to link
   * if accessing server resources requested by a client.
   * @return the normalised path
   */
  public String normalisedPath() {
    def ret = this.delegate.normalisedPath();
    return ret;
  }
  /**
   * Get the cookie with the specified name. The context must have first been routed to a link
   * for this to work.
   * @param name the cookie name
   * @return the cookie
   */
  public Cookie getCookie(String name) {
    def ret= Cookie.FACTORY.apply(this.delegate.getCookie(name));
    return ret;
  }
  /**
   * Add a cookie. This will be sent back to the client in the response. The context must have first been routed
   * to a link for this to work.
   * @param cookie the cookie
   * @return a reference to this, so the API can be used fluently
   */
  public RoutingContext addCookie(Cookie cookie) {
    this.delegate.addCookie((io.vertx.ext.apex.Cookie)cookie.getDelegate());
    return this;
  }
  /**
   * Remove a cookie. The context must have first been routed to a link
   * for this to work.
   * @param name the name of the cookie
   * @return the cookie, if it existed, or null
   */
  public Cookie removeCookie(String name) {
    def ret= Cookie.FACTORY.apply(this.delegate.removeCookie(name));
    return ret;
  }
  /**
   * @return the number of cookies. The context must have first been routed to a link
   * for this to work.
   * @return 
   */
  public int cookieCount() {
    def ret = this.delegate.cookieCount();
    return ret;
  }
  /**
   * @return a set of all the cookies. The context must have first been routed to a link
   * for this to be populated.
   * @return 
   */
  public Set<Cookie> cookies() {
    def ret = this.delegate.cookies()?.collect({underpants -> Cookie.FACTORY.apply(underpants)}) as Set;
    return ret;
  }
  /**
   * @return  the entire HTTP request body as a string, assuming UTF-8 encoding. The context must have first been routed to a
   * link for this to be populated.
   * @return 
   */
  public String getBodyAsString() {
    def ret = this.delegate.getBodyAsString();
    return ret;
  }
  /**
   * Get the entire HTTP request body as a string, assuming the specified encoding. The context must have first been routed to a
   * link for this to be populated.
   * @param encoding the encoding, e.g. "UTF-16"
   * @return the body
   */
  public String getBodyAsString(String encoding) {
    def ret = this.delegate.getBodyAsString(encoding);
    return ret;
  }
  /**
   * @return Get the entire HTTP request body as a link. The context must have first been routed to a
   * link for this to be populated.
   * @return 
   */
  public Map<String, Object> getBodyAsJson() {
    def ret = this.delegate.getBodyAsJson()?.getMap();
    return ret;
  }
  /**
   * @return Get the entire HTTP request body as a link. The context must have first been routed to a
   * link for this to be populated.
   * @return 
   */
  public Buffer getBody() {
    def ret= Buffer.FACTORY.apply(this.delegate.getBody());
    return ret;
  }
  /**
   * @return a set of fileuploads (if any) for the request. The context must have first been routed to a
   * link for this to work.
   * @return 
   */
  public Set<FileUpload> fileUploads() {
    def ret = this.delegate.fileUploads()?.collect({underpants -> FileUpload.FACTORY.apply(underpants)}) as Set;
    return ret;
  }
  /**
   * Get the session. The context must have first been routed to a link
   * for this to be populated.
   * Sessions live for a browser session, and are maintained by session cookies.
   * @return the session.
   */
  public Session session() {
    def ret= Session.FACTORY.apply(this.delegate.session());
    return ret;
  }
  /**
   * If the context is being routed to failure handlers after a failure has been triggered by calling
   * link  then this will return that status code.  It can be used by failure handlers to render a response,
   * e.g. create a failure response page.
   * @return the status code used when signalling failure
   */
  public int statusCode() {
    if (cached_2 != null) {
      return cached_2;
    }
    def ret = this.delegate.statusCode();
    cached_2 = ret;
    return ret;
  }
  /**
   * If the route specifies produces matches, e.g. produces `text/html` and `text/plain`, and the `accept` header
   * matches one or more of these then this returns the most acceptable match.
   * @return the most acceptable content type.
   */
  public String getAcceptableContentType() {
    def ret = this.delegate.getAcceptableContentType();
    return ret;
  }
  /**
   * Add a handler that will be called just before headers are written to the response. This gives you a hook where
   * you can write any extra headers before the response has been written when it will be too late.
   * @param handler the handler
   * @return the id of the handler. This can be used if you later want to remove the handler.
   */
  public int addHeadersEndHandler(Handler<Void> handler) {
    def ret = this.delegate.addHeadersEndHandler(handler);
    return ret;
  }
  /**
   * Remove a headers end handler
   * @param handlerID the id as returned from {@link io.vertx.ext.apex.RoutingContext#addHeadersEndHandler(io.vertx.core.Handler)}.
   * @return true if the handler existed and was removed, false otherwise
   */
  public boolean removeHeadersEndHandler(int handlerID) {
    def ret = this.delegate.removeHeadersEndHandler(handlerID);
    return ret;
  }
  /**
   * Add a handler that will be called just before the response body has been completely written.
   * This gives you a hook where you can write any extra data to the response before it has ended when it will be too late.
   * @param handler the handler
   * @return the id of the handler. This can be used if you later want to remove the handler.
   */
  public int addBodyEndHandler(Handler<Void> handler) {
    def ret = this.delegate.addBodyEndHandler(handler);
    return ret;
  }
  /**
   * Remove a body end handler
   * @param handlerID the id as returned from {@link io.vertx.ext.apex.RoutingContext#addBodyEndHandler(io.vertx.core.Handler)}.
   * @return true if the handler existed and was removed, false otherwise
   */
  public boolean removeBodyEndHandler(int handlerID) {
    def ret = this.delegate.removeBodyEndHandler(handlerID);
    return ret;
  }
  /**
   * @return true if the context is being routed to failure handlers.
   * @return 
   */
  public boolean failed() {
    def ret = this.delegate.failed();
    return ret;
  }
  /**
   * Set the body. Used by the link. You will not normally call this method.
   * @param body the body
   */
  public void setBody(Buffer body) {
    this.delegate.setBody((io.vertx.core.buffer.Buffer)body.getDelegate());
  }
  /**
   * Set the session. Used by the link. You will not normally call this method.
   * @param session the session
   */
  public void setSession(Session session) {
    this.delegate.setSession((io.vertx.ext.apex.Session)session.getDelegate());
  }
  /**
   * Set the acceptable content type. Used by
   * @param contentType the content type
   */
  public void setAcceptableContentType(String contentType) {
    this.delegate.setAcceptableContentType(contentType);
  }
  private HttpServerRequest cached_0;
  private HttpServerResponse cached_1;
  private int cached_2;

  static final java.util.function.Function<io.vertx.ext.apex.RoutingContext, RoutingContext> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.RoutingContext arg -> new RoutingContext(arg);
  };
}
