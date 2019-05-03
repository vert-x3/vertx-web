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
package io.vertx.ext.web.handler.sockjs;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocketBase;
import io.vertx.test.core.TestUtils;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SockJSWriteTest extends SockJSTestBase {

  @Test
  public void testRaw() throws Exception {
    waitFor(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected), onSuccess(v -> {
        complete();
      }));
    };
    startServers();
    client.websocket("/test/websocket", ws -> {
      ws.handler(buffer -> {
        if (buffer.toString().equals(expected)) {
          complete();
        }
      });
    });
    await();
  }

  @Test
  public void testRawFailure() throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected), onFailure(err -> {
          testComplete();
        }));
      });
    };
    startServers();
    client.websocket("/test/websocket", WebSocketBase::close);
    await();
  }

  @Test
  public void testWebSocket() throws Exception {
    waitFor(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected), onSuccess(v -> {
        complete();
      }));
    };
    startServers();
    client.websocket("/test/400/8ne8e94a/websocket", ws -> {
      ws.handler(buffer -> {
        if (buffer.toString().equals("a[\"" + expected + "\"]")) {
          complete();
        }
      });
    });
    await();
  }

  @Test
  public void testWebSocketFailure() throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected), onFailure(err -> {
          testComplete();
        }));
      });
    };
    startServers();
    client.websocket("/test/400/8ne8e94a/websocket", WebSocketBase::close);
    await();
  }

  @Test
  public void testEventSource() throws Exception {
    waitFor(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected), onSuccess(v -> {
        complete();
      }));
    };
    startServers();
    client.get("/test/400/8ne8e94a/eventsource", resp -> {
      resp.handler(buffer -> {
        if (buffer.toString().equals("data: a[\"" + expected + "\"]\r\n\r\n")) {
          complete();
        }
      });
    }).end();
    await();
  }

  @Test
  public void testEventSourceFailure() throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected), onFailure(err -> {
          testComplete();
        }));
      });
    };
    startServers();
    client.get("/test/400/8ne8e94a/eventsource", resp -> {
      resp.request().connection().close();
    }).end();
    await();
  }

  @Test
  public void testXHRStreaming() throws Exception {
    waitFor(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected), onSuccess(v -> {
        complete();
      }));
    };
    startServers();
    client.post("/test/400/8ne8e94a/xhr_streaming", resp -> {
      assertEquals(200, resp.statusCode());
      resp.handler(buffer -> {
        if (buffer.toString().equals("a[\"" + expected + "\"]\n")) {
          complete();
        }
      });
    }).end();
    await();
  }

  @Test
  public void testXHRStreamingFailure() throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected), onFailure(err -> {
          testComplete();
        }));
      });
    };
    startServers();
    client.post("/test/400/8ne8e94a/xhr_streaming", resp -> {
      resp.request().connection().close();
    }).end();
    await();
  }

  @Test
  public void testXHRPolling() throws Exception {
    waitFor(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected), onSuccess(v -> {
        complete();
      }));
    };
    startServers();
    Runnable[] task = new Runnable[1];
    task[0] = () ->
    client.post("/test/400/8ne8e94a/xhr", resp -> {
      assertEquals(200, resp.statusCode());
      resp.handler(buffer -> {
        if (buffer.toString().equals("a[\"" + expected + "\"]\n")) {
          complete();
        } else {
          task[0].run();
        }
      });
    }).end();
    task[0].run();
    await();
  }

  @Test
  public void testXHRPollingClose() throws Exception {
    // Take 5 seconds which is the hearbeat timeout
    waitFor(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected), onFailure(err -> {
        complete();
      }));
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected), onFailure(err -> {
          complete();
        }));
      });
      socket.close();
    };
    startServers();
    client.post("/test/400/8ne8e94a/xhr", resp -> {
      assertEquals(200, resp.statusCode());
    }).end();
    await();
  }

  @Test
  public void testXHRPollingShutdown() throws Exception {
    // Take 5 seconds which is the hearbeat timeout
    waitFor(2);
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(Buffer.buffer(expected), onFailure(err -> {
        complete();
      }));
      socket.endHandler(v -> {
        socket.write(Buffer.buffer(expected), onFailure(err -> {
          complete();
        }));
      });
    };
    startServers();
    client.post("/test/400/8ne8e94a/xhr", resp -> {
      assertEquals(200, resp.statusCode());
    }).end();
    await();
  }
}
