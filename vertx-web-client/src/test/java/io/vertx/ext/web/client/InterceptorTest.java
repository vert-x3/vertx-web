package io.vertx.ext.web.client;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpTestBase;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.web.client.impl.ClientPhase;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.impl.WebClientInternal;
import io.vertx.ext.web.codec.BodyCodec;

import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class InterceptorTest extends HttpTestBase {

  private WebClientInternal client;

  @Override
  protected VertxOptions getOptions() {
    return super.getOptions().setAddressResolverOptions(new AddressResolverOptions().
      setHostsValue(Buffer.buffer(
        "127.0.0.1 somehost\n" +
        "127.0.0.1 localhost")));
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    super.client = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(8080).setDefaultHost("localhost"));
    client = (WebClientInternal) WebClient.wrap(super.client);
    server.close();
    server = vertx.createHttpServer(new HttpServerOptions().setPort(DEFAULT_HTTP_PORT).setHost(DEFAULT_HTTP_HOST));
  }

  private void handleMutateRequest(HttpContext context) {
    if (context.phase() == ClientPhase.PREPARE_REQUEST) {
      context.request().host("localhost");
      context.request().port(8080);
    }
    context.next();
  }

  @Rule
  public RepeatRule rule = new RepeatRule();

  @Test
  public void testMutateRequestInterceptor() throws Exception {
    server.requestHandler(req -> req.response().end());
    startServer();
    client.addInterceptor(this::handleMutateRequest);
    HttpRequest<Buffer> builder = client.get("/somepath").host("another-host").port(8081);
    builder.send(onSuccess(resp -> complete()));
    await();
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
    client.addInterceptor(this::handleMutateCodec);
    HttpRequest<Void> builder = client.get("/somepath").as(BodyCodec.pipe(foo));
    builder.send(onSuccess(resp -> {
      foo.write(Buffer.buffer("bar!"));
      foo.close(onSuccess(v -> {
        assertEquals("bar!", vertx.fileSystem().readFileBlocking(f.getAbsolutePath()).toString());
        testComplete();
      }));
    }));
    await();
    if (f.exists()) {
      f.delete();
    }
  }

  private void mutateResponseHandler(HttpContext context) {
    if (context.phase() == ClientPhase.DISPATCH_RESPONSE) {
      HttpResponse<?> resp = context.response();
      assertEquals(500, resp.statusCode());
      context.response(new HttpResponseImpl<Object>() {
        @Override
        public int statusCode() {
          return 200;
        }
      });
    }
    context.next();
  }

  @Test
  public void testMutateResponseInterceptor() throws Exception {
    server.requestHandler(req -> req.response().setStatusCode(500).end());
    startServer();
    client.addInterceptor(this::mutateResponseHandler);
    HttpRequest<Buffer> builder = client.get("/somepath");
    builder.send(onSuccess(resp -> {
      assertEquals(200, resp.statusCode());
      complete();
    }));
    await();
  }

  @Test
  public void testInterceptorsOrder() throws Exception {
    server.requestHandler(req -> req.response().setStatusCode(204).end());
    startServer();
    List<String> events = Collections.synchronizedList(new ArrayList<>());
    client.addInterceptor(context -> {
      events.add(context.phase().name() + "_1");
      context.next();
    });
    client.addInterceptor(context -> {
      events.add(context.phase().name() + "_2");
      context.next();
    });
    HttpRequest<Buffer> builder = client.get("/somepath");
    builder.send(onSuccess(resp -> {
      assertEquals(Arrays.asList(
        "PREPARE_REQUEST_1", "PREPARE_REQUEST_2",
        "SEND_REQUEST_1", "SEND_REQUEST_2",
        "RECEIVE_RESPONSE_1", "RECEIVE_RESPONSE_2",
        "DISPATCH_RESPONSE_1", "DISPATCH_RESPONSE_2"), events);
      complete();
    }));
    await();
  }

  @Test
  public void testPhasesThreadFromNonVertxThread() throws Exception {
    server.requestHandler(req -> req.response().end());
    startServer();
    testPhasesThread((t1, t2) -> Arrays.asList(t1, t1, t2, t2));
    await();
  }

  @Test
  public void testPhasesThreadFromVertxThread() throws Exception {
    server.requestHandler(req -> req.response().end());
    startServer();
    vertx.getOrCreateContext().runOnContext(v -> {
      testPhasesThread((t1, t2) -> Arrays.asList(t2, t2, t2, t2));
    });
    await();
  }

  private void testPhasesThread(BiFunction<Thread, Thread, List<Thread>> abc) {
    Thread testThread = Thread.currentThread();
    List<Thread> phaseThreads = Collections.synchronizedList(new ArrayList<>());
    client.addInterceptor(context -> {
      phaseThreads.add(Thread.currentThread());
      context.next();
    });
    HttpRequest<Buffer> builder = client.get("/somepath");
    builder.send(onSuccess(resp -> {
      Thread contextThread = Thread.currentThread();
      assertEquals(abc.apply(testThread, contextThread), phaseThreads);
      complete();
    }));
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
    client.addInterceptor(retryInterceptorHandler(reqCount, respCount, num));
    HttpRequest<Buffer> builder = client.get("/");
    builder.send(onSuccess(resp -> {
      assertEquals(num + 1, reqCount.get());
      assertEquals(num + 1, respCount.get());
      assertEquals(503, resp.statusCode());
      complete();
    }));
    await();
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
    client.addInterceptor(this::cacheInterceptorHandler);
    HttpRequest<Buffer> builder = client.get("/somepath").host("localhost").port(8080);
    builder.send(onSuccess(resp -> {
      assertEquals(200, resp.statusCode());
      complete();
    }));
    await();
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
    client.addInterceptor(ctx -> {
      phases.add(ctx.phase());
      switch (ctx.phase()) {
        case PREPARE_REQUEST:
          assertEquals(0, ctx.redirects());
          break;
        case SEND_REQUEST:
          assertEquals(redirects.getAndIncrement(), ctx.redirects());
          requestUris.add(ctx.clientRequest().path());
          break;
      }
      ctx.next();
    });
    HttpRequest<Buffer> builder = client.get("/1").host("localhost").port(8080);
    builder.send(onSuccess(resp -> {
      assertEquals(200, resp.statusCode());
      assertEquals(Arrays.asList(
        ClientPhase.PREPARE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.RECEIVE_RESPONSE,
        ClientPhase.DISPATCH_RESPONSE), phases);
      assertEquals(Arrays.asList("/1", "/2"), requestUris);
      complete();
    }));
    await();
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
    client.addInterceptor(ctx -> {
      phases.add(ctx.phase());
      ctx.next();
    });
    HttpRequest<Buffer> builder = client.get("/").host("localhost").port(8080);
    builder.send(onSuccess(resp -> {
      assertEquals(302, resp.statusCode());
      assertEquals(Arrays.asList(
        ClientPhase.PREPARE_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.SEND_REQUEST,
        ClientPhase.SEND_REQUEST,
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
      complete();
    }));
    await();
  }


}
