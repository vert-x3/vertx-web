package io.vertx.ext.web.client.impl;

import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.client.spi.CookieStore;

import java.net.URI;
import java.util.List;

import static io.vertx.core.http.HttpHeaders.AUTHORIZATION;

/**
 * A stateless interceptor for session management that operates on the {@code HttpContext}
 */
public class SessionAwareInterceptor implements Handler<HttpContext<?>> {

  private static final Logger LOG = LoggerFactory.getLogger(SessionAwareInterceptor.class);

  @Override
  public void handle(HttpContext<?> context) {
    switch (context.phase()) {
      case CREATE_REQUEST:
        createRequest(context)
          .onComplete(result -> {
            if (result.failed()) {
              context.fail(result.cause());
            } else {
              context.next();
            }
          });
        break;
      case FOLLOW_REDIRECT:
        processRedirectCookies(context);
        context.next();
        break;
      case DISPATCH_RESPONSE:
        processResponse(context);
        context.next();
        break;
      default:
        context.next();
        break;
    }
  }

  private Future<Void> createRequest(HttpContext<?> context) {

    HttpRequestImpl<?> request = (HttpRequestImpl<?>) context.request();
    WebClientSessionAware webclient = (WebClientSessionAware) request.client;

    RequestOptions requestOptions = context.requestOptions();
    MultiMap headers = requestOptions.getHeaders();
    if (headers == null) {
      headers = HttpHeaders.headers();
      requestOptions.setHeaders(headers);
    }
    headers.addAll(webclient.headers());

    String domain = request.virtualHost();
    if (domain == null) {
      domain = request.host();
    }

    Iterable<Cookie> cookies = webclient.cookieStore().get(request.ssl, domain, request.uri);
    String encodedCookies = ClientCookieEncoder.STRICT.encode(cookies);
    if (encodedCookies != null) {
      headers.add(HttpHeaders.COOKIE, encodedCookies);
    }

    Promise<Void> promise = Promise.promise();
    if (webclient.isWithAuthentication()) {
      if (webclient.getUser() != null) {
        if (webclient.getUser().expired()) {
          //Token has expired we need to invalidate the session
          webclient.getOAuth2Auth().refresh(webclient.getUser())
            .onSuccess(userResult -> {
              webclient.setUser(userResult);
              webclient.setWithAuthentication(false);
              context.requestOptions().addHeader(AUTHORIZATION, "Bearer " + userResult.principal().getString("access_token"));
              promise.complete();
            })
            .onFailure(error -> {
              // Refresh token failed, we can try standard authentication
              webclient.getOAuth2Auth().authenticate(webclient.getTokenConfig())
                .onSuccess(userResult -> {
                  webclient.setUser(userResult);
                  webclient.setWithAuthentication(false);
                  context.requestOptions().addHeader(AUTHORIZATION, "Bearer " + userResult.principal().getString("access_token"));
                  promise.complete();
                })
                .onFailure(errorAuth -> {
                  //Refresh token did not work and failed to obtain new authentication token, we need to fail
                  webclient.setUser(null);
                  webclient.setWithAuthentication(false);
                  promise.fail(errorAuth);
                });
            });
        } else {
          //User is not expired, access_token is valid
          webclient.setWithAuthentication(false);
          context.requestOptions().addHeader(AUTHORIZATION, webclient.getUser().principal().getString("access_token"));
          promise.complete();
        }
      } else {
        webclient.getOAuth2Auth().authenticate(webclient.getTokenConfig())
          .onSuccess(userResult -> {
            webclient.setUser(userResult);
            webclient.setWithAuthentication(false);
            context.requestOptions().addHeader(AUTHORIZATION, "Bearer " + userResult.principal().getString("access_token"));
            promise.complete();
          })
          .onFailure(promise::fail);
      }
    } else {
      promise.complete();
    }

    return promise.future();
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

    WebClientSessionAware webclient = (WebClientSessionAware) ((HttpRequestImpl<?>) context.request()).client;
    HttpRequestImpl<?> originalRequest = (HttpRequestImpl<?>) context.request();
    CookieStore cookieStore = webclient.cookieStore();
    String domain = URI.create(context.clientResponse().request().absoluteURI()).getHost();
    if (domain.equals(originalRequest.host()) && originalRequest.virtualHost != null) {
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
    RequestOptions redirectRequest = context.requestOptions();

    HttpRequestImpl<?> originalRequest = (HttpRequestImpl<?>) context.request();
    String redirectHost = redirectRequest.getHost();
    String domain;
    if (redirectHost.equals(originalRequest.host()) && originalRequest.virtualHost != null) {
      domain = originalRequest.virtualHost;
    } else {
      domain = redirectHost;
    }

    WebClientSessionAware webclient = (WebClientSessionAware) originalRequest.client;
    String path = parsePath(redirectRequest.getURI());
    Iterable<Cookie> cookies = webclient.cookieStore().get(originalRequest.ssl, domain, path);
    String encodedCookies = ClientCookieEncoder.STRICT.encode(cookies);
    if (encodedCookies != null) {
      redirectRequest.putHeader(HttpHeaders.COOKIE, encodedCookies);
    }
  }

  private static String parsePath(String uri) {
    if (uri.length() == 0) {
      return "";
    }
    int i;
    if (uri.charAt(0) == '/') {
      i = 0;
    } else {
      i = uri.indexOf("://");
      if (i == -1) {
        i = 0;
      } else {
        i = uri.indexOf('/', i + 3);
        if (i == -1) {
          // contains no /
          return "/";
        }
      }
    }

    int queryStart = uri.indexOf('?', i);
    if (queryStart == -1) {
      queryStart = uri.length();
    }
    return uri.substring(i, queryStart);
  }

  private void processResponse(HttpContext<?> context) {
    List<String> cookieHeaders = context.clientResponse().cookies();
    if (cookieHeaders == null) {
      return;
    }

    WebClientSessionAware webclient = (WebClientSessionAware) ((HttpRequestImpl<?>) context.request()).client;
    HttpRequestImpl<?> request = (HttpRequestImpl<?>) context.request();
    CookieStore cookieStore = webclient.cookieStore();
    cookieHeaders.forEach(header -> {
      Cookie cookie = ClientCookieDecoder.STRICT.decode(header);
      if (cookie != null) {
        if (cookie.domain() == null) {
          // Set the domain if missing, because we need to send cookies
          // only to the domains we received them from.
          cookie.setDomain(request.virtualHost != null ? request.virtualHost : request.host());
        }
        cookieStore.put(cookie);
      }
    });
  }
}
