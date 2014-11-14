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

package io.vertx.ext.rest.impl;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.rest.FailureRoutingContext;
import io.vertx.ext.rest.Route;
import io.vertx.ext.rest.RoutingContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * This class is thread-safe
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RouteImpl implements Route {

  private static final Logger log = LoggerFactory.getLogger(RouteImpl.class);

  private final RouterImpl router;

  private final Set<HttpMethod> methods = new HashSet<>();
  private final Set<String> consumes = new HashSet<>();
  private final Set<String> produces = new HashSet<>();
  private String path;
  private String regex;
  private int order;
  private boolean enabled = true;
  private Handler<RoutingContext> contextHandler;
  private Handler<FailureRoutingContext> exceptionHandler;
  private boolean added;

  RouteImpl(RouterImpl router, int order) {
    this.router = router;
    this.order = order;
  }

  RouteImpl(RouterImpl router, int order, HttpMethod method, String path) {
    this(router, order);
    methods.add(method);
    checkPath(path);
    this.path = path;
  }

  RouteImpl(RouterImpl router, int order, String path) {
    this(router, order);
    checkPath(path);
    this.path = path;
  }

  RouteImpl(RouterImpl router, int order, HttpMethod method, String regex, boolean bregex) {
    this(router, order);
    methods.add(method);
    this.regex = regex;
  }

  RouteImpl(RouterImpl router, int order, String regex, boolean bregex) {
    this(router, order);
    this.regex = regex;
  }

  private Pattern pattern;
  Set<String> groups;

  private void setRegex(String regex) {
    // We need to search for any :<token name> tokens in the String and replace them with named capture groups
    Matcher m =  Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)").matcher(regex);
    StringBuffer sb = new StringBuffer();
    groups = new HashSet<>();
    while (m.find()) {
      String group = m.group().substring(1);
      if (groups.contains(group)) {
        throw new IllegalArgumentException("Cannot use identifier " + group + " more than once in pattern string");
      }
      m.appendReplacement(sb, "(?<$1>[^\\/]+)");
      groups.add(group);
    }
    m.appendTail(sb);
    regex = sb.toString();
    pattern = Pattern.compile(regex);
  }

  boolean matches(HttpServerRequest request, boolean failure) {
    System.out.println("req: " + request.path() + " method: " + request.method());
    if (failure && exceptionHandler == null || !failure && contextHandler == null) {
      return false;
    }
    if (!enabled) {
      return false;
    }
    if (!methods.isEmpty() && !methods.contains(request.method())) {
      return false;
    }
    if (path != null && !request.path().startsWith(path)) {
      return false;
    }
    if (pattern != null) {
      Matcher m = pattern.matcher(request.path());
      if (m.matches()) {
        Map<String, String> params = new HashMap<>(m.groupCount());
        if (groups != null) {
          // Named params
          for (String param: groups) {
            params.put(param, m.group(param));
          }
        } else {
          // Un-named params
          for (int i = 0; i < m.groupCount(); i++) {
            params.put("param" + i, m.group(i + 1));
          }
        }
        request.params().addAll(params);
      } else {
        return false;
      }
    }
    if (!consumes.isEmpty()) {
      String contentType = request.headers().get("content-type");
      if (contentType == null || !consumes.contains(contentType)) {
        return false;
      }
    }
    if (!produces.isEmpty()) {
      String accept = request.headers().get("accept");
      // TODO accept header matching
      return false;
    }
    return true;
  }

  synchronized void handleContext(RoutingContext context) {
    if (contextHandler != null) {
      contextHandler.handle(context);
    }
  }

  synchronized void handleFailure(FailureRoutingContext context) {
    if (exceptionHandler != null) {
      exceptionHandler.handle(context);
    }
  }

  @Override
  public synchronized Route method(HttpMethod method) {
    methods.add(method);
    return this;
  }

  @Override
  public synchronized Route path(String path) {
    checkPath(path);
    this.path = path;
    return this;
  }

  @Override
  public synchronized Route pathRegex(String path) {
    this.regex = path;
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
  public Route last(boolean last) {
    return order(Integer.MAX_VALUE);
  }

  @Override
  public synchronized Route handler(Handler<RoutingContext> contextHandler) {
    this.contextHandler = contextHandler;
    checkAdd();
    return this;
  }

  @Override
  public synchronized Route exceptionHandler(Handler<FailureRoutingContext> exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
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

  public String toString() {
    return "Route: " + System.identityHashCode(this) + ", path: " + path + ", regex: " + regex + ", method: " + methods;
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
