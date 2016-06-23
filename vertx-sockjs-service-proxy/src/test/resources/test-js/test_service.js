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
var utils = require('vertx-js/util/utils');
var Vertx = require('vertx-js/vertx');
var TestConnection = require('test-js/test_connection');
var TestConnectionWithCloseFuture = require('test-js/test_connection_with_close_future');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JTestService = io.vertx.serviceproxy.testmodel.TestService;
var TestDataObject = io.vertx.serviceproxy.testmodel.TestDataObject;

/**

 @class
*/
var TestService = function(j_val) {

  var j_testService = j_val;
  var that = this;

  /**

   @public
   @param resultHandler {function} 
   */
  this.longDeliverySuccess = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["longDeliverySuccess(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.longDeliveryFailed = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["longDeliveryFailed(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
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
      j_testService["createConnection(java.lang.String,io.vertx.core.Handler)"](str, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnVertxGen(ar.result(), TestConnection), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.createConnectionWithCloseFuture = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["createConnectionWithCloseFuture(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnVertxGen(ar.result(), TestConnectionWithCloseFuture), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   */
  this.noParams = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_testService["noParams()"]();
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
      j_testService["basicTypes(java.lang.String,byte,short,int,long,float,double,char,boolean)"](str, b, s, i, l, f, d, c, bool);
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
      j_testService["basicBoxedTypes(java.lang.String,java.lang.Byte,java.lang.Short,java.lang.Integer,java.lang.Long,java.lang.Float,java.lang.Double,java.lang.Character,java.lang.Boolean)"](str, utils.convParamByte(b), utils.convParamShort(s), utils.convParamInteger(i), utils.convParamLong(l), utils.convParamFloat(f), utils.convParamDouble(d), utils.convParamCharacter(c), bool);
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
      j_testService["basicBoxedTypesNull(java.lang.String,java.lang.Byte,java.lang.Short,java.lang.Integer,java.lang.Long,java.lang.Float,java.lang.Double,java.lang.Character,java.lang.Boolean)"](str, utils.convParamByte(b), utils.convParamShort(s), utils.convParamInteger(i), utils.convParamLong(l), utils.convParamFloat(f), utils.convParamDouble(d), utils.convParamCharacter(c), bool);
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
      j_testService["jsonTypes(io.vertx.core.json.JsonObject,io.vertx.core.json.JsonArray)"](utils.convParamJsonObject(jsonObject), utils.convParamJsonArray(jsonArray));
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
      j_testService["jsonTypesNull(io.vertx.core.json.JsonObject,io.vertx.core.json.JsonArray)"](utils.convParamJsonObject(jsonObject), utils.convParamJsonArray(jsonArray));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param someEnum {Object} 
   */
  this.enumType = function(someEnum) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_testService["enumType(io.vertx.serviceproxy.testmodel.SomeEnum)"](io.vertx.serviceproxy.testmodel.SomeEnum.valueOf(someEnum));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param someEnum {Object} 
   */
  this.enumTypeNull = function(someEnum) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_testService["enumTypeNull(io.vertx.serviceproxy.testmodel.SomeEnum)"](io.vertx.serviceproxy.testmodel.SomeEnum.valueOf(someEnum));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param someEnum {function} 
   */
  this.enumTypeAsResult = function(someEnum) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["enumTypeAsResult(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        someEnum(utils.convReturnEnum(ar.result()), null);
      } else {
        someEnum(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param someEnum {function} 
   */
  this.enumTypeAsResultNull = function(someEnum) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["enumTypeAsResultNull(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        someEnum(utils.convReturnEnum(ar.result()), null);
      } else {
        someEnum(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param options {Object} 
   */
  this.dataObjectType = function(options) {
    var __args = arguments;
    if (__args.length === 1 && (typeof __args[0] === 'object' && __args[0] != null)) {
      j_testService["dataObjectType(io.vertx.serviceproxy.testmodel.TestDataObject)"](options != null ? new TestDataObject(new JsonObject(JSON.stringify(options))) : null);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param options {Object} 
   */
  this.dataObjectTypeNull = function(options) {
    var __args = arguments;
    if (__args.length === 1 && (typeof __args[0] === 'object' && __args[0] != null)) {
      j_testService["dataObjectTypeNull(io.vertx.serviceproxy.testmodel.TestDataObject)"](options != null ? new TestDataObject(new JsonObject(JSON.stringify(options))) : null);
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
      j_testService["listParams(java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List)"](utils.convParamListBasicOther(listString), utils.convParamListByte(listByte), utils.convParamListShort(listShort), utils.convParamListBasicOther(listInt), utils.convParamListLong(listLong), utils.convParamListJsonObject(listJsonObject), utils.convParamListJsonArray(listJsonArray), utils.convParamListDataObject(listDataObject, function(json) { return new TestDataObject(json); }));
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
      j_testService["setParams(java.util.Set,java.util.Set,java.util.Set,java.util.Set,java.util.Set,java.util.Set,java.util.Set,java.util.Set)"](utils.convParamSetBasicOther(setString), utils.convParamSetByte(setByte), utils.convParamSetShort(setShort), utils.convParamSetBasicOther(setInt), utils.convParamSetLong(setLong), utils.convParamSetJsonObject(setJsonObject), utils.convParamSetJsonArray(setJsonArray), utils.convParamSetDataObject(setDataObject, function(json) { return new TestDataObject(json); }));
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
      j_testService["mapParams(java.util.Map,java.util.Map,java.util.Map,java.util.Map,java.util.Map,java.util.Map,java.util.Map)"](mapString, utils.convParamMapByte(mapByte), utils.convParamMapShort(mapShort), mapInt, utils.convParamMapLong(mapLong), utils.convParamMapJsonObject(mapJsonObject), utils.convParamMapJsonArray(mapJsonArray));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.stringHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["stringHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.stringNullHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["stringNullHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.byteHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["byteHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.byteNullHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["byteNullHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.shortHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["shortHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.shortNullHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["shortNullHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.intHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["intHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.intNullHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["intNullHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.longHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["longHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnLong(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.longNullHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["longNullHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnLong(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.floatHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["floatHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.floatNullHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["floatNullHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.doubleHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["doubleHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.doubleNullHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["doubleNullHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.charHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["charHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.charNullHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["charNullHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.booleanHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["booleanHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.booleanNullHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["booleanNullHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.jsonObjectHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["jsonObjectHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.jsonObjectNullHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["jsonObjectNullHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.jsonArrayHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["jsonArrayHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.jsonArrayNullHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["jsonArrayNullHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.dataObjectHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["dataObjectHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.dataObjectNullHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["dataObjectNullHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.voidHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["voidHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
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
      j_testService["fluentMethod(java.lang.String,io.vertx.core.Handler)"](str, function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
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
      j_testService["fluentNoParams()"]();
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
      j_testService["failingMethod(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
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
      j_testService["invokeWithMessage(io.vertx.core.json.JsonObject,java.lang.String,int,char,io.vertx.serviceproxy.testmodel.SomeEnum,io.vertx.core.Handler)"](utils.convParamJsonObject(object), str, i, chr, io.vertx.serviceproxy.testmodel.SomeEnum.valueOf(senum), function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.listStringHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["listStringHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.listByteHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["listByteHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.listShortHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["listShortHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.listIntHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["listIntHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.listLongHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["listLongHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnListSetLong(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.listFloatHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["listFloatHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.listDoubleHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["listDoubleHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.listCharHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["listCharHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.listBoolHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["listBoolHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.listJsonObjectHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["listJsonObjectHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnListSetJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.listJsonArrayHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["listJsonArrayHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnListSetJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.listDataObjectHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["listDataObjectHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnListSetDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.setStringHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["setStringHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnSet(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.setByteHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["setByteHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnSet(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.setShortHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["setShortHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnSet(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.setIntHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["setIntHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnSet(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.setLongHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["setLongHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnListSetLong(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.setFloatHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["setFloatHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnSet(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.setDoubleHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["setDoubleHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnSet(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.setCharHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["setCharHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnSet(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.setBoolHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["setBoolHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnSet(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.setJsonObjectHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["setJsonObjectHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnListSetJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.setJsonArrayHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["setJsonArrayHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnListSetJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.setDataObjectHandler = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_testService["setDataObjectHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnListSetDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   */
  this.ignoredMethod = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_testService["ignoredMethod()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_testService;
};

/**

 @memberof module:test-js/test_service
 @param vertx {Vertx} 
 @return {TestService}
 */
TestService.create = function(vertx) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(JTestService["create(io.vertx.core.Vertx)"](vertx._jdel), TestService);
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
    return utils.convReturnVertxGen(JTestService["createProxy(io.vertx.core.Vertx,java.lang.String)"](vertx._jdel, address), TestService);
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
    return utils.convReturnVertxGen(JTestService["createProxyLongDelivery(io.vertx.core.Vertx,java.lang.String)"](vertx._jdel, address), TestService);
  } else throw new TypeError('function invoked with invalid arguments');
};

// We export the Constructor function
module.exports = TestService;