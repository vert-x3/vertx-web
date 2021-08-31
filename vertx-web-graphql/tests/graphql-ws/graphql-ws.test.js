/*
 * Copyright 2021 Red Hat, Inc.
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

import {createClient} from "graphql-ws";

let client;

afterEach(() => {
  if (client) {
    client.dispose();
    client = null;
  }
});

test('query', async () => {
  client = createClient({
    url: 'ws://localhost:8080/graphql',
  });

  const result = await new Promise((resolve, reject) => {
    let result;
    client.subscribe(
      {
        query: '{ hello }',
      },
      {
        next: (data) => (result = data),
        error: reject,
        complete: () => resolve(result),
      },
    );
  });

  expect(result).toEqual({data: {hello: 'Hello World!'}});
});

test('subscription', async () => {
  client = createClient({
    url: 'ws://localhost:8080/graphql',
  });

  const onNext = jest.fn(() => {
  });

  await new Promise((resolve, reject) => {
    client.subscribe(
      {
        query: 'subscription { greetings }',
      },
      {
        next: onNext,
        error: reject,
        complete: resolve,
      },
    );
  });

  expect(onNext).toBeCalledTimes(5); // we say "Hi" in 5 languages
});

test('ws link subscription with failed promise', async () => {
  client = createClient({
    url: 'ws://localhost:8080/graphql',
    connectionParams: {
      rejectMessage: "test"
    }
  });

  try {
    await new Promise((resolve, reject) => {
      client.subscribe(
        {
          query: 'subscription { greetings }',
        },
        {
          next: value => {
          },
          error: error => reject(error),
          complete: resolve,
        },
      );
    });
  } catch (e) {
    expect(e).toBeInstanceOf(CloseEvent);
    expect(e.code).toEqual(4401);
  }
});
