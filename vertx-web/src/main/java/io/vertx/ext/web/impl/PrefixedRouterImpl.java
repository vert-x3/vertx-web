package io.vertx.ext.web.impl;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;

import java.util.regex.Pattern;

/**
 * A router which adds a prefix to every route added to it.
 * It's useful in place where a router is accepted and is used as a sub-router.
 * A sub-router can't be used with regular expressions or named path segments.
 *
 * <pre>
 *     Router prefixedRouter = Router.prefixedRouter(vertx, "/api/:name/events");
 *     SockJSHandler sockHandler = SockJSHandler.create(vertx, prefixedRouter).bridge(options)
 *
 *     Router httpRouter = Router.create(vertx)
 *     httpRouter.route("/*").handler(sockHandler)
 * </pre>
 *
 * @author jansorg
 */
public class PrefixedRouterImpl extends RouterImpl {
  private final String prefix;
  private final String escapedPrefix;
  private final boolean isRegex;

  public PrefixedRouterImpl(Vertx vertx, String routePrefix, boolean isRegex) {
    super(vertx);
    this.prefix = routePrefix;
    this.isRegex = isRegex;

    this.escapedPrefix = Pattern.quote(routePrefix);
  }

  @Override
  public Route route() {
    return isRegex
      ? super.routeWithRegex(prefix)
      : super.route(prefix);
  }

  @Override
  public Route route(String path) {
    return isRegex
      ? super.routeWithRegex(prefix + Pattern.quote(path))
      : super.route(prefix + path);
  }

  @Override
  public Route route(HttpMethod method, String path) {
    return isRegex
      ? super.routeWithRegex(method, prefix + Pattern.quote(path))
      : super.route(method, prefix + path);
  }

  @Override
  public Route routeWithRegex(String regex) {
    return isRegex
      ? super.routeWithRegex(prefix + regex)
      : super.routeWithRegex(escapedPrefix + regex);
  }

  @Override
  public Route routeWithRegex(HttpMethod method, String regex) {
    return isRegex
      ? super.routeWithRegex(method, prefix + regex)
      : super.routeWithRegex(method, escapedPrefix + regex);
  }

  @Override
  public Route getWithRegex(String path) {
    return isRegex
      ? super.getWithRegex(prefix + path)
      : super.getWithRegex(escapedPrefix + path);
  }

  @Override
  public Route headWithRegex(String path) {
    return isRegex
      ? super.headWithRegex(prefix + path)
      : super.headWithRegex(escapedPrefix + path);
  }

  @Override
  public Route optionsWithRegex(String path) {
    return isRegex
      ? super.optionsWithRegex(prefix + path)
      : super.optionsWithRegex(escapedPrefix + path);
  }

  @Override
  public Route putWithRegex(String path) {
    return isRegex
      ? super.putWithRegex(prefix + path)
      : super.putWithRegex(escapedPrefix + path);
  }

  @Override
  public Route postWithRegex(String path) {
    return isRegex
      ? super.postWithRegex(prefix + path)
      : super.postWithRegex(escapedPrefix + path);
  }

  @Override
  public Route deleteWithRegex(String path) {
    return isRegex
      ? super.deleteWithRegex(prefix + path)
      : super.deleteWithRegex(escapedPrefix + path);
  }

  @Override
  public Route traceWithRegex(String path) {
    return isRegex
      ? super.traceWithRegex(prefix + path)
      : super.traceWithRegex(escapedPrefix + path);
  }

  @Override
  public Route connectWithRegex(String path) {
    return isRegex
      ? super.connectWithRegex(prefix + path)
      : super.connectWithRegex(escapedPrefix + path);
  }

  @Override
  public Route patchWithRegex(String path) {
    return isRegex
      ? super.patchWithRegex(prefix + path)
      : super.patchWithRegex(escapedPrefix + path);
  }
}
