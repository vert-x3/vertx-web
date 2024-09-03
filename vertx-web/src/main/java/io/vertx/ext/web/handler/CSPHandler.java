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
package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.handler.impl.CSPHandlerBuilderImpl;

/**
 * Content Security Policy (CSP) is an added layer of security that helps to detect and mitigate certain types of
 * attacks, including Cross Site Scripting (XSS) and data injection attacks. These attacks are used for everything from
 * data theft to site defacement to distribution of malware.
 *
 * CSP is designed to be fully backward compatible. Browsers that don't support it still work with servers that
 * implement it, and vice-versa: browsers that don't support CSP simply ignore it, functioning as usual, defaulting to
 * the standard same-origin policy for web content. If the site doesn't offer the CSP header, browsers likewise use the
 * standard same-origin policy.
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@VertxGen
public interface CSPHandler extends SecurityPolicyHandler {

  /**
   * Creates a new instance of the handler.
   * @return a new CSP handler.
   */
  static CSPHandler create() {
    return builder().build();
  }

  /**
   * Create a new {@link CSPHandlerBuilder}.
   */
  static CSPHandlerBuilder builder() {
    return new CSPHandlerBuilderImpl();
  }
}
