package io.vertx.ext.web.client;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpTestBase;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.impl.ClientEventType;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.impl.WebClientInternal;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
    if (context.eventType() == ClientEventType.PREPARE_REQUEST) {
      context.request().host("localhost");
      context.request().port(8080);
    }
    context.next();
  }

  @Test
  public void testMutateRequestInterceptor() throws Exception {
    server.requestHandler(req -> req.response().end());
    startServer();
    client.addInterceptor(this::handleMutateRequest);
    HttpRequest<Buffer> builder = client.get("/somepath").host("another-host").port(8081);
    builder.send(onSuccess(resp -> complete()));
    await();
  }

  private void mutateResponseHandler(HttpContext context) {
    if (context.eventType() == ClientEventType.DISPATCH_RESPONSE) {
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
      events.add(context.eventType().name() + "_1");
      context.next();
    });
    client.addInterceptor(context -> {
      events.add(context.eventType().name() + "_2");
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

  private Handler<HttpContext<?>> retryInterceptorHandler(AtomicInteger reqCount, AtomicInteger respCount, int num) {
    return ctx -> {
      if (ctx.eventType() == ClientEventType.PREPARE_REQUEST) {
        reqCount.incrementAndGet();
      } else if (ctx.eventType() == ClientEventType.DISPATCH_RESPONSE) {
        respCount.incrementAndGet();
        HttpResponse<?> resp = ctx.response();
        if (resp.statusCode() == 503) {
          Integer count = ctx.get("retries");
          if (count == null) {
            count = 0;
          }
          if (count < num) {
            ctx.set("retries", count + 1);
            ctx.prepareRequest();
            return;
          }
        }
      }
      ctx.next();
    };
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
    if (context.eventType() == ClientEventType.PREPARE_REQUEST) {
      context.response(new HttpResponseImpl<>());
      context.dispatchResponse();
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
}
