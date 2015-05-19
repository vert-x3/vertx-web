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

/** @module vertx-web-js/clustered_session_store */
var utils = require('vertx-js/util/utils');
var SessionStore = require('vertx-web-js/session_store');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JClusteredSessionStore = io.vertx.ext.web.sstore.ClusteredSessionStore;

/**
 A session store which stores sessions in a distributed map so they are available across the cluster.

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
 Create a session store

 @memberof module:vertx-web-js/clustered_session_store
 @param vertx {Vertx} the Vert.x instance 
 @param sessionMapName {string} the session map name 
 @return {ClusteredSessionStore} the session store
 */
ClusteredSessionStore.create = function() {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return new ClusteredSessionStore(JClusteredSessionStore["create(io.vertx.core.Vertx)"](__args[0]._jdel));
  }else if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
    return new ClusteredSessionStore(JClusteredSessionStore["create(io.vertx.core.Vertx,java.lang.String)"](__args[0]._jdel, __args[1]));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = ClusteredSessionStore;