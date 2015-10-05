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

/** @module vertx-web-js/template_engine */
var utils = require('vertx-js/util/utils');
var Buffer = require('vertx-js/buffer');
var RoutingContext = require('vertx-web-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JTemplateEngine = io.vertx.ext.web.templ.TemplateEngine;

/**
 A template engine uses a specific template and the data in a routing context to render a resource into a buffer.
 <p>
 Concrete implementations exist for several well-known template engines.

 @class
*/
var TemplateEngine = function(j_val) {

  var j_templateEngine = j_val;
  var that = this;

  /**
   Render

   @public
   @param context {RoutingContext} the routing context 
   @param templateFileName {string} the template file name to use 
   @param handler {function} the handler that will be called with a result containing the buffer or a failure. 
   */
  this.render = function(context, templateFileName, handler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string' && typeof __args[2] === 'function') {
      j_templateEngine["render(io.vertx.ext.web.RoutingContext,java.lang.String,io.vertx.core.Handler)"](context._jdel, templateFileName, function(ar) {
      if (ar.succeeded()) {
        handler(utils.convReturnVertxGen(ar.result(), Buffer), null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_templateEngine;
};

// We export the Constructor function
module.exports = TemplateEngine;