package io.vertx.ext.web.client.impl;

import java.net.URI;
import java.util.List;

import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.ext.web.client.spi.CookieStore;

/**
 * A stateless interceptor for session management that operates on the {@code HttpContext}
 */
public class SessionAwareInterceptor implements Handler<HttpContext<?>> {

  private static final String HEADERS_CONTEXT_KEY = "_originalHeaders";

  @Override
  public void handle(HttpContext<?> context) {
    switch(context.phase()) {
    case PREPARE_REQUEST:
      prepareRequest(context);
      break;
    case FOLLOW_REDIRECT:
      processRedirectCookies(context);
      break;
    case DISPATCH_RESPONSE:
      processResponse(context);
	    break;
    default:
      break;
    }

    context.next();
  }

  private void prepareRequest(HttpContext<?> context) {

    HttpRequestImpl<?> request = (HttpRequestImpl<?>) context.request();
    WebClientSessionAware webclient = (WebClientSessionAware) request.client;

    MultiMap headers = context.get(HEADERS_CONTEXT_KEY);
    if (headers == null) {
      headers = new CaseInsensitiveHeaders().addAll(request.headers());
      context.set(SessionAwareInterceptor.HEADERS_CONTEXT_KEY, headers);
    }

    // we need to reset the headers at every "send" because cookies can be changed,
    // either by the server (that sent new ones) or by the user.
    request.headers().clear().addAll(headers).addAll(webclient.headers());

    String domain = request.virtualHost;
    if (domain == null) {
      domain = request.host;
    }

    Iterable<Cookie> cookies = webclient.cookieStore().get(request.ssl, domain, request.uri);
    for (Cookie c : cookies) {
      request.headers().add("cookie", ClientCookieEncoder.STRICT.encode(c));
    }
  }

  private void processRedirectCookies(HttpContext<?> context) {
    this.processRedirectResponse(context);
    this.prepareRedirectRequest(context);
  }

  private void processRedirectResponse(HttpContext<?> context) {
    // Now the context contains the redirect request in clientRequest() and the original request in request()
    List<String> cookieHeaders = context.clientResponse().cookies();
    if (cookieHeaders == null) {
      return;
    }

    WebClientSessionAware webclient = (WebClientSessionAware) ((HttpRequestImpl)context.request()).client;
    HttpRequestImpl<?> originalRequest = (HttpRequestImpl<?>) context.request();
    CookieStore cookieStore = webclient.cookieStore();
    String domain = URI.create(context.clientResponse().request().absoluteURI()).getHost();
    if (domain.equals(originalRequest.host) && originalRequest.virtualHost != null) {
      domain = originalRequest.virtualHost;
    }
    final String finalDomain = domain;
    cookieHeaders.forEach(header -> {
      Cookie cookie = ClientCookieDecoder.STRICT.decode(header);
      if (cookie != null) {
        if (cookie.domain() == null) {
          // Set the domain if missing, because we need to send cookies
          // only to the domains we received them from.
          cookie.setDomain(finalDomain);
        }
        cookieStore.put(cookie);
      }
    });
  }

  private void prepareRedirectRequest(HttpContext<?> context) {
    // Now the context contains the redirect request in clientRequest() and the original request in request()
    HttpClientRequest redirectRequest = context.clientRequest();
    HttpRequestImpl<?> originalRequest = (HttpRequestImpl<?>) context.request();
    WebClientSessionAware webclient = (WebClientSessionAware) originalRequest.client;

    MultiMap headers = context.get(HEADERS_CONTEXT_KEY);
    if (headers == null) {
      headers = new CaseInsensitiveHeaders().addAll(redirectRequest.headers());
      context.set(SessionAwareInterceptor.HEADERS_CONTEXT_KEY, headers);
    }

    String redirectHost = URI.create(redirectRequest.absoluteURI()).getHost();
    String domain;
    if (redirectHost.equals(originalRequest.host) && originalRequest.virtualHost != null) {
      domain = originalRequest.virtualHost;
    } else {
      domain = redirectHost;
    }

    Iterable<Cookie> cookies = webclient.cookieStore().get(originalRequest.ssl, domain, redirectRequest.path());
    for (Cookie c : cookies) {
      redirectRequest.headers().add("cookie", ClientCookieEncoder.STRICT.encode(c));
    }
  }

  private void processResponse(HttpContext<?> context) {
    List<String> cookieHeaders = context.clientResponse().cookies();
    if (cookieHeaders == null) {
      return;
    }

    WebClientSessionAware webclient = (WebClientSessionAware) ((HttpRequestImpl)context.request()).client;
    HttpRequestImpl<?> request = (HttpRequestImpl<?>) context.request();
    CookieStore cookieStore = webclient.cookieStore();
    cookieHeaders.forEach(header -> {
      Cookie cookie = ClientCookieDecoder.STRICT.decode(header);
      if (cookie != null) {
        if (cookie.domain() == null) {
          // Set the domain if missing, because we need to send cookies
          // only to the domains we received them from.
          cookie.setDomain(request.virtualHost != null ? request.virtualHost : request.host);
        }
        cookieStore.put(cookie);
      }
    });
  }
}
