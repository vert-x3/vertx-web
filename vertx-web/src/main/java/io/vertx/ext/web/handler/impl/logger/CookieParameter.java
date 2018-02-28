package io.vertx.ext.web.handler.impl.logger;

import static io.vertx.core.http.HttpHeaders.COOKIE;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.vertx.ext.web.RoutingContext;
import java.util.Set;

public class CookieParameter extends BaseParameter {

  private String cookieName;

  public CookieParameter(String cookieName) {
    super(cookieName);
    this.cookieName = cookieName;
  }

  @Override
  protected String getValue(RoutingContext context) {
    String cookieHeader = context.request().headers().get(COOKIE);

    if (cookieHeader != null) {
      Set<Cookie> nettyCookies = ServerCookieDecoder.STRICT.decode(cookieHeader);
      return nettyCookies.stream()
        .filter(cookie -> cookie.name().equals(cookieName))
        .findFirst()
        .map(cookie -> escape(cookie.toString()))
        .orElse(null);

    }
    return null;
  }
}
