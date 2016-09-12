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

/** @module vertx-web-js/thymeleaf_template_engine */
var utils = require('vertx-js/util/utils');
var Buffer = require('vertx-js/buffer');
var TemplateEngine = require('vertx-web-js/template_engine');
var RoutingContext = require('vertx-web-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JThymeleafTemplateEngine = io.vertx.ext.web.templ.ThymeleafTemplateEngine;

/**
 A template engine that uses the Thymeleaf library.

 @class
*/
var ThymeleafTemplateEngine = function(j_val) {

  var j_thymeleafTemplateEngine = j_val;
  var that = this;
  TemplateEngine.call(this, j_val);

  /**

   @public
   @param arg0 {RoutingContext} 
   @param arg1 {string} 
   @param arg2 {function} 
   */
  this.render = function(arg0, arg1, arg2) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string' && typeof __args[2] === 'function') {
      j_thymeleafTemplateEngine["render(io.vertx.ext.web.RoutingContext,java.lang.String,io.vertx.core.Handler)"](arg0._jdel, arg1, function(ar) {
      if (ar.succeeded()) {
        arg2(utils.convReturnVertxGen(ar.result(), Buffer), null);
      } else {
        arg2(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set the mode for the engine

   @public
   @param mode {Object} the mode 
   @return {ThymeleafTemplateEngine} a reference to this for fluency
   */
  this.setMode = function(mode) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_thymeleafTemplateEngine["setMode(org.thymeleaf.templatemode.TemplateMode)"](org.thymeleaf.templatemode.TemplateMode.valueOf(mode)), ThymeleafTemplateEngine);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_thymeleafTemplateEngine;
};

/**
 Create a template engine using defaults

 @memberof module:vertx-web-js/thymeleaf_template_engine

 @return {ThymeleafTemplateEngine} the engine
 */
ThymeleafTemplateEngine.create = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return utils.convReturnVertxGen(JThymeleafTemplateEngine["create()"](), ThymeleafTemplateEngine);
  } else throw new TypeError('function invoked with invalid arguments');
};

// We export the Constructor function
module.exports = ThymeleafTemplateEngine;