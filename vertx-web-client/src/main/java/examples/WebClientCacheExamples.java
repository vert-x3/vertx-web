package examples;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.client.CachingWebClient;
import io.vertx.ext.web.client.CachingWebClientOptions;
import io.vertx.ext.web.client.cache.CacheStore;
import io.vertx.ext.web.client.impl.cache.NoOpCacheStore;

public class WebClientCacheExamples {

  public void simpleGetWithCaching(Vertx vertx) {
    CacheStore adapter = new NoOpCacheStore();
    CachingWebClientOptions options = new CachingWebClientOptions()
      .enableCaching();
    CachingWebClient client = CachingWebClient.create(vertx, adapter, options);

    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send()
      .onSuccess(response -> System.out
        .println("Received response with age" + response.headers().get(HttpHeaders.AGE)))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }
}
