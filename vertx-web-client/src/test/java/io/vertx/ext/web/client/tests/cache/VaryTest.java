package io.vertx.ext.web.client.tests.cache;

import io.vertx.core.MultiMap;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.client.impl.cache.Vary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * As Netty is always decompressing the response and removing Content-Encoding header, we ignore this kind of variation.
 * @author <a href="mailto:jllachf@gmail.com">Jordi Llach</a>
 */
public class VaryTest {

  private static final String ACCEPT_ENCODING = "Accept-Encoding";
  private static final String CONTENT_ENCODING = "Content-Encoding";
  private static final String VARY = "Vary";
  private static final String USER_AGENT = "User-Agent";

  @Test
  public void testVaryPerUserAgent() {
    MultiMap requestHeaders = MultiMap.caseInsensitiveMultiMap()
      .add(USER_AGENT, "Concrete Mobile User Agent");
    MultiMap responseHeaders = MultiMap.caseInsensitiveMultiMap()
      .add(VARY, USER_AGENT)
      .add(USER_AGENT, "Mobile");
    Vary instance = new Vary(requestHeaders, responseHeaders);

    RequestOptions requestMatches = new RequestOptions().addHeader(USER_AGENT, "Another Mobile User Agent");
    RequestOptions requestDoesNotMatch = buildEmptyRequestOptions(); // Desktop by default
    assertTrue(instance.matchesRequest(requestMatches), "User Agent Vary should match");
    assertFalse(instance.matchesRequest(requestDoesNotMatch), "User Agent Vary should not match");
  }

  @Test
  public void testVaryPerAcceptEncodingIsIgnored() {
    MultiMap requestHeaders = MultiMap.caseInsensitiveMultiMap()
      .add(ACCEPT_ENCODING, "gzip");
    MultiMap responseHeaders = MultiMap.caseInsensitiveMultiMap()
      .add(VARY, ACCEPT_ENCODING)
      .add(CONTENT_ENCODING, "gzip");
    Vary instance = new Vary(requestHeaders, responseHeaders);

    RequestOptions requestMatches = new RequestOptions().addHeader(ACCEPT_ENCODING, "gzip");
    RequestOptions requestMatchesToo = new RequestOptions().addHeader(ACCEPT_ENCODING, "deflate");
    RequestOptions requestMatchesTooo = buildEmptyRequestOptions();
    assertTrue(instance.matchesRequest(requestMatches), "Encoding matches");
    assertTrue(instance.matchesRequest(requestMatchesToo), "Encoding deflate does not match but it is ok");
    assertTrue(instance.matchesRequest(requestMatchesTooo), "No encoding specified is also ok");
  }

  @Test
  public void testVaryForOtherCasesRequestMustMatch() {
    MultiMap requestHeaders = MultiMap.caseInsensitiveMultiMap()
      .add("X-Vertx", "jordi");
    MultiMap responseHeaders = MultiMap.caseInsensitiveMultiMap()
      .add("Vary", "X-Vertx")
      .add("X-Vertx", "jordi");
    Vary instance = new Vary(requestHeaders, responseHeaders);

    RequestOptions requestMatches = new RequestOptions().addHeader("X-Vertx", "jordi");
    RequestOptions requestFails = new RequestOptions().addHeader("X-Vertx", "llach");
    RequestOptions requestFailsToo = buildEmptyRequestOptions();
    assertTrue(instance.matchesRequest(requestMatches), "Vary per custom header matches");
    assertFalse(instance.matchesRequest(requestFails), "Vary per custom header does not match");
    assertFalse(instance.matchesRequest(requestFailsToo), "Vary per custom header not present");
  }

  private RequestOptions buildEmptyRequestOptions() {
    return new RequestOptions().setHeaders(MultiMap.caseInsensitiveMultiMap());
  }

}
