/*
 * Copyright 2024 Red Hat, Inc.
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

package io.vertx.ext.web.handler.impl;

import io.vertx.ext.web.handler.CSPHandler;
import io.vertx.ext.web.handler.CSPHandlerBuilder;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CSPHandlerBuilderImpl implements CSPHandlerBuilder {

  private static final List<String> MUST_BE_QUOTED = Arrays.asList(
    "none",
    "self",
    "unsafe-inline",
    "unsafe-eval"
  );

  private final Map<String, String> policy;
  private boolean reportOnly;

  public CSPHandlerBuilderImpl() {
    policy = new LinkedHashMap<>();
    addDirective("default-src", "self");
  }

  @Override
  public CSPHandlerBuilder setDirective(String name, String value) {
    if (name == null) {
      throw new IllegalArgumentException("name cannot be null");
    }

    if (value == null) {
      policy.remove(name);
    }

    if (MUST_BE_QUOTED.contains(value)) {
      // these policies are special, they must be quoted
      value = "'" + value + "'";
    }

    policy.put(name, value);

    return this;
  }

  @Override
  public CSPHandlerBuilder addDirective(String name, String value) {
    if (name == null) {
      throw new IllegalArgumentException("name cannot be null");
    }

    if (value == null) {
      policy.remove(name);
    }

    if (MUST_BE_QUOTED.contains(value)) {
      // these policies are special, they must be quoted
      value = "'" + value + "'";
    }

    String previous = policy.get(name);
    if (previous == null || previous.isEmpty()) {
      policy.put(name, value);
    } else {
      policy.put(name, previous + " " + value);
    }

    return this;
  }

  @Override
  public CSPHandlerBuilder reportOnly(boolean reportOnly) {
    this.reportOnly = reportOnly;
    return this;
  }

  @Override
  public CSPHandler build() {
    if (reportOnly && !policy.containsKey("report-uri") && !policy.containsKey("report-to")) {
      throw new IllegalArgumentException("Please disable CSP reportOnly or add a report-uri/report-to policy.");
    }
    return new CSPHandlerImpl(getPolicyString(), reportOnly);
  }

  private String getPolicyString() {
    StringBuilder buffer = new StringBuilder();

    for (Map.Entry<String, String> entry : policy.entrySet()) {
      if (buffer.length() > 0) {
        buffer.append("; ");
      }
      buffer
        .append(entry.getKey())
        .append(' ')
        .append(entry.getValue());
    }

    return buffer.toString();
  }
}
