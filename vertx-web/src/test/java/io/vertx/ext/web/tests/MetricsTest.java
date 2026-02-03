package io.vertx.ext.web.tests;

import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.VertxMetricsFactory;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.core.spi.observability.HttpRequest;
import io.vertx.core.spi.observability.HttpResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.test.fakemetrics.ConnectionMetric;
import io.vertx.test.fakemetrics.FakeMetricsBase;
import io.vertx.test.fakemetrics.HttpServerMetric;
import io.vertx.test.fakemetrics.WebSocketMetric;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class MetricsTest extends WebTestBase {

  final FakeHttpServerMetrics fakeHttpServerMetrics = new FakeHttpServerMetrics();

  @Override
  protected VertxMetricsFactory getMetrics() {
    return options -> new VertxMetrics() {
      public HttpServerMetrics<?, ?, ?> createHttpServerMetrics(HttpServerOptions options, SocketAddress localAddress) {
        return fakeHttpServerMetrics;
      }
    };
  }

  @Override
  public VertxOptions getOptions() {
    return new VertxOptions().setMetricsOptions(new MetricsOptions().setEnabled(true));
  }

  @Test
  public void testSimpleRoute() throws Exception {
    fakeHttpServerMetrics.reset(new String[] { null });
    // ensure that the metrics are called in the right order
    router.route().handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, "/", 200, "OK");
  }

  @Test
  public void testSimpleRouterMultipleRoutes() throws Exception {
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
  public void testSimpleRouterMultipleRoutesSomeSkiped() throws Exception {
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

class FakeHttpServerMetrics extends FakeMetricsBase implements HttpServerMetrics<HttpServerMetric, WebSocketMetric, ConnectionMetric> {

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

  public HttpServerMetric requestBegin(ConnectionMetric connectionMetric, HttpRequest request) {
    assertEquals(2, seq.incrementAndGet());
    return new HttpServerMetric(request, connectionMetric);
  }

  public void requestEnd(HttpServerMetric requestMetric, HttpRequest request, long bytesRead) {
    assertEquals(5, seq.incrementAndGet());
    requestMetric.requestEnded.set(true);
    requestMetric.bytesRead.set(bytesRead);
  }

  public HttpServerMetric responsePushed(ConnectionMetric connectionMetric, HttpMethod method, String uri, HttpResponse response) {
    HttpServerMetric requestMetric = new HttpServerMetric(uri, connectionMetric);
    requestMetric.response.set(response);
    return requestMetric;
  }

  public void requestReset(HttpServerMetric requestMetric) {
    requestMetric.failed.set(true);
  }

  public void responseBegin(HttpServerMetric requestMetric, HttpResponse response) {
    assertEquals(3, seq.incrementAndGet());
    requestMetric.response.set(response);
  }

  public void responseEnd(HttpServerMetric requestMetric, HttpResponse response, long bytesWritten) {
    assertEquals(4, seq.incrementAndGet());
    requestMetric.responseEnded.set(true);
    requestMetric.bytesWritten.set(bytesWritten);
  }

  public ConnectionMetric connected(SocketAddress remoteAddress, String remoteName) {
    assertEquals(1, seq.incrementAndGet());
    return new ConnectionMetric(remoteAddress, remoteName);
  }

  public void disconnected(ConnectionMetric connectionMetric, SocketAddress remoteAddress) {
    assertEquals(6, seq.incrementAndGet());
    connectionMetric.connected.set(false);
  }

  public void bytesRead(ConnectionMetric connectionMetric, SocketAddress remoteAddress, long numberOfBytes) {
    connectionMetric.bytesRead.addAndGet(numberOfBytes);
    connectionMetric.bytesReadEvents.add(numberOfBytes);
  }

  public void bytesWritten(ConnectionMetric connectionMetric, SocketAddress remoteAddress, long numberOfBytes) {
    connectionMetric.bytesWritten.addAndGet(numberOfBytes);
    connectionMetric.bytesWrittenEvents.add(numberOfBytes);
  }

  public void exceptionOccurred(ConnectionMetric connectionMetric, SocketAddress remoteAddress, Throwable t) {
  }

  public void requestRouted(HttpServerMetric requestMetric, String route) {
    assertEquals(args[routed.getAndIncrement()], route);
    requestMetric.route.set(route);
  }
}
