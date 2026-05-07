package io.vertx.ext.web.client.tests;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.impl.ClientPhase;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.codec.BodyCodec;

import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class InterceptorTest extends WebClientTestBase {

  private void handleMutateRequest(HttpContext<?> context) {
    if (context.phase() == ClientPhase.PREPARE_REQUEST) {
      context.request().host("localhost");
      context.request().port(8080);
    }
    context.next();
  }

  @Test
  public void testMutateRequestInterceptor() throws Exception {
    server.requestHandler(req -> req.response().end());
    startServer();
    webClient.addInterceptor(this::handleMutateRequest);
    HttpRequest<Buffer> builder = webClient.get("/somepath").host("another-host").port(8081);
    builder.send().await();
  }

  private void handleMutateCodec(HttpContext context) {
    if (context.phase() == ClientPhase.RECEIVE_RESPONSE) {
      if (context.clientResponse().statusCode() == 200) {
        context.request().as(BodyCodec.none());
      }
    }
    context.next();
  }

  @Test
  public void testMutateCodecInterceptor() throws Exception {
    server.requestHandler(req -> req.response().end("foo!"));
    startServer();
    File f = Files.createTempFile("vertx", ".dat").toFile();
    assertTrue(f.delete());
    AsyncFile foo = vertx.fileSystem().openBlocking(f.getAbsolutePath(), new OpenOptions().setSync(true).setTruncateExisting(true));
    webClient.addInterceptor(this::handleMutateCodec);
    HttpRequest<Void> builder = webClient.get("/somepath").as(BodyCodec.pipe(foo));
    builder.send().await();
    foo.write(Buffer.buffer("bar!"));
    foo.close().await();
    assertEquals("bar!", vertx.fileSystem().readFileBlocking(f.getAbsolutePath()).toString());
    if (f.exists()) {
      f.delete();
    }
  }

  private void mutateResponseHandler(HttpContext context) {
    if (context.phase() == ClientPhase.DISPATCH_RESPONSE) {
      HttpResponse<?> resp = context.response();
      assertEquals(500, resp.statusCode());
      context.response(new HttpResponseImpl<>());
    }
    context.next();
  }

  @Test
  public void testMutateResponseInterceptor() throws Exception {
    server.requestHandler(req -> req.response().setStatusCode(500).end());
    startServer();
    webClient.addInterceptor(this::mutateResponseHandler);
    HttpRequest<Buffer> builder = webClient.get("/somepath");
    HttpResponse<Buffer> resp = builder.send().await();
    assertEquals(200, resp.statusCode());
  }

  @Test
  public void testInterceptorsOrder() throws Exception {
    server.requestHandler(req -> req.response().setStatusCode(204).end());
    startServer();
    List<String> events = Collections.synchronizedList(new ArrayList<>());
    webClient.addInterceptor(context -> {
      events.add(context.phase().name() + "_1");
      context.next();
    });
    webClient.addInterceptor(context -> {
      events.add(context.phase().name() + "_2");
      context.next();
    });
    HttpRequest<Buffer> builder = webClient.get("/somepath");
    builder.send().await();
    assertEquals(Arrays.asList(
      "PREPARE_REQUEST_1", "PREPARE_REQUEST_2",
      "CREATE_REQUEST_1", "CREATE_REQUEST_2",
      "SEND_REQUEST_1", "SEND_REQUEST_2",
      "RECEIVE_RESPONSE_1", "RECEIVE_RESPONSE_2",
      "DISPATCH_RESPONSE_1", "DISPATCH_RESPONSE_2"), events);
  }

  @Test
  public void testInterceptorsOrderFailOutsideInterceptor(Checkpoint failLatch) throws Exception {
    List<String> events = Collections.synchronizedList(new ArrayList<>());

    webClient.addInterceptor(context -> {
      events.add(context.phase().name() + "_1");
      context.next();
    });

    HttpContext[] httpCtx = {null};
    webClient.addInterceptor(context -> {
      events.add(context.phase().name() + "_2");
      if (context.phase() == ClientPhase.CREATE_REQUEST) {
        httpCtx[0] = context;
        failLatch.flag();
      } else {
        context.next();
      }
    });

    webClient.addInterceptor(context -> {
      events.add(context.phase().name() + "_3");
      context.next();
    });

    HttpRequest<Buffer> builder = webClient.get("/somepath");
    Future<HttpResponse<Buffer>> fut = builder.send();

    failLatch.await();
    httpCtx[0].fail(new Exception("Something happens"));

    Assertions.assertThatThrownBy(fut::await);
    assertEquals(Arrays.asList(
      "PREPARE_REQUEST_1", "PREPARE_REQUEST_2", "PREPARE_REQUEST_3",
      "CREATE_REQUEST_1", "CREATE_REQUEST_2",
      "FAILURE_1", "FAILURE_2", "FAILURE_3"), events);
  }

  @Test
  public void testPhasesThreadFromNonVertxThread() throws Exception {
    server.requestHandler(req -> req.response().end());
    startServer();
    testPhasesThread((t1, t2) -> Arrays.asList(t1, t1, t2, t2, t2)).await();
  }

  @Test
  public void testPhasesThreadFromVertxThread(VertxTestContext testContext, Checkpoint checkpoint) throws Exception {
    server.requestHandler(req -> req.response().end());
    startServer();
    vertx.getOrCreateContext().runOnContext(v -> {
      testPhasesThread((t1, t2) -> Arrays.asList(t2, t2, t2, t2, t2))
        .onSuccess(v2 -> checkpoint.flag())
        .onFailure(testContext::failNow);
    });
  }

  private Future<Void> testPhasesThread(BiFunction<Thread, Thread, List<Thread>> abc) {
    Thread testThread = Thread.currentThread();
    List<Thread> phaseThreads = Collections.synchronizedList(new ArrayList<>());
    webClient.addInterceptor(context -> {
      phaseThreads.add(Thread.currentThread());
      context.next();
    });
    HttpRequest<Buffer> builder = webClient.get("/somepath");
    return builder.send().map(resp -> {
      Thread contextThread = Thread.currentThread();
      assertEquals(abc.apply(testThread, contextThread), phaseThreads);
      return null;
    });
  }

  private <T> void handle(HttpContext<T> ctx, AtomicInteger reqCount, AtomicInteger respCount, int num) {
    if (ctx.phase() == ClientPhase.PREPARE_REQUEST) {
      reqCount.incrementAndGet();
    } else if (ctx.phase() == ClientPhase.DISPATCH_RESPONSE) {
      respCount.incrementAndGet();
      HttpResponse<?> resp = ctx.response();
      if (resp.statusCode() == 503) {
        Integer count = ctx.get("retries");
        if (count == null) {
          count = 0;
        }
        if (count < num) {
          ctx.set("retries", count + 1);
          ctx.prepareRequest(ctx.request(), ctx.contentType(), ctx.body());
          return;
        }
      }
    }
    ctx.next();
  }

  private Handler<HttpContext<?>> retryInterceptorHandler(AtomicInteger reqCount, AtomicInteger respCount, int num) {
    return ctx -> handle(ctx, reqCount, respCount, num);
  }

  @Test
  public void testRetry() throws Exception {
    int num = 3;
    server.requestHandler(req -> req.response().setStatusCode(503).end(req.path()));
    startServer();
    AtomicInteger reqCount = new AtomicInteger();
    AtomicInteger respCount = new AtomicInteger();
    webClient.addInterceptor(retryInterceptorHandler(reqCount, respCount, num));
    HttpRequest<Buffer> builder = webClient.get("/");
    HttpResponse<Buffer> resp = builder.send().await();
    assertEquals(num + 1, reqCount.get());
    assertEquals(num + 1, respCount.get());
    assertEquals(503, resp.statusCode());
  }

  private void cacheInterceptorHandler(HttpContext<?> context) {
    if (context.phase() == ClientPhase.PREPARE_REQUEST) {
      context.dispatchResponse(new HttpResponseImpl<>());
    } else {
      context.next();
    }
  }

  @Test
  public void testCacheInterceptor() throws Exception {
    server.requestHandler(req -> fail());
    startServer();
    webClient.addInterceptor(this::cacheInterceptorHandler);
    HttpRequest<Buffer> builder = webClient.get("/somepath").host("localhost").port(8080);
    HttpResponse<Buffer> resp = builder.send().await();
    assertEquals(200, resp.statusCode());
  }

  private static class HttpResponseImpl<R> implements HttpResponse<R> {
    @Override
    public HttpVersion version() {
      return HttpVersion.HTTP_1_1;
    }

    @Override
    public int statusCode() {
      return 200;
    }

    @Override
    public String statusMessage() {
      return null;
    }

    @Override
    public MultiMap headers() {
      return null;
    }

    @Override
    public String getHeader(String headerName) {
      return null;
    }

    @Override
    public String getHeader(CharSequence headerName) {
      return null;
    }

    @Override
    public MultiMap trailers() {
      return null;
    }

    @Override
    public String getTrailer(String trailerName) {
      return null;
    }

    @Override
    public List<String> cookies() {
      return null;
    }

    @Override
    public R body() {
      return null;
    }

    @Override
    public Buffer bodyAsBuffer() {
      return null;
    }

    @Override
    public List<String> followedRedirects() {
      return null;
    }

    @Override
    public JsonArray bodyAsJsonArray() {
      return null;
    }
  }

  @Test
  public void testFollowRedirects() throws Exception {
    server.requestHandler(req -> {
      switch (req.path()) {
        case "/1":
          req.response().setStatusCode(302).putHeader("location", "http://localhost:8080/2").end();
          break;
        default:
          req.response().end();
      }
    });
    startServer();
    List<ClientPhase> phases = new ArrayList<>();
    List<String> requestUris = new ArrayList<>();
    AtomicInteger redirects = new AtomicInteger();
    webClient.addInterceptor(ctx -> {
      phases.add(ctx.phase());
      switch (ctx.phase()) {
        case PREPARE_REQUEST:
          assertEquals(0, ctx.redirects());
          break;
        case SEND_REQUEST:
          assertEquals(redirects.getAndIncrement(), ctx.redirects());
          requestUris.add(ctx.requestOptions().getURI());
          break;
      }
      ctx.next();
    });
    HttpRequest<Buffer> builder = webClient.get("/1").host("localhost").port(8080);
    HttpResponse<Buffer> resp = builder.send().await();
    assertEquals(200, resp.statusCode());
    assertEquals(Arrays.asList(
      ClientPhase.PREPARE_REQUEST,
      ClientPhase.CREATE_REQUEST,
      ClientPhase.SEND_REQUEST,
      ClientPhase.FOLLOW_REDIRECT,
      ClientPhase.CREATE_REQUEST,
      ClientPhase.SEND_REQUEST,
      ClientPhase.RECEIVE_RESPONSE,
      ClientPhase.DISPATCH_RESPONSE), phases);
    assertEquals(Arrays.asList("/1", "/2"), requestUris);
  }

  @Test
  public void testMaxRedirects() throws Exception {
    CopyOnWriteArrayList<String> requests = new CopyOnWriteArrayList<>();
    server.requestHandler(req -> {
      requests.add(req.path());
      req.response().setStatusCode(302).putHeader("location", "http://localhost:8080" + req.path() + "0").end();
    });
    startServer();
    List<ClientPhase> phases = new ArrayList<>();
    webClient.addInterceptor(ctx -> {
      phases.add(ctx.phase());
      ctx.next();
    });
    HttpRequest<Buffer> builder = webClient.get("/").host("localhost").port(8080);
    HttpResponse<Buffer> resp = builder.send().await();
    assertEquals(302, resp.statusCode());
    assertEquals(Arrays.asList(
        ClientPhase.PREPARE_REQUEST,
        ClientPhase.CREATE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.FOLLOW_REDIRECT,
        ClientPhase.CREATE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.FOLLOW_REDIRECT,
        ClientPhase.CREATE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.FOLLOW_REDIRECT,
        ClientPhase.CREATE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.FOLLOW_REDIRECT,
        ClientPhase.CREATE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.FOLLOW_REDIRECT,
        ClientPhase.CREATE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.FOLLOW_REDIRECT,
        ClientPhase.CREATE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.FOLLOW_REDIRECT,
        ClientPhase.CREATE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.FOLLOW_REDIRECT,
        ClientPhase.CREATE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.FOLLOW_REDIRECT,
        ClientPhase.CREATE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.FOLLOW_REDIRECT,
        ClientPhase.CREATE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.FOLLOW_REDIRECT,
        ClientPhase.CREATE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.FOLLOW_REDIRECT,
        ClientPhase.CREATE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.FOLLOW_REDIRECT,
        ClientPhase.CREATE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.FOLLOW_REDIRECT,
        ClientPhase.CREATE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.FOLLOW_REDIRECT,
        ClientPhase.CREATE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.FOLLOW_REDIRECT,
        ClientPhase.CREATE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.RECEIVE_RESPONSE,
        ClientPhase.DISPATCH_RESPONSE
        ), phases);
    assertEquals(Arrays.asList(
      "/",
      "/0",
      "/00",
      "/000",
      "/0000",
      "/00000",
      "/000000",
      "/0000000",
      "/00000000",
      "/000000000",
      "/0000000000",
      "/00000000000",
      "/000000000000",
      "/0000000000000",
      "/00000000000000",
      "/000000000000000",
      "/0000000000000000"
    ), requests);
  }

  @Test
  public void testCallNextAsynchronously() throws Exception {
    server.requestHandler(req -> req.response().end());
    startServer();
    AtomicBoolean synchronous = new AtomicBoolean();
    AtomicBoolean first = new AtomicBoolean();
    webClient.addInterceptor(ctx -> {
      first.set(true);
      synchronous.set(true);
      vertx.setTimer(10, id -> {
        synchronous.set(false);
        ctx.next();
      });
    });
    List<Long> list = Collections.synchronizedList(new ArrayList<>());
    webClient.addInterceptor(ctx -> {
      assertTrue(first.getAndSet(false));
      assertFalse(synchronous.get());
      list.add(System.currentTimeMillis());
      ctx.next();
    });
    HttpRequest<Buffer> builder = webClient.get("/somepath").host("localhost").port(8080);
    builder.send().await();
    long prev = 0L;
    for (long val : list) {
      assertTrue(val >= prev);
      prev = val;
    }
  }

  @Test
  public void testSynchronousInterceptorFailure() throws Exception {
    RuntimeException failure = new RuntimeException();
    webClient.addInterceptor(ctx -> {
      throw failure;
    });
    webClient.addInterceptor(ctx -> {
      fail("Should never be executed");
    });
    HttpRequest<Buffer> builder = webClient.get("/somepath").host("localhost").port(8080);
    Assertions.assertThatThrownBy(() -> builder.send().await()).isSameAs(failure);
  }

  @Test
  public void testClientRequest() throws Exception {
    server.requestHandler(req -> req.response().end());
    startServer();
    webClient.addInterceptor(ctx -> {
      if (ctx.phase() == ClientPhase.SEND_REQUEST) {
        assertNotNull(ctx.clientRequest());
      } else {
        assertNull(ctx.clientRequest());
      }
      ctx.next();
    });
    HttpRequest<Buffer> builder = webClient.get("/somepath").host("localhost").port(8080);
    builder.send().await();
  }
}
