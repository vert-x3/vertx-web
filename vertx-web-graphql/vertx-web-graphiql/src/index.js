import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import 'graphiql/graphiql.css';
import GraphiQL from 'graphiql';
import fetch from 'isomorphic-fetch';
import merge from 'lodash/merge';

const config = merge({
  graphQLUri: '/graphql',
  headers: {
    'Accept': 'application/json',
    'Content-Type': 'application/json'
  },
  parameters: {}
}, window.VERTX_GRAPHIQL_CONFIG);

const search = window.location.search;
search.substr(1).split('&').forEach(function (entry) {
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

function graphQLFetcher(graphQLParams) {
  return fetch(window.location.origin + config.graphQLUri, {
    method: 'post',
    headers: config.headers,
    body: JSON.stringify(graphQLParams),
    credentials: 'include'
  }).then(response => response.json());
}

ReactDOM.render(<GraphiQL fetcher={graphQLFetcher}
                          query={config.parameters.query}
                          variables={config.parameters.variables}
                          operationName={config.parameters.operationName}
                          onEditQuery={onEditQuery}
                          onEditVariables={onEditVariables}
                          onEditOperationName={onEditOperationName}/>, document.getElementById('root'));
