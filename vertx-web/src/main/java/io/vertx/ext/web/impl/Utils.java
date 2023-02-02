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

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.HttpUtils;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;


/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public class Utils {

  public static ClassLoader getClassLoader() {
    // try current thread
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    if (cl == null) {
      // try the current class
      cl = Utils.class.getClassLoader();
      if (cl == null) {
        // fall back to a well known object that should alwways exist
        cl = Object.class.getClassLoader();
      }
    }
    return cl;
  }

  private static final ZoneId ZONE_GMT = ZoneId.of("GMT");

  public static String formatRFC1123DateTime(final long time) {
    return DateTimeFormatter.RFC_1123_DATE_TIME.format(Instant.ofEpochMilli(time).atZone(ZONE_GMT));
  }

  public static long parseRFC1123DateTime(final String header) {
    try {
      return header == null || header.isEmpty() ? -1 :
        LocalDateTime.parse(header, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant(ZoneOffset.UTC).toEpochMilli();
    } catch (DateTimeParseException ex) {
      return -1;
    }
  }

  public static String pathOffset(String path, RoutingContext context) {
    final Route route = context.currentRoute();

    // cannot make any assumptions
    if (route == null) {
      return path;
    }

    if (!route.isExactPath()) {
      String rest = context.pathParam("*");
      if (rest != null) {
        // normalize
        if (rest.length() > 0) {
          // remove any attempt to escape the web root and use UNIX style path separators
          rest = HttpUtils.removeDots(rest.replace('\\', '/'));
          if (rest.charAt(0) == '/') {
            return rest;
          } else {
            return "/" + rest;
          }
        } else {
          return "/";
        }
      }
    }
    int prefixLen = 0;
    String mountPoint = context.mountPoint();
    if (mountPoint != null) {
      prefixLen = mountPoint.length();
      // special case we need to verify if a trailing slash  is present and exclude
      if (mountPoint.charAt(mountPoint.length() - 1) == '/') {
        prefixLen--;
      }
    }
    // we can only safely skip the route path if there are no variables or regex
    if (!route.isRegexPath()) {
      String routePath = route.getPath();
      if (routePath != null) {
        prefixLen += routePath.length();
        // special case we need to verify if a trailing slash  is present and exclude
        if (routePath.charAt(routePath.length() - 1) == '/') {
          prefixLen--;
        }
      }
    }
    return prefixLen != 0 ? path.substring(prefixLen) : path;
  }

  public static long secondsFactor(long millis) {
    return millis - (millis % 1000);
  }

  public static boolean isJsonContentType(String contentType) {
    return contentType.contains("application/json") || contentType.contains("+json");
  }

  public static boolean isXMLContentType(String contentType) {
    return contentType.contains("application/xml") || contentType.contains("text/xml") || contentType.contains("+xml");
  }

  public static void addToMapIfAbsent(MultiMap map, CharSequence key, CharSequence value) {
    if (!map.contains(key)) {
      map.set(key, value);
    }
  }

  public static void appendToMapIfAbsent(MultiMap map, CharSequence key, CharSequence sep, CharSequence value) {
    if (!map.contains(key)) {
      map.set(key, value);
    } else {
      String existing = map.get(key);
      map.set(key, existing + sep + value);
    }
  }

  /**
   * RegExp to check for no-cache token in Cache-Control.
   */
  private static final Pattern CACHE_CONTROL_NO_CACHE_REGEXP = Pattern.compile("(?:^|,)\\s*?no-cache\\s*?(?:,|$)");

  public static boolean fresh(RoutingContext ctx) {
    return fresh(ctx, -1);
  }

  public static boolean fresh(RoutingContext ctx, long lastModified) {

    final HttpServerRequest req = ctx.request();
    final HttpServerResponse res = ctx.response();


    // fields
    final String modifiedSince = req.getHeader(HttpHeaders.IF_MODIFIED_SINCE);
    final String noneMatch = req.getHeader(HttpHeaders.IF_NONE_MATCH);

    // unconditional request
    if (modifiedSince == null && noneMatch == null) {
      return false;
    }

    // Always return stale when Cache-Control: no-cache
    // to support end-to-end reload requests
    // https://tools.ietf.org/html/rfc2616#section-14.9.4
    final String cacheControl = req.getHeader(HttpHeaders.CACHE_CONTROL);
    if (cacheControl != null && CACHE_CONTROL_NO_CACHE_REGEXP.matcher(cacheControl).find()) {
      return false;
    }

    // if-none-match
    if (noneMatch != null && !"*".equals(noneMatch)) {
      final String etag = res.headers().get(HttpHeaders.ETAG);

      if (etag == null) {
        return false;
      }

      boolean etagStale = true;

      // lookup etags
      int end = 0;
      int start = 0;

      loop:
      for (int i = 0; i < noneMatch.length(); i++) {
        switch (noneMatch.charAt(i)) {
          case ' ':
            if (start == end) {
              start = end = i + 1;
            }
            break;
          case ',':
            String match = noneMatch.substring(start, end);
            if (match.equals(etag) || match.equals("W/" + etag) || ("W/" + match).equals(etag)) {
              etagStale = false;
              break loop;
            }
            start = end = i + 1;
            break;
          default:
            end = i + 1;
            break;
        }
      }

      if (etagStale) {
        // the parser run out of bytes, need to check if the match is valid
        String match = noneMatch.substring(start, end);
        if (!match.equals(etag) && !match.equals("W/" + etag) && !("W/" + match).equals(etag)) {
          return false;
        }
      }
    }

    // if-modified-since
    if (modifiedSince != null) {
      if (lastModified == -1) {
        // no custom last modified provided, will use the response headers if any
        lastModified = parseRFC1123DateTime(res.headers().get(HttpHeaders.LAST_MODIFIED));
      }

      boolean modifiedStale = lastModified == -1 || !(lastModified <= parseRFC1123DateTime(modifiedSince));

      return !modifiedStale;
    }

    return true;
  }

  public static boolean canUpgradeToWebsocket(HttpServerRequest req) {
    // verify if we can upgrade
    // 1. Connection header contains "Upgrade"
    // 2. Upgrade header is "websocket"
    final MultiMap headers = req.headers();
    if (headers.contains(HttpHeaders.CONNECTION)) {
      for (String connection : headers.getAll(HttpHeaders.CONNECTION)) {
        if (connection.toLowerCase().contains(HttpHeaders.UPGRADE)) {
          if (headers.contains(HttpHeaders.UPGRADE)) {
            for (String upgrade : headers.getAll(HttpHeaders.UPGRADE)) {
              if (upgrade.toLowerCase().contains(HttpHeaders.WEBSOCKET)) {
                return true;
              }
            }
          }
        }
      }
    }

    return false;
  }
}
