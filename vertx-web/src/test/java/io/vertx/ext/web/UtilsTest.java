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

package io.vertx.ext.web;

import io.vertx.core.net.impl.URIDecoder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class UtilsTest {

  @Test
  public void testSockJSEscape() throws Exception {
    assertEquals("[\"x\"]", URIDecoder.decodeURIComponent("%5B%22x%22%5D", true));
    assertEquals("[\"abc\"]", URIDecoder.decodeURIComponent("%5B%22abc%22%5D", true));
    assertEquals("[\"x", URIDecoder.decodeURIComponent("%5B%22x", true));
    assertEquals("[\"b\"]", URIDecoder.decodeURIComponent("%5B%22b%22%5D", true));
  }
}
