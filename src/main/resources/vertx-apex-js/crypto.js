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

/** @module vertx-apex-js/crypto */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JCrypto = io.vertx.ext.apex.Crypto;

/**
 A crypto provides method to encrypt and decrypt data. 
 It is a fairly generic abstraction able to encrypt/decrypt vertx Buffers, although the main use case right now it to encrypt Cookies and Sessions
 The first concrete implementation of this interface is based is based on AES/CBC/Padding + Hmac.
 
 @class
*/
var Crypto = function(j_val) {

  var j_crypto = j_val;
  var that = this;

  /**
   Encrypts the specified buffer

   @public
   @param unencryptedData {string} 
   @return {string} a new buffer containing encrypted data
   */
  this.encrypt = function(unencryptedData) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return j_crypto["encrypt(java.lang.String)"](unencryptedData);
    } else utils.invalidArgs();
  };

  /**
   Decrypts the specified buffer

   @public
   @param encryptedData {string} a buffer which must have previously been encrypted by {link {@link Crypto#encrypt(Buffer)} 
   @return {string} a new buffer containing decrypted data
   */
  this.decrypt = function(encryptedData) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return j_crypto["decrypt(java.lang.String)"](encryptedData);
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_crypto;
};

// We export the Constructor function
module.exports = Crypto;