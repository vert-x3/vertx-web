/*
 * Copyright (c) 2011-2022 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.client.impl;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

class ClientUri {
  final String protocol;
  final Boolean ssl;
  final int port;
  final String host;
  final String uri;

  private ClientUri(String protocol, boolean ssl, int port, String host, String uri) {
    this.protocol = protocol;
    this.ssl = ssl;
    this.port = port;
    this.host = host;
    this.uri = uri;
  }

  static ClientUri parse(String suri) throws URISyntaxException, MalformedURLException {
    URL url = new URL(suri);
    boolean ssl = false;
    int port = url.getPort();
    String protocol = url.getProtocol();
    if ("ftp".equals(protocol)) {
      if (port == -1) {
        port = 21;
      }
    } else {
      char chend = protocol.charAt(protocol.length() - 1);
      if (chend == 'p') {
        if (port == -1) {
          port = 80;
        }
      } else if (chend == 's'){
        ssl = true;
        if (port == -1) {
          port = 443;
        }
      }
    }
    String file = url.getFile();
    String host = url.getHost();
    return new ClientUri(protocol, ssl, port, host, file);
  }
}
