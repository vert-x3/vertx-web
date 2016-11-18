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

/** @module vertx-web-js/logger_handler */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('vertx-web-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JLoggerHandler = io.vertx.ext.web.handler.LoggerHandler;

/**
 A handler which logs request information to the Vert.x logger.

 @class
*/
var LoggerHandler = function(j_val) {

  var j_loggerHandler = j_val;
  var that = this;

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handle = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_loggerHandler["handle(io.vertx.ext.web.RoutingContext)"](arg0._jdel);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_loggerHandler;
};

LoggerHandler._jclass = utils.getJavaClass("io.vertx.ext.web.handler.LoggerHandler");
LoggerHandler._jtype = {
  accept: function(obj) {
    return LoggerHandler._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(LoggerHandler.prototype, {});
    LoggerHandler.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
LoggerHandler._create = function(jdel) {
  var obj = Object.create(LoggerHandler.prototype, {});
  LoggerHandler.apply(obj, arguments);
  return obj;
}
/**
 Create a handler with he specified format

 @memberof module:vertx-web-js/logger_handler
 @param immediate {boolean} true if logging should occur as soon as request arrives 
 @param format {Object} the format 
 @return {LoggerHandler} the handler
 */
LoggerHandler.create = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return utils.convReturnVertxGen(LoggerHandler, JLoggerHandler["create()"]());
  }else if (__args.length === 1 && typeof __args[0] === 'string') {
    return utils.convReturnVertxGen(LoggerHandler, JLoggerHandler["create(io.vertx.ext.web.handler.LoggerFormat)"](io.vertx.ext.web.handler.LoggerFormat.valueOf(__args[0])));
  }else if (__args.length === 2 && typeof __args[0] ==='boolean' && typeof __args[1] === 'string') {
    return utils.convReturnVertxGen(LoggerHandler, JLoggerHandler["create(boolean,io.vertx.ext.web.handler.LoggerFormat)"](__args[0], io.vertx.ext.web.handler.LoggerFormat.valueOf(__args[1])));
  } else throw new TypeError('function invoked with invalid arguments');
};

module.exports = LoggerHandler;