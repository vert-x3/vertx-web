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

/** @module vertx-web-client-js/form */
var utils = require('vertx-js/util/utils');
var Buffer = require('vertx-js/buffer');
var ReadStream = require('vertx-js/read_stream');
var MultiMap = require('vertx-js/multi_map');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JForm = io.vertx.webclient.Form;

/**

 @class
*/
var Form = function(j_val) {

  var j_form = j_val;
  var that = this;

  /**
   Adds a new value with the specified name and value.

   @public
   @param name {string} The name 
   @param value {string} The value being added 
   @return {Form} a reference to this, so the API can be used fluently
   */
  this.addAttr = function(name, value) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
      j_form["addAttr(java.lang.String,java.lang.String)"](name, value);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param name {string} 
   @param file {ReadStream} 
   @return {Form}
   */
  this.addFile = function(name, file) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'object' && __args[1]._jdel) {
      j_form["addFile(java.lang.String,io.vertx.core.streams.ReadStream)"](name, file._jdel);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Adds all the entries from another MultiMap to this one

   @public
   @param map {MultiMap} 
   @return {Form} a reference to this, so the API can be used fluently
   */
  this.addAll = function(map) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_form["addAll(io.vertx.core.MultiMap)"](map._jdel);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Sets a value under the specified name.
   <p>
   If there is an existing header with the same name, it is removed.

   @public
   @param name {string} The name 
   @param value {string} The value 
   @return {Form} a reference to this, so the API can be used fluently
   */
  this.set = function(name, value) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
      j_form["set(java.lang.String,java.lang.String)"](name, value);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Cleans this instance.

   @public
   @param map {MultiMap} 
   @return {Form} a reference to this, so the API can be used fluently
   */
  this.setAll = function(map) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_form["setAll(io.vertx.core.MultiMap)"](map._jdel);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {ReadStream}
   */
  this.bilto = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(ReadStream, j_form["bilto()"](), Buffer._jtype);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_form;
};

Form._jclass = utils.getJavaClass("io.vertx.webclient.Form");
Form._jtype = {
  accept: function(obj) {
    return Form._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(Form.prototype, {});
    Form.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
Form._create = function(jdel) {
  var obj = Object.create(Form.prototype, {});
  Form.apply(obj, arguments);
  return obj;
}
/**

 @memberof module:vertx-web-client-js/form

 @return {Form}
 */
Form.create = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return utils.convReturnVertxGen(Form, JForm["create()"]());
  } else throw new TypeError('function invoked with invalid arguments');
};

/**

 @memberof module:vertx-web-client-js/form

 @return {Form}
 */
Form.multipart = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return utils.convReturnVertxGen(Form, JForm["multipart()"]());
  } else throw new TypeError('function invoked with invalid arguments');
};

module.exports = Form;