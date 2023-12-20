/*
 * Copyright 2020 Red Hat, Inc.
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

package io.vertx.ext.web.handler.sse;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SSEReceiveDataTest extends SSEBaseTest {

  @Test
  public void testSimpleDataHandler() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    final String message = "Happiness is a warm puppy";
    final EventSource eventSource = eventSource();
    eventSource.connect(SSE_ENDPOINT + "?token=" + TOKEN, handler -> {
      assertTrue(handler.succeeded());
      assertFalse(handler.failed());
      assertNull(handler.cause());
      assertNotNull(connection);
      eventSource.messageHandler(msg -> {
        assertEquals(message, msg);
        latch.countDown();
      });
      connection.data(message);
    });
    awaitLatch(latch);
  }

  @Test
  public void testConsecutiveDataHandler() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    final List<String> quotes = createData();
    final EventSource eventSource = eventSource();
    eventSource.connect(SSE_ENDPOINT + "?token=" + TOKEN, handler -> {
      assertTrue(handler.succeeded());
      assertFalse(handler.failed());
      assertNull(handler.cause());
      assertNotNull(connection);
      final List<String> received = new ArrayList<>();
      eventSource.messageHandler(msg -> {
        received.add(msg);
        if (received.size() == quotes.size()) {
          for (int i = 0; i < received.size(); i++) {
            assertEquals("Received quotes don't match", quotes.get(i), received.get(i));
          }
          latch.countDown();
        }
      });
      quotes.forEach(connection::data);
    });
    awaitLatch(latch);
  }

  @Test
  public void testEventHandler() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    final String eventName = "quotes";
    final List<String> quotes = createData();
    final EventSource eventSource = eventSource();
    eventSource.connect(SSE_ENDPOINT + "?token=" + TOKEN, handler -> {
      assertTrue(handler.succeeded());
      assertFalse(handler.failed());
      assertNull(handler.cause());
      assertNotNull(connection);
      String quote = quotes.get(0);
      eventSource.eventHandler("wrong", msg -> {
        throw new RuntimeException("this handler should not be called, at all !");
      });
      eventSource.eventHandler(eventName, msg -> {
        assertEquals(quote, msg);
        latch.countDown();
      });
      connection.event(eventName);
      connection.data(quote);
    });
    awaitLatch(latch);
  }

  @Test
  public void testId() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    final String id = "SomeIdentifier";
    final List<String> quotes = createData();
    final EventSource eventSource = eventSource();
    eventSource.connect("/sse?token=" + TOKEN, handler -> {
      assertTrue(handler.succeeded());
      assertFalse(handler.failed());
      assertNull(handler.cause());
      assertNotNull(connection);
      String quote = quotes.get(2);
      eventSource.messageHandler(msg -> {
        assertEquals(quote, msg);
        assertEquals(id, eventSource.lastId());
        eventSource.close();
        eventSource.connectHandler(SSE_ENDPOINT + "?token=" + TOKEN, eventSource.lastId(), secondHandler -> {
          assertTrue(handler.succeeded());
          assertFalse(handler.failed());
          assertNull(handler.cause());
          assertNotNull(connection);
          assertEquals(id, connection.lastId());
          latch.countDown();
        });
      });
      connection.id(id);
      connection.data(quote);
    });
    awaitLatch(latch);
  }

  @Test
  public void receiveDataWithAndWithoutId() throws Exception {
    CountDownLatch latch = new CountDownLatch(2);
    EventSource es = eventSource();
    List<String> messagesReceived = new ArrayList<>(2);
    es.messageHandler(msg -> {
      assertNotNull(es.lastId());
      assertNotNull(msg);
      messagesReceived.add(msg);
      latch.countDown(); // we should receive 2 messages, first has an id, the second one doesn't, lastId should never been discarded
    });
    es.connect(SSE_MULTIPLE_MESSAGES_ENDPOINT, res -> {
      assertFalse(res.failed());
    });
    awaitLatch(latch);
    assertTrue(messagesReceived.contains("some-other-data-without-id"));
  }

  @Test
  public void disconnectAndReconnectWithId() throws Exception {
    CountDownLatch latch = new CountDownLatch(7);
    EventSource es = eventSource();
    List<Integer> idsReceived = new ArrayList<>();
    es.messageHandler(msg -> {
      assertNotNull(es.lastId());
      idsReceived.add(Integer.parseInt(es.lastId()));
      latch.countDown();
    });
    es.connect(SSE_ID_TEST_ENDPOINT, res -> { // this endpoint periodically increments a counter and send it as 'id', with "last-event-id" as initial value
      assertFalse(res.failed());
    });
    awaitLatch(latch);
    assertEquals(7, idsReceived.size());
    List<Integer> expectedIds = Arrays.asList(1, 2, 3, 4, 5, 6, 7); // we should not have received (1, 2, 1, 2, 1, 2) which would happen if we did not reconnect with last-id properly set
    assertEquals(expectedIds, idsReceived);
  }

  @Test
  public void testMultilineData() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    eventSource()
      .messageHandler(msg -> {
        assertEquals(multilineMsg, msg);
        latch.countDown();
      })
      .connect(SSE_MULTILINE_ENDPOINT, res -> {
        assertFalse(res.failed());
      });
    awaitLatch(latch);
  }

  private List<String> createData() {
    final List<String> data = new ArrayList<>(3);
    data.add("Happiness is a warm puppy");
    data.add("Bleh!");
    data.add("That's the secret of life... replace one worry with another");
    return data;
  }

}
