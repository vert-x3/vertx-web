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

import java.util.concurrent.CountDownLatch;

public class SSETestClose extends SSETestBase {

  private void waitSafely() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException ie) {}
  }

  @Test
  public void closeHandlerOnServer() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    final EventSource eventSource = eventSource();
    eventSource.connect("/sse?token=" + TOKEN, handler -> {
      assertTrue(handler.succeeded());
      assertNotNull(connection);
      connection.closeHandler(sse -> latch.countDown());
      waitSafely();
      eventSource.close(); /* closed by client */
    });
    awaitLatch(latch);
  }

  @Test
  public void closeHandlerOnClient() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    final EventSource eventSource = eventSource();
    eventSource.onClose(handler -> latch.countDown());
    eventSource.connect("/sse?token=" + TOKEN, handler -> {
      assertNotNull(connection);
      connection.close();
    });
    awaitLatch(latch);
  }

}
