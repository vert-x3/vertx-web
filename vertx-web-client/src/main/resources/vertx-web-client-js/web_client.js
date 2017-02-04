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

/** @module vertx-web-client-js/web_client */
var utils = require('vertx-js/util/utils');
var Buffer = require('vertx-js/buffer');
var Vertx = require('vertx-js/vertx');
var HttpClient = require('vertx-js/http_client');
var HttpRequest = require('vertx-web-client-js/http_request');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JWebClient = Java.type('io.vertx.ext.web.client.WebClient');

/**

 @class
*/
var WebClient = function(j_val) {

  var j_webClient = j_val;
  var that = this;

  /**
   Create an HTTP request to send to the server at the specified host and port.

   @public
   @param method {Object} the HTTP method 
   @param port {number} the port 
   @param host {string} the host 
   @param requestURI {string} the relative URI 
   @return {HttpRequest} an HTTP client request object
   */
  this.request = function() {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["request(io.vertx.core.http.HttpMethod,java.lang.String)"](io.vertx.core.http.HttpMethod.valueOf(__args[0]), __args[1]), Buffer._jtype);
    }  else if (__args.length === 3 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["request(io.vertx.core.http.HttpMethod,java.lang.String,java.lang.String)"](io.vertx.core.http.HttpMethod.valueOf(__args[0]), __args[1], __args[2]), Buffer._jtype);
    }  else if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] ==='number' && typeof __args[2] === 'string' && typeof __args[3] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["request(io.vertx.core.http.HttpMethod,int,java.lang.String,java.lang.String)"](io.vertx.core.http.HttpMethod.valueOf(__args[0]), __args[1], __args[2], __args[3]), Buffer._jtype);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Create an HTTP request to send to the server using an absolute URI

   @public
   @param method {Object} the HTTP method 
   @param absoluteURI {string} the absolute URI 
   @return {HttpRequest} an HTTP client request object
   */
  this.requestAbs = function(method, absoluteURI) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["requestAbs(io.vertx.core.http.HttpMethod,java.lang.String)"](io.vertx.core.http.HttpMethod.valueOf(method), absoluteURI), Buffer._jtype);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Create an HTTP GET request to send to the server at the specified host and port.

   @public
   @param port {number} the port 
   @param host {string} the host 
   @param requestURI {string} the relative URI 
   @return {HttpRequest} an HTTP client request object
   */
  this.get = function() {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["get(java.lang.String)"](__args[0]), Buffer._jtype);
    }  else if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["get(java.lang.String,java.lang.String)"](__args[0], __args[1]), Buffer._jtype);
    }  else if (__args.length === 3 && typeof __args[0] ==='number' && typeof __args[1] === 'string' && typeof __args[2] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["get(int,java.lang.String,java.lang.String)"](__args[0], __args[1], __args[2]), Buffer._jtype);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Create an HTTP GET request to send to the server using an absolute URI, specifying a response handler to receive
   the response

   @public
   @param absoluteURI {string} the absolute URI 
   @return {HttpRequest} an HTTP client request object
   */
  this.getAbs = function(absoluteURI) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["getAbs(java.lang.String)"](absoluteURI), Buffer._jtype);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Create an HTTP POST request to send to the server at the specified host and port.

   @public
   @param port {number} the port 
   @param host {string} the host 
   @param requestURI {string} the relative URI 
   @return {HttpRequest} an HTTP client request object
   */
  this.post = function() {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["post(java.lang.String)"](__args[0]), Buffer._jtype);
    }  else if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["post(java.lang.String,java.lang.String)"](__args[0], __args[1]), Buffer._jtype);
    }  else if (__args.length === 3 && typeof __args[0] ==='number' && typeof __args[1] === 'string' && typeof __args[2] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["post(int,java.lang.String,java.lang.String)"](__args[0], __args[1], __args[2]), Buffer._jtype);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Create an HTTP POST request to send to the server using an absolute URI, specifying a response handler to receive
   the response

   @public
   @param absoluteURI {string} the absolute URI 
   @return {HttpRequest} an HTTP client request object
   */
  this.postAbs = function(absoluteURI) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["postAbs(java.lang.String)"](absoluteURI), Buffer._jtype);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Create an HTTP PUT request to send to the server at the specified host and port.

   @public
   @param port {number} the port 
   @param host {string} the host 
   @param requestURI {string} the relative URI 
   @return {HttpRequest} an HTTP client request object
   */
  this.put = function() {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["put(java.lang.String)"](__args[0]), Buffer._jtype);
    }  else if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["put(java.lang.String,java.lang.String)"](__args[0], __args[1]), Buffer._jtype);
    }  else if (__args.length === 3 && typeof __args[0] ==='number' && typeof __args[1] === 'string' && typeof __args[2] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["put(int,java.lang.String,java.lang.String)"](__args[0], __args[1], __args[2]), Buffer._jtype);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Create an HTTP PUT request to send to the server using an absolute URI, specifying a response handler to receive
   the response

   @public
   @param absoluteURI {string} the absolute URI 
   @return {HttpRequest} an HTTP client request object
   */
  this.putAbs = function(absoluteURI) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["putAbs(java.lang.String)"](absoluteURI), Buffer._jtype);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Create an HTTP DELETE request to send to the server at the specified host and port.

   @public
   @param port {number} the port 
   @param host {string} the host 
   @param requestURI {string} the relative URI 
   @return {HttpRequest} an HTTP client request object
   */
  this.delete = function() {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["delete(java.lang.String)"](__args[0]), Buffer._jtype);
    }  else if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["delete(java.lang.String,java.lang.String)"](__args[0], __args[1]), Buffer._jtype);
    }  else if (__args.length === 3 && typeof __args[0] ==='number' && typeof __args[1] === 'string' && typeof __args[2] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["delete(int,java.lang.String,java.lang.String)"](__args[0], __args[1], __args[2]), Buffer._jtype);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Create an HTTP DELETE request to send to the server using an absolute URI, specifying a response handler to receive
   the response

   @public
   @param absoluteURI {string} the absolute URI 
   @return {HttpRequest} an HTTP client request object
   */
  this.deleteAbs = function(absoluteURI) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["deleteAbs(java.lang.String)"](absoluteURI), Buffer._jtype);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Create an HTTP PATCH request to send to the server at the specified host and port.

   @public
   @param port {number} the port 
   @param host {string} the host 
   @param requestURI {string} the relative URI 
   @return {HttpRequest} an HTTP client request object
   */
  this.patch = function() {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["patch(java.lang.String)"](__args[0]), Buffer._jtype);
    }  else if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["patch(java.lang.String,java.lang.String)"](__args[0], __args[1]), Buffer._jtype);
    }  else if (__args.length === 3 && typeof __args[0] ==='number' && typeof __args[1] === 'string' && typeof __args[2] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["patch(int,java.lang.String,java.lang.String)"](__args[0], __args[1], __args[2]), Buffer._jtype);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Create an HTTP PATCH request to send to the server using an absolute URI, specifying a response handler to receive
   the response

   @public
   @param absoluteURI {string} the absolute URI 
   @return {HttpRequest} an HTTP client request object
   */
  this.patchAbs = function(absoluteURI) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["patchAbs(java.lang.String)"](absoluteURI), Buffer._jtype);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Create an HTTP HEAD request to send to the server at the specified host and port.

   @public
   @param port {number} the port 
   @param host {string} the host 
   @param requestURI {string} the relative URI 
   @return {HttpRequest} an HTTP client request object
   */
  this.head = function() {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["head(java.lang.String)"](__args[0]), Buffer._jtype);
    }  else if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["head(java.lang.String,java.lang.String)"](__args[0], __args[1]), Buffer._jtype);
    }  else if (__args.length === 3 && typeof __args[0] ==='number' && typeof __args[1] === 'string' && typeof __args[2] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["head(int,java.lang.String,java.lang.String)"](__args[0], __args[1], __args[2]), Buffer._jtype);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Create an HTTP HEAD request to send to the server using an absolute URI, specifying a response handler to receive
   the response

   @public
   @param absoluteURI {string} the absolute URI 
   @return {HttpRequest} an HTTP client request object
   */
  this.headAbs = function(absoluteURI) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequest, j_webClient["headAbs(java.lang.String)"](absoluteURI), Buffer._jtype);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Close the client. Closing will close down any pooled connections.
   Clients should always be closed after use.

   @public

   */
  this.close = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_webClient["close()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_webClient;
};

WebClient._jclass = utils.getJavaClass("io.vertx.ext.web.client.WebClient");
WebClient._jtype = {
  accept: function(obj) {
    return WebClient._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(WebClient.prototype, {});
    WebClient.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
WebClient._create = function(jdel) {
  var obj = Object.create(WebClient.prototype, {});
  WebClient.apply(obj, arguments);
  return obj;
}
/**
 Create a web client using the provided <code>vertx</code> instance.

 @memberof module:vertx-web-client-js/web_client
 @param vertx {Vertx} the vertx instance 
 @return {WebClient} the created web client
 */
WebClient.create = function(vertx) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(WebClient, JWebClient["create(io.vertx.core.Vertx)"](vertx._jdel));
  } else throw new TypeError('function invoked with invalid arguments');
};

/**
 Wrap an <code>httpClient</code> with a web client.

 @memberof module:vertx-web-client-js/web_client
 @param httpClient {HttpClient} the  to wrap 
 @return {WebClient} the web client
 */
WebClient.wrap = function(httpClient) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(WebClient, JWebClient["wrap(io.vertx.core.http.HttpClient)"](httpClient._jdel));
  } else throw new TypeError('function invoked with invalid arguments');
};

module.exports = WebClient;