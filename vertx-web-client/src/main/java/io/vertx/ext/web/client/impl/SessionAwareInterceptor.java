package io.vertx.ext.web.client.impl;

import java.util.List;

import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.ext.web.client.CookieStore;
import io.vertx.ext.web.client.HttpResponse;

public class SessionAwareInterceptor implements Handler<HttpContext> {
  public static final String CLIENT_CONTEXT_KEY = "_SessionAwareWebClient";
  private static final String HEADERS_CONTEXT_KEY = "_originalHeaders";
  
  @Override
  public void handle(HttpContext context) {
    HttpRequestImpl<?> request = (HttpRequestImpl<?>) context.request();
    SessionAwareWebClientImpl webclient = context.get(CLIENT_CONTEXT_KEY);
    assert webclient != null : "WRONG API usage: missing SessionAwareWebClient in HttpContext";

    // we need to reset the headers at every "send" because cookies can be changed,
    // either by the server (that sent new ones) or by the user.
    MultiMap headers = context.get(HEADERS_CONTEXT_KEY);
    request.headers().clear().addAll(headers).addAll(webclient.headers());

    String domain = request.virtualHost;
    if (domain == null) {
      domain = request.host;
    }

    String uri = request.uri;
    int pos = uri.indexOf('?');
    if (pos > -1) {
      uri = uri.substring(0, pos);
    }

    Iterable<Cookie> cookies = webclient.getCookieStore().get(request.ssl, domain, uri);
    for (Cookie c : cookies) {
      request.headers().add("cookie", ClientCookieEncoder.STRICT.encode(c));
    }
    
    Handler<AsyncResult<HttpResponse<Object>>> originalHandler = context.getResponseHandler();
    context.setResponseHandler(ar -> {
      if (ar.succeeded()) {
        List<String> cookieHeaders = ar.result().cookies();
        if (cookieHeaders == null) {
          originalHandler.handle(ar);
          return;
        }
        CookieStore cookieStore = webclient.getCookieStore();
        cookieHeaders.forEach(header -> {
          Cookie cookie = ClientCookieDecoder.STRICT.decode(header);
          if (cookie != null) {
            if (cookie.domain() == null) {
              // Set the domain if missing, because we need to send cookies
              // only to the domains we received them from.
              cookie.setDomain(request.virtualHost != null ? request.virtualHost : request.host);
            }
            if (cookieStore instanceof InternalCookieStore) {
              ((InternalCookieStore) cookieStore).put(cookie);
            } else {
              cookieStore.put(cookie.name(), cookie.value(), cookie.domain(), cookie.path(), cookie.maxAge(),
                  cookie.isSecure());
            }
          }
        });
      }
      originalHandler.handle(ar);
    });
    
    context.next();
  }

  public static void prepareContext(HttpContext context, SessionAwareWebClientImpl client) {
    context.set(SessionAwareInterceptor.CLIENT_CONTEXT_KEY, client);

    HttpRequestImpl<?> request = (HttpRequestImpl<?>) context.request();
    MultiMap headers = new CaseInsensitiveHeaders().addAll(request.headers());
    context.set(SessionAwareInterceptor.HEADERS_CONTEXT_KEY, headers);
  }

}
