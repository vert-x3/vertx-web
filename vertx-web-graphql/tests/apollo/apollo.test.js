import fetch from 'unfetch';
import {execute, makePromise} from 'apollo-link';
import {HttpLink} from 'apollo-link-http';
import {BatchHttpLink} from 'apollo-link-batch-http';
import {WebSocketLink} from 'apollo-link-ws';
import {SubscriptionClient} from 'subscriptions-transport-ws';
import {ApolloClient} from 'apollo-client';
import {createUploadLink} from 'apollo-upload-client';
import {InMemoryCache} from 'apollo-cache-inmemory';

import gql from 'graphql-tag';

const uri = 'http://localhost:8080/graphql';
const wsUri = 'ws://localhost:8080/graphql';

const client = new ApolloClient({
  cache: new InMemoryCache(),
  link: createUploadLink({ uri, fetch })
});

const allLinksQuery = gql`
{
  allLinks {
    url
  }
}
`;

const secureOnlyQuery = gql`
{
  allLinks (secureOnly: true) {
    url
  }
}
`;

const staticCounterQuery = gql`
query Query {
  staticCounter (num: 5) {
    count
  }
}
`;

const counterSubscription = gql`
subscription Subscription {
  counter {
    count
  }
}
`;

const uploadFileMutation = gql`
mutation Mutation($file: Upload!) {
  singleUpload(file: $file) {
    id
  }
}
`;

const verify = result => {
  expect(result).toHaveProperty('data.allLinks');
  expect(result.data.allLinks).toBeInstanceOf(Array);

  result.data.allLinks.forEach(link => {
    expect(Object.keys(link)).toEqual(['url']);
    expect(link.url).toEqual(expect.anything());
  })
};

test('http link', async () => {
  let link = new HttpLink({uri: uri, fetch: fetch});
  let result = await makePromise(execute(link, {query: allLinksQuery}));
  verify(result);
});

test('batch http link', async () => {
  let link = new BatchHttpLink({uri: uri, fetch: fetch});
  let results = await Promise.all([allLinksQuery, secureOnlyQuery].map(q => makePromise(execute(link, {query: q}))));
  results.forEach(verify);
});

test('ws link', async () => {
  const client = new SubscriptionClient(wsUri);
  const link = new WebSocketLink(client);
  let result = await makePromise(execute(link, {query: staticCounterQuery}));

  expect(result).toHaveProperty('data.staticCounter');
  expect(result.data.staticCounter).toBeInstanceOf(Object);

  expect(result.data.staticCounter.count).toEqual(5);
});

test('ws link subscription', () => {
  const client = new SubscriptionClient(wsUri);
  const link = new WebSocketLink(client);

  return new Promise((resolve, reject) => {
    execute(link, {query: counterSubscription})
      .subscribe(result => {
        expect(result).toHaveProperty('data.counter');
        expect(result.data.counter).toBeInstanceOf(Object);

        expect(result.data.counter.count).toEqual(1);

        resolve();
      });
  });
});

test('upload file mutation', async () => {
  const file = new Blob(['Foo.'], { type: 'text/plain' })

  const result = await client.mutate({
    mutation: uploadFileMutation,
    variables: {
      file: file
    }
  });

  expect(result).toHaveProperty('data.singleUpload.id');
  expect(result.data.singleUpload.id).toEqual('blob');
})
