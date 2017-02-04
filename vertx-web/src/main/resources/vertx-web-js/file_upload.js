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

/** @module vertx-web-js/file_upload */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JFileUpload = Java.type('io.vertx.ext.web.FileUpload');

/**
 Represents a file-upload from an HTTP multipart form submission.
 <p>

 @class
*/
var FileUpload = function(j_val) {

  var j_fileUpload = j_val;
  var that = this;

  /**

   @public

   @return {string} the name of the upload as provided in the form submission
   */
  this.name = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_fileUpload["name()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {string} the actual temporary file name on the server where the file was uploaded to.
   */
  this.uploadedFileName = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_fileUpload["uploadedFileName()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {string} the file name of the upload as provided in the form submission
   */
  this.fileName = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_fileUpload["fileName()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {number} the size of the upload, in bytes
   */
  this.size = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_fileUpload["size()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {string} the content type (MIME type) of the upload
   */
  this.contentType = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_fileUpload["contentType()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {string} the content transfer encoding of the upload - this describes how the upload was encoded in the form submission.
   */
  this.contentTransferEncoding = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_fileUpload["contentTransferEncoding()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {string} the charset of the upload
   */
  this.charSet = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_fileUpload["charSet()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_fileUpload;
};

FileUpload._jclass = utils.getJavaClass("io.vertx.ext.web.FileUpload");
FileUpload._jtype = {
  accept: function(obj) {
    return FileUpload._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(FileUpload.prototype, {});
    FileUpload.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
FileUpload._create = function(jdel) {
  var obj = Object.create(FileUpload.prototype, {});
  FileUpload.apply(obj, arguments);
  return obj;
}
module.exports = FileUpload;