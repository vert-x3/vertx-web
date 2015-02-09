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

/** @module vertx-apex-addons-js/path_template_handler */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('vertx-apex-core-js/routing_context');
var TemplateEngine = require('vertx-apex-addons-js/template_engine');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JPathTemplateHandler = io.vertx.ext.apex.addons.PathTemplateHandler;

/**

 @class
*/
var PathTemplateHandler = function(j_val) {

  var j_pathTemplateHandler = j_val;
  var that = this;

  /**

   @public
   @param context {RoutingContext} 
   */
  this.handle = function(context) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_pathTemplateHandler.handle(context._jdel);
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_pathTemplateHandler;
};

/**

 @memberof module:vertx-apex-addons-js/path_template_handler
 @param engine {TemplateEngine} 
 @param templateDirectory {string} 
 @param contentType {string} 
 @return {PathTemplateHandler}
 */
PathTemplateHandler.templateHandler = function() {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return new PathTemplateHandler(JPathTemplateHandler.templateHandler(__args[0]._jdel));
  }else if (__args.length === 3 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string' && typeof __args[2] === 'string') {
    return new PathTemplateHandler(JPathTemplateHandler.templateHandler(__args[0]._jdel, __args[1], __args[2]));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = PathTemplateHandler;