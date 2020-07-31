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
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.ext.web.AllowForwardHeaders;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ForwardedParser {
  private static final Logger log = LoggerFactory.getLogger(RouterImpl.class);

  private static final String HTTP_SCHEME = "http";
  private static final String HTTPS_SCHEME = "https";
  private static final AsciiString FORWARDED = AsciiString.cached("Forwarded");
  private static final AsciiString X_FORWARDED_SSL = AsciiString.cached("X-Forwarded-Ssl");
  private static final AsciiString X_FORWARDED_PROTO = AsciiString.cached("X-Forwarded-Proto");
  private static final AsciiString X_FORWARDED_HOST = AsciiString.cached("X-Forwarded-Host");
  private static final AsciiString X_FORWARDED_PORT = AsciiString.cached("X-Forwarded-Port");
  private static final AsciiString X_FORWARDED_FOR = AsciiString.cached("X-Forwarded-For");

  private static final Pattern FORWARDED_HOST_PATTERN = Pattern.compile("host=\"?([^;,\"]+)\"?");
  private static final Pattern FORWARDED_PROTO_PATTERN = Pattern.compile("proto=\"?([^;,\"]+)\"?");
  private static final Pattern FORWARDED_FOR_PATTERN = Pattern.compile("for=\"?([^;,\"]+)\"?");

  private final HttpServerRequest delegate;
  private final AllowForwardHeaders allowForward;

  private boolean calculated;
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

  String host() {
    if (!calculated)
      calculate();
    return host;
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
    setHostAndPort(delegate.host(), port);

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

    host = host + (port >= 0 ? ":" + port : "");
    absoluteURI = scheme + "://" + host + delegate.uri();
  }

  private void calculateForward() {
    String forwarded = delegate.getHeader(FORWARDED);
    if (forwarded != null) {
      String forwardedToUse = forwarded.split(",")[0];
      Matcher matcher = FORWARDED_PROTO_PATTERN.matcher(forwardedToUse);
      if (matcher.find()) {
        scheme = (matcher.group(1).trim());
        port = -1;
      }

      matcher = FORWARDED_HOST_PATTERN.matcher(forwardedToUse);
      if (matcher.find()) {
        setHostAndPort(matcher.group(1).trim(), port);
      }

      matcher = FORWARDED_FOR_PATTERN.matcher(forwardedToUse);
      if (matcher.find()) {
        remoteAddress = parseFor(matcher.group(1).trim(), remoteAddress.port());
      }
    }
  }

  private void calculateXForward() {
    String forwardedSsl = delegate.getHeader(X_FORWARDED_SSL);
    boolean isForwardedSslOn = forwardedSsl != null && forwardedSsl.equalsIgnoreCase("on");

    String protocolHeader = delegate.getHeader(X_FORWARDED_PROTO);
    if (protocolHeader != null) {
      scheme = protocolHeader.split(",")[0];
      port = -1;
    } else if (isForwardedSslOn) {
      scheme = HTTPS_SCHEME;
      port = -1;
    }

    String hostHeader = delegate.getHeader(X_FORWARDED_HOST);
    if (hostHeader != null) {
      setHostAndPort(hostHeader.split(",")[0], port);
    }

    String portHeader = delegate.getHeader(X_FORWARDED_PORT);
    if (portHeader != null) {
      port = parsePort(portHeader.split(",")[0], port);
    }

    String forHeader = delegate.getHeader(X_FORWARDED_FOR);
    if (forHeader != null) {
      remoteAddress = parseFor(forHeader.split(",")[0], remoteAddress.port());
    }
  }

  private void setHostAndPort(String hostToParse, int defaultPort) {
    if (hostToParse == null) {
      // no header is provided
      host = null;
      port = -1;
    } else {
      int portSeparatorIdx = hostToParse.lastIndexOf(':');
      if (portSeparatorIdx > hostToParse.lastIndexOf(']')) {
        host = hostToParse.substring(0, portSeparatorIdx);
        port = parsePort(hostToParse.substring(portSeparatorIdx + 1), defaultPort);
      } else {
        host = hostToParse;
        port = -1;
      }
    }
  }

  private SocketAddress parseFor(String forToParse, int defaultPort) {
    String host = forToParse;
    int port = defaultPort;
    int portSeparatorIdx = forToParse.lastIndexOf(':');
    if (portSeparatorIdx > forToParse.lastIndexOf(']')) {
      host = forToParse.substring(0, portSeparatorIdx);
      port = parsePort(forToParse.substring(portSeparatorIdx + 1), defaultPort);
    }

    return new SocketAddressImpl(port, host);
  }


  private int parsePort(String portToParse, int defaultPort) {
    try {
      return Integer.parseInt(portToParse);
    } catch (NumberFormatException ignored) {
      log.error("Failed to parse a port from \"forwarded\"-type headers.");
      return defaultPort;
    }
  }
}
