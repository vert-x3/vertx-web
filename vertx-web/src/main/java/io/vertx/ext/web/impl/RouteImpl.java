/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.impl;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is thread-safe
 * <p>
 * Some parts (e.g. content negotiation) from Yoke by Paulo Lopes
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public class RouteImpl implements Route {

  private final RouterImpl router;
  private volatile RouteState state;

  RouteImpl(RouterImpl router, int order) {
    this.router = router;
    this.state = new RouteState(this, order);
  }

  RouteImpl(RouterImpl router, int order, String path) {
    this(router, order);
    checkPath(path);
    setPath(path);
  }

  RouteImpl(RouterImpl router, int order, HttpMethod method, String path) {
    this(router, order);
    method(method);
    checkPath(path);
    setPath(path);
  }

  RouteImpl(RouterImpl router, int order, String regex, boolean bregex) {
    this(router, order);
    setRegex(regex);
  }

  RouteImpl(RouterImpl router, int order, HttpMethod method, String regex, boolean bregex) {
    this(router, order);
    method(method);
    setRegex(regex);
  }

  RouteState state() {
    return state;
  }

  @Override
  public synchronized Route putMetadata(String key, Object value) {
    state = state.putMetadata(key, value);
    return this;
  }

  @Override
  public synchronized Route method(HttpMethod method) {
    state = state.addMethod(method);
    return this;
  }

  @Override
  public Route path(String path) {
    checkPath(path);
    setPath(path);
    return this;
  }

  @Override
  public Route pathRegex(String regex) {
    setRegex(regex);
    return this;
  }

  @Override
  public synchronized Route produces(String contentType) {
    state = state.addProduce(new ParsableMIMEValue(contentType).forceParse());
    return this;
  }

  @Override
  public synchronized Route consumes(String contentType) {
    state = state.addConsume(new ParsableMIMEValue(contentType).forceParse());
    return this;
  }

  @Override
  public synchronized Route virtualHost(String hostnamePattern) {
    state = state
      .setVirtualHostPattern(
        Pattern.compile(
          hostnamePattern
            .replaceAll("\\.", "\\\\.")
            .replaceAll("[*]", "(.*?)"), Pattern.CASE_INSENSITIVE));

    return this;
  }

  @Override
  public synchronized Route order(int order) {
    if (state.isAdded()) {
      throw new IllegalStateException("Can't change order after route is active");
    }
    state = state.setOrder(order);
    return this;
  }

  @Override
  public Route last() {
    return order(Integer.MAX_VALUE);
  }

  @Override
  public synchronized Route handler(Handler<RoutingContext> contextHandler) {
    if (state.isExclusive()) {
      throw new IllegalStateException("This Route is exclusive for already mounted sub router.");
    }
    state = state.addContextHandler(contextHandler);

    checkAdd();

    // after the checkAdd the flag "added" should be true, fixing the order of the route

    if (contextHandler instanceof OrderListener) {
      ((OrderListener) contextHandler).onOrder(order());
    }

    return this;
  }

  @Override
  public Route blockingHandler(Handler<RoutingContext> contextHandler) {
    return blockingHandler(contextHandler, true);
  }

  @Override
  public synchronized Route subRouter(Router subRouter) {

    // The route path must end with a wild card
    if (state.getPath() != null && state.isExactPath()) {
      throw new IllegalStateException("Sub router cannot be mounted on an exact path.");
    }
    // Parameters are allowed but full regex patterns not
    if (state.getPath() == null && state.getPattern() != null) {
      throw new IllegalStateException("Sub router cannot be mounted on a regular expression path.");
    }
    // No other handler can be registered before or after this call (but they can on a new route object for the same path)
    if (state.isExclusive()) {
      throw new IllegalStateException("Only one sub router per Route object is allowed.");
    }

    handler(subRouter::handleContext);
    failureHandler(subRouter::handleFailure);

    subRouter.modifiedHandler(this::validateMount);

    // trigger a validation
    validateMount(subRouter);

    // mark the route as exclusive from now on
    this.state = state.setExclusive(true).setSubRouter(subRouter);
    return this;
  }

  @Override
  public Route blockingHandler(Handler<RoutingContext> contextHandler, boolean ordered) {
    return handler(new BlockingHandlerDecorator(contextHandler, ordered));
  }

  @Override
  public synchronized Route failureHandler(Handler<RoutingContext> exceptionHandler) {
    if (state.isExclusive()) {
      throw new IllegalStateException("This Route is exclusive for already mounted sub router.");
    }

    state = state.addFailureHandler(exceptionHandler);
    checkAdd();
    return this;
  }

  @Override
  public Route remove() {
    router.remove(this);
    return this;
  }

  @Override
  public synchronized Route disable() {
    state = state.setEnabled(false);
    return this;
  }

  @Override
  public synchronized Route enable() {
    state = state.setEnabled(true);
    return this;
  }

  @Override
  public synchronized Route useNormalizedPath(boolean useNormalizedPath) {
    state = state.setUseNormalizedPath(useNormalizedPath);
    return this;
  }

  @Override
  public Map<String, Object> metadata() {
    Map<String, Object> metadata = state.getMetadata();
    return metadata != null ? metadata : Collections.emptyMap();
  }

  @Override
  public String getPath() {
    return state.getPath();
  }

  @Override
  public boolean isRegexPath() {
    return state.getPattern() != null;
  }

  @Override
  public boolean isExactPath() {
    return state.isExactPath();
  }

  @Override
  public Set<HttpMethod> methods() {
    return state.getMethods();
  }

  @Override
  public synchronized Route setRegexGroupsNames(List<String> groups) {
    state = state.setGroups(groups);
    return this;
  }

  @Override
  public synchronized Route setName(String name) {
    state = state.setName(name);
    return this;
  }

  @Override
  public String getName() {
    return state.getName();
  }

  @Override
  public String toString() {
    return "RouteImpl@" + System.identityHashCode(this) +
      "{" +
      "state=" + state +
      '}';
  }

  RouterImpl router() {
    return router;
  }

  private synchronized void setPath(String path) {
    // See if the path is a wildcard "*" is present - If so we need to configure this path to be not exact
    if (path.charAt(path.length() - 1) != '*') {
      state = state.setExactPath(true);
      state = state.setPath(path);
    } else {
      state = state.setExactPath(false);
      state = state.setPath(path.substring(0, path.length() - 1));
    }

    state = state.setPathEndsWithSlash(state.getPath().endsWith("/"));

    // See if the path contains ":" - if so then it contains parameter capture groups and we have to generate
    // a regex for that
    int params = 0;
    for (int i = 0; i < path.length(); i++) {
      if (path.charAt(i) == ':') {
        params++;
      }
    }
    if (params > 0) {
      int found = createPatternRegex(path);
      if (params != found) {
        throw new IllegalArgumentException("path param does not follow the variable naming rules, expected (" + params + ") found (" + found + ")");
      }
    }
  }

  private synchronized void setRegex(String regex) {
    state = state.setPattern(Pattern.compile(regex));
    state = state.setExactPath(true);
    findNamedGroups(state.getPattern().pattern());
  }

  private synchronized void findNamedGroups(String path) {
    Matcher m = RE_TOKEN_NAME_SEARCH.matcher(path);
    while (m.find()) {
      state = state.addNamedGroupInRegex(m.group(1));
    }
  }

  // Allow end users to select either the regular valid characters or the extender pattern
  private static final String RE_VAR_NAME = Boolean.getBoolean("io.vertx.web.route.param.extended-pattern") ?
    "[A-Za-z_$][A-Za-z0-9_$-]*" :
    "[A-Za-z0-9_]+";

  // Pattern for :<token name> in path, optionally followed by a ::<type> annotation, e.g.: ":id::integer"
  private static final Pattern RE_TOKEN_SEARCH = Pattern.compile(":(" + RE_VAR_NAME + ")(?:::(" + RE_VAR_NAME + "))?");

  // Known path parameter types, mapped to the regex a path value must match, e.g.: ":id::integer" only matches integers.
  private static final Map<String, String> TYPE_PATTERNS = Map.of(
    "integer", "-?\\d+",
    "number", "-?\\d+(?:\\.\\d+)?",
    "boolean", "true|false",
    "uuid", "\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}");

  // Pattern for (?<token name>) in path
  private static final Pattern RE_TOKEN_NAME_SEARCH = Pattern.compile("\\(\\?<(" + RE_VAR_NAME + ")>");

  // intersection of regex chars and https://tools.ietf.org/html/rfc3986#section-3.3
  private static final Pattern RE_OPERATORS_NO_STAR = Pattern.compile("([\\(\\)\\$\\+\\.])");

  private synchronized int createPatternRegex(String path) {
    // escape path from any regex special chars
    path = RE_OPERATORS_NO_STAR.matcher(path).replaceAll("\\\\$1");
    // allow usage of * at the end as per documentation
    if (path.charAt(path.length() - 1) == '*') {
      path = path.substring(0, path.length() - 1) + "(?<rest>.*)";
      state = state.setExactPath(false);
    } else {
      state = state.setExactPath(true);
    }

    // We need to search for any :<token name> tokens in the String and replace them with named capture groups
    Matcher m = RE_TOKEN_SEARCH.matcher(path);
    StringBuffer sb = new StringBuffer();
    List<String> groups = new ArrayList<>();
    int colons = 0;
    while (m.find()) {
      String name = m.group(1);
      String type = m.group(2);
      StringBuilder replacement = new StringBuilder();
      if (type == null) {
        appendNamedGroup(replacement, groups, name, "[^/]+");
        colons += 1;
      } else {
        String typePattern = TYPE_PATTERNS.get(type);
        if (typePattern == null) {
          throw new IllegalArgumentException("Unknown path parameter type: " + type + ". Known types: " + TYPE_PATTERNS.keySet());
        }
        // a typed param, e.g.: ":id::integer"
        appendNamedGroup(replacement, groups, name, typePattern);
        colons += 3; // 1 for :name, 2 for ::type
      }
      m.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
    }
    m.appendTail(sb);
    if (state.isExactPath() && !state.isPathEndsWithSlash()) {
      sb.append("/?");
    }
    path = sb.toString();

    state = state.setGroups(groups);
    state = state.setPattern(Pattern.compile(path));
    return colons;
  }

  private static void appendNamedGroup(StringBuilder sb, List<String> groups, String name, String pattern) {
    if (groups.contains(name)) {
      throw new IllegalArgumentException("Cannot use identifier " + name + " more than once in pattern string");
    }
    sb.append("(?<p").append(groups.size()).append('>').append(pattern).append(')');
    groups.add(name);
  }

  private void checkPath(String path) {
    if ("".equals(path) || path.charAt(0) != '/') {
      throw new IllegalArgumentException("Path must start with /");
    }
  }

  int order() {
    return state.getOrder();
  }

  private synchronized void checkAdd() {
    if (!state.isAdded()) {
      router.add(this);
      state = state.setAdded(true);
    }
  }

  public synchronized RouteImpl setEmptyBodyPermittedWithConsumes(boolean emptyBodyPermittedWithConsumes) {
    state = state.setEmptyBodyPermittedWithConsumes(emptyBodyPermittedWithConsumes);
    return this;
  }

  private void validateMount(Router router) {
    for (Route route : router.getRoutes()) {
      final String combinedPath;

      if (route.getPath() == null) {
        // This is a router with pattern and not path
        // we cannot validate
        continue;
      }

      // this method is similar to what the pattern generation does but
      // it will not generate a pattern, it will only verify if the paths do not contain
      // colliding parameter names with the mount path

      // escape path from any regex special chars
      combinedPath = RE_OPERATORS_NO_STAR
        .matcher(state.getPath() + (state.isPathEndsWithSlash() ? route.getPath().substring(1) : route.getPath()))
        .replaceAll("\\\\$1");

      // We need to search for any :<token name> tokens in the String
      Matcher m = RE_TOKEN_SEARCH.matcher(combinedPath);
      Set<String> groups = new HashSet<>();
      while (m.find()) {
        String name = m.group(1);
        if (!groups.add(name)) {
          throw new IllegalStateException("Cannot use identifier :" + name + " more than once in pattern string");
        }
      }
    }
  }

  @Override
  public Router getSubRouter() {
    return this.state.getSubRouter();
  }

}
