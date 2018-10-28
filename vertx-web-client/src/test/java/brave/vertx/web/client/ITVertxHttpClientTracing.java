package brave.vertx.web.client;

import brave.test.http.ITHttpAsyncClient;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.impl.WebClientInternal;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;

// WARNING: this is internal hook!
public class ITVertxHttpClientTracing extends ITHttpAsyncClient<WebClientInternal> {
  Vertx vertx = Vertx.vertx(new VertxOptions());

  @Override protected WebClientInternal newClient(int port) {
    return ((WebClientInternal) WebClient.wrap(vertx.createHttpClient(new HttpClientOptions()
      .setDefaultPort(port)
      .setDefaultHost("127.0.0.1"))))
      .addInterceptor(new TracingHttpClientRequestHandler(httpTracing));
  }

  // TODO: we are unaware of the redirect. Ideally, redirects re-use the same infrastructure, simply
  // looping through again. The redirect handler likely needs to be re-considered to reduce
  // complexity vs what currently seems like a wrapping game.
  @Test @Override public void redirect() throws Exception {
    super.redirect();
  }

  @Override protected void closeClient(WebClientInternal client) {
    client.close();
  }

  @Override protected void get(WebClientInternal client, String pathIncludingQuery) {
    CountDownLatch latch = new CountDownLatch(1);
    client.get(pathIncludingQuery).send(r -> latch.countDown());

    // TODO: not sure if there's a blocking api anywhere
    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    }
  }

  @Override protected void post(WebClientInternal client, String pathIncludingQuery, String body) {
    CountDownLatch latch = new CountDownLatch(1);
    client.post(pathIncludingQuery).sendBuffer(Buffer.buffer(body), r -> latch.countDown());

    // TODO: not sure if there's a blocking api anywhere
    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    }
  }

  @Override protected void getAsync(WebClientInternal client, String pathIncludingQuery) {
    client.get(pathIncludingQuery).send(r -> {
    });
  }
}
