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

/** @module vertx-apex-js/thymeleaf_template_engine */
var utils = require('vertx-js/util/utils');
var TemplateEngine = require('vertx-apex-js/template_engine');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JThymeleafTemplateEngine = io.vertx.ext.apex.templ.ThymeleafTemplateEngine;

/**
 A template engine that uses the Thymeleaf library.

 @class
*/
var ThymeleafTemplateEngine = function(j_val) {

  var j_thymeleafTemplateEngine = j_val;
  var that = this;
  TemplateEngine.call(this, j_val);

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_thymeleafTemplateEngine;
};

/**
 Create a template engine

 @memberof module:vertx-apex-js/thymeleaf_template_engine
 @param resourcePrefix {string} the resource prefix 
 @param templateMode {string} the template mode - e.g. XHTML 
 @return {ThymeleafTemplateEngine} the engine
 */
ThymeleafTemplateEngine.create = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return new ThymeleafTemplateEngine(JThymeleafTemplateEngine.create());
  }else if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
    return new ThymeleafTemplateEngine(JThymeleafTemplateEngine.create(__args[0], __args[1]));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = ThymeleafTemplateEngine;