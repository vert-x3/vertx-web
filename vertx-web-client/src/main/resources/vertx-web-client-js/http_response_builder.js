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

/** @module vertx-web-client-js/http_response_builder */
var utils = require('vertx-js/util/utils');
var Buffer = require('vertx-js/buffer');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JHttpResponseBuilder = io.vertx.webclient.HttpResponseBuilder;

/**
 A builder for configuring client-side HTTP responses.

 @class
*/
var HttpResponseBuilder = function(j_val, j_arg_0) {

  var j_httpResponseBuilder = j_val;
  var that = this;
  var j_T = typeof j_arg_0 !== 'undefined' ? j_arg_0 : utils.unknown_jtype;

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_httpResponseBuilder;
};

HttpResponseBuilder._jclass = utils.getJavaClass("io.vertx.webclient.HttpResponseBuilder");
HttpResponseBuilder._jtype = {
  accept: function(obj) {
    return HttpResponseBuilder._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(HttpResponseBuilder.prototype, {});
    HttpResponseBuilder.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
HttpResponseBuilder._create = function(jdel) {
  var obj = Object.create(HttpResponseBuilder.prototype, {});
  HttpResponseBuilder.apply(obj, arguments);
  return obj;
}
/**

 @memberof module:vertx-web-client-js/http_response_builder
 @param enc {string} 
 @return {HttpResponseBuilder}
 */
HttpResponseBuilder.string = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return utils.convReturnVertxGen(HttpResponseBuilder, JHttpResponseBuilder["string()"](), undefined);
  }else if (__args.length === 1 && typeof __args[0] === 'string') {
    return utils.convReturnVertxGen(HttpResponseBuilder, JHttpResponseBuilder["string(java.lang.String)"](__args[0]), undefined);
  } else throw new TypeError('function invoked with invalid arguments');
};

/**

 @memberof module:vertx-web-client-js/http_response_builder

 @return {HttpResponseBuilder}
 */
HttpResponseBuilder.buffer = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return utils.convReturnVertxGen(HttpResponseBuilder, JHttpResponseBuilder["buffer()"](), Buffer._jtype);
  } else throw new TypeError('function invoked with invalid arguments');
};

/**

 @memberof module:vertx-web-client-js/http_response_builder

 @return {HttpResponseBuilder}
 */
HttpResponseBuilder.jsonObject = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return utils.convReturnVertxGen(HttpResponseBuilder, JHttpResponseBuilder["jsonObject()"](), undefined);
  } else throw new TypeError('function invoked with invalid arguments');
};

module.exports = HttpResponseBuilder;