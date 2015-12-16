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

/** @module test-js/test_service */
!function (factory) {
  if (typeof require === 'function' && typeof module !== 'undefined') {
    factory();
  } else if (typeof define === 'function' && define.amd) {
    // AMD loader
    define('test-js/test_service-proxy', [], factory);
  } else {
    // plain old include
    TestService = factory();
  }
}(function () {
  var TestConnection = require('test-js/test_connection-proxy');
  var TestConnectionWithCloseFuture = require('test-js/test_connection_with_close_future-proxy');

  /**

 @class
  */
  var TestService = function(eb, address) {

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
     */
    this.longDeliverySuccess = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"longDeliverySuccess"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.longDeliveryFailed = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"longDeliveryFailed"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param str {string} 
     @param resultHandler {function} 
     */
    this.createConnection = function(str, resultHandler) {
      var __args = arguments;
      if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"str":__args[0]}, {"action":"createConnection"}, function(err, result) { __args[1](err, result &&new TestConnection(j_eb, result.headers.proxyaddr)); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.createConnectionWithCloseFuture = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"createConnectionWithCloseFuture"}, function(err, result) { __args[0](err, result &&new TestConnectionWithCloseFuture(j_eb, result.headers.proxyaddr)); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public

     */
    this.noParams = function() {
      var __args = arguments;
      if (__args.length === 0) {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"noParams"});
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param str {string} 
     @param b {number} 
     @param s {number} 
     @param i {number} 
     @param l {number} 
     @param f {number} 
     @param d {number} 
     @param c {string} 
     @param bool {boolean} 
     */
    this.basicTypes = function(str, b, s, i, l, f, d, c, bool) {
      var __args = arguments;
      if (__args.length === 9 && typeof __args[0] === 'string' && typeof __args[1] ==='number' && typeof __args[2] ==='number' && typeof __args[3] ==='number' && typeof __args[4] ==='number' && typeof __args[5] ==='number' && typeof __args[6] ==='number' && typeof __args[7] ==='string' && typeof __args[8] ==='boolean') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"str":__args[0], "b":__args[1], "s":__args[2], "i":__args[3], "l":__args[4], "f":__args[5], "d":__args[6], "c":__args[7].charCodeAt(0), "bool":__args[8]}, {"action":"basicTypes"});
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param str {string} 
     @param b {number} 
     @param s {number} 
     @param i {number} 
     @param l {number} 
     @param f {number} 
     @param d {number} 
     @param c {string} 
     @param bool {boolean} 
     */
    this.basicBoxedTypes = function(str, b, s, i, l, f, d, c, bool) {
      var __args = arguments;
      if (__args.length === 9 && typeof __args[0] === 'string' && typeof __args[1] ==='number' && typeof __args[2] ==='number' && typeof __args[3] ==='number' && typeof __args[4] ==='number' && typeof __args[5] ==='number' && typeof __args[6] ==='number' && typeof __args[7] ==='string' && typeof __args[8] ==='boolean') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"str":__args[0], "b":__args[1], "s":__args[2], "i":__args[3], "l":__args[4], "f":__args[5], "d":__args[6], "c":__args[7].charCodeAt(0), "bool":__args[8]}, {"action":"basicBoxedTypes"});
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param str {string} 
     @param b {number} 
     @param s {number} 
     @param i {number} 
     @param l {number} 
     @param f {number} 
     @param d {number} 
     @param c {string} 
     @param bool {boolean} 
     */
    this.basicBoxedTypesNull = function(str, b, s, i, l, f, d, c, bool) {
      var __args = arguments;
      if (__args.length === 9 && typeof __args[0] === 'string' && typeof __args[1] ==='number' && typeof __args[2] ==='number' && typeof __args[3] ==='number' && typeof __args[4] ==='number' && typeof __args[5] ==='number' && typeof __args[6] ==='number' && typeof __args[7] ==='string' && typeof __args[8] ==='boolean') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"str":__args[0], "b":__args[1], "s":__args[2], "i":__args[3], "l":__args[4], "f":__args[5], "d":__args[6], "c":__args[7].charCodeAt(0), "bool":__args[8]}, {"action":"basicBoxedTypesNull"});
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param jsonObject {Object} 
     @param jsonArray {todo} 
     */
    this.jsonTypes = function(jsonObject, jsonArray) {
      var __args = arguments;
      if (__args.length === 2 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'object' && __args[1] instanceof Array) {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"jsonObject":__args[0], "jsonArray":__args[1]}, {"action":"jsonTypes"});
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param jsonObject {Object} 
     @param jsonArray {todo} 
     */
    this.jsonTypesNull = function(jsonObject, jsonArray) {
      var __args = arguments;
      if (__args.length === 2 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'object' && __args[1] instanceof Array) {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"jsonObject":__args[0], "jsonArray":__args[1]}, {"action":"jsonTypesNull"});
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param someEnum {Object} 
     */
    this.enumType = function(someEnum) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'string') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"someEnum":__args[0]}, {"action":"enumType"});
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param someEnum {Object} 
     */
    this.enumTypeNull = function(someEnum) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'string') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"someEnum":__args[0]}, {"action":"enumTypeNull"});
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param someEnum {function} 
     */
    this.enumTypeAsResult = function(someEnum) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"enumTypeAsResult"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param someEnum {function} 
     */
    this.enumTypeAsResultNull = function(someEnum) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"enumTypeAsResultNull"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param options {Object} 
     */
    this.dataObjectType = function(options) {
      var __args = arguments;
      if (__args.length === 1 && (typeof __args[0] === 'object' && __args[0] != null)) {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"options":__args[0]}, {"action":"dataObjectType"});
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param options {Object} 
     */
    this.dataObjectTypeNull = function(options) {
      var __args = arguments;
      if (__args.length === 1 && (typeof __args[0] === 'object' && __args[0] != null)) {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"options":__args[0]}, {"action":"dataObjectTypeNull"});
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param listString {Array.<string>} 
     @param listByte {Array.<number>} 
     @param listShort {Array.<number>} 
     @param listInt {Array.<number>} 
     @param listLong {Array.<number>} 
     @param listJsonObject {Array.<Object>} 
     @param listJsonArray {Array.<todo>} 
     @param listDataObject {Array.<Object>} 
     */
    this.listParams = function(listString, listByte, listShort, listInt, listLong, listJsonObject, listJsonArray, listDataObject) {
      var __args = arguments;
      if (__args.length === 8 && typeof __args[0] === 'object' && __args[0] instanceof Array && typeof __args[1] === 'object' && __args[1] instanceof Array && typeof __args[2] === 'object' && __args[2] instanceof Array && typeof __args[3] === 'object' && __args[3] instanceof Array && typeof __args[4] === 'object' && __args[4] instanceof Array && typeof __args[5] === 'object' && __args[5] instanceof Array && typeof __args[6] === 'object' && __args[6] instanceof Array && typeof __args[7] === 'object' && __args[7] instanceof Array) {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"listString":__args[0], "listByte":__args[1], "listShort":__args[2], "listInt":__args[3], "listLong":__args[4], "listJsonObject":__args[5], "listJsonArray":__args[6], "listDataObject":__args[7]}, {"action":"listParams"});
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param setString {Array.<string>} 
     @param setByte {Array.<number>} 
     @param setShort {Array.<number>} 
     @param setInt {Array.<number>} 
     @param setLong {Array.<number>} 
     @param setJsonObject {Array.<Object>} 
     @param setJsonArray {Array.<todo>} 
     @param setDataObject {Array.<Object>} 
     */
    this.setParams = function(setString, setByte, setShort, setInt, setLong, setJsonObject, setJsonArray, setDataObject) {
      var __args = arguments;
      if (__args.length === 8 && typeof __args[0] === 'object' && __args[0] instanceof Array && typeof __args[1] === 'object' && __args[1] instanceof Array && typeof __args[2] === 'object' && __args[2] instanceof Array && typeof __args[3] === 'object' && __args[3] instanceof Array && typeof __args[4] === 'object' && __args[4] instanceof Array && typeof __args[5] === 'object' && __args[5] instanceof Array && typeof __args[6] === 'object' && __args[6] instanceof Array && typeof __args[7] === 'object' && __args[7] instanceof Array) {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"setString":__args[0], "setByte":__args[1], "setShort":__args[2], "setInt":__args[3], "setLong":__args[4], "setJsonObject":__args[5], "setJsonArray":__args[6], "setDataObject":__args[7]}, {"action":"setParams"});
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param mapString {Array.<string>} 
     @param mapByte {Array.<string>} 
     @param mapShort {Array.<string>} 
     @param mapInt {Array.<string>} 
     @param mapLong {Array.<string>} 
     @param mapJsonObject {Array.<string>} 
     @param mapJsonArray {Array.<string>} 
     */
    this.mapParams = function(mapString, mapByte, mapShort, mapInt, mapLong, mapJsonObject, mapJsonArray) {
      var __args = arguments;
      if (__args.length === 7 && (typeof __args[0] === 'object' && __args[0] != null) && (typeof __args[1] === 'object' && __args[1] != null) && (typeof __args[2] === 'object' && __args[2] != null) && (typeof __args[3] === 'object' && __args[3] != null) && (typeof __args[4] === 'object' && __args[4] != null) && (typeof __args[5] === 'object' && __args[5] != null) && (typeof __args[6] === 'object' && __args[6] != null)) {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"mapString":__args[0], "mapByte":__args[1], "mapShort":__args[2], "mapInt":__args[3], "mapLong":__args[4], "mapJsonObject":__args[5], "mapJsonArray":__args[6]}, {"action":"mapParams"});
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.stringHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"stringHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.stringNullHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"stringNullHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.byteHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"byteHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.byteNullHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"byteNullHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.shortHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"shortHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.shortNullHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"shortNullHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.intHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"intHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.intNullHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"intNullHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.longHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"longHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.longNullHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"longNullHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.floatHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"floatHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.floatNullHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"floatNullHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.doubleHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"doubleHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.doubleNullHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"doubleNullHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.charHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"charHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.charNullHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"charNullHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.booleanHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"booleanHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.booleanNullHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"booleanNullHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.jsonObjectHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"jsonObjectHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.jsonObjectNullHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"jsonObjectNullHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.jsonArrayHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"jsonArrayHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.jsonArrayNullHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"jsonArrayNullHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.dataObjectHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"dataObjectHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.dataObjectNullHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"dataObjectNullHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.voidHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"voidHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param str {string} 
     @param resultHandler {function} 
     @return {TestService}
     */
    this.fluentMethod = function(str, resultHandler) {
      var __args = arguments;
      if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"str":__args[0]}, {"action":"fluentMethod"}, function(err, result) { __args[1](err, result &&result.body); });
        return that;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public

     @return {TestService}
     */
    this.fluentNoParams = function() {
      var __args = arguments;
      if (__args.length === 0) {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"fluentNoParams"});
        return that;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.failingMethod = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"failingMethod"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param object {Object} 
     @param str {string} 
     @param i {number} 
     @param chr {string} 
     @param senum {Object} 
     @param resultHandler {function} 
     */
    this.invokeWithMessage = function(object, str, i, chr, senum, resultHandler) {
      var __args = arguments;
      if (__args.length === 6 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'string' && typeof __args[2] ==='number' && typeof __args[3] ==='string' && typeof __args[4] === 'string' && typeof __args[5] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"object":__args[0], "str":__args[1], "i":__args[2], "chr":__args[3].charCodeAt(0), "senum":__args[4]}, {"action":"invokeWithMessage"}, function(err, result) { __args[5](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.listStringHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"listStringHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.listByteHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"listByteHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.listShortHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"listShortHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.listIntHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"listIntHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.listLongHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"listLongHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.listFloatHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"listFloatHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.listDoubleHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"listDoubleHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.listCharHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"listCharHandler"}, function(err, result) { __args[0](err, result &&convCharCollection(result.body)); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.listBoolHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"listBoolHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.listJsonObjectHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"listJsonObjectHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.listJsonArrayHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"listJsonArrayHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.listDataObjectHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"listDataObjectHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.setStringHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"setStringHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.setByteHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"setByteHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.setShortHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"setShortHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.setIntHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"setIntHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.setLongHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"setLongHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.setFloatHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"setFloatHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.setDoubleHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"setDoubleHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.setCharHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"setCharHandler"}, function(err, result) { __args[0](err, result &&convCharCollection(result.body)); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.setBoolHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"setBoolHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.setJsonObjectHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"setJsonObjectHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.setJsonArrayHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"setJsonArrayHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.setDataObjectHandler = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"setDataObjectHandler"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

  };

  /**

   @memberof module:test-js/test_service
   @param vertx {Vertx} 
   @return {TestService}
   */
  TestService.create = function(vertx) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      if (closed) {
        throw new Error('Proxy is closed');
      }
      j_eb.send(j_address, {"vertx":__args[0]}, {"action":"create"});
      return;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @memberof module:test-js/test_service
   @param vertx {Vertx} 
   @param address {string} 
   @return {TestService}
   */
  TestService.createProxy = function(vertx, address) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
      if (closed) {
        throw new Error('Proxy is closed');
      }
      j_eb.send(j_address, {"vertx":__args[0], "address":__args[1]}, {"action":"createProxy"});
      return;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @memberof module:test-js/test_service
   @param vertx {Vertx} 
   @param address {string} 
   @return {TestService}
   */
  TestService.createProxyLongDelivery = function(vertx, address) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
      if (closed) {
        throw new Error('Proxy is closed');
      }
      j_eb.send(j_address, {"vertx":__args[0], "address":__args[1]}, {"action":"createProxyLongDelivery"});
      return;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = TestService;
    } else {
      exports.TestService = TestService;
    }
  } else {
    return TestService;
  }
});