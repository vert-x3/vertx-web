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

import {WebSocket} from "ws"
import {createClient} from "graphql-ws"

let client

afterEach(() => {
  if (client) {
    client.dispose()
    client = null
  }
})

test('query', async () => {
  client = createClient({
    url: 'ws://localhost:8080/graphql',
    webSocketImpl: WebSocket
  })

  const result = await new Promise((resolve, reject) => {
    let result
    client.subscribe(
      {
        query: '{ hello }',
      },
      {
        next(data) {
          result = data
        },
        error: reject,
        complete() {
          resolve(result)
        },
      },
    )
  })

  expect(result).toEqual({data: {hello: 'Hello World!'}})
})

test('subscription', async () => {
  client = createClient({
    url: 'ws://localhost:8080/graphql',
    webSocketImpl: WebSocket
  })

  const result = await new Promise((resolve, reject) => {
    let result = []
    client.subscribe(
      {
        query: 'subscription { greetings }',
      },
      {
        next(val) {
          result.push(val)
        },
        error: reject,
        complete() {
          resolve(result)
        },
      },
    )
  })

  const expected = ['Hi', 'Bonjour', 'Hola', 'Ciao', 'Zdravo'].map(value => ({data: {greetings: value}}))
  expect(result).toStrictEqual(expected)
})

test('subscription with error', async () => {
  client = createClient({
    url: 'ws://localhost:8080/graphql',
    webSocketImpl: WebSocket
  })

  const result = await new Promise((resolve, reject) => {
    let result = []
    client.subscribe(
      {
        query: 'subscription { greetAndFail }',
      },
      {
        next(val) {
          result.push(val)
        },
        error(reason) {
          result.push(reason)
          resolve(result)
        },
        complete: reject,
      },
    )
  })

  const expected = [{data: {greetAndFail: 'Hi'}}, [{message: 'java.lang.Exception: boom'}]]
  expect(result).toStrictEqual(expected)
})

test('ws link subscription with failed promise', async () => {
  client = createClient({
    url: 'ws://localhost:8080/graphqlWithInitHandler',
    webSocketImpl: WebSocket,
    connectionParams: {
      rejectMessage: 'test'
    }
  })

  let err
  try {
    await new Promise((resolve, reject) => {
      client.subscribe(
        {
          query: 'subscription { greetings }',
        },
        {
          next: () => {
          },
          error: error => reject(error),
          complete: resolve,
        },
      )
    })
  } catch (e) {
    err = e
  }
  expect(err).toBeDefined()
  expect(err.code).toEqual(4401)
})
