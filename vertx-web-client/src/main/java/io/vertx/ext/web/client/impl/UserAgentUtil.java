/*
 * Copyright 2017 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.ext.web.client.impl;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Thomas Segismont
 */
public class UserAgentUtil {
  private static final Logger log = LoggerFactory.getLogger(HttpRequestImpl.class);

  public static String loadUserAgent() {
    StringBuilder userAgent = new StringBuilder("Vert.x-WebClient");
    URL url = HttpRequestImpl.class.getClassLoader().getResource("vertx-web-client-version.txt");
    if (url == null) {
      log.warn("Failed to load Web Client version");
    } else {
      try {
        Path path = Paths.get(url.toURI());
        String version = Files.readAllLines(path, Charset.defaultCharset()).get(0);
        userAgent.append("/").append(version);
      } catch (Exception e) {
        log.warn("Failed to determine Web Client version", e);
      }
    }
    return userAgent.toString();
  }

  private UserAgentUtil() {
    // Utility class
  }
}
