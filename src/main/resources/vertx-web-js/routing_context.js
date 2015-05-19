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

/** @module vertx-web-js/routing_context */
var utils = require('vertx-js/util/utils');
var Route = require('vertx-web-js/route');
var Cookie = require('vertx-web-js/cookie');
var FileUpload = require('vertx-web-js/file_upload');
var HttpServerRequest = require('vertx-js/http_server_request');
var Session = require('vertx-web-js/session');
var User = require('vertx-auth-js/user');
var Buffer = require('vertx-js/buffer');
var HttpServerResponse = require('vertx-js/http_server_response');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JRoutingContext = io.vertx.ext.web.RoutingContext;

/**
 Represents the context for the handling of a request in Vert.x-Web.
 <p>
 A new instance is created for each HTTP request that is received in the
 @class
*/
var RoutingContext = function(j_val) {

  var j_routingContext = j_val;
  var that = this;

  /**
   @return the HTTP request object

   @public

   @return {HttpServerRequest}
   */
  this.request = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedrequest == null) {
        that.cachedrequest = new HttpServerRequest(j_routingContext["request()"]());
      }
      return that.cachedrequest;
    } else utils.invalidArgs();
  };

  /**
   @return the HTTP response object

   @public

   @return {HttpServerResponse}
   */
  this.response = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedresponse == null) {
        that.cachedresponse = new HttpServerResponse(j_routingContext["response()"]());
      }
      return that.cachedresponse;
    } else utils.invalidArgs();
  };

  /**
   Tell the router to route this context to the next matching route (if any).
   This method, if called, does not need to be called during the execution of the handler, it can be called
   some arbitrary time later, if required.
   <p>
   If next is not called for a handler then the handler should make sure it ends the response or no response
   will be sent.

   @public

   */
  this.next = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_routingContext["next()"]();
    } else utils.invalidArgs();
  };

  /**
   Fail the context with the specified status code.
   <p>
   This will cause the router to route the context to any matching failure handlers for the request. If no failure handlers
   match a default failure response will be sent.

   @public
   @param statusCode {number} the HTTP status code 
   */
  this.fail = function(statusCode) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      j_routingContext["fail(int)"](statusCode);
    } else utils.invalidArgs();
  };

  /**
   Put some arbitrary data in the context. This will be available in any handlers that receive the context.

   @public
   @param key {string} the key for the data 
   @param obj {Object} the data 
   @return {RoutingContext} a reference to this, so the API can be used fluently
   */
  this.put = function(key, obj) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && true) {
      j_routingContext["put(java.lang.String,java.lang.Object)"](key, utils.convParamTypeUnknown(obj));
      return that;
    } else utils.invalidArgs();
  };

  /**
   Get some data from the context. The data is available in any handlers that receive the context.

   @public
   @param key {string} the key for the data 
   @return {Object} the data
   */
  this.get = function(key) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnTypeUnknown(j_routingContext["get(java.lang.String)"](key));
    } else utils.invalidArgs();
  };

  /**
   @return the Vert.x instance associated to the initiating {@link Router} for this context

   @public

   @return {Vertx}
   */
  this.vertx = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return new Vertx(j_routingContext["vertx()"]());
    } else utils.invalidArgs();
  };

  /**
   @return the mount point for this router. It will be null for a top level router. For a sub-router it will be the path
   at which the subrouter was mounted.

   @public

   @return {string}
   */
  this.mountPoint = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_routingContext["mountPoint()"]();
    } else utils.invalidArgs();
  };

  /**
   @return the current route this context is being routed through.

   @public

   @return {Route}
   */
  this.currentRoute = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return new Route(j_routingContext["currentRoute()"]());
    } else utils.invalidArgs();
  };

  /**
   Return the normalised path for the request.
   <p>
   The normalised path is where the URI path has been decoded, i.e. any unicode or other illegal URL characters that
   were encoded in the original URL with `%` will be returned to their original form. E.g. `%20` will revert to a space.
   Also `+` reverts to a space in a query.
   <p>
   The normalised path will also not contain any `..` character sequences to prevent resources being accessed outside
   of the permitted area.
   <p>
   It's recommended to always use the normalised path as opposed to {@link HttpServerRequest#path}
   if accessing server resources requested by a client.

   @public

   @return {string} the normalised path
   */
  this.normalisedPath = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_routingContext["normalisedPath()"]();
    } else utils.invalidArgs();
  };

  /**
   Get the cookie with the specified name. The context must have first been routed to a {@link CookieHandler}
   for this to work.

   @public
   @param name {string} the cookie name 
   @return {Cookie} the cookie
   */
  this.getCookie = function(name) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return new Cookie(j_routingContext["getCookie(java.lang.String)"](name));
    } else utils.invalidArgs();
  };

  /**
   Add a cookie. This will be sent back to the client in the response. The context must have first been routed
   to a {@link CookieHandler} for this to work.

   @public
   @param cookie {Cookie} the cookie 
   @return {RoutingContext} a reference to this, so the API can be used fluently
   */
  this.addCookie = function(cookie) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_routingContext["addCookie(io.vertx.ext.web.Cookie)"](cookie._jdel);
      return that;
    } else utils.invalidArgs();
  };

  /**
   Remove a cookie. The context must have first been routed to a {@link CookieHandler}
   for this to work.

   @public
   @param name {string} the name of the cookie 
   @return {Cookie} the cookie, if it existed, or null
   */
  this.removeCookie = function(name) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return new Cookie(j_routingContext["removeCookie(java.lang.String)"](name));
    } else utils.invalidArgs();
  };

  /**
   @return the number of cookies. The context must have first been routed to a {@link CookieHandler}
   for this to work.

   @public

   @return {number}
   */
  this.cookieCount = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_routingContext["cookieCount()"]();
    } else utils.invalidArgs();
  };

  /**
   @return a set of all the cookies. The context must have first been routed to a {@link CookieHandler}
   for this to be populated.

   @public

   @return {Array.<Cookie>}
   */
  this.cookies = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnListSetVertxGen(j_routingContext["cookies()"](), Cookie);
    } else utils.invalidArgs();
  };

  /**
   Get the entire HTTP request body as a string, assuming the specified encoding. The context must have first been routed to a
   {@link BodyHandler} for this to be populated.

   @public
   @param encoding {string} the encoding, e.g. "UTF-16" 
   @return {string} the body
   */
  this.getBodyAsString = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_routingContext["getBodyAsString()"]();
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return j_routingContext["getBodyAsString(java.lang.String)"](__args[0]);
    } else utils.invalidArgs();
  };

  /**
   @return Get the entire HTTP request body as a {@link JsonObject}. The context must have first been routed to a
   {@link BodyHandler} for this to be populated.

   @public

   @return {Object}
   */
  this.getBodyAsJson = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnJson(j_routingContext["getBodyAsJson()"]());
    } else utils.invalidArgs();
  };

  /**
   @return Get the entire HTTP request body as a {@link Buffer}. The context must have first been routed to a
   {@link BodyHandler} for this to be populated.

   @public

   @return {Buffer}
   */
  this.getBody = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return new Buffer(j_routingContext["getBody()"]());
    } else utils.invalidArgs();
  };

  /**
   @return a set of fileuploads (if any) for the request. The context must have first been routed to a
   {@link BodyHandler} for this to work.

   @public

   @return {Array.<FileUpload>}
   */
  this.fileUploads = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnListSetVertxGen(j_routingContext["fileUploads()"](), FileUpload);
    } else utils.invalidArgs();
  };

  /**
   Get the session. The context must have first been routed to a {@link SessionHandler}
   for this to be populated.
   Sessions live for a browser session, and are maintained by session cookies.

   @public

   @return {Session} the session.
   */
  this.session = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return new Session(j_routingContext["session()"]());
    } else utils.invalidArgs();
  };

  /**
   Get the authenticated user (if any). This will usually be injected by an auth handler if authentication if successful.

   @public

   @return {User} the user, or null if the current user is not authenticated.
   */
  this.user = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return new User(j_routingContext["user()"]());
    } else utils.invalidArgs();
  };

  /**
   If the context is being routed to failure handlers after a failure has been triggered by calling
   {@link RoutingContext#fail}  then this will return that status code.  It can be used by failure handlers to render a response,
   e.g. create a failure response page.

   @public

   @return {number} the status code used when signalling failure
   */
  this.statusCode = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedstatusCode == null) {
        that.cachedstatusCode = j_routingContext["statusCode()"]();
      }
      return that.cachedstatusCode;
    } else utils.invalidArgs();
  };

  /**
   If the route specifies produces matches, e.g. produces `text/html` and `text/plain`, and the `accept` header
   matches one or more of these then this returns the most acceptable match.

   @public

   @return {string} the most acceptable content type.
   */
  this.getAcceptableContentType = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_routingContext["getAcceptableContentType()"]();
    } else utils.invalidArgs();
  };

  /**
   Add a handler that will be called just before headers are written to the response. This gives you a hook where
   you can write any extra headers before the response has been written when it will be too late.

   @public
   @param handler {function} the handler 
   @return {number} the id of the handler. This can be used if you later want to remove the handler.
   */
  this.addHeadersEndHandler = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      return j_routingContext["addHeadersEndHandler(io.vertx.core.Handler)"](handler);
    } else utils.invalidArgs();
  };

  /**
   Remove a headers end handler

   @public
   @param handlerID {number} the id as returned from {@link io.vertx.ext.web.RoutingContext#addHeadersEndHandler(io.vertx.core.Handler)}. 
   @return {boolean} true if the handler existed and was removed, false otherwise
   */
  this.removeHeadersEndHandler = function(handlerID) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      return j_routingContext["removeHeadersEndHandler(int)"](handlerID);
    } else utils.invalidArgs();
  };

  /**
   Add a handler that will be called just before the response body has been completely written.
   This gives you a hook where you can write any extra data to the response before it has ended when it will be too late.

   @public
   @param handler {function} the handler 
   @return {number} the id of the handler. This can be used if you later want to remove the handler.
   */
  this.addBodyEndHandler = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      return j_routingContext["addBodyEndHandler(io.vertx.core.Handler)"](handler);
    } else utils.invalidArgs();
  };

  /**
   Remove a body end handler

   @public
   @param handlerID {number} the id as returned from {@link io.vertx.ext.web.RoutingContext#addBodyEndHandler(io.vertx.core.Handler)}. 
   @return {boolean} true if the handler existed and was removed, false otherwise
   */
  this.removeBodyEndHandler = function(handlerID) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      return j_routingContext["removeBodyEndHandler(int)"](handlerID);
    } else utils.invalidArgs();
  };

  /**
   @return true if the context is being routed to failure handlers.

   @public

   @return {boolean}
   */
  this.failed = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_routingContext["failed()"]();
    } else utils.invalidArgs();
  };

  /**
   Set the body. Used by the {@link BodyHandler}. You will not normally call this method.

   @public
   @param body {Buffer} the body 
   */
  this.setBody = function(body) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_routingContext["setBody(io.vertx.core.buffer.Buffer)"](body._jdel);
    } else utils.invalidArgs();
  };

  /**
   Set the session. Used by the {@link SessionHandler}. You will not normally call this method.

   @public
   @param session {Session} the session 
   */
  this.setSession = function(session) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_routingContext["setSession(io.vertx.ext.web.Session)"](session._jdel);
    } else utils.invalidArgs();
  };

  /**
   Set the user. Usually used by auth handlers to inject a User. You will not normally call this method.

   @public
   @param user {User} the user 
   */
  this.setUser = function(user) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_routingContext["setUser(io.vertx.ext.auth.User)"](user._jdel);
    } else utils.invalidArgs();
  };

  /**
   Set the acceptable content type. Used by

   @public
   @param contentType {string} the content type 
   */
  this.setAcceptableContentType = function(contentType) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_routingContext["setAcceptableContentType(java.lang.String)"](contentType);
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_routingContext;
};

// We export the Constructor function
module.exports = RoutingContext;