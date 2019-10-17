/*
 * Copyright 2019 Red Hat, Inc.
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
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.impl.URIDecoder;
import io.vertx.ext.web.MIMEHeader;
import io.vertx.ext.web.RoutingContext;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class encapsulates the route state, all mutations are atomic and return a new state with the mutation.
 * <p>
 * This class is thread-safe
 *
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
final class RouteState {

  private final RouteImpl route;

  private final String path;
  private final int order;
  private final boolean enabled;
  private final Set<HttpMethod> methods;
  private final Set<MIMEHeader> consumes;
  private final boolean emptyBodyPermittedWithConsumes;
  private final Set<MIMEHeader> produces;
  private final List<Handler<RoutingContext>> contextHandlers;
  private final List<Handler<RoutingContext>> failureHandlers;
  private final boolean added;
  private final Pattern pattern;
  private final List<String> groups;
  private final boolean useNormalisedPath;
  private final Set<String> namedGroupsInRegex;
  private final Pattern virtualHostPattern;
  private final boolean pathEndsWithSlash;
  private final boolean exclusive;
  private final boolean exactPath;

  private RouteState(RouteImpl route, String path, int order, boolean enabled, Set<HttpMethod> methods, Set<MIMEHeader> consumes, boolean emptyBodyPermittedWithConsumes, Set<MIMEHeader> produces, List<Handler<RoutingContext>> contextHandlers, List<Handler<RoutingContext>> failureHandlers, boolean added, Pattern pattern, List<String> groups, boolean useNormalisedPath, Set<String> namedGroupsInRegex, Pattern virtualHostPattern, boolean pathEndsWithSlash, boolean exclusive, boolean exactPath) {
    this.route = route;
    this.path = path;
    this.order = order;
    this.enabled = enabled;
    this.methods = methods;
    this.consumes = consumes;
    this.emptyBodyPermittedWithConsumes = emptyBodyPermittedWithConsumes;
    this.produces = produces;
    this.contextHandlers = contextHandlers;
    this.failureHandlers = failureHandlers;
    this.added = added;
    this.pattern = pattern;
    this.groups = groups;
    this.useNormalisedPath = useNormalisedPath;
    this.namedGroupsInRegex = namedGroupsInRegex;
    this.virtualHostPattern = virtualHostPattern;
    this.pathEndsWithSlash = pathEndsWithSlash;
    this.exclusive = exclusive;
    this.exactPath = exactPath;
  }

  RouteState(RouteImpl route, int order) {
    this(
      route,
      null,
      order,
      true,
      null,
      null,
      false,
      null,
      null,
      null,
      false,
      null,
      null,
      true,
      null,
      null,
      false,
      false,
      false);
  }

  public RouteImpl getRoute() {
    return route;
  }

  public RouterImpl getRouter() {
    return route.router();
  }

  public String getPath() {
    return path;
  }

  RouteState setPath(String path) {
    return new RouteState(
      this.route,
      path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);
  }

  public int getOrder() {
    return order;
  }

  RouteState setOrder(int order) {
    return new RouteState(
      this.route,
      this.path,
      order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);
  }

  public boolean isEnabled() {
    return enabled;
  }

  RouteState setEnabled(boolean enabled) {
    return new RouteState(
      this.route,
      this.path,
      this.order,
      enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);
  }

  public Set<HttpMethod> getMethods() {
    return methods;
  }

  RouteState setMethods(Set<HttpMethod> methods) {
    return new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);
  }

  public RouteState addMethod(HttpMethod method) {
    RouteState newState = new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods == null ? new HashSet<>() : new HashSet<>(this.methods),
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);

    newState.methods.add(method);
    return newState;
  }

  public Set<MIMEHeader> getConsumes() {
    return consumes;
  }

  RouteState setConsumes(Set<MIMEHeader> consumes) {
    return new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);
  }

  RouteState addConsume(MIMEHeader mime) {
    RouteState newState = new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes == null ? new HashSet<>() : new HashSet<>(this.consumes),
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);

    newState.consumes.add(mime);
    return newState;
  }

  public boolean isEmptyBodyPermittedWithConsumes() {
    return emptyBodyPermittedWithConsumes;
  }

  RouteState setEmptyBodyPermittedWithConsumes(boolean emptyBodyPermittedWithConsumes) {
    return new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);
  }

  public Set<MIMEHeader> getProduces() {
    return produces;
  }

  RouteState setProduces(Set<MIMEHeader> produces) {
    return new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);
  }

  RouteState addProduce(MIMEHeader mime) {
    RouteState newState = new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces == null ? new HashSet<>() : new HashSet<>(this.produces),
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);

    newState.produces.add(mime);
    return newState;
  }

  public List<Handler<RoutingContext>> getContextHandlers() {
    return contextHandlers;
  }

  public int getContextHandlersLength() {
    return contextHandlers == null ? 0 : contextHandlers.size();
  }

  RouteState setContextHandlers(List<Handler<RoutingContext>> contextHandlers) {
    return new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);
  }

  RouteState addContextHandler(Handler<RoutingContext> contextHandler) {
    RouteState newState = new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers == null ? new ArrayList<>() : new ArrayList<>(this.contextHandlers),
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);

    newState.contextHandlers.add(contextHandler);
    return newState;
  }

  public List<Handler<RoutingContext>> getFailureHandlers() {
    return failureHandlers;
  }

  public int getFailureHandlersLength() {
    return failureHandlers == null ? 0 : failureHandlers.size();
  }

  RouteState setFailureHandlers(List<Handler<RoutingContext>> failureHandlers) {
    return new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);
  }

  RouteState addFailureHandler(Handler<RoutingContext> failureHandler) {
    RouteState newState = new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers == null ? new ArrayList<>() : new ArrayList<>(this.failureHandlers),
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);

    newState.failureHandlers.add(failureHandler);
    return newState;
  }

  public boolean isAdded() {
    return added;
  }

  RouteState setAdded(boolean added) {
    return new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);
  }

  public Pattern getPattern() {
    return pattern;
  }

  RouteState setPattern(Pattern pattern) {
    return new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);
  }

  public List<String> getGroups() {
    return groups;
  }

  RouteState setGroups(List<String> groups) {
    return new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);
  }

  RouteState addGroup(String group) {
    RouteState newState = new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups == null ? new ArrayList<>() : new ArrayList<>(groups),
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);

    newState.groups.add(group);
    return newState;
  }

  public boolean isUseNormalisedPath() {
    return useNormalisedPath;
  }

  RouteState setUseNormalisedPath(boolean useNormalisedPath) {
    return new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);
  }

  public Set<String> getNamedGroupsInRegex() {
    return namedGroupsInRegex;
  }

  RouteState setNamedGroupsInRegex(Set<String> namedGroupsInRegex) {
    return new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);
  }

  RouteState addNamedGroupInRegex(String namedGroupInRegex) {
    RouteState newState = new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex == null ? new HashSet<>() : new HashSet<>(this.namedGroupsInRegex),
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);

    newState.namedGroupsInRegex.add(namedGroupInRegex);
    return newState;
  }

  public Pattern getVirtualHostPattern() {
    return virtualHostPattern;
  }

  RouteState setVirtualHostPattern(Pattern virtualHostPattern) {
    return new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      this.exactPath);
  }

  public boolean isPathEndsWithSlash() {
    return pathEndsWithSlash;
  }

  RouteState setPathEndsWithSlash(boolean pathEndsWithSlash) {
    return new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      pathEndsWithSlash,
      this.exclusive,
      this.exactPath);
  }

  public boolean isExclusive() {
    return exclusive;
  }

  RouteState setExclusive(boolean exclusive) {
    return new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      exclusive,
      this.exactPath);
  }

  public boolean isExactPath() {
    return exactPath;
  }

  RouteState setExactPath(boolean exactPath) {
    return new RouteState(
      this.route,
      this.path,
      this.order,
      this.enabled,
      this.methods,
      this.consumes,
      this.emptyBodyPermittedWithConsumes,
      this.produces,
      this.contextHandlers,
      this.failureHandlers,
      this.added,
      this.pattern,
      this.groups,
      this.useNormalisedPath,
      this.namedGroupsInRegex,
      this.virtualHostPattern,
      this.pathEndsWithSlash,
      this.exclusive,
      exactPath);
  }

  private static <T> boolean contains(Collection<T> collection, T value) {
    return collection != null && !collection.isEmpty() && collection.contains(value);
  }

  private static <T> boolean isEmpty(Collection<T> collection) {
    return collection == null || collection.isEmpty();
  }

  /**
   * @return 0 if route matches, otherwise it return the status code
   */
  public int matches(RoutingContextImplBase context, String mountPoint, boolean failure) {

    if (failure && !hasNextFailureHandler(context) || !failure && !hasNextContextHandler(context)) {
      return 404;
    }
    if (!enabled) {
      return 404;
    }
    HttpServerRequest request = context.request();
    if (path != null && pattern == null && !pathMatches(mountPoint, context)) {
      return 404;
    }
    if (pattern != null) {
      String path = useNormalisedPath ? context.normalisedPath() : context.request().path();
      if (mountPoint != null) {
        path = path.substring(mountPoint.length());
      }

      Matcher m = pattern.matcher(path);
      if (m.matches()) {

        if (!isEmpty(methods) && !contains(methods, request.method())) {
          // If I'm here path or path pattern matches, but the method is wrong
          return 405;
        }

        context.matchRest = -1;
        context.matchNormalized = useNormalisedPath;

        if (m.groupCount() > 0) {
          if (!exactPath) {
            context.matchRest = m.start("rest");
          }

          if (!isEmpty(groups)) {
            // Pattern - named params
            // decode the path as it could contain escaped chars.
            for (int i = 0; i < Math.min(groups.size(), m.groupCount()); i++) {
              final String k = groups.get(i);
              String undecodedValue;
              // We try to take value in three ways:
              // 1. group name of type p0, p1, pN (most frequent and used by vertx params)
              // 2. group name inside the regex
              // 3. No group name
              try {
                undecodedValue = m.group("p" + i);
              } catch (IllegalArgumentException e) {
                try {
                  undecodedValue = m.group(k);
                } catch (IllegalArgumentException e1) {
                  // Groups starts from 1 (0 group is total match)
                  undecodedValue = m.group(i + 1);
                }
              }
              addPathParam(context, k, undecodedValue);
            }
          } else {
            // Straight regex - un-named params
            // decode the path as it could contain escaped chars.
            if (!isEmpty(namedGroupsInRegex)) {
              for (String namedGroup : namedGroupsInRegex) {
                String namedGroupValue = m.group(namedGroup);
                if (namedGroupValue != null) {
                  addPathParam(context, namedGroup, namedGroupValue);
                }
              }
            }
            for (int i = 0; i < m.groupCount(); i++) {
              String group = m.group(i + 1);
              if (group != null) {
                final String k = "param" + i;
                addPathParam(context, k, group);
              }
            }
          }
        }
      } else {
        return 404;
      }
    } else {
      // no pattern check for wrong method
      if (!isEmpty(methods) && !contains(methods, request.method())) {
        // If I'm here path or path pattern matches, but the method is wrong
        return 405;
      }
    }

    if (!isEmpty(consumes)) {
      // Can this route consume the specified content type
      MIMEHeader contentType = context.parsedHeaders().contentType();
      MIMEHeader consumal = contentType.findMatchedBy(consumes);
      if (consumal == null && !(contentType.rawValue().isEmpty() && emptyBodyPermittedWithConsumes)) {
        if (contentType.rawValue().isEmpty()) {
          return 400;
        } else {
          return 415;
        }
      }
    }
    List<MIMEHeader> acceptableTypes = context.parsedHeaders().accept();
    if (!isEmpty(produces) && !acceptableTypes.isEmpty()) {
      MIMEHeader selectedAccept = context.parsedHeaders().findBestUserAcceptedIn(acceptableTypes, produces);
      if (selectedAccept != null) {
        context.setAcceptableContentType(selectedAccept.rawValue());
      } else {
        return 406;
      }
    }
    if (!virtualHostMatches(context.request.host())) {
      return 404;
    }
    return 0;
  }

  private boolean pathMatches(String mountPoint, RoutingContext ctx) {
    String thePath = mountPoint == null ? path : mountPoint + path;
    String requestPath;

    if (useNormalisedPath) {
      // never null
      requestPath = ctx.normalisedPath();
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
      if (pathEndsWithSlash) {
        if (requestPath.charAt(requestPath.length() - 1) == '/') {
          if (requestPath.equals(thePath)) {
            return true;
          }
        } else {
          if (thePath.regionMatches(0, requestPath, 0, thePath.length())) {
            return true;
          }
        }
      }
      return requestPath.startsWith(thePath);
    }
  }

  private boolean virtualHostMatches(String host) {
    if (virtualHostPattern == null) return true;
    boolean match = false;
    for (String h : host.split(":")) {
      if (virtualHostPattern.matcher(h).matches()) {
        match = true;
        break;
      }
    }
    return match;
  }

  private boolean pathMatchesExact(String path1, String path2) {
    // Ignore trailing slash when matching paths
    final int idx1 = path1.length() - 1;
    return pathEndsWithSlash ?
      (path1.charAt(idx1) == '/' ? path1.equals(path2) : path2.regionMatches(0, path1, 0, path1.length()))
      : (path1.charAt(idx1) != '/' ? path1.equals(path2) : path1.regionMatches(0, path2, 0, path2.length()));
  }

  private void addPathParam(RoutingContext context, String name, String value) {
    HttpServerRequest request = context.request();
    final String decodedValue = URIDecoder.decodeURIComponent(value, false);
    if (!request.params().contains(name)) {
      request.params().add(name, decodedValue);
    }
    context.pathParams().put(name, decodedValue);
  }

  boolean hasNextContextHandler(RoutingContextImplBase context) {
    return context.currentRouteNextHandlerIndex() < getContextHandlersLength();
  }

  boolean hasNextFailureHandler(RoutingContextImplBase context) {
    return context.currentRouteNextFailureHandlerIndex() < getFailureHandlersLength();
  }

  void handleContext(RoutingContextImplBase context) {
    contextHandlers
      .get(context.currentRouteNextHandlerIndex() - 1)
      .handle(context);
  }

  void handleFailure(RoutingContextImplBase context) {
    failureHandlers
      .get(context.currentRouteNextFailureHandlerIndex() - 1)
      .handle(context);
  }

  @Override
  public String toString() {
    return "RouteState{" +
      "path='" + path + '\'' +
      ", order=" + order +
      ", enabled=" + enabled +
      ", methods=" + methods +
      ", consumes=" + consumes +
      ", emptyBodyPermittedWithConsumes=" + emptyBodyPermittedWithConsumes +
      ", produces=" + produces +
      ", contextHandlers=" + contextHandlers +
      ", failureHandlers=" + failureHandlers +
      ", added=" + added +
      ", pattern=" + pattern +
      ", groups=" + groups +
      ", useNormalisedPath=" + useNormalisedPath +
      ", namedGroupsInRegex=" + namedGroupsInRegex +
      ", virtualHostPattern=" + virtualHostPattern +
      ", pathEndsWithSlash=" + pathEndsWithSlash +
      ", exclusive=" + exclusive +
      ", exactPath=" + exactPath +
      '}';
  }
}
