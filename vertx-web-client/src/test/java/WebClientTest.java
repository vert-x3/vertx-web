import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.ProxyOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.test.core.HttpProxy;

/**
 * @author <a href="https://oss.lehmann.cx/">Alexander Lehmann</a>
 *
 */
@RunWith(VertxUnitRunner.class)
public class WebClientTest {
  private HttpProxy proxy;
  private Vertx vertx = Vertx.vertx();

  @Test
  public void testWebClient(TestContext context) {
    Async async = context.async();
    proxy.setForceUri("http://localhost/");
    WebClientOptions options = new WebClientOptions();
    options.setProxyOptions(new ProxyOptions().setPort(proxy.getPort()));
    WebClient client = WebClient.create(vertx, options);
    client
    .getAbs("ftp://ftp.gnu.org:21/gnu/")
    .send(ar -> {
      if (ar.succeeded()) {
        // Obtain response
        HttpResponse<Buffer> response = ar.result();
        System.out.println("Received response with status code" + response.statusCode());
        context.assertEquals("ftp://ftp.gnu.org/gnu/", proxy.getLastUri());
        async.complete();
      } else {
        System.out.println("Something went wrong " + ar.cause().getMessage());
        context.fail(ar.cause());
      }
    });
  }

  @Before
  public void startProxy(TestContext context) {
    Async async = context.async();
    proxy = new HttpProxy(null);
    proxy.start(vertx, v -> async.complete());
  }

  @After
  public void stopProxy(TestContext context) {
    if (proxy !=null) {
      proxy.stop();
    }
  }

}
