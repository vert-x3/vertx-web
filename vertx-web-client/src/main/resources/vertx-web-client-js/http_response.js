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

/** @module vertx-web-client-js/http_response */
var utils = require('vertx-js/util/utils');
var HttpClientResponse = require('vertx-js/http_client_response');
var Buffer = require('vertx-js/buffer');
var MultiMap = require('vertx-js/multi_map');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JHttpResponse = io.vertx.webclient.HttpResponse;

/**
 An HTTP response.
 <p>
 The usual HTTP response attributes are available:
 <ul>
 @class
*/
var HttpResponse = function(j_val, j_arg_0) {

  var j_httpResponse = j_val;
  var that = this;
  var j_T = typeof j_arg_0 !== 'undefined' ? j_arg_0 : utils.unknown_jtype;

  /**

   @public

   @return {Object} the version of the response
   */
  this.version = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedversion == null) {
        that.cachedversion = utils.convReturnEnum(j_httpResponse["version()"]());
      }
      return that.cachedversion;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {number} the status code of the response
   */
  this.statusCode = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedstatusCode == null) {
        that.cachedstatusCode = j_httpResponse["statusCode()"]();
      }
      return that.cachedstatusCode;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {string} the status message of the response
   */
  this.statusMessage = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedstatusMessage == null) {
        that.cachedstatusMessage = j_httpResponse["statusMessage()"]();
      }
      return that.cachedstatusMessage;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {MultiMap} the headers
   */
  this.headers = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedheaders == null) {
        that.cachedheaders = utils.convReturnVertxGen(MultiMap, j_httpResponse["headers()"]());
      }
      return that.cachedheaders;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Return the first header value with the specified name

   @public
   @param headerName {string} the header name 
   @return {string} the header value
   */
  this.getHeader = function(headerName) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return j_httpResponse["getHeader(java.lang.String)"](headerName);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {MultiMap} the trailers
   */
  this.trailers = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedtrailers == null) {
        that.cachedtrailers = utils.convReturnVertxGen(MultiMap, j_httpResponse["trailers()"]());
      }
      return that.cachedtrailers;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Return the first trailer value with the specified name

   @public
   @param trailerName {string} the trailer name 
   @return {string} the trailer value
   */
  this.getTrailer = function(trailerName) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return j_httpResponse["getTrailer(java.lang.String)"](trailerName);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {Array.<string>} the Set-Cookie headers (including trailers)
   */
  this.cookies = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedcookies == null) {
        that.cachedcookies = j_httpResponse["cookies()"]();
      }
      return that.cachedcookies;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {Object} the response body in the format it was decoded.
   */
  this.body = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedbody == null) {
        that.cachedbody = j_T.wrap(j_httpResponse["body()"]());
      }
      return that.cachedbody;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {Buffer} the response body decoded as a 
   */
  this.bodyAsBuffer = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(Buffer, j_httpResponse["bodyAsBuffer()"]());
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param encoding {string} 
   @return {string} the response body decoded as a <code>String</code> given a specific <code>encoding</code>
   */
  this.bodyAsString = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_httpResponse["bodyAsString()"]();
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return j_httpResponse["bodyAsString(java.lang.String)"](__args[0]);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {Object} the response body decoded as a json object
   */
  this.bodyAsJsonObject = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnJson(j_httpResponse["bodyAsJsonObject()"]());
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param type {todo} 
   @return {Object} the response body decoded as the specified <code>type</code> with the Jackson mapper.
   */
  this.bodyAs = function(type) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      return utils.get_jtype(type).wrap(j_httpResponse["bodyAs(java.lang.Class)"](utils.get_jclass(type)));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {HttpClientResponse}
   */
  this.abc = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(HttpClientResponse, j_httpResponse["abc()"]());
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param handler {function} 
   */
  this.bufferBody = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_httpResponse["bufferBody(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        handler(utils.convReturnVertxGen(Buffer, ar.result()), null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_httpResponse;
};

HttpResponse._jclass = utils.getJavaClass("io.vertx.webclient.HttpResponse");
HttpResponse._jtype = {
  accept: function(obj) {
    return HttpResponse._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(HttpResponse.prototype, {});
    HttpResponse.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
HttpResponse._create = function(jdel) {
  var obj = Object.create(HttpResponse.prototype, {});
  HttpResponse.apply(obj, arguments);
  return obj;
}
module.exports = HttpResponse;