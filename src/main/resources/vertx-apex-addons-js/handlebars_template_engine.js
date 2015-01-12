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

/** @module vertx-apex-addons-js/handlebars_template_engine */
var utils = require('vertx-js/util/utils');
var TemplateEngine = require('vertx-apex-addons-js/template_engine');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JHandlebarsTemplateEngine = io.vertx.ext.apex.addons.HandlebarsTemplateEngine;

/**
 Thread-safe but best performance is to be achieved by not sharing between contexts

 @class
*/
var HandlebarsTemplateEngine = function(j_val) {

  var j_handlebarsTemplateEngine = j_val;
  var that = this;
  TemplateEngine.call(this, j_val);

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_handlebarsTemplateEngine;
};

/**

 @memberof module:vertx-apex-addons-js/handlebars_template_engine

 @return {HandlebarsTemplateEngine}
 */
HandlebarsTemplateEngine.create = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return new HandlebarsTemplateEngine(JHandlebarsTemplateEngine.create());
  }else if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
    return new HandlebarsTemplateEngine(JHandlebarsTemplateEngine.create(__args[0], __args[1]));
  }else if (__args.length === 3 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] ==='number') {
    return new HandlebarsTemplateEngine(JHandlebarsTemplateEngine.create(__args[0], __args[1], __args[2]));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = HandlebarsTemplateEngine;