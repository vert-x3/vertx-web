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

/** @module vertx-apex-addons-js/clustered_session_store */
var utils = require('vertx-js/util/utils');
var SessionStore = require('vertx-apex-core-js/session_store');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JClusteredSessionStore = io.vertx.ext.apex.addons.ClusteredSessionStore;

/**

 @class
*/
var ClusteredSessionStore = function(j_val) {

  var j_clusteredSessionStore = j_val;
  var that = this;
  SessionStore.call(this, j_val);

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_clusteredSessionStore;
};

/**

 @memberof module:vertx-apex-addons-js/clustered_session_store
 @param vertx {Vertx} 
 @return {SessionStore}
 */
ClusteredSessionStore.clusteredSessionStore = function() {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return new SessionStore(JClusteredSessionStore.clusteredSessionStore(__args[0]._jdel));
  }else if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
    return new SessionStore(JClusteredSessionStore.clusteredSessionStore(__args[0]._jdel, __args[1]));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = ClusteredSessionStore;