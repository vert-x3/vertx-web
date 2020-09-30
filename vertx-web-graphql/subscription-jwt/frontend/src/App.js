/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

import React, {useEffect, useState} from 'react';
import './App.css';
import {WebSocketLink} from "@apollo/client/link/ws";
import gql from "graphql-tag";
import {ApolloClient, ApolloProvider, HttpLink, InMemoryCache, split, useQuery, useSubscription} from "@apollo/client";
import {setContext} from '@apollo/client/link/context';
import {getMainDefinition} from "@apollo/client/utilities";
import axios from 'axios';

const SUB = gql`
  subscription Sub {
    default {
      data
    }
  }
`;

const QUERY = gql`
  query Query {
    default {
      data
    }
  }
`;

const getHeaders = (headers, token) => {
  return {
    headers: {
      ...headers,
      authorization: "Bearer " + token
    }
  }
}

const authLink = token => setContext((_, { headers }) => {
  return getHeaders(headers, token);
});

const httpLink = new HttpLink({
  uri: `http://localhost:8080/v1/graphql`,
});

const wsLink = token => new WebSocketLink({
  uri: "ws://localhost:8080/v1/graphql",
  options: {
    reconnect: true,
    connectionParams: getHeaders({}, token)
  }
});

const link = token => split(
  ({ query }) => {
    const mainDefinition = getMainDefinition(query);
    return mainDefinition.kind === "OperationDefinition" ? mainDefinition.operation === "subscription" : false;
  },
  wsLink(token),
  authLink(token).concat(httpLink)
);

const apolloClient = token => new ApolloClient({
  link: link(token),
  cache: new InMemoryCache()
})

function App() {

  const [client, setClient] = useState(new ApolloClient({ link: null, cache: new InMemoryCache()}));

  useEffect(() => {
    axios.get("http://localhost:8080/v1/token")
      .then(result => setClient(apolloClient(result.data)));
  }, []);

  return (
    <ApolloProvider client={client}>
      <AppInternal/>
    </ApolloProvider>
  )
}

function AppInternal() {

  const { data, loading} = useSubscription(SUB);
  const {loading: loadingQuery, data: dataQuery } = useQuery(QUERY);

  return (
      <div className="App">
        <header className="App-header">
          <p>
            query response: {!loadingQuery && dataQuery && dataQuery.default.data}
          </p>
          <p>
            subscription response: {!loading && data && data.default.data}
          </p>
        </header>
      </div>
  );
}

export default App;
