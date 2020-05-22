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
      eventSource.onMessage(msg -> {
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
      eventSource.onMessage(msg -> {
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
      eventSource.addEventListener("wrong", msg -> {
        throw new RuntimeException("this handler should not be called, at all !");
      });
      eventSource.addEventListener(eventName, msg -> {
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
      eventSource.onMessage(msg -> {
        assertEquals(quote, msg);
        assertEquals(id, eventSource.lastId());
        eventSource.close();
        eventSource.connect(SSE_ENDPOINT + "?token=" + TOKEN, eventSource.lastId(), secondHandler -> {
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

  private List<String> createData() {
    final List<String> data = new ArrayList<>(3);
    data.add("Happiness is a warm puppy");
    data.add("Bleh!");
    data.add("That's the secret of life... replace one worry with another");
    return data;
  }

}
