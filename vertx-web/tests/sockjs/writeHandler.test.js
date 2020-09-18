/*
 * Copyright 2020 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the 'License'); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import fetch from 'node-fetch';

async function fetchTransports() {
  let response = await fetch('http://localhost:8080/transports', {});
  expect(response.status).toBe(200);
  return response.json();
}

async function runTest(transport, registered, local, expected) {
  await page.goto('http://localhost:8080/sockjs/writeHandler.html');
  await page.evaluate((transport, registered, local) => {
    doTest(transport, registered, local);
  }, transport, registered, local);
  let element = await page.waitForSelector('div');
  let text = await page.evaluate(element => element.textContent, element);
  expect(text).toBe(expected);
}

test('unregistered', async () => {
  let transports = await fetchTransports();
  for (let transport of transports) {
    await runTest(transport, false, true, '');
  }
}, 1000 * 60);

test('registered_local', async () => {
  let transports = await fetchTransports();
  for (let transport of transports) {
    await runTest(transport, true, true, 'foo');
  }
}, 1000 * 60);

test('registered_clustered', async () => {
  let transports = await fetchTransports();
  for (let transport of transports) {
    await runTest(transport, true, false, 'foobar');
  }
}, 1000 * 60);
