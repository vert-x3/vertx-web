package io.vertx.ext.web.client.impl.cache;

import io.vertx.core.MultiMap;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    MultiMap requestHeaders = new HeadersMultiMap()
      .add(USER_AGENT, "Concrete Mobile User Agent");
    MultiMap responseHeaders = new HeadersMultiMap()
      .add(VARY, USER_AGENT)
      .add(USER_AGENT, "Mobile");
    Vary instance = new Vary(requestHeaders, responseHeaders);

    RequestOptions requestMatches = new RequestOptions().addHeader(USER_AGENT, "Another Mobile User Agent");
    RequestOptions requestDoesNotMatch = buildEmptyRequestOptions(); // Desktop by default
    assertTrue("User Agent Vary should match", instance.matchesRequest(requestMatches));
    assertFalse("User Agent Vary should not match", instance.matchesRequest(requestDoesNotMatch));
  }

  @Test
  public void testVaryPerAcceptEncodingIsIgnored() {
    MultiMap requestHeaders = new HeadersMultiMap()
      .add(ACCEPT_ENCODING, "gzip");
    MultiMap responseHeaders = new HeadersMultiMap()
      .add(VARY, ACCEPT_ENCODING)
      .add(CONTENT_ENCODING, "gzip");
    Vary instance = new Vary(requestHeaders, responseHeaders);

    RequestOptions requestMatches = new RequestOptions().addHeader(ACCEPT_ENCODING, "gzip");
    RequestOptions requestMatchesToo = new RequestOptions().addHeader(ACCEPT_ENCODING, "deflate");
    RequestOptions requestMatchesTooo = buildEmptyRequestOptions();
    assertTrue("Encoding matches", instance.matchesRequest(requestMatches));
    assertTrue("Encoding deflate does not match but it is ok", instance.matchesRequest(requestMatchesToo));
    assertTrue("No encoding specified is also ok", instance.matchesRequest(requestMatchesTooo));
  }

  @Test
  public void testVaryForOtherCasesRequestMustMatch() {
    MultiMap requestHeaders = new HeadersMultiMap()
      .add("X-Vertx", "jordi");
    MultiMap responseHeaders = new HeadersMultiMap()
      .add("Vary", "X-Vertx")
      .add("X-Vertx", "jordi");
    Vary instance = new Vary(requestHeaders, responseHeaders);

    RequestOptions requestMatches = new RequestOptions().addHeader("X-Vertx", "jordi");
    RequestOptions requestFails = new RequestOptions().addHeader("X-Vertx", "llach");
    RequestOptions requestFailsToo = buildEmptyRequestOptions();
    assertTrue("Vary per custom header matches", instance.matchesRequest(requestMatches));
    assertFalse("Vary per custom header does not match", instance.matchesRequest(requestFails));
    assertFalse("Vary per custom header not present", instance.matchesRequest(requestFailsToo));
  }

  private RequestOptions buildEmptyRequestOptions() {
    return new RequestOptions().setHeaders(new HeadersMultiMap());
  }

}
