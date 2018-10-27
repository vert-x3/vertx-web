package brave.vertx.web.client;

import brave.Span;
import brave.Tracer;
import brave.Tracer.SpanInScope;
import brave.http.HttpClientAdapter;
import brave.http.HttpClientHandler;
import brave.http.HttpTracing;
import brave.propagation.Propagation.Setter;
import brave.propagation.TraceContext;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.client.impl.HttpContext;

/**
 * @param <T> only used for fluent api. We don't read it here.
 */
final class TracingHttpClientRequestHandler<T> implements Handler<HttpContext<T>> {
  static final Setter<HttpClientRequest, String> SETTER = new Setter<HttpClientRequest, String>() {
    @Override public void put(HttpClientRequest carrier, String key, String value) {
      carrier.putHeader(key, value);
    }

    @Override public String toString() {
      return "HttpClientRequest::putHeader";
    }
  };

  final Tracer tracer;
  final HttpClientHandler<HttpClientRequest, HttpClientResponse> handler;
  final TraceContext.Injector<HttpClientRequest> injector;
  final String serverName;

  TracingHttpClientRequestHandler(HttpTracing httpTracing) {
    tracer = httpTracing.tracing().tracer();
    handler = HttpClientHandler.create(httpTracing, new Adapter());
    injector = httpTracing.tracing().propagation().injector(SETTER);
    serverName = httpTracing.serverName();
  }

  @Override public void handle(HttpContext<T> context) {
    // TODO: for redirects
    Span span = null;
    switch (context.eventType()) {
      case SEND_REQUEST:
        span = handler.handleSend(injector, context.clientRequest());
        context.set(Span.class.getName(), span);
        break;
      case DISPATCH_RESPONSE:
      case FAILURE:
        span = context.get(Span.class.getName());
        assert span != null : "unexpected lifecycle";
        if (span == null) break; // don't break
        parseConnectionAddress(context.clientRequest(), span);
        handler.handleReceive(context.clientResponse(), context.failure(), span);
        context.set(Span.class.getName(), null);
        span = null;
        break;
    }

    // TODO: test that this is readable ex trace IDs in logs
    if (span != null) {
      try (SpanInScope ws = tracer.withSpanInScope(span)) {
        context.next();
      } catch (RuntimeException | Error e) {
        span.error(e); // TODO: test that other interceptors having an error end up in the span
        context.set(Span.class.getName(), null);
        throw e;
      }
    } else {
      context.next();
    }
  }

  static void parseConnectionAddress(HttpClientRequest request, Span span) {
    if (span.isNoop()) return;
    HttpConnection connection = request.connection();
    if (connection == null) return;
    SocketAddress address = connection.remoteAddress();
    if (address != null) {
      if (span.remoteIpAndPort(address.host(), address.port())) return;
    }
  }

  static final class Adapter extends HttpClientAdapter<HttpClientRequest, HttpClientResponse> {

    @Override public String method(HttpClientRequest request) {
      return request.method().name();
    }

    @Override public String path(HttpClientRequest request) {
      return request.path();
    }

    @Override public String url(HttpClientRequest request) {
      return request.absoluteURI();
    }

    @Override public String requestHeader(HttpClientRequest request, String name) {
      return request.headers().get(name);
    }

    @Override public Integer statusCode(HttpClientResponse response) {
      return statusCodeAsInt(response);
    }

    @Override public int statusCodeAsInt(HttpClientResponse response) {
      return response.statusCode();
    }
  }
}
