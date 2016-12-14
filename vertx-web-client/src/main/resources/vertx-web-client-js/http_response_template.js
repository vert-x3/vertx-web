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

/** @module vertx-web-client-js/http_response_template */
var utils = require('vertx-js/util/utils');
var Buffer = require('vertx-js/buffer');
var ReadStream = require('vertx-js/read_stream');
var HttpResponse = require('vertx-web-client-js/http_response');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JHttpResponseTemplate = io.vertx.webclient.HttpResponseTemplate;

/**
 A template for configuring client-side HTTP responses.

 @class
*/
var HttpResponseTemplate = function(j_val, j_arg_0) {

  var j_httpResponseTemplate = j_val;
  var that = this;
  var j_T = typeof j_arg_0 !== 'undefined' ? j_arg_0 : utils.unknown_jtype;

  /**
   Send a request, the <code>handler</code> will receive the response as an {@link HttpResponse}.

   @public
   @param handler {function} 
   */
  this.send = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_httpResponseTemplate["send(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        handler(utils.convReturnVertxGen(HttpResponse, ar.result(), undefined), null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Like {@link HttpResponseTemplate#send} but with an HTTP request <code>body</code> stream.

   @public
   @param body {ReadStream} the body 
   @param handler {function} 
   */
  this.sendStream = function(body, handler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'function') {
      j_httpResponseTemplate["sendStream(io.vertx.core.streams.ReadStream,io.vertx.core.Handler)"](body._jdel, function(ar) {
      if (ar.succeeded()) {
        handler(utils.convReturnVertxGen(HttpResponse, ar.result(), undefined), null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Like {@link HttpResponseTemplate#send} but with an HTTP request <code>body</code> buffer.

   @public
   @param body {Buffer} the body 
   @param handler {function} 
   */
  this.sendBuffer = function(body, handler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'function') {
      j_httpResponseTemplate["sendBuffer(io.vertx.core.buffer.Buffer,io.vertx.core.Handler)"](body._jdel, function(ar) {
      if (ar.succeeded()) {
        handler(utils.convReturnVertxGen(HttpResponse, ar.result(), undefined), null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Like {@link HttpResponseTemplate#send} but with an HTTP request <code>body</code> json and the content type
   set to <code>application/json</code>.

   @public
   @param body {Object} the body 
   @param handler {function} 
   */
  this.sendJson = function(body, handler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] !== 'function' && typeof __args[1] === 'function') {
      j_httpResponseTemplate["sendJson(java.lang.Object,io.vertx.core.Handler)"](utils.convParamTypeUnknown(body), function(ar) {
      if (ar.succeeded()) {
        handler(utils.convReturnVertxGen(HttpResponse, ar.result(), undefined), null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Like {@link HttpResponseTemplate#asString} but with the specified <code>encoding</code> param.

   @public
   @param encoding {string} 
   @return {HttpResponseTemplate}
   */
  this.asString = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(HttpResponseTemplate, j_httpResponseTemplate["asString()"](), undefined);
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(HttpResponseTemplate, j_httpResponseTemplate["asString(java.lang.String)"](__args[0]), undefined);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Configure the template to decode the response as a Json object.

   @public

   @return {HttpResponseTemplate} a new <code>HttpResponseTemplate</code> instance decoding the response as a Json object
   */
  this.asJsonObject = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(HttpResponseTemplate, j_httpResponseTemplate["asJsonObject()"](), undefined);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_httpResponseTemplate;
};

HttpResponseTemplate._jclass = utils.getJavaClass("io.vertx.webclient.HttpResponseTemplate");
HttpResponseTemplate._jtype = {
  accept: function(obj) {
    return HttpResponseTemplate._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(HttpResponseTemplate.prototype, {});
    HttpResponseTemplate.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
HttpResponseTemplate._create = function(jdel) {
  var obj = Object.create(HttpResponseTemplate.prototype, {});
  HttpResponseTemplate.apply(obj, arguments);
  return obj;
}
module.exports = HttpResponseTemplate;