import {gql} from 'graphql-tag'
import {ApolloClient, execute, HttpLink, InMemoryCache, toPromise} from '@apollo/client/core'
import {BatchHttpLink} from '@apollo/client/link/batch-http'
import {WebSocket} from 'ws'
import {WebSocketLink} from '@apollo/client/link/ws';
import {SubscriptionClient} from 'subscriptions-transport-ws';
import {createUploadLink} from 'apollo-upload-client';

const uri = 'http://localhost:8080/graphql';
const wsUri = 'ws://localhost:8080/graphql';

const client = new ApolloClient({
  cache: new InMemoryCache(),
  link: createUploadLink({uri, fetch})
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
  let link = new HttpLink({uri: uri});
  let result = await toPromise(execute(link, {query: allLinksQuery}));
  verify(result);
});

test('batch http link', async () => {
  let link = new BatchHttpLink({uri: uri});
  let results = await Promise.all([allLinksQuery, secureOnlyQuery].map(q => toPromise(execute(link, {query: q}))));
  results.forEach(verify);
});

test('ws link', async () => {
  const client = new SubscriptionClient(wsUri, {}, WebSocket);
  const link = new WebSocketLink(client);
  let result = await toPromise(execute(link, {query: staticCounterQuery}));

  expect(result).toHaveProperty('data.staticCounter');
  expect(result.data.staticCounter).toBeInstanceOf(Object);

  expect(result.data.staticCounter.count).toEqual(5);
});

test('ws link subscription', () => {
  const client = new SubscriptionClient(wsUri, {}, WebSocket);
  const link = new WebSocketLink(client);

  return new Promise((resolve) => {
    execute(link, {query: counterSubscription})
      .subscribe(result => {
        expect(result).toHaveProperty('data.counter');
        expect(result.data.counter).toBeInstanceOf(Object);

        expect(result.data.counter.count).toEqual(1);

        resolve();
      });
  });
});

test('ws link subscription with connection params', () => {
  const client = new SubscriptionClient(wsUri, {
    connectionParams: {
      count: 2
    }
  }, WebSocket);
  const link = new WebSocketLink(client);

  return new Promise((resolve) => {
    execute(link, {query: counterSubscription})
      .subscribe(result => {
        expect(result).toHaveProperty('data.counter');
        expect(result.data.counter).toBeInstanceOf(Object);

        expect(result.data.counter.count).toEqual(2);

        resolve();
      });
  });
});

test('ws link subscription with failed promise', () => {
  return new Promise((resolve) => {
    new SubscriptionClient(wsUri, {
      connectionParams: {
        rejectMessage: 'test'
      },
      connectionCallback: error => {
        expect(error).toEqual('test');
        resolve()
      }
    }, WebSocket);
  });
});

test('upload file mutation', async () => {
  const file = new Blob(['Foo.'], {type: 'text/plain'})
  file.name = 'text.txt'

  const result = await client.mutate({
    mutation: uploadFileMutation,
    variables: {
      file: file
    }
  });

  expect(result).toHaveProperty('data.singleUpload.id');
  expect(result.data.singleUpload.id).toEqual('text.txt');
})
