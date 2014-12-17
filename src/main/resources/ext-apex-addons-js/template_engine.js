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

/** @module ext-apex-addons-js/template_engine */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('ext-apex-core-js/routing_context');
var Buffer = require('vertx-js/buffer');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JTemplateEngine = io.vertx.ext.apex.addons.TemplateEngine;

/**

 @class
*/
var TemplateEngine = function(j_val) {

  var j_templateEngine = j_val;
  var that = this;

  /**

   @public
   @param context {RoutingContext} 
   @param templateFileName {string} 
   @param handler {function} 
   */
  this.render = function(context, templateFileName, handler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string' && typeof __args[2] === 'function') {
      j_templateEngine.render(context._jdel, templateFileName, function(ar) {
      if (ar.succeeded()) {
        handler(new Buffer(ar.result()), null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**

   @public
   @param context {RoutingContext} 
   @param templateFileName {string} 
   @param contentType {string} 
   */
  this.renderResponse = function(context, templateFileName, contentType) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string' && typeof __args[2] === 'string') {
      j_templateEngine.renderResponse(context._jdel, templateFileName, contentType);
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_templateEngine;
};

// We export the Constructor function
module.exports = TemplateEngine;