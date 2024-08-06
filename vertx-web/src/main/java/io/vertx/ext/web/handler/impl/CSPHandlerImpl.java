package io.vertx.ext.web.handler.impl;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CSPHandler;

public class CSPHandlerImpl implements CSPHandler {

  private static final CharSequence CONTENT_SECURITY_POLICY = HttpHeaders.createOptimized("Content-Security-Policy");
  private static final CharSequence CONTENT_SECURITY_POLICY_REPORT_ONLY = HttpHeaders.createOptimized("Content-Security-Policy-Report-Only");

  private final CharSequence policy;
  private final boolean reportOnly;

  public CSPHandlerImpl(String policy, boolean reportOnly) {
    this.policy = HttpHeaders.createOptimized(policy);
    this.reportOnly = reportOnly;
  }

  @Override
  public void handle(RoutingContext ctx) {
    ctx.response().putHeader(reportOnly ? CONTENT_SECURITY_POLICY_REPORT_ONLY : CONTENT_SECURITY_POLICY, policy);
    ctx.next();
  }
}
