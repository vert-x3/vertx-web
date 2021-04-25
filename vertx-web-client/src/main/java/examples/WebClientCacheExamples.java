package examples;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.client.WebClientCache;
import io.vertx.ext.web.client.WebClientCacheOptions;
import io.vertx.ext.web.client.cache.CacheAdapter;
import io.vertx.ext.web.client.impl.cache.NoOpCacheAdapter;

public class WebClientCacheExamples {

  public void simpleGetWithCaching(Vertx vertx) {
    CacheAdapter adapter = new NoOpCacheAdapter();
    WebClientCacheOptions options = new WebClientCacheOptions()
      .enableCaching();
    WebClientCache client = WebClientCache.create(vertx, adapter, options);

    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send()
      .onSuccess(response -> System.out
        .println("Received response with age" + response.headers().get(HttpHeaders.AGE)))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }
}
