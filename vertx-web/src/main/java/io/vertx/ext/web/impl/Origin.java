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

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

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

  private static final Logger LOG = LoggerFactory.getLogger(Origin.class);

  private static final String DEFAULT_FTP_PORT = "21";
  private static final String DEFAULT_HTTP_PORT = "80";
  private static final String DEFAULT_HTTPS_PORT = "443";

  private final String protocol;
  private final String host;
  private final int port;
  private final String resource;
  private final boolean isNull;

  // internal
  private final String base;
  private final String BASE;
  private final String optional;

  private Origin(String protocol, String host, String port, String resource) {

    if (protocol == null && host == null && port == null && resource == null) {
      this.protocol = null;
      this.host = null;
      this.port = -1;
      this.resource = null;
      isNull = true;
      this.base = null;
      this.BASE = null;
      this.optional = null;
      return;
    } else {
      isNull = false;
    }

    String defaultPort;
    if (protocol == null) {
      throw new IllegalStateException("Unsupported protocol: null");
    }
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
      case "chrome-extension":
      case "moz-extension":
        this.protocol = protocol;
        defaultPort = "-1";
        break;
      default:
        throw new IllegalStateException("Unsupported protocol: " + protocol);
    }
    if (host == null) {
      throw new IllegalStateException("Null host not allowed");
    }
    if ("chrome-extension".equals(protocol)) {
      if (!isValidChromeExtensionId(host, 0)) {
        throw new IllegalStateException("Illegal Chrome Extension id: " + host);
      }
    } else if ("moz-extension".equals(protocol)) {
      if (!isValidMozExtensionId(host, 0)) {
        throw new IllegalStateException("Illegal Moz Extension id: " + host);
      }
    } else {
      // hosts are either domain names, dot separated or ipv6 like
      // https://tools.ietf.org/html/rfc1123
      boolean ipv6 = false;
      for (int i = 0; i < host.length(); i++) {
        char c = host.charAt(i);
        switch (c) {
          case '[':
            if (i == 0) {
              ipv6 = true;
            } else {
              throw new IllegalStateException("Illegal character in hostname: " + host);
            }
            break;
          case ']':
            if (!ipv6 || i != host.length() - 1) {
              throw new IllegalStateException("Illegal character in hostname: " + host);
            }
            break;
          case ':':
            if (!ipv6) {
              throw new IllegalStateException("Illegal character in hostname: " + host);
            }
            break;
          default:
            if (!Character.isLetterOrDigit(c) && c != '.' && c != '-') {
              throw new IllegalStateException("Illegal character in hostname: " + host);
            }
            break;
        }
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

    if (text.length() == 4) {
      if ("null".equals(text)) {
        return new Origin(null, null, null, null);
      }
    }

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
    if (text.length() == 4) {
      if ("null".equals(text)) {
        return true;
      }
    }

    int sep0 = text.indexOf("://");

    if (sep0 > 0) {
      // there is a protocol
      String protocol = text.substring(0, sep0);

      switch (protocol.toLowerCase()) {
        case "ftp":
        case "http":
        case "https":
          break;
        case "chrome-extension":
          return isValidChromeExtensionId(text, sep0 + 3);
        case "moz-extension":
          return isValidMozExtensionId(text, sep0 + 3);
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

  private static boolean isValidChromeExtensionId(String text, int offset) {
    // Chrome extensions IDs are 32 chars long strings
    boolean valid = (text.length() - offset == 32);
    for (int i = offset; valid && i < text.length(); i++) {
      char c = text.charAt(i);
      // Chrome extensions IDs contain chars from 'a' to 'p'
      valid = c >= 'a' && c <= 'p';
    }
    return valid;
  }

  private static boolean isValidMozExtensionId(String text, int offset) {
    boolean valid = text.length() - offset == 36;
    for (int i = offset, pos = 0; valid && i < text.length(); i++, pos++) {
      char c = text.charAt(i);
      if (pos == 8 || pos == 13 || pos == 18 || pos == 23) {
        valid = c == '-';
      } else {
        valid = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z');
      }
    }
    return valid;
  }

  private static boolean check(String host, String port) {
    if (host == null) {
      return false;
    }
    // hosts are either domain names, dot separated or ipv6 like
    // https://tools.ietf.org/html/rfc1123
    boolean ipv6 = false;
    for (int i = 0; i < host.length(); i++) {
      char c = host.charAt(i);
      switch (c) {
        case '[':
          if (i == 0) {
            ipv6 = true;
          } else {
            return false;
          }
          break;
        case ']':
          if (!ipv6 || i != host.length() - 1) {
            return false;
          }
          break;
        case ':':
          if (!ipv6) {
            return false;
          }
          break;
        default:
          if (!Character.isLetterOrDigit(c) && c != '.' && c != '-') {
            return false;
          }
          break;
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

    if (isNull) {
      return "null".equals(other);
    }

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

  public @Nullable String encode() {
    if (isNull) {
      return "<null>";
    }

    switch (protocol) {
      case "http":
        return protocol + "://" + host + (port == 80 ? "" : ":" + port);
      case "https":
        return protocol + "://" + host + (port == 443 ? "" : ":" + port);
      case "ftp":
        return protocol + "://" + host + (port == 21 ? "" : ":" + port);
      default:
        return null;
    }
  }

  @Override
  public String toString() {
    if (isNull) {
      return "null";
    }

    return base;
  }

  /**
   * An hyperlink representation of this origin. Like on web browsers.
   */
  public String href() {
    if (isNull) {
      return "null";
    }

    return base + (resource == null ? "/" : resource);
  }

  /**
   * Check if a string is null or empty (including containing only spaces)
   *
   * @param s Source string
   * @return TRUE if source string is null or empty (including containing only spaces)
   */
  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

  public static boolean check(Origin origin, RoutingContext ctx) {
    /* Verifying Same Origin with Standard Headers */
    if (origin != null) {
      //Try to get the source from the "Origin" header
      String source = ctx.request().getHeader(HttpHeaders.ORIGIN);
      if (isBlank(source)) {
        //If empty then fallback on "Referer" header
        source = ctx.request().getHeader(HttpHeaders.REFERER);
        //If this one is empty too then we trace the event and we block the request (recommendation of the article)...
        if (isBlank(source)) {
          LOG.trace("ORIGIN and REFERER request headers are both absent/empty");
          return false;
        }
      }

      //Compare the source against the expected target origin
      if (!origin.sameOrigin(source)) {
        //One the part do not match, so we trace the event and we block the request
        LOG.trace("Protocol/Host/Port do not fully match");
        return false;
      }
    }
    // no configured origin or origin is valid
    return true;
  }
}
