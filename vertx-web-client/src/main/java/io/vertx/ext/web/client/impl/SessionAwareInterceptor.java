package io.vertx.ext.web.client.impl;

import java.util.List;

import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.RequestOptions;

/**
 * A stateless interceptor for session management that operates on the {@code HttpContext}
 */
public class SessionAwareInterceptor implements Handler<HttpContext<?>> {

  private final WebClientSessionAware parentClient;
  private final CookieJar cookieJar;

  public SessionAwareInterceptor(WebClientSessionAware clientSessionAware) {
    this.parentClient = clientSessionAware;
    this.cookieJar = new CookieJar(clientSessionAware.cookieStore());
  }

  @Override
  public void handle(HttpContext<?> context) {
    List<String> cookies;
    String requestUri;
    switch(context.phase()) {
      case SEND_REQUEST:
        sendRequest(context);
        break;
      case FOLLOW_REDIRECT:
        cookies = context.clientResponse().cookies();
        requestUri = context.clientResponse().request().getURI();
        if (cookies != null) {
          String host = context.clientResponse().request().getHost();
          processResponseCookies(context, host, requestUri, cookies);
        };
        break;
      case DISPATCH_RESPONSE:
        cookies = context.response().cookies();
        requestUri = context.request().uri();
        if (cookies != null) {
          RequestOptions request = context.requestOptions();
          String host = context.request().virtualHost() != null ? context.request().virtualHost() : request.getHost();
          processResponseCookies(context, host, requestUri, cookies);
        }
        break;
      default:
        break;
    }

    context.next();
  }

  private void sendRequest(HttpContext<?> context) {
    HttpClientRequest request = context.clientRequest();
    MultiMap headers = request.headers();

    // Handle client headers
    headers.addAll(parentClient.headers());

    // Set cookies
    String domain = request.getHost();
    boolean ssl = request.connection().isSsl();
    Iterable<Cookie> cookies = cookieJar.cookies(ssl, domain, request.getURI());
    String encodedCookies = ClientCookieEncoder.STRICT.encode(cookies);
    if (encodedCookies != null) {
      headers.add(HttpHeaders.COOKIE, encodedCookies);
    }
  }

  private void processResponseCookies(HttpContext<?> context, String host, String requestUri, List<String> cookieHeaders) {
    long creationTime = System.currentTimeMillis();
    for (String cookieHeader : cookieHeaders) {
      Cookie cookie = ClientCookieDecoder.STRICT.decode(cookieHeader);
      if (cookie != null) {
        cookieJar.setCookie(creationTime, host, requestUri, cookie);
      }
    }
  }
}
