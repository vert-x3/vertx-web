package examples;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.CachingWebClient;
import io.vertx.ext.web.client.CachingWebClientOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientSession;
import io.vertx.ext.web.client.spi.CacheStore;
import io.vertx.ext.web.client.impl.cache.NoOpCacheStore;
import java.util.HashSet;
import java.util.Set;

public class CachingWebClientExamples {

  public void create(Vertx vertx) {
    WebClient client = WebClient.create(vertx);
    WebClient cachingWebClient = CachingWebClient.create(client);
  }

  public void createWithOptions(Vertx vertx) {
    Set<HttpMethod> cachedMethods = new HashSet<>(2);
    cachedMethods.add(HttpMethod.GET);
    cachedMethods.add(HttpMethod.HEAD);

    Set<Integer> cachedStatusCodes = new HashSet<>(1);
    cachedStatusCodes.add(200);

    CachingWebClientOptions options = new CachingWebClientOptions()
      .setCachedMethods(cachedMethods)
      .setCachedStatusCodes(cachedStatusCodes)
      .setEnableVaryCaching(true);

    WebClient client = WebClient.create(vertx);
    WebClient cachingWebClient = CachingWebClient.create(client, options);
  }

  public void createWithCustomStore(Vertx vertx) {
    WebClient client = WebClient.create(vertx);
    CacheStore store = new NoOpCacheStore();
    WebClient cachingWebClient = CachingWebClient.create(client, store);
  }

  public void createWithSession(Vertx vertx) {
    WebClient client = WebClient.create(vertx);
    WebClient cachingWebClient = CachingWebClient.create(client);
    WebClient sessionClient = WebClientSession.create(cachingWebClient);
  }

  public void simpleGetWithCaching(Vertx vertx) {
    WebClient client = WebClient.create(vertx);
    WebClient cachingWebClient = CachingWebClient.create(client);

    cachingWebClient
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send()
      .onSuccess(response -> System.out
        .println("Received response with age" + response.headers().get(HttpHeaders.AGE)))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }
}
