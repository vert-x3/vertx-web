package examples;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.client.CachingWebClient;
import io.vertx.ext.web.client.CachingWebClientOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.spi.CacheStore;
import io.vertx.ext.web.client.impl.cache.NoOpCacheStore;

public class CachingWebClientExamples {

  public void simpleGetWithCaching(Vertx vertx) {
    CacheStore cacheStore = new NoOpCacheStore();
    CachingWebClientOptions options = new CachingWebClientOptions()
      .setEnablePublicCaching(true);
    WebClient client = CachingWebClient.create(vertx, cacheStore, options);

    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send()
      .onSuccess(response -> System.out
        .println("Received response with age" + response.headers().get(HttpHeaders.AGE)))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void simpleGetWithSharedDataCaching(Vertx vertx) {
    WebClient client = CachingWebClient.create(vertx);

    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send()
      .onSuccess(response -> System.out
        .println("Received response with age" + response.headers().get(HttpHeaders.AGE)))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }
}
