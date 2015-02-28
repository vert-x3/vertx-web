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

package io.vertx.ext.apex.impl;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.Route;
import io.vertx.ext.apex.RoutingContext;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * This class is thread-safe
 *
 * Some parts (e.g. content negotiation) from Yoke by Paulo Lopes
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public class RouteImpl implements Route {

  private static final Logger log = LoggerFactory.getLogger(RouteImpl.class);

  private final RouterImpl router;
  private final Set<HttpMethod> methods = new HashSet<>();
  private final Set<String> consumes = new HashSet<>();
  private final Set<String> produces = new HashSet<>();
  private String path;
  private int order;
  private boolean enabled = true;
  private Handler<RoutingContext> contextHandler;
  private Handler<RoutingContext> failureHandler;
  private boolean added;
  private Pattern pattern;
  private Set<String> groups;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Route[ ");
    sb.append("path:").append(path);
    sb.append(" pattern:").append(pattern);
    sb.append(" handler:").append(contextHandler);
    sb.append(" failureHandler:").append(failureHandler);
    sb.append(" order:").append(order);
    sb.append(" methods:[");
    int cnt = 0;
    for (HttpMethod method: methods) {
      sb.append(method);
      cnt++;
      if (cnt < methods.size()) {
        sb.append(",");
      }
    }
    sb.append("]]@").append(System.identityHashCode(this));
    return sb.toString();
  }

  RouteImpl(RouterImpl router, int order) {
    this.router = router;
    this.order = order;
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
  public synchronized Route pathRegex(String path) {
    setRegex(path);
    return this;
  }

  @Override
  public synchronized Route produces(String contentType) {
    produces.add(contentType);
    return this;
  }

  @Override
  public synchronized Route consumes(String contentType) {
    consumes.add(contentType);
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
  public synchronized Route last(boolean last) {
    return order(Integer.MAX_VALUE);
  }

  @Override
  public synchronized Route handler(Handler<RoutingContext> contextHandler) {
    if (this.contextHandler != null) {
      log.warn("Setting handler for a route more than once!");
    }
    this.contextHandler = contextHandler;
    checkAdd();
    return this;
  }

  @Override
  public synchronized Route failureHandler(Handler<RoutingContext> exceptionHandler) {
    if (this.failureHandler != null) {
      log.warn("Setting failureHandler for a route more than once!");
    }
    this.failureHandler = exceptionHandler;
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
  public String getPath() {
    return path;
  }

  synchronized void handleContext(RoutingContext context) {
    if (contextHandler != null) {
      contextHandler.handle(context);
    }
  }

  synchronized void handleFailure(RoutingContext context) {
    if (failureHandler != null) {
      failureHandler.handle(context);
    }
  }

  private boolean pathMatches(String mountPoint, HttpServerRequest request) {
    //System.out.println("Matches, mountPoint: " + mountPoint);
    String requestPath = request.path();
//    System.out.println("request path: " + requestPath);
//    System.out.println("path: " + path);
    if (mountPoint == null) {
      return requestPath.startsWith(path);
    } else {
      return requestPath.startsWith(mountPoint + path);
    }
  }

  synchronized boolean matches(RoutingContext context, String mountPoint, boolean failure) {
    if (failure && failureHandler == null || !failure && contextHandler == null) {
      return false;
    }
    if (!enabled) {
      return false;
    }
    HttpServerRequest request = context.request();
    if (!methods.isEmpty() && !methods.contains(request.method())) {
      return false;
    }
    if (path != null && !pathMatches(mountPoint, request)) {
      return false;
    }
    if (pattern != null) {
      String path  = request.path();
      if (mountPoint != null) {
        path = path.substring(mountPoint.length());
      }
      Matcher m = pattern.matcher(path);
      if (m.matches()) {
        if (m.groupCount() > 0) {
          Map<String, String> params = new HashMap<>(m.groupCount());
          if (groups != null) {
            // Pattern - named params
            for (String param : groups) {
              params.put(param, m.group(param));
            }
          } else {
            // Straight regex - un-named params
            for (int i = 0; i < m.groupCount(); i++) {
              params.put("param" + i, m.group(i + 1));
            }
          }
          request.params().addAll(params);
        }
      } else {
        return false;
      }
    }
    if (!consumes.isEmpty()) {
      // Can this route consume the specified content type
      String contentType = request.headers().get("content-type");
      boolean matches = false;
      for (String ct: consumes) {
        if (ctMatches(contentType, ct)) {
          matches = true;
          break;
        }
      }
      if (!matches) {
        return false;
      }
    }
    if (!produces.isEmpty()) {
      String accept = request.headers().get("accept");
      if (accept != null) {
        List<String> acceptableTypes = Utils.getSortedAcceptableMimeTypes(accept);
        for (String acceptable: acceptableTypes) {
          for (String produce : produces) {
            if (ctMatches(produce, acceptable)) {
              context.setAcceptableContentType(produce);
              return true;
            }
          }
        }
      }
      return false;
    }
    return true;
  }

  RouterImpl router() {
    return router;
  }

  /*
  E.g.
  "text/html", "text/*"  - returns true
  "text/html", "html" - returns true
  "application/json", "json" - returns true
  "application/*", "json" - returns true
  TODO - don't parse consumes types on each request - they can be preparsed!
   */
  private boolean ctMatches(String actualCT, String allowsCT) {

    if (allowsCT.equals("*") || allowsCT.equals("*/*")) {
      return true;
    }

    if (actualCT == null) {
      return false;
    }
    
    // get the content type only (exclude charset)
    actualCT = actualCT.split(";")[0];

    // if we received an incomplete CT
    if (allowsCT.indexOf('/') == -1) {
      // when the content is incomplete we assume */type, e.g.:
      // json -> */json
      allowsCT = "*/" + allowsCT;
    }

    // process wildcards
    if (allowsCT.contains("*")) {
      String[] consumesParts = allowsCT.split("/");
      String[] requestParts = actualCT.split("/");
      return "*".equals(consumesParts[0]) && consumesParts[1].equals(requestParts[1]) ||
             "*".equals(consumesParts[1]) && consumesParts[0].equals(requestParts[0]);
    }

    return actualCT.contains(allowsCT);
  }

  private void setPath(String path) {
    // See if the path contains ":" - if so then it contains parameter capture groups and we have to generate
    // a regex for that
    if (path.indexOf(':') != -1) {
      createPatternRegex(path);
    } else {
      this.path = path;
    }
  }

  private void setRegex(String regex) {
    pattern = Pattern.compile(regex + "(/.*)?"); // The regex is not an exact match - it matches the *start* of the path
  }

  private void createPatternRegex(String path) {
    // We need to search for any :<token name> tokens in the String and replace them with named capture groups
    Matcher m =  Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)").matcher(path);
    StringBuffer sb = new StringBuffer();
    groups = new HashSet<>();
    while (m.find()) {
      String group = m.group().substring(1);
      if (groups.contains(group)) {
        throw new IllegalArgumentException("Cannot use identifier " + group + " more than once in pattern string");
      }
      m.appendReplacement(sb, "(?<$1>[^/]+)");
      groups.add(group);
    }
    m.appendTail(sb);
    sb.append("(/.*)?"); // Match anything after that - i.e. the regex matches the *start* of the path
    path = sb.toString();

    pattern = Pattern.compile(path);
  }

  private void checkPath(String path) {
    if (path.charAt(0) != '/') {
      throw new IllegalArgumentException("Path must start with /");
    }
  }

  int order() {
    return order;
  }

  private void checkAdd() {
    if (!added) {
      router.add(this);
      added = true;
    }
  }

}
