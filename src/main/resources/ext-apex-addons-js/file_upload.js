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

var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JFileUpload = io.vertx.ext.apex.addons.FileUpload;

/**

  @class
*/
var FileUpload = function(j_val) {

  var j_fileUpload = j_val;
  var that = this;

  this.name = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_fileUpload.name();
    } else utils.invalidArgs();
  };

  this.uploadedFileName = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_fileUpload.uploadedFileName();
    } else utils.invalidArgs();
  };

  this.fileName = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_fileUpload.fileName();
    } else utils.invalidArgs();
  };

  this.size = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_fileUpload.size();
    } else utils.invalidArgs();
  };

  this.contentType = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_fileUpload.contentType();
    } else utils.invalidArgs();
  };

  this.contentTransferEncoding = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_fileUpload.contentTransferEncoding();
    } else utils.invalidArgs();
  };

  this.charSet = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_fileUpload.charSet();
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_fileUpload;
};

// We export the Constructor function
module.exports = FileUpload;