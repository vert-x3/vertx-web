package io.vertx.ext.web.client;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.codec.SseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class SseClientTest {

  private Vertx vertx;
  private WebClient client;
  private HttpServer server;

  @Before
  public void setup(TestContext tc) {
    vertx = Vertx.vertx();
    client = WebClient.create(vertx, new WebClientOptions().setDefaultPort(8080).setDefaultHost("localhost"));

    server = vertx.createHttpServer().requestHandler(req -> {
      String path = req.path();
      req.response().setChunked(true);

      // set headers
      req.response().headers().add("Content-Type", "text/event-stream;charset=UTF-8");
      req.response().headers().add("Connection", "keep-alive");
      req.response().headers().add("Cache-Control", "no-cache");
      req.response().headers().add("Access-Control-Allow-Origin", "*");

      if (null != path) switch (path) {
        case "/basic":
          int count = Integer.parseInt(req.getParam("count"));
          vertx.setPeriodic(50, new Handler<Long>() {
            private int index = 0;

            @Override
            public void handle(Long timerId) {
              if (index < count) {
                String event = String.format("event: event%d\ndata: data%d\nid: %d\n\n", index, index, index);
                index++;
                req.response().write(event);
              } else {
                vertx.cancelTimer(timerId);
                req.response().end();
              }
            }
          });
          break;
        case "/multiline-data":
          req.response().write("data: line1\ndata: line2\ndata: line3\n\n");
          req.response().end();
          break;
        case "/comments":
          req.response().write(": this is a comment\ndata: test data\n\n");
          req.response().end();
          break;
        case "/retry":
          req.response().write("retry: 5000\ndata: test\n\n");
          req.response().end();
          break;
        case "/no-event-type":
          req.response().write("data: message without event type\n\n");
          req.response().end();
          break;
        case "/burst":
          // Send many events quickly to test backpressure
          count = Integer.parseInt(req.getParam("count"));
          for (int i = 0; i < count; i++) {
            String event = String.format("data: burst%d\n\n", i);
            req.response().write(event);
          }   
          req.response().end();
          break;
        case "/slow":{
          // Send events slowly to test pause/resume
          count = Integer.parseInt(req.getParam("count"));
          vertx.setPeriodic(200, new Handler<Long>() {
            private int index = 0;
            
            @Override
            public void handle(Long timerId) {
              if (index < count) {
                String event = String.format("data: slow%d\n\n", index);
                index++;
                req.response().write(event);
              } else {
                vertx.cancelTimer(timerId);
                req.response().end();
              }
            }
          }); break;
          }
        case "/invalid-retry":
          req.response().write("retry: not-a-number\ndata: test\n\n");
          req.response().end();
          break;
        default:
          break;
      }
    });

    server.listen(8080).onComplete(tc.asyncAssertSuccess());
  }

  @Test(timeout = 10000)
  public void testGetSseEvents(TestContext tc) throws Exception {
    Async async = tc.async();
    final List<SseEvent> events = new ArrayList<>();

    client.get("/basic?count=5").as(BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
      stream.endHandler(v -> {
        tc.assertEquals(5, events.size());
        for (int i = 0; i < 5; i++) {
          tc.assertEquals("event" + i, events.get(i).event());
          tc.assertEquals("data" + i, events.get(i).data());
          tc.assertEquals(String.valueOf(i), events.get(i).id());
        }
        async.complete();
      });
    })).send().onFailure(tc::fail);
  }

  @Test(timeout = 10000)
  public void testMultilineData(TestContext tc) throws Exception {
    Async async = tc.async();
    final List<SseEvent> events = new ArrayList<>();

    client.get("/multiline-data").as(BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
      stream.endHandler(v -> {
        tc.assertEquals(1, events.size());
        // Per SSE spec, multi-line data should be joined by newlines
        tc.assertEquals("line1\nline2\nline3", events.get(0).data());
        async.complete();
      });
    })).send().onFailure(tc::fail);
  }

  @Test(timeout = 10000)
  public void testComments(TestContext tc) throws Exception {
    Async async = tc.async();
    final List<SseEvent> events = new ArrayList<>();

    client.get("/comments").as(BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
      stream.endHandler(v -> {
        tc.assertEquals(1, events.size());
        tc.assertEquals("test data", events.get(0).data());
        async.complete();
      });
    })).send().onFailure(tc::fail);
  }

  @Test(timeout = 10000)
  public void testRetryField(TestContext tc) throws Exception {
    Async async = tc.async();
    final List<SseEvent> events = new ArrayList<>();

    client.get("/retry").as(BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
      stream.endHandler(v -> {
        tc.assertEquals(1, events.size());
        tc.assertEquals("test", events.get(0).data());
        tc.assertEquals(5000, events.get(0).retry());
        async.complete();
      });
    })).send().onFailure(tc::fail);
  }

  @Test(timeout = 10000)
  public void testNoEventType(TestContext tc) throws Exception {
    Async async = tc.async();
    final List<SseEvent> events = new ArrayList<>();

    client.get("/no-event-type").as(BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
      stream.endHandler(v -> {
        tc.assertEquals(1, events.size());
        tc.assertEquals("message without event type", events.get(0).data());
        // Per SSE spec, the default event type is "message".
        // This implementation uses null. This test verifies the implementation's behavior.
        tc.assertEquals("message", events.get(0).event());
        async.complete();
      });
    })).send().onFailure(tc::fail);
  }

  @Test(timeout = 10000)
  public void testBurstEvents(TestContext tc) throws Exception {
    Async async = tc.async();
    final List<SseEvent> events = new ArrayList<>();

    client.get("/burst?count=100").as(BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
      stream.endHandler(v -> {
        tc.assertEquals(100, events.size());
        for (int i = 0; i < 100; i++) {
          tc.assertEquals("burst" + i, events.get(i).data());
        }
        async.complete();
      });
    })).send().onFailure(tc::fail);
  }

  @Test(timeout = 10000)
  public void testPauseResume(TestContext tc) throws Exception {
    Async async = tc.async();
    final List<SseEvent> events = new ArrayList<>();
    final AtomicInteger pauseCount = new AtomicInteger(0);

    client.get("/basic?count=10").as(BodyCodec.sseStream(stream -> {
      stream.handler(event -> {
        events.add(event);
        // Pause after every 3 events
        if (events.size() % 3 == 0 && pauseCount.get() < 2) {
          stream.pause();
          pauseCount.incrementAndGet();
          // Resume after a short delay
          vertx.setTimer(100, id -> stream.resume());
        }
      });
      stream.endHandler(v -> {
        tc.assertEquals(10, events.size());
        tc.assertTrue(pauseCount.get() >= 2, "Stream should have been paused at least twice");
        async.complete();
      });
    })).send().onFailure(tc::fail);
  }

  @Test(timeout = 10000)
  public void testFetch(TestContext tc) throws Exception {
    Async async = tc.async();
    final List<SseEvent> events = new ArrayList<>();
    final AtomicInteger fetchCount = new AtomicInteger(0);

    client.get("/basic?count=10").as(BodyCodec.sseStream(stream -> {
      stream.pause(); // Start paused
      stream.handler(event -> {
        events.add(event);
        fetchCount.incrementAndGet();
        // Only fetch 3 events total
        if (fetchCount.get() < 3) {
          stream.fetch(1);
        } else {
          // After receiving 3 events, complete the test
          vertx.setTimer(500, id -> {
            tc.assertEquals(3, events.size());
            async.complete();
          });
        }
      });
      stream.endHandler(v -> {
        // End handler may not be called if we don't fetch all events
      });
      // Kick off by fetching the first event
      stream.fetch(1);
    })).send().onFailure(tc::fail);
  }

  @Test(timeout = 15000)
  public void testBackpressure(TestContext tc) throws Exception {
    Async async = tc.async();
    final List<SseEvent> events = new ArrayList<>();
    final List<Long> timestamps = new ArrayList<>();

    client.get("/slow?count=5").as(BodyCodec.sseStream(stream -> {
      stream.handler(event -> {
        timestamps.add(System.currentTimeMillis());
        events.add(event);
        // Simulate slow processing by pausing briefly
        if (events.size() < 5) {
          stream.pause();
          vertx.setTimer(50, id -> stream.resume());
        }
      });
      stream.endHandler(v -> {
        tc.assertEquals(5, events.size());
        // Verify events were received over time (not all at once)
        long totalTime = timestamps.get(timestamps.size() - 1) - timestamps.get(0);
        tc.assertTrue(totalTime >= 750, "Events should be spread over time due to backpressure. Total time was " + totalTime);
        async.complete();
      });
    })).send().onFailure(tc::fail);
  }

  @Test(timeout = 10000)
  public void testExceptionHandler(TestContext tc) throws Exception {
    Async async = tc.async();
    final List<Throwable> exceptions = new ArrayList<>();

    client.get("/invalid-retry").as(BodyCodec.sseStream(stream -> {
      stream.handler(event -> {
        // This might or might not be called depending on when the parser fails
      });
      stream.exceptionHandler(exceptions::add);
      stream.endHandler(v -> {
        tc.assertEquals(1, exceptions.size(), "Expected one exception");
        tc.assertTrue(exceptions.get(0) instanceof RuntimeException, "Expected a RuntimeException");
        tc.assertTrue(exceptions.get(0).getMessage().contains("Invalid \"retry\" value"));
        tc.assertNotNull(exceptions.get(0).getCause(), "Expected a cause for the exception");
        tc.assertTrue(exceptions.get(0).getCause() instanceof NumberFormatException, "Expected cause to be a NumberFormatException");
        async.complete();
      });
    })).send().onFailure(tc::fail);
  }

  @After
  public void close(TestContext tc) {
    if (server != null) {
      server.close();
    }
    if (client != null) {
      client.close();
    }
    if (vertx != null) {
      vertx.close().onComplete(tc.asyncAssertSuccess());
    }
  }

}
