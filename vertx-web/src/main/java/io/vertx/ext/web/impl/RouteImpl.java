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

import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.MIMEHeader;
import io.vertx.ext.web.Route;
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

  private static final Logger log = LoggerFactory.getLogger(RouteImpl.class);

  private final RouterImpl router;
  private final Set<HttpMethod> methods = new HashSet<>();
  private final Set<MIMEHeader> consumes = new LinkedHashSet<>();
  private final Set<MIMEHeader> produces = new LinkedHashSet<>();
  private String path;
  private int order;
  private boolean enabled = true;
  private List<Handler<RoutingContext>> contextHandlers;
  private int actualHandlerIndex;
  private List<Handler<RoutingContext>> failureHandlers;
  private int actualFailureHandlerIndex;
  private boolean added;
  private Pattern pattern;
  private List<String> groups;
  private boolean useNormalisedPath = true;

  RouteImpl(RouterImpl router, int order) {
    this.router = router;
    this.order = order;
    this.contextHandlers = new ArrayList<>();
    this.failureHandlers = new ArrayList<>();
    resetIndexes();
  }

  RouteImpl(RouterImpl router, int order, HttpMethod method, String path) {
    this(router, order);
    methods.add(method);
    checkPath(path);
    setPath(path);
  }

  RouteImpl(RouterImpl router, int order, String path) {
    this(router, order);
    checkPath(path);
    setPath(path);
  }

  RouteImpl(RouterImpl router, int order, HttpMethod method, String regex, boolean bregex) {
    this(router, order);
    methods.add(method);
    setRegex(regex);
  }

  RouteImpl(RouterImpl router, int order, String regex, boolean bregex) {
    this(router, order);
    setRegex(regex);
  }

  @Override
  public synchronized Route method(HttpMethod method) {
    methods.add(method);
    return this;
  }

  @Override
  public synchronized Route path(String path) {
    checkPath(path);
    setPath(path);
    return this;
  }

  @Override
  public synchronized Route pathRegex(String regex) {
    setRegex(regex);
    return this;
  }

  @Override
  public synchronized Route produces(String contentType) {
    ParsableMIMEValue value = new ParsableMIMEValue(contentType).forceParse();
    produces.add(value);
    return this;
  }

  @Override
  public synchronized Route consumes(String contentType) {
    ParsableMIMEValue value = new ParsableMIMEValue(contentType).forceParse();
    consumes.add(value);
    return this;
  }

  @Override
  public synchronized Route order(int order) {
    if (added) {
      throw new IllegalStateException("Can't change order after route is active");
    }
    this.order = order;
    return this;
  }

  @Override
  public synchronized Route last() {
    return order(Integer.MAX_VALUE);
  }

  @Override
  public synchronized Route handler(Handler<RoutingContext> contextHandler) {
    this.contextHandlers.add(contextHandler);
    checkAdd();
    return this;
  }

  @Override
  public Route blockingHandler(Handler<RoutingContext> contextHandler) {
    return blockingHandler(contextHandler, true);
  }

  @Override
  public synchronized Route blockingHandler(Handler<RoutingContext> contextHandler, boolean ordered) {
    return handler(new BlockingHandlerDecorator(contextHandler, ordered));
  }

  @Override
  public synchronized Route failureHandler(Handler<RoutingContext> exceptionHandler) {
    this.failureHandlers.add(exceptionHandler);
    checkAdd();
    return this;
  }

  @Override
  public synchronized Route remove() {
    router.remove(this);
    return this;
  }

  @Override
  public synchronized Route disable() {
    enabled = false;
    return this;
  }

  @Override
  public synchronized Route enable() {
    enabled = true;
    return this;
  }

  @Override
  public Route useNormalisedPath(boolean useNormalisedPath) {
    this.useNormalisedPath = useNormalisedPath;
    return this;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public Route setRegexGroupsNames(List<String> groups) {
    this.groups = groups;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Route[ ");
    sb.append("path:").append(path);
    sb.append(" pattern:").append(pattern);
    sb.append(" handlers:").append(contextHandlers);
    sb.append(" failureHandlers:").append(failureHandlers);
    sb.append(" order:").append(order);
    sb.append(" methods:[");
    int cnt = 0;
    for (HttpMethod method : methods) {
      sb.append(method);
      cnt++;
      if (cnt < methods.size()) {
        sb.append(",");
      }
    }
    sb.append("]]@").append(System.identityHashCode(this));
    return sb.toString();
  }

  synchronized void handleContext(RoutingContext context) {
    if (this.hasNextContextHandler()) {
      actualHandlerIndex++;
      contextHandlers.get(actualHandlerIndex - 1).handle(context);
    }
  }

  synchronized void handleFailure(RoutingContext context) {
    if (this.hasNextFailureHandler()) {
      actualFailureHandlerIndex++;
      failureHandlers.get(actualFailureHandlerIndex - 1).handle(context);
    }
  }

  synchronized boolean matches(RoutingContext context, String mountPoint, boolean failure) {

    if (failure && !hasNextFailureHandler() || !failure && !hasNextContextHandler()) {
      return false;
    }
    if (!enabled) {
      return false;
    }
    HttpServerRequest request = context.request();
    if (!methods.isEmpty() && !methods.contains(request.method())) {
      return false;
    }
    if (path != null && pattern == null && !pathMatches(mountPoint, context)) {
      return false;
    }
    if (pattern != null) {
      String path = useNormalisedPath ? Utils.normalizePath(context.request().path()) : context.request().path();
      if (mountPoint != null) {
        path = path.substring(mountPoint.length());
      }

      Matcher m = pattern.matcher(path);
      if (m.matches()) {
        if (m.groupCount() > 0) {
          Map<String, String> params = new HashMap<>(m.groupCount());
          if (groups != null) {
            // Pattern - named params
            // decode the path as it could contain escaped chars.
            for (int i = 0; i < groups.size(); i++) {
              final String k = groups.get(i);
              String undecodedValue;
              try {
                undecodedValue = m.group("p" + i);
              } catch (IllegalArgumentException e) {
                undecodedValue = m.group(k);
              }
              final String value = Utils.urlDecode(undecodedValue, false);
              if (!request.params().contains(k)) {
                params.put(k, value);
              } else {
                context.pathParams().put(k, value);
              }
            }
          } else {
            // Straight regex - un-named params
            // decode the path as it could contain escaped chars.
            for (int i = 0; i < m.groupCount(); i++) {
              String group = m.group(i + 1);
              if (group != null) {
                final String k = "param" + i;
                final String value = Utils.urlDecode(group, false);
                if (!request.params().contains(k)) {
                  params.put(k, value);
                } else {
                  context.pathParams().put(k, value);
                }
              }
            }
          }
          request.params().addAll(params);
          context.pathParams().putAll(params);
        }
      } else {
        return false;
      }
    }

    // Check if query params are already parsed
    if (context.queryParams().size() == 0) {
      // Decode query parameters and put inside context.queryParams
      Map<String, List<String>> decodedParams = new QueryStringDecoder(request.uri()).parameters();

      for (Map.Entry<String, List<String>> entry : decodedParams.entrySet())
        context.queryParams().add(entry.getKey(), entry.getValue());
    }

    if (!consumes.isEmpty()) {
      // Can this route consume the specified content type
      MIMEHeader contentType = context.parsedHeaders().contentType();
      MIMEHeader consumal = contentType.findMatchedBy(consumes);
      if (consumal == null) {
        return false;
      }
    }
    List<MIMEHeader> acceptableTypes = context.parsedHeaders().accept();
    if (!produces.isEmpty() && !acceptableTypes.isEmpty()) {
      MIMEHeader selectedAccept = context.parsedHeaders().findBestUserAcceptedIn(acceptableTypes, produces);
      if (selectedAccept != null) {
        context.setAcceptableContentType(selectedAccept.rawValue());
        return true;
      }
      return false;
    }
    return true;
  }

  RouterImpl router() {
    return router;
  }

  private boolean pathMatches(String mountPoint, RoutingContext ctx) {
    String thePath = mountPoint == null ? path : mountPoint + path;
    String requestPath;

    if (useNormalisedPath) {
      // never null
      requestPath = Utils.normalizePath(ctx.request().path());
    } else {
      requestPath = ctx.request().path();
      // can be null
      if (requestPath == null) {
        requestPath = "/";
      }
    }

    if (exactPath) {
      return pathMatchesExact(requestPath, thePath);
    } else {
      if (thePath.endsWith("/") && requestPath.equals(removeTrailing(thePath))) {
        return true;
      }
      return requestPath.startsWith(thePath);
    }
  }

  private boolean pathMatchesExact(String path1, String path2) {
    // Ignore trailing slash when matching paths
    return removeTrailing(path1).equals(removeTrailing(path2));
  }

  private String removeTrailing(String path) {
    int i = path.length();
    if (path.charAt(i - 1) == '/') {
      path = path.substring(0, i - 1);
    }
    return path;
  }

  private void setPath(String path) {
    // See if the path contains ":" - if so then it contains parameter capture groups and we have to generate
    // a regex for that
    if (path.indexOf(':') != -1) {
      createPatternRegex(path);
      this.path = path;
    } else {
      if (path.charAt(path.length() - 1) != '*') {
        exactPath = true;
        this.path = path;
      } else {
        exactPath = false;
        this.path = path.substring(0, path.length() - 1);
      }
    }
  }

  private void setRegex(String regex) {
    // Check if there are any groups with names
    pattern = Pattern.compile(regex);
  }

  // intersection of regex chars and https://tools.ietf.org/html/rfc3986#section-3.3
  private static final Pattern RE_OPERATORS_NO_STAR = Pattern.compile("([\\(\\)\\$\\+\\.])");

  private void createPatternRegex(String path) {
    // escape path from any regex special chars
    path = RE_OPERATORS_NO_STAR.matcher(path).replaceAll("\\\\$1");
    // allow usage of * at the end as per documentation
    if (path.charAt(path.length() - 1) == '*') {
      path = path.substring(0, path.length() - 1) + ".*";
    }
    // We need to search for any :<token name> tokens in the String and replace them with named capture groups
    Matcher m = Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)").matcher(path);
    StringBuffer sb = new StringBuffer();
    groups = new ArrayList<>();
    int index = 0;
    while (m.find()) {
      String param = "p" + index;
      String group = m.group().substring(1);
      if (groups.contains(group)) {
        throw new IllegalArgumentException("Cannot use identifier " + group + " more than once in pattern string");
      }
      m.appendReplacement(sb, "(?<" + param + ">[^/]+)");
      groups.add(group);
      index++;
    }
    m.appendTail(sb);
    path = sb.toString();
    pattern = Pattern.compile(path);
  }

  private void checkPath(String path) {
    if ("".equals(path) || path.charAt(0) != '/') {
      throw new IllegalArgumentException("Path must start with /");
    }
  }

  private boolean exactPath;

  int order() {
    return order;
  }

  private void checkAdd() {
    if (!added) {
      router.add(this);
      added = true;
    }
  }

  synchronized protected boolean hasNextContextHandler() {
    if (actualHandlerIndex < contextHandlers.size()) return true;
    else return false;
  }

  synchronized protected boolean hasNextFailureHandler() {
    if (actualFailureHandlerIndex < failureHandlers.size()) return true;
    else return false;
  }

  synchronized protected void resetIndexes() {
    actualFailureHandlerIndex = 0;
    actualHandlerIndex = 0;
  }
}
