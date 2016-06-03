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

/** @module test-js/service */
var utils = require('vertx-js/util/utils');
var Vertx = require('vertx-js/vertx');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JService = io.vertx.serviceproxy.clustered.Service;
var TestDataObject = io.vertx.serviceproxy.testmodel.TestDataObject;

/**

 @class
*/
var Service = function(j_val) {

  var j_service = j_val;
  var that = this;

  /**

   @public
   @param name {string} 
   @param result {function} 
   @return {Service}
   */
  this.hello = function(name, result) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_service["hello(java.lang.String,io.vertx.core.Handler)"](name, function(ar) {
      if (ar.succeeded()) {
        result(ar.result(), null);
      } else {
        result(null, ar.cause());
      }
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param e {Object} 
   @param result {function} 
   @return {Service}
   */
  this.methodUsingEnum = function(e, result) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_service["methodUsingEnum(io.vertx.serviceproxy.testmodel.SomeEnum,io.vertx.core.Handler)"](io.vertx.serviceproxy.testmodel.SomeEnum.valueOf(__args[0]), function(ar) {
      if (ar.succeeded()) {
        result(ar.result(), null);
      } else {
        result(null, ar.cause());
      }
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param result {function} 
   @return {Service}
   */
  this.methodReturningEnum = function(result) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_service["methodReturningEnum(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        result(utils.convReturnEnum(ar.result()), null);
      } else {
        result(null, ar.cause());
      }
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param result {function} 
   @return {Service}
   */
  this.methodReturningVertxEnum = function(result) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_service["methodReturningVertxEnum(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        result(utils.convReturnEnum(ar.result()), null);
      } else {
        result(null, ar.cause());
      }
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param json {Object} 
   @param result {function} 
   @return {Service}
   */
  this.methodWithJsonObject = function(json, result) {
    var __args = arguments;
    if (__args.length === 2 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'function') {
      j_service["methodWithJsonObject(io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](utils.convParamJsonObject(json), function(ar) {
      if (ar.succeeded()) {
        result(utils.convReturnJson(ar.result()), null);
      } else {
        result(null, ar.cause());
      }
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param json {todo} 
   @param result {function} 
   @return {Service}
   */
  this.methodWithJsonArray = function(json, result) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'object' && __args[0] instanceof Array && typeof __args[1] === 'function') {
      j_service["methodWithJsonArray(io.vertx.core.json.JsonArray,io.vertx.core.Handler)"](utils.convParamJsonArray(json), function(ar) {
      if (ar.succeeded()) {
        result(utils.convReturnJson(ar.result()), null);
      } else {
        result(null, ar.cause());
      }
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param list {Array.<string>} 
   @param result {function} 
   @return {Service}
   */
  this.methodWithList = function(list, result) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'object' && __args[0] instanceof Array && typeof __args[1] === 'function') {
      j_service["methodWithList(java.util.List,io.vertx.core.Handler)"](list, function(ar) {
      if (ar.succeeded()) {
        result(ar.result(), null);
      } else {
        result(null, ar.cause());
      }
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param data {Object} 
   @param result {function} 
   @return {Service}
   */
  this.methodWithDataObject = function(data, result) {
    var __args = arguments;
    if (__args.length === 2 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'function') {
      j_service["methodWithDataObject(io.vertx.serviceproxy.testmodel.TestDataObject,io.vertx.core.Handler)"](data != null ? new TestDataObject(new JsonObject(JSON.stringify(data))) : null, function(ar) {
      if (ar.succeeded()) {
        result(utils.convReturnDataObject(ar.result()), null);
      } else {
        result(null, ar.cause());
      }
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param list {Array.<Object>} 
   @param result {function} 
   @return {Service}
   */
  this.methodWithListOfDataObject = function(list, result) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'object' && __args[0] instanceof Array && typeof __args[1] === 'function') {
      j_service["methodWithListOfDataObject(java.util.List,io.vertx.core.Handler)"](utils.convParamListDataObject(list, function(json) { return new TestDataObject(json); }), function(ar) {
      if (ar.succeeded()) {
        result(utils.convReturnListSetDataObject(ar.result()), null);
      } else {
        result(null, ar.cause());
      }
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param list {Array.<Object>} 
   @param result {function} 
   @return {Service}
   */
  this.methodWithListOfJsonObject = function(list, result) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'object' && __args[0] instanceof Array && typeof __args[1] === 'function') {
      j_service["methodWithListOfJsonObject(java.util.List,io.vertx.core.Handler)"](utils.convParamListJsonObject(list), function(ar) {
      if (ar.succeeded()) {
        result(utils.convReturnListSetJson(ar.result()), null);
      } else {
        result(null, ar.cause());
      }
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_service;
};

/**

 @memberof module:test-js/service
 @param vertx {Vertx} 
 @param address {string} 
 @return {Service}
 */
Service.createProxy = function(vertx, address) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
    return utils.convReturnVertxGen(JService["createProxy(io.vertx.core.Vertx,java.lang.String)"](vertx._jdel, address), Service);
  } else throw new TypeError('function invoked with invalid arguments');
};

// We export the Constructor function
module.exports = Service;