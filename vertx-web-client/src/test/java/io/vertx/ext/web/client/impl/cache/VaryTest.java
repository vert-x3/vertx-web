package io.vertx.ext.web.client.impl.cache;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.impl.WebClientBase;
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

  private static final String HOST = "host.com";
  private static final String PATH = "/somepath";

  private static final WebClient WEB_CLIENT = new WebClientBase(null, new WebClientOptions());

  @Test
  public void testVaryPerUserAgent() {
    MultiMap requestHeaders = new HeadersMultiMap()
      .add(USER_AGENT, "Concrete Mobile User Agent");
    MultiMap responseHeaders = new HeadersMultiMap()
      .add(VARY, USER_AGENT)
      .add(USER_AGENT, "Mobile");
    Vary instance = new Vary(requestHeaders, responseHeaders);

    HttpRequest<Buffer> requestMatches = buildHttpRequest().putHeader(USER_AGENT, "Another Mobile User Agent");
    HttpRequest<Buffer> requestDoesNotMatch = buildHttpRequest(); // Desktop by default
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

    HttpRequest<Buffer> requestMatches = buildHttpRequest().putHeader(ACCEPT_ENCODING, "gzip");
    HttpRequest<Buffer> requestMatchesToo = buildHttpRequest().putHeader(ACCEPT_ENCODING, "deflate");
    HttpRequest<Buffer> requestMatchesTooo = buildHttpRequest();
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

    HttpRequest<Buffer> requestMatches = buildHttpRequest().putHeader("X-Vertx", "jordi");
    HttpRequest<Buffer> requestFails = buildHttpRequest().putHeader("X-Vertx", "llach");
    HttpRequest<Buffer> requestFailsToo = buildHttpRequest();
    assertTrue("Vary per custom header matches", instance.matchesRequest(requestMatches));
    assertFalse("Vary per custom header does not match", instance.matchesRequest(requestFails));
    assertFalse("Vary per custom header not present", instance.matchesRequest(requestFailsToo));
  }

  private HttpRequest<Buffer> buildHttpRequest() {
    return WEB_CLIENT.get(HOST, PATH);
  }

}
