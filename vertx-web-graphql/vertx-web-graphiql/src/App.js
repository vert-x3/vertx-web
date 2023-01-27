/*
 * Copyright 2023 Red Hat, Inc.
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

import React from 'react';
import GraphiQL from 'graphiql';
import 'graphiql/graphiql.min.css';
import {createGraphiQLFetcher} from '@graphiql/toolkit';
import merge from 'lodash/merge';

const config = merge({
  httpEnabled: true,
  graphQLUri: '/graphql',
  graphQLWSEnabled: true,
  graphQLWSUri: '/graphql',
  wsConnectionParams: {},
  headers: {},
  parameters: {}
}, window.VERTX_GRAPHIQL_CONFIG);

const graphiQLFetcherOptions = {
  headers: config.headers,
};

if (config.httpEnabled) {
  graphiQLFetcherOptions.url = window.location.origin + config.graphQLUri;
}

if (config.graphQLWSEnabled) {
  const urlObj = new URL(window.location.origin);
  urlObj.protocol = urlObj.protocol === 'https:' ? 'wss:' : 'ws:';
  graphiQLFetcherOptions.subscriptionUrl = urlObj.toString() + config.graphQLWSUri;
  graphiQLFetcherOptions.wsConnectionParams = config.wsConnectionParams;
}

const fetcher = createGraphiQLFetcher(graphiQLFetcherOptions);

const search = window.location.search;
search.substring(1).split('&').forEach(function (entry) {
  const eq = entry.indexOf('=');
  if (eq >= 0) {
    config.parameters[decodeURIComponent(entry.slice(0, eq))] =
      decodeURIComponent(entry.slice(eq + 1));
  }
});

if (config.parameters.variables) {
  try {
    config.parameters.variables =
      JSON.stringify(JSON.parse(config.parameters.variables), null, 2);
  } catch (e) {
  }
}

function onEditQuery(newQuery) {
  config.parameters.query = newQuery;
  updateURL();
}

function onEditVariables(newVariables) {
  config.parameters.variables = newVariables;
  updateURL();
}

function onEditOperationName(newOperationName) {
  config.parameters.operationName = newOperationName;
  updateURL();
}

function updateURL() {
  const newSearch = '?' + Object.keys(config.parameters).filter(function (key) {
    return Boolean(config.parameters[key]);
  }).map(function (key) {
    return encodeURIComponent(key) + '=' +
      encodeURIComponent(config.parameters[key]);
  }).join('&');
  window.history.replaceState(null, null, newSearch);
}

const App = () => (
  <GraphiQL
    fetcher={fetcher}
    query={config.parameters.query}
    variables={config.parameters.variables}
    operationName={config.parameters.operationName}
    onEditQuery={onEditQuery}
    onEditVariables={onEditVariables}
    onEditOperationName={onEditOperationName}
  />
);

export default App;
