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
  private final String routePrefix;

  public PrefixedRouterImpl(Vertx vertx, String routePrefix) {
    super(vertx);
    this.routePrefix = routePrefix;
  }

  @Override
  public Route route() {
    return super.route(routePrefix);
  }

  @Override
  public Route route(String path) {
    return super.route(prefixedPath(path));
  }

  @Override
  public Route route(HttpMethod method, String path) {
    return super.route(method, prefixedPath(path));
  }

  @Override
  public Route routeWithRegex(HttpMethod method, String regex) {
    return super.routeWithRegex(method, prefixedRegex(regex));
  }

  @Override
  public Route routeWithRegex(String regex) {
    return super.routeWithRegex(prefixedRegex(regex));
  }


  private String prefixedPath(String path) {
    return routePrefix + path;
  }

  private String prefixedRegex(String regex) {
    return Pattern.quote(routePrefix) + regex;
  }
}
