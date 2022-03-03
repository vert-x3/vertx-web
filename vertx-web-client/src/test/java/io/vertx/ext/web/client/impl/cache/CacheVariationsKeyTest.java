package io.vertx.ext.web.client.impl.cache;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.impl.WebClientBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CacheVariationsKeyTest {

  @Test
  public void testCacheVariationKeyNotAffectedByHttpReqImplMutability() {
    WebClient webClient = new WebClientBase(null, new WebClientOptions());
    HttpRequest<Buffer> request = webClient.request(HttpMethod.GET, 8080, "server.com", "/somepath?key=value");
    CacheVariationsKey instance1 = new CacheVariationsKey(request);
    CacheVariationsKey instance2 = new CacheVariationsKey(request);
    assertEquals(instance1, instance2);
  }

}
