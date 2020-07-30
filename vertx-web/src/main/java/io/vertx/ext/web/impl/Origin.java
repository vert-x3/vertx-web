/*
 * Copyright 2020 Red Hat, Inc.
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

/**
 * An origin follows rfc6454#section-7
 * and is expected to have the format: {@code <scheme> "://" <hostname> [ ":" <port> ]}
 * <p>
 * This class allows parsing of web urls and match against http headers that require such
 * validation.
 *
 * @author Paulo Lopes
 */
public final class Origin {

  private static final String DEFAULT_FTP_PORT = "21";
  private static final String DEFAULT_HTTP_PORT = "80";
  private static final String DEFAULT_HTTPS_PORT = "443";

  private final String protocol;
  private final String host;
  private final int port;
  private final String resource;

  // internal
  private final String base;
  private final String BASE;
  private final String optional;

  private Origin(String protocol, String host, String port, String resource) {
    String defaultPort;
    switch (protocol.toLowerCase()) {
      case "ftp":
        this.protocol = protocol;
        defaultPort = DEFAULT_FTP_PORT;
        break;
      case "http":
        this.protocol = protocol;
        defaultPort = DEFAULT_HTTP_PORT;
        break;
      case "https":
        this.protocol = protocol;
        defaultPort = DEFAULT_HTTPS_PORT;
        break;
      default:
        throw new IllegalStateException("Unsupported protocol: " + protocol);
    }
    if (host == null) {
      throw new IllegalStateException("Null host not allowed");
    }
    // hosts are either domain names, dot seperated or ipv6 like
    for (int i = 0; i < host.length(); i++) {
      char c = host.charAt(i);
      if (!Character.isLetterOrDigit(c) && c != '.' && c != ':' && c != '[' && c != ']') {
        throw new IllegalStateException("Illegal character in hostname: " + host);
      }
    }
    this.host = host;

    // port should be numeric
    if (port != null) {
      for (int i = 0; i < port.length(); i++) {
        char c = port.charAt(i);
        if (!Character.isDigit(c)) {
          throw new IllegalStateException("Illegal character in port: " + port);
        }
      }
      this.port = Integer.parseInt(port);
    } else {
      this.port = Integer.parseInt(defaultPort);
    }

    this.resource = resource;

    if (port == null) {
      base = protocol + "://" + host;
      optional = ":" + defaultPort;
    } else {
      base = protocol + "://" + host + ":" + port;
      optional = "";
    }
    BASE = base.toUpperCase();
  }

  public static Origin parse(String text) {

    int sep0 = text.indexOf("://");

    if (sep0 > 0) {
      // there is a protocol
      String protocol = text.substring(0, sep0);

      int sep1 = -1;

      // if sep0 + 3 == [ assume IPV6 address
      if (text.charAt(sep0 + 3) == '[') {
        int endHost = text.indexOf(']', sep0 + 3);
        if (endHost != -1) {
          sep1 = text.indexOf(':', endHost);
        }
      } else {
        sep1 = text.indexOf(':', sep0 + 3);
      }

      int sep2 = text.indexOf('/', Math.max(sep0 + 3, sep1 + 1));

      if (sep1 == -1 && sep2 == -1) {
        // there's just a host
        return new Origin(protocol, text.substring(sep0 + 3), null, null);
      }

      if (sep1 != -1 && sep2 == -1) {
        // there's a host + port
        return new Origin(protocol, text.substring(sep0 + 3, sep1), text.substring(sep1 + 1), null);
      }

      if (sep1 == -1) {
        // there's a host + path
        return new Origin(protocol, text.substring(sep0 + 3, sep2), null, text.substring(sep2));
      }

      // there's a host + port + path
      return new Origin(protocol, text.substring(sep0 + 3, sep1), text.substring(sep1 + 1, sep2), text.substring(sep2));
    }
    // invalid
    throw new IllegalStateException("Invalid Origin, expected <protocol>://<domain>[:<port>][</resource>]");
  }

  /**
   * Checks if the origin header is valid according to:
   * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Origin
   * https://tools.ietf.org/html/rfc6454#section-7
   */
  public static boolean isValid(String text) {
    int sep0 = text.indexOf("://");

    if (sep0 > 0) {
      // there is a protocol
      String protocol = text.substring(0, sep0);

      switch (protocol.toLowerCase()) {
        case "ftp":
        case "http":
        case "https":
          break;
        default:
          return false;
      }

      int sep1 = -1;

      // if sep0 + 3 == [ assume IPV6 address
      if (text.charAt(sep0 + 3) == '[') {
        int endHost = text.indexOf(']', sep0 + 3);
        if (endHost != -1) {
          sep1 = text.indexOf(':', endHost);
        }
      } else {
        sep1 = text.indexOf(':', sep0 + 3);
      }

      int sep2 = text.indexOf('/', Math.max(sep0 + 3, sep1 + 1));

      if (sep1 == -1 && sep2 == -1) {
        // there's just a host
        return check(text.substring(sep0 + 3), null);
      }

      if (sep1 != -1 && sep2 == -1) {
        // there's a host + port
        return check(text.substring(sep0 + 3, sep1), text.substring(sep1 + 1));
      }

      if (sep1 == -1) {
        // there's a host + path
        return check(text.substring(sep0 + 3, sep2), null);
      }

      // there's a host + port + path
      return check(text.substring(sep0 + 3, sep1), text.substring(sep1 + 1, sep2));
    }

    // invalid
    return false;
  }

  private static boolean check(String host, String port) {
    if (host == null) {
      return false;
    }
    // hosts are either domain names, dot seperated or ipv6 like
    for (int i = 0; i < host.length(); i++) {
      char c = host.charAt(i);
      if (!Character.isLetterOrDigit(c) && c != '.' && c != ':' && c != '[' && c != ']') {
        return false;
      }
    }

    // port should be numeric
    if (port != null) {
      for (int i = 0; i < port.length(); i++) {
        char c = port.charAt(i);
        if (!Character.isDigit(c)) {
          return false;
        }
      }
    }

    return true;
  }

  public String protocol() {
    return protocol;
  }

  public String host() {
    return host;
  }

  public int port() {
    return port;
  }

  public String resource() {
    return resource;
  }

  public boolean sameOrigin(String other) {
    // for each char of other
    // if any base chars != other abort
    // if more chars
    // if current char == : and optional > 0
    // if any optionals chars != other abort
    // if current char == /
    // success
    // else
    // fail

    int offset = 0;
    int len = other.length();

    if (base.length() > len) {
      return false;
    }
    for (int i = 0; i < base.length(); i++) {
      char c = other.charAt(offset + i);
      if (c != base.charAt(i) && c != BASE.charAt(i)) {
        return false;
      }
    }
    offset += base.length();
    len -= base.length();
    if (len > 0) {
      if (other.charAt(offset) == ':') {
        if (optional.length() > len) {
          return false;
        }
        for (int i = 0; i < optional.length(); i++) {
          char c = other.charAt(offset + i);
          if (c != optional.charAt(i)) {
            return false;
          }
        }
        offset += optional.length();
        len -= optional.length();
      }
      if (len > 0) {
        return other.charAt(offset) == '/';
      }
    }

    return true;
  }

  @Override
  public String toString() {
    return base;
  }
}
