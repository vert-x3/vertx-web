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

/** @module examples-js/some_database_service */
!function (factory) {
  if (typeof require === 'function' && typeof module !== 'undefined') {
    factory();
  } else if (typeof define === 'function' && define.amd) {
    // AMD loader
    define('examples-js/some_database_service-proxy', [], factory);
  } else {
    // plain old include
    SomeDatabaseService = factory();
  }
}(function () {

  /**

 @class
  */
  var SomeDatabaseService = function(eb, address) {

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
     @param collection {string} 
     @param document {Object} 
     @param result {function} 
     */
    this.save = function(collection, document, result) {
      var __args = arguments;
      if (__args.length === 3 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"collection":__args[0], "document":__args[1]}, {"action":"save"}, function(err, result) { __args[2](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param collection {string} 
     @param document {Object} 
     @param result {function} 
     @return {todo}
     */
    this.foo = function(collection, document, result) {
      var __args = arguments;
      if (__args.length === 3 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"collection":__args[0], "document":__args[1]}, {"action":"foo"}, function(err, result) { __args[2](err, result &&result.body); });
        return that;
      } else throw new TypeError('function invoked with invalid arguments');
    };

  };

  /**

   @memberof module:examples-js/some_database_service
   @param vertx {Vertx} 
   @param address {string} 
   @return {todo}
   */
  SomeDatabaseService.createProxy = function(vertx, address) {
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
      exports = module.exports = SomeDatabaseService;
    } else {
      exports.SomeDatabaseService = SomeDatabaseService;
    }
  } else {
    return SomeDatabaseService;
  }
});