import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.ProxyOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * 
 */

/**
 * @author <a href="https://oss.lehmann.cx/">Alexander Lehmann</a>
 *
 */
@RunWith(VertxUnitRunner.class)
public class WebClientTest {
  @Test
  public void testWebClient(TestContext context) {
    Async async = context.async();
    Vertx vertx = Vertx.vertx();
    WebClientOptions options = new WebClientOptions();
    options.setProxyOptions(new ProxyOptions());
    WebClient client = WebClient.create(vertx, options);
    client
    .getAbs("ftp://ftp.gnu.org:21/gnu/")
    .send(ar -> {
      if (ar.succeeded()) {
        // Obtain response
        HttpResponse<Buffer> response = ar.result();
        System.out.println("Received response with status code" + response.statusCode());
        async.complete();
      } else {
        System.out.println("Something went wrong " + ar.cause().getMessage());
        context.fail(ar.cause());
      }
    });
  }
}
