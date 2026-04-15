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
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.WebSocketBase;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import io.vertx.test.core.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SockJSWriteTest extends SockJSTestBase {

  @Test
  public void testRaw(VertxTestContext testContext) throws Exception {
    Checkpoint cp = testContext.checkpoint(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onSuccess(v -> {
        cp.flag();
      }));
    };
    startServers();
    vertx.runOnContext(v -> {
      wsClient.connect("/test/websocket").onComplete(TestUtils.onSuccess(ws -> {
        ws.handler(buffer -> {
          if (buffer.toString().equals(expected)) {
            cp.flag();
          }
        });
      }));
    });
  }

  @Test
  public void testRawFailure(VertxTestContext testContext) throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onFailure(err -> {
          testContext.completeNow();
        }));
      });
    };
    startServers();
    vertx.runOnContext(v -> {
      wsClient.connect("/test/websocket").onComplete(TestUtils.onSuccess(WebSocketBase::close));
    });
  }

  @Test
  public void testWebSocket(VertxTestContext testContext) throws Exception {
    Checkpoint cp = testContext.checkpoint(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onSuccess(v -> {
        cp.flag();
      }));
    };
    startServers();
    vertx.runOnContext(v -> {
      wsClient.connect("/test/400/8ne8e94a/websocket").onComplete(TestUtils.onSuccess(ws -> {
        ws.handler(buffer -> {
          if (buffer.toString().equals("a[\"" + expected + "\"]")) {
            cp.flag();
          }
        });
      }));
    });
  }

  @Test
  public void testWebSocketFailure(VertxTestContext testContext) throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onFailure(err -> {
          testContext.completeNow();
        }));
      });
    };
    startServers();
    vertx.runOnContext(v -> {
      wsClient.connect("/test/400/8ne8e94a/websocket").onComplete(TestUtils.onSuccess(WebSocketBase::close));
    });
  }

  @Test
  public void testEventSource(VertxTestContext testContext) throws Exception {
    Checkpoint cp = testContext.checkpoint(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onSuccess(v -> {
        cp.flag();
      }));
    };
    startServers();
    client.request(HttpMethod.GET, "/test/400/8ne8e94a/eventsource")
      .onComplete(TestUtils.onSuccess(req -> req.send().onComplete(TestUtils.onSuccess(resp -> {
        resp.handler(buffer -> {
          if (buffer.toString().equals("data: a[\"" + expected + "\"]\r\n\r\n")) {
            cp.flag();
          }
        });
      }))));
  }

  @Test
  public void testEventSourceFailure(VertxTestContext testContext) throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onFailure(err -> {
          testContext.completeNow();
        }));
      });
    };
    startServers();
    client.request(HttpMethod.GET, "/test/400/8ne8e94a/eventsource")
      .onComplete(TestUtils.onSuccess(req -> req.send().onComplete(TestUtils.onSuccess(resp -> {
        req.connection().close();
      }))));
  }

  @Test
  public void testXHRStreaming(VertxTestContext testContext) throws Exception {
    Checkpoint cp = testContext.checkpoint(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onSuccess(v -> {
        cp.flag();
      }));
    };
    startServers();
    client.request(HttpMethod.POST, "/test/400/8ne8e94a/xhr_streaming")
      .onComplete(TestUtils.onSuccess(req -> req.send(Buffer.buffer()).onComplete(TestUtils.onSuccess(resp -> {
        assertEquals(200, resp.statusCode());
        resp.handler(buffer -> {
          if (buffer.toString().equals("a[\"" + expected + "\"]\n")) {
            cp.flag();
          }
        });
      }))));
  }

  @Test
  public void testXHRStreamingFailure(VertxTestContext testContext) throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onFailure(err -> {
          testContext.completeNow();
        }));
      });
    };
    startServers();
    client.request(HttpMethod.POST, "/test/400/8ne8e94a/xhr_streaming")
      .onComplete(TestUtils.onSuccess(req -> req.send().onComplete(TestUtils.onSuccess(resp -> {
        req.connection().close();
      }))));
  }

  @Test
  public void testXHRPolling(VertxTestContext testContext) throws Exception {
    Checkpoint cp = testContext.checkpoint(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onSuccess(v -> {
        cp.flag();
      }));
    };
    startServers();
    Runnable[] task = new Runnable[1];
    task[0] = () ->
      client.request(HttpMethod.POST, "/test/400/8ne8e94a/xhr")
        .onComplete(TestUtils.onSuccess(req -> req.send(Buffer.buffer()).onComplete(TestUtils.onSuccess(resp -> {
          assertEquals(200, resp.statusCode());
          resp.handler(buffer -> {
            if (buffer.toString().equals("a[\"" + expected + "\"]\n")) {
              cp.flag();
            } else {
              task[0].run();
            }
          });
        }))));
    task[0].run();
  }

  @Test
  public void testXHRPollingClose(VertxTestContext testContext) throws Exception {
    // Take 5 seconds which is the hearbeat timeout
    Checkpoint cp = testContext.checkpoint(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onFailure(err -> {
        cp.flag();
      }));
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onFailure(err -> {
          cp.flag();
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
  public void testXHRPollingShutdown(VertxTestContext testContext) throws Exception {
    // Take 5 seconds which is the hearbeat timeout
    Checkpoint cp = testContext.checkpoint(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onFailure(err -> {
        cp.flag();
      }));
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected)).onComplete(TestUtils.onFailure(err -> {
          cp.flag();
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
