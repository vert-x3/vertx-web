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

// This code was Heavily influenced from spring forward header parser
// https://github.com/spring-projects/spring-framework/blob/master/spring-web/src/main/java/org/springframework/web/util/UriComponentsBuilder.java#L849

package io.vertx.ext.web.impl;

import io.netty.util.AsciiString;
import io.netty.util.NetUtil;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.core.net.HostAndPort;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.AllowForwardHeaders;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ForwardedParser {
  private static final Logger LOG = LoggerFactory.getLogger(RouterImpl.class);

  private static final String HTTP_SCHEME = "http";
  private static final String HTTPS_SCHEME = "https";
  private static final AsciiString FORWARDED = AsciiString.cached("Forwarded");
  private static final AsciiString X_FORWARDED_SSL = AsciiString.cached("X-Forwarded-Ssl");
  private static final AsciiString X_FORWARDED_PROTO = AsciiString.cached("X-Forwarded-Proto");
  private static final AsciiString X_FORWARDED_HOST = AsciiString.cached("X-Forwarded-Host");
  private static final AsciiString X_FORWARDED_PORT = AsciiString.cached("X-Forwarded-Port");
  private static final AsciiString X_FORWARDED_FOR = AsciiString.cached("X-Forwarded-For");

  private static final Pattern FORWARDED_HOST_PATTERN = Pattern.compile("host=\"?([^;,\"]+)\"?", Pattern.CASE_INSENSITIVE);
  private static final Pattern FORWARDED_PROTO_PATTERN = Pattern.compile("proto=\"?([^;,\"]+)\"?", Pattern.CASE_INSENSITIVE);
  private static final Pattern FORWARDED_FOR_PATTERN = Pattern.compile("for=\"?([^;,\"]+)\"?", Pattern.CASE_INSENSITIVE);

  private final HttpServerRequest delegate;
  private final AllowForwardHeaders allowForward;

  private boolean calculated;
  private HostAndPort authority;
  private String host;
  private int port = -1;
  private String scheme;
  private String absoluteURI;
  private SocketAddress remoteAddress;

  ForwardedParser(HttpServerRequest delegate, AllowForwardHeaders allowForward) {
    this.delegate = delegate;
    this.allowForward = allowForward;
  }

  public String scheme() {
    if (!calculated)
      calculate();
    return scheme;
  }

  HostAndPort authority() {
    if (!calculated) {
      calculate();
    }
    return authority;
  }

  boolean isSSL() {
    if (!calculated)
      calculate();

    return scheme.equals(HTTPS_SCHEME);
  }

  String absoluteURI() {
    if (!calculated)
      calculate();

    return absoluteURI;
  }

  SocketAddress remoteAddress() {
    if (!calculated)
      calculate();

    return remoteAddress;
  }

  /**
   * Parses the current {@code Forward} header if present.
   */
  private void calculate() {
    calculated = true;
    remoteAddress = delegate.remoteAddress();
    scheme = delegate.scheme();

    HostAndPort authority = delegate.authority();
    if (authority != null) {
      setHostAndPort(authority);
    }

    switch (allowForward) {
      case X_FORWARD:
        calculateXForward();
        break;
      case FORWARD:
        calculateForward();
        break;
      case ALL:
        calculateXForward();
        calculateForward();
        break;
      case NONE:
      default:
        break;
    }

    if (((scheme.equalsIgnoreCase(HTTP_SCHEME) && port == 80) || (scheme.equalsIgnoreCase(HTTPS_SCHEME) && port == 443))) {
      port = -1;
    }

    if (host != null) {
      this.authority = HostAndPort.create(host, port);
      host = host + (port >= 0 ? ":" + port : "");

      String uri = delegate.uri();
      if (uri.startsWith(scheme) && uri.startsWith("://", scheme.length())) {
        // URI is already absolute, only append the path and query
        uri = delegate.path();
        if (delegate.query() != null) {
          uri += "?" + delegate.query();
        }
      }
      absoluteURI = scheme + "://" + host + uri;
    }
  }

  private void calculateForward() {
    String forwardedToUse = firstElement(delegate.getHeader(FORWARDED));
    if (forwardedToUse != null) {
      String forwardedProto = forwardedValue(FORWARDED_PROTO_PATTERN, forwardedToUse);
      if (forwardedProto != null) {
        scheme = forwardedProto;
        port = -1;
      }

      String forwardedHost = forwardedValue(FORWARDED_HOST_PATTERN, forwardedToUse);
      if (forwardedHost != null) {
        setHostAndPort(HostAndPort.parseAuthority(forwardedHost, -1));
      }

      String forwardedFor = forwardedValue(FORWARDED_FOR_PATTERN, forwardedToUse);
      if (forwardedFor != null) {
        remoteAddress = parseFor(forwardedFor);
      }
    }
  }

  /**
   * Find the value of a {@code Forwarded} parameter.
   *
   * @return the value, {@code null} when the parameter is missing or blank
   */
  private static String forwardedValue(Pattern parameter, String forwarded) {
    Matcher matcher = parameter.matcher(forwarded);
    if (matcher.find()) {
      String value = matcher.group(1).trim();
      if (!value.isEmpty()) {
        return value;
      }
    }
    return null;
  }

  private void calculateXForward() {
    String forwardedSsl = delegate.getHeader(X_FORWARDED_SSL);
    boolean isForwardedSslOn = forwardedSsl != null && forwardedSsl.equalsIgnoreCase("on");

    String protocolHeader = firstElement(delegate.getHeader(X_FORWARDED_PROTO));
    if (protocolHeader != null) {
      scheme = protocolHeader;
      port = -1;
    } else if (isForwardedSslOn) {
      scheme = HTTPS_SCHEME;
      port = -1;
    }

    String hostHeader = firstElement(delegate.getHeader(X_FORWARDED_HOST));
    if (hostHeader != null) {
      setHostAndPort(HostAndPort.parseAuthority(hostHeader, -1));
    }

    String portHeader = firstElement(delegate.getHeader(X_FORWARDED_PORT));
    if (portHeader != null) {
      port = parsePort(portHeader, port);
    }

    String forHeader = firstElement(delegate.getHeader(X_FORWARDED_FOR));
    if (forHeader != null) {
      remoteAddress = parseFor(forHeader);
    }
  }

  /**
   * Find the first element of a comma separated header value.
   *
   * @return the element, {@code null} when the header is missing or the element is blank
   */
  private static String firstElement(String value) {
    if (value == null) {
      return null;
    }
    int idx = value.indexOf(',');
    String element = (idx == -1 ? value : value.substring(0, idx)).trim();
    return element.isEmpty() ? null : element;
  }

  private void setHostAndPort(HostAndPort authority) {
    if (authority == null) {
      LOG.error("Failed to parse a host from \"forwarded\"-type headers.");
      return;
    }
    host = authority.host();
    port = authority.port();
  }

  private SocketAddress parseFor(String forToParse) {
    // the remote address of a server bound on a domain socket is null or has no port
    int defaultPort = remoteAddress != null && remoteAddress.isInetSocket() ? remoteAddress.port() : 0;
    String host = forToParse;
    int port = defaultPort;
    if (forToParse.length() > 0 && forToParse.charAt(0) == '[') {
      int idx = forToParse.lastIndexOf("]");
      if (idx > 0) {
        int portSeparatorIdx = forToParse.indexOf(':', idx + 1);
        if (portSeparatorIdx > 0) {
          host = forToParse.substring(0, idx + 1);
          port = parsePort(forToParse.substring(idx + 2), defaultPort);
        }
      }
    } else {
      if (!NetUtil.isValidIpV6Address(forToParse)) {
        int portSeparatorIdx = forToParse.lastIndexOf(':');
        if (portSeparatorIdx > 0) {
          host = forToParse.substring(0, portSeparatorIdx);
          port = parsePort(forToParse.substring(portSeparatorIdx + 1), defaultPort);
        }
      }
    }
    return SocketAddress.inetSocketAddress(port, host);
  }


  private int parsePort(String portToParse, int defaultPort) {
    try {
      int parsed = Integer.parseInt(portToParse);
      if (parsed >= 0 && parsed <= 65535) {
        return parsed;
      }
    } catch (NumberFormatException ignored) {
      // reported below
    }
    LOG.error("Failed to parse a port from \"forwarded\"-type headers.");
    return defaultPort;
  }
}
