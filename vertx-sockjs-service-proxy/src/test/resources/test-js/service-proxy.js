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
!function (factory) {
  if (typeof require === 'function' && typeof module !== 'undefined') {
    factory();
  } else if (typeof define === 'function' && define.amd) {
    // AMD loader
    define('test-js/service-proxy', [], factory);
  } else {
    // plain old include
    Service = factory();
  }
}(function () {

  /**

 @class
  */
  var Service = function(eb, address) {

    var j_eb = eb;
    var j_address = address;
    var closed = false;
    var that = this;
    var convCharCollection = function(coll) {
      var ret = [];
      for (var i = 0;i < coll.length;i++) {
        ret.push(String.fromCharCode(coll[i]));
      }
      return ret;
    };

    /**

     @public
     @param name {string} 
     @param result {function} 
     @return {Service}
     */
    this.hello = function(name, result) {
      var __args = arguments;
      if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"name":__args[0]}, {"action":"hello"}, function(err, result) { __args[1](err, result &&result.body); });
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"e":__args[0]}, {"action":"methodUsingEnum"}, function(err, result) { __args[1](err, result &&result.body); });
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"methodReturningEnum"}, function(err, result) { __args[0](err, result &&result.body); });
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"methodReturningVertxEnum"}, function(err, result) { __args[0](err, result &&result.body); });
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"json":__args[0]}, {"action":"methodWithJsonObject"}, function(err, result) { __args[1](err, result &&result.body); });
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"json":__args[0]}, {"action":"methodWithJsonArray"}, function(err, result) { __args[1](err, result &&result.body); });
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"list":__args[0]}, {"action":"methodWithList"}, function(err, result) { __args[1](err, result &&result.body); });
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"data":__args[0]}, {"action":"methodWithDataObject"}, function(err, result) { __args[1](err, result &&result.body); });
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"list":__args[0]}, {"action":"methodWithListOfDataObject"}, function(err, result) { __args[1](err, result &&result.body); });
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"list":__args[0]}, {"action":"methodWithListOfJsonObject"}, function(err, result) { __args[1](err, result &&result.body); });
        return that;
      } else throw new TypeError('function invoked with invalid arguments');
    };

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
      if (closed) {
        throw new Error('Proxy is closed');
      }
      j_eb.send(j_address, {"vertx":__args[0], "address":__args[1]}, {"action":"createProxy"});
      return;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = Service;
    } else {
      exports.Service = Service;
    }
  } else {
    return Service;
  }
});