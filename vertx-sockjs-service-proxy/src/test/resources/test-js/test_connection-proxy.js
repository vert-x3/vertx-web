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

/** @module test-js/test_connection */
!function (factory) {
  if (typeof require === 'function' && typeof module !== 'undefined') {
    factory();
  } else if (typeof define === 'function' && define.amd) {
    // AMD loader
    define('test-js/test_connection-proxy', [], factory);
  } else {
    // plain old include
    TestConnection = factory();
  }
}(function () {

  /**

 @class
  */
  var TestConnection = function(eb, address) {

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
     @param resultHandler {function} 
     @return {TestConnection}
     */
    this.startTransaction = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"startTransaction"}, function(err, result) { __args[0](err, result &&result.body); });
        return that;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param name {string} 
     @param data {Object} 
     @param resultHandler {function} 
     @return {TestConnection}
     */
    this.insert = function(name, data, resultHandler) {
      var __args = arguments;
      if (__args.length === 3 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"name":__args[0], "data":__args[1]}, {"action":"insert"}, function(err, result) { __args[2](err, result &&result.body); });
        return that;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     @return {TestConnection}
     */
    this.commit = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"commit"}, function(err, result) { __args[0](err, result &&result.body); });
        return that;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     @return {TestConnection}
     */
    this.rollback = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"rollback"}, function(err, result) { __args[0](err, result &&result.body); });
        return that;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public

     */
    this.close = function() {
      var __args = arguments;
      if (__args.length === 0) {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"close"});
        closed = true;
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

  };

  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = TestConnection;
    } else {
      exports.TestConnection = TestConnection;
    }
  } else {
    return TestConnection;
  }
});