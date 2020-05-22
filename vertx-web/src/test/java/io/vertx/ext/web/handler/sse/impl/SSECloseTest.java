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

package io.vertx.ext.web.handler.sse.impl;

import io.vertx.ext.web.handler.sse.EventSource;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class SSECloseTest extends SSEBaseTest {

  private void waitSafely() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException ie) {}
  }

  @Test
  public void closeHandlerOnServer() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    final EventSource eventSource = eventSource();
    eventSource.connect(SSE_ENDPOINT + "?token=" + TOKEN, handler -> {
      assertTrue(handler.succeeded());
      assertNotNull(connection);
      connection.closeHandler(sse -> latch.countDown());
      waitSafely();
      eventSource.close(); /* closed by client */
    });
    awaitLatch(latch);
  }

}
