/*
 * Copyright 2019 Red Hat, Inc.
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
const fs = require('fs/promises');
const copy = require('recursive-copy');

const destDir = '../src/main/resources/io/vertx/ext/web/handler/graphiql';

fs.rm(destDir, {force: true, recursive: true})
  .then(() =>
    copy('build', destDir, {
      filter: [
        'index.html',
        'static/**/*.js',
        'static/**/*.css'
      ]
    }))
  .catch(function (error) {
    console.error('Copy failed: ' + error);
    process.exit(1);
  });
