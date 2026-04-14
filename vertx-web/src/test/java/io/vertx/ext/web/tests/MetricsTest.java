package io.vertx.ext.web.tests;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerConfig;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.VertxMetricsFactory;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.core.spi.observability.HttpRequest;
import io.vertx.core.spi.observability.HttpResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.VertxTestContext;
import io.vertx.junit5.VertxProvider;
import io.vertx.junit5.ProvidedBy;
import io.vertx.test.fakemetrics.FakeMetricsBase;
import io.vertx.test.fakemetrics.HttpServerMetric;
import io.vertx.test.fakemetrics.WebSocketMetric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetricsTest extends WebTestBase2 {

  static final FakeHttpServerMetrics fakeHttpServerMetrics = new FakeHttpServerMetrics();

  public static class MetricsVertxProvider implements VertxProvider {
    @Override
    public Vertx get() {
      return Vertx.builder()
        .with(new VertxOptions().setMetricsOptions(new MetricsOptions().setEnabled(true)))
        .withMetrics(options -> new VertxMetrics() {
          @Override
          public HttpServerMetrics<?, ?> createHttpServerMetrics(HttpServerConfig config, SocketAddress tcpLocalAddress, SocketAddress udpLocalAddress) {
            return fakeHttpServerMetrics;
          }
        })
        .build();
    }
  }

  @Override
  @BeforeEach
  public void setUp(@ProvidedBy(MetricsVertxProvider.class) Vertx vertx, VertxTestContext testContext) throws Exception {
    super.setUp(vertx, testContext);
  }

  @Test
  public void testSimpleRoute() {
    fakeHttpServerMetrics.reset(new String[] { null });
    // ensure that the metrics are called in the right order
    router.route().handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, "/", 200, "OK");
  }

  @Test
  public void testSimpleRouterMultipleRoutes() {
    fakeHttpServerMetrics.reset("A", "B", "C", "D", "E");
    // ensure that the metrics are called in the right order
    router.route().setName("A").handler(RoutingContext::next);
    router.route().setName("B").handler(RoutingContext::next);
    router.route().setName("C").handler(RoutingContext::next);
    router.route().setName("D").handler(RoutingContext::next);
    router.route().setName("E").handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, "/", 200, "OK");
  }

  @Test
  public void testSimpleRouterMultipleRoutesSomeSkiped() {
    fakeHttpServerMetrics.reset("A", "B", "D", "E");
    // ensure that the metrics are called in the right order
    router.route().setName("A").handler(RoutingContext::next);
    router.route().setName("B").handler(RoutingContext::next);
    router.route("/skip-me").setName("C").handler(RoutingContext::next);
    router.route().setName("D").handler(RoutingContext::next);
    router.route().setName("E").handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, "/", 200, "OK");
  }
}

class FakeHttpServerMetrics extends FakeMetricsBase implements HttpServerMetrics<HttpServerMetric, WebSocketMetric> {

  public FakeHttpServerMetrics() {
  }

  private final AtomicInteger seq = new AtomicInteger(0);
  private final AtomicInteger routed = new AtomicInteger(0);
  private String[] args;

  public void reset(String... args) {
    seq.set(0);
    routed.set(0);
    this.args = args == null ? new String[0] : args;
  }

  @Override
  public HttpServerMetric requestBegin(SocketAddress remoteAddress, HttpRequest request) {
    assertEquals(1, seq.incrementAndGet());
    return new HttpServerMetric(request, remoteAddress);
  }

  public void requestEnd(HttpServerMetric requestMetric, HttpRequest request, long bytesRead) {
    assertEquals(4, seq.incrementAndGet());
    requestMetric.requestEnded.set(true);
    requestMetric.bytesRead.set(bytesRead);
  }

  @Override
  public HttpServerMetric responsePushed(SocketAddress remoteAddress, HttpMethod method, String uri, HttpResponse response) {
    HttpServerMetric requestMetric = new HttpServerMetric(uri, remoteAddress);
    requestMetric.response.set(response);
    return requestMetric;
  }

  public void requestReset(HttpServerMetric requestMetric) {
    requestMetric.failed.set(true);
  }

  public void responseBegin(HttpServerMetric requestMetric, HttpResponse response) {
    assertEquals(2, seq.incrementAndGet());
    requestMetric.response.set(response);
  }

  public void responseEnd(HttpServerMetric requestMetric, HttpResponse response, long bytesWritten) {
    assertEquals(3, seq.incrementAndGet());
    requestMetric.responseEnded.set(true);
    requestMetric.bytesWritten.set(bytesWritten);
  }

  @Override
  public WebSocketMetric connected(HttpRequest request) {
    assertEquals(1, seq.incrementAndGet());
    return new WebSocketMetric(request);
  }

  @Override
  public void disconnected(WebSocketMetric webSocketMetric) {
    assertEquals(5, seq.incrementAndGet());
  }

  public void requestRouted(HttpServerMetric requestMetric, String route) {
    assertEquals(args[routed.getAndIncrement()], route);
    requestMetric.route.set(route);
  }
}
