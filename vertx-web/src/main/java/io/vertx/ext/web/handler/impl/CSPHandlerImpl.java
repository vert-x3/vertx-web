package io.vertx.ext.web.handler.impl;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CSPHandler;
import io.vertx.ext.web.handler.HttpException;

import java.util.*;

public class CSPHandlerImpl implements CSPHandler {

  private static final List<String> MUST_BE_QUOTED = Arrays.asList(
    "none",
    "self",
    "unsafe-inline",
    "unsafe-eval"
  );

  private final Map<String, String> policy = new LinkedHashMap<>();
  // cache the computed policy
  private String policyString;

  private boolean reportOnly;

  public CSPHandlerImpl() {
    addDirective("default-src", "self");
  }

  @Override
  public synchronized CSPHandler setDirective(String name, String value) {
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

    // invalidate cache
    policyString = null;
    return this;
  }

  @Override
  public CSPHandler addDirective(String name, String value) {
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
    if (previous == null || "".equals(previous)) {
      policy.put(name, value);
    } else {
      policy.put(name, previous + " " + value);
    }

    // invalidate cache
    policyString = null;
    return this;
  }

  @Override
  public CSPHandler setReportOnly(boolean reportOnly) {
    this.reportOnly = reportOnly;
    return this;
  }

  private String getPolicyString() {
    if (policyString == null) {
      final StringBuilder buffer = new StringBuilder();

      for (Map.Entry<String, String> entry : policy.entrySet()) {
        if (buffer.length() > 0) {
          buffer.append("; ");
        }
        buffer
          .append(entry.getKey())
          .append(' ')
          .append(entry.getValue());
      }

      policyString = buffer.toString();
    }

    return policyString;
  }

    @Override
  public void handle(RoutingContext ctx) {

    final String policyString = getPolicyString();

    if (reportOnly) {
      // add support for 'report-to'
      if (!policy.containsKey("report-uri") || !policy.containsKey("report-to")) {
        ctx.fail(new HttpException(500, "Please disable CSP reportOnly or add a report-uri/report-to policy."));
      } else {
        ctx.response()
          .putHeader("Content-Security-Policy-Report-Only", policyString);
        ctx.next();
      }
    } else {
      ctx.response()
        .putHeader("Content-Security-Policy", policyString);
      ctx.next();
    }
  }
}
