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

/** @module test-js/test_connection_with_close_future */
!function (factory) {
  if (typeof require === 'function' && typeof module !== 'undefined') {
    factory();
  } else if (typeof define === 'function' && define.amd) {
    // AMD loader
    define('test-js/test_connection_with_close_future-proxy', [], factory);
  } else {
    // plain old include
    TestConnectionWithCloseFuture = factory();
  }
}(function () {

  /**

 @class
  */
  var TestConnectionWithCloseFuture = function(eb, address) {

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
     @param handler {function} 
     */
    this.close = function(handler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"close"}, function(err, result) { __args[0](err, result &&result.body); });
        closed = true;
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.someMethod = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"someMethod"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

  };

  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = TestConnectionWithCloseFuture;
    } else {
      exports.TestConnectionWithCloseFuture = TestConnectionWithCloseFuture;
    }
  } else {
    return TestConnectionWithCloseFuture;
  }
});