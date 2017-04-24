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
package io.vertx.ext.web.handler.impl;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.VirtualHostHandler;

import java.util.regex.Pattern;

/**
 * @author <a href="http://plopes@redhat.com">Paulo Lopes</a>
 */
public class VirtualHostHandlerImpl implements VirtualHostHandler {

  private final Pattern regex;
  private final Handler<RoutingContext> handler;

  public VirtualHostHandlerImpl(String hostname, Handler<RoutingContext> handler) {
    this.handler = handler;
    this.regex = Pattern.compile("^" + hostname.replaceAll("\\.", "\\\\.").replaceAll("[*]", "(.*?)") + "$", Pattern.CASE_INSENSITIVE);
  }

  @Override
  public void handle(RoutingContext ctx) {
    String host = ctx.request().host();
    if (host == null) {
      ctx.next();
    } else {
      boolean match = false;
      for (String h : host.split(":")) {
        if (regex.matcher(h).matches()) {
          match = true;
          break;
        }
      }

      if (match) {
        handler.handle(ctx);
      } else {
        ctx.next();
      }
    }
  }
}
