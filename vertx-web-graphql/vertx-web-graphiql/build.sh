#!/usr/bin/env bash

rm -rf build
rm -rf ../src/main/resources/io/vertx/ext/web/handler/graphiql
mkdir --parents ../src/main/resources/io/vertx/ext/web/handler/graphiql
npm install
npm run build
node copy-to-resources.js
