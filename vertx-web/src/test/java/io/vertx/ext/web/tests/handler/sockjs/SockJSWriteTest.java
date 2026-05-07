/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.tests.handler.sockjs;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.WebSocketBase;
import io.vertx.junit5.Checkpoint;
import io.vertx.test.core.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SockJSWriteTest extends SockJSTestBase {

  @Test
  public void testRaw(Checkpoint checkpoint) throws Exception {
    CountDownLatch cp = checkpoint.asLatch(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onSuccess(v -> {
        cp.countDown();
      }));
    };
    startServers();
    vertx.runOnContext(v -> {
      wsClient.connect("/test/websocket").onComplete(TestUtils.onSuccess(ws -> {
        ws.handler(buffer -> {
          if (buffer.toString().equals(expected)) {
            cp.countDown();
          }
        });
      }));
    });
  }

  @Test
  public void testRawFailure(Checkpoint checkpoint) throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onFailure(err -> {
          checkpoint.flag();
        }));
      });
    };
    startServers();
    vertx.runOnContext(v -> {
      wsClient.connect("/test/websocket").onComplete(TestUtils.onSuccess(WebSocketBase::close));
    });
  }

  @Test
  public void testWebSocket(Checkpoint checkpoint) throws Exception {
    CountDownLatch cp = checkpoint.asLatch(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onSuccess(v -> {
        cp.countDown();
      }));
    };
    startServers();
    vertx.runOnContext(v -> {
      wsClient.connect("/test/400/8ne8e94a/websocket").onComplete(TestUtils.onSuccess(ws -> {
        ws.handler(buffer -> {
          if (buffer.toString().equals("a[\"" + expected + "\"]")) {
            cp.countDown();
          }
        });
      }));
    });
  }

  @Test
  public void testWebSocketFailure(Checkpoint checkpoint) throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onFailure(err -> {
          checkpoint.flag();
        }));
      });
    };
    startServers();
    vertx.runOnContext(v -> {
      wsClient.connect("/test/400/8ne8e94a/websocket").onComplete(TestUtils.onSuccess(WebSocketBase::close));
    });
  }

  @Test
  public void testEventSource(Checkpoint checkpoint) throws Exception {
    CountDownLatch cp = checkpoint.asLatch(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onSuccess(v -> {
        cp.countDown();
      }));
    };
    startServers();
    client.request(HttpMethod.GET, "/test/400/8ne8e94a/eventsource")
      .compose(HttpClientRequest::send)
      .onComplete(TestUtils.onSuccess(resp -> {
        resp.handler(buffer -> {
          if (buffer.toString().equals("data: a[\"" + expected + "\"]\r\n\r\n")) {
            cp.countDown();
          }
        });
      }));
  }

  @Test
  public void testEventSourceFailure(Checkpoint checkpoint) throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onFailure(err -> {
          checkpoint.flag();
        }));
      });
    };
    startServers();
    client.request(HttpMethod.GET, "/test/400/8ne8e94a/eventsource")
      .compose(HttpClientRequest::send)
      .onComplete(TestUtils.onSuccess(resp -> {
        resp.request().connection().close();
      }));
  }

  @Test
  public void testXHRStreaming(Checkpoint checkpoint) throws Exception {
    CountDownLatch cp = checkpoint.asLatch(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onSuccess(v -> {
        cp.countDown();
      }));
    };
    startServers();
    client.request(HttpMethod.POST, "/test/400/8ne8e94a/xhr_streaming")
      .compose(req -> req.send(Buffer.buffer()))
      .onComplete(TestUtils.onSuccess(resp -> {
        assertEquals(200, resp.statusCode());
        resp.handler(buffer -> {
          if (buffer.toString().equals("a[\"" + expected + "\"]\n")) {
            cp.countDown();
          }
        });
      }));
  }

  @Test
  public void testXHRStreamingFailure(Checkpoint checkpoint) throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onFailure(err -> {
          checkpoint.flag();
        }));
      });
    };
    startServers();
    client.request(HttpMethod.POST, "/test/400/8ne8e94a/xhr_streaming")
      .compose(HttpClientRequest::send)
      .onComplete(TestUtils.onSuccess(resp -> {
        resp.request().connection().close();
      }));
  }

  @Test
  public void testXHRPolling(Checkpoint checkpoint) throws Exception {
    CountDownLatch cp = checkpoint.asLatch(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onSuccess(v -> {
        cp.countDown();
      }));
    };
    startServers();
    Runnable[] task = new Runnable[1];
    task[0] = () ->
      client.request(HttpMethod.POST, "/test/400/8ne8e94a/xhr")
        .compose(req -> req.send(Buffer.buffer()))
        .onComplete(TestUtils.onSuccess(resp -> {
          assertEquals(200, resp.statusCode());
          resp.handler(buffer -> {
            if (buffer.toString().equals("a[\"" + expected + "\"]\n")) {
              cp.countDown();
            } else {
              task[0].run();
            }
          });
        }));
    task[0].run();
  }

  @Test
  public void testXHRPollingClose(Checkpoint checkpoint) throws Exception {
    // Take 5 seconds which is the hearbeat timeout
    CountDownLatch cp = checkpoint.asLatch(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onFailure(err -> {
        cp.countDown();
      }));
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onFailure(err -> {
          cp.countDown();
        }));
      });
      socket.close();
    };
    startServers();
    webClient.post("/test/400/8ne8e94a/xhr")
      .send()
      .expecting(io.vertx.core.http.HttpResponseExpectation.SC_OK)
      .await();
  }

  @Test
  public void testXHRPollingShutdown(Checkpoint checkpoint) throws Exception {
    // Take 5 seconds which is the hearbeat timeout
    CountDownLatch cp = checkpoint.asLatch(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onFailure(err -> {
        cp.countDown();
      }));
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onFailure(err -> {
          cp.countDown();
        }));
      });
    };
    startServers();
    webClient.post("/test/400/8ne8e94a/xhr")
      .send()
      .expecting(io.vertx.core.http.HttpResponseExpectation.SC_OK)
      .await();
  }
}
