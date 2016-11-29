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
var HttpRequestTemplate = require('vertx-web-client-js/http_request_template');
var HttpClient = require('vertx-js/http_client');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JWebClient = io.vertx.webclient.WebClient;

/**

 @class
*/
var WebClient = function(j_val) {

  var j_webClient = j_val;
  var that = this;

  /**

   @public
   @param port {number} 
   @param host {string} 
   @param requestURI {string} 
   @return {HttpRequestTemplate}
   */
  this.get = function(port, host, requestURI) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] ==='number' && typeof __args[1] === 'string' && typeof __args[2] === 'string') {
      return utils.convReturnVertxGen(HttpRequestTemplate, j_webClient["get(int,java.lang.String,java.lang.String)"](port, host, requestURI));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param port {number} 
   @param host {string} 
   @param requestURI {string} 
   @return {HttpRequestTemplate}
   */
  this.post = function(port, host, requestURI) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] ==='number' && typeof __args[1] === 'string' && typeof __args[2] === 'string') {
      return utils.convReturnVertxGen(HttpRequestTemplate, j_webClient["post(int,java.lang.String,java.lang.String)"](port, host, requestURI));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param port {number} 
   @param host {string} 
   @param requestURI {string} 
   @return {HttpRequestTemplate}
   */
  this.put = function(port, host, requestURI) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] ==='number' && typeof __args[1] === 'string' && typeof __args[2] === 'string') {
      return utils.convReturnVertxGen(HttpRequestTemplate, j_webClient["put(int,java.lang.String,java.lang.String)"](port, host, requestURI));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param port {number} 
   @param host {string} 
   @param requestURI {string} 
   @return {HttpRequestTemplate}
   */
  this.delete = function(port, host, requestURI) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] ==='number' && typeof __args[1] === 'string' && typeof __args[2] === 'string') {
      return utils.convReturnVertxGen(HttpRequestTemplate, j_webClient["delete(int,java.lang.String,java.lang.String)"](port, host, requestURI));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param port {number} 
   @param host {string} 
   @param requestURI {string} 
   @return {HttpRequestTemplate}
   */
  this.patch = function(port, host, requestURI) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] ==='number' && typeof __args[1] === 'string' && typeof __args[2] === 'string') {
      return utils.convReturnVertxGen(HttpRequestTemplate, j_webClient["patch(int,java.lang.String,java.lang.String)"](port, host, requestURI));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param port {number} 
   @param host {string} 
   @param requestURI {string} 
   @return {HttpRequestTemplate}
   */
  this.head = function(port, host, requestURI) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] ==='number' && typeof __args[1] === 'string' && typeof __args[2] === 'string') {
      return utils.convReturnVertxGen(HttpRequestTemplate, j_webClient["head(int,java.lang.String,java.lang.String)"](port, host, requestURI));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param absoluteURI {string} 
   @return {HttpRequestTemplate}
   */
  this.getAbs = function(absoluteURI) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequestTemplate, j_webClient["getAbs(java.lang.String)"](absoluteURI));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param absoluteURI {string} 
   @return {HttpRequestTemplate}
   */
  this.postAbs = function(absoluteURI) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequestTemplate, j_webClient["postAbs(java.lang.String)"](absoluteURI));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param absoluteURI {string} 
   @return {HttpRequestTemplate}
   */
  this.putAbs = function(absoluteURI) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequestTemplate, j_webClient["putAbs(java.lang.String)"](absoluteURI));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param absoluteURI {string} 
   @return {HttpRequestTemplate}
   */
  this.deleteAbs = function(absoluteURI) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequestTemplate, j_webClient["deleteAbs(java.lang.String)"](absoluteURI));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param absoluteURI {string} 
   @return {HttpRequestTemplate}
   */
  this.patchAbs = function(absoluteURI) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequestTemplate, j_webClient["patchAbs(java.lang.String)"](absoluteURI));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param absoluteURI {string} 
   @return {HttpRequestTemplate}
   */
  this.headAbs = function(absoluteURI) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpRequestTemplate, j_webClient["headAbs(java.lang.String)"](absoluteURI));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_webClient;
};

WebClient._jclass = utils.getJavaClass("io.vertx.webclient.WebClient");
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

 @memberof module:vertx-web-client-js/web_client
 @param client {HttpClient} 
 @return {WebClient}
 */
WebClient.create = function(client) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(WebClient, JWebClient["create(io.vertx.core.http.HttpClient)"](client._jdel));
  } else throw new TypeError('function invoked with invalid arguments');
};

module.exports = WebClient;