import fetch from 'unfetch';
import {execute, makePromise} from 'apollo-link';
import {HttpLink} from 'apollo-link-http';
import {BatchHttpLink} from 'apollo-link-batch-http';
import gql from 'graphql-tag';

const uri = 'http://localhost:8080/graphql';

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
