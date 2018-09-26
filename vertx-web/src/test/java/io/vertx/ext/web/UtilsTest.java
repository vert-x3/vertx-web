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
import io.vertx.ext.web.impl.Utils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class UtilsTest {

  @Test
  public void testNoLeadingSlash() throws Exception {
    assertEquals("/path/with/no/leading/slash", Utils.normalizePath("path/with/no/leading/slash"));
  }

  @Test
  public void testNullPath() throws Exception {
    assertEquals("/", Utils.normalizePath(null));
  }

  @Test
  public void testPathWithSpaces1() throws Exception {
    // this is a special case since only percent encoded values should be unescaped from the path
    assertEquals("/foo+blah/eek", Utils.normalizePath("/foo+blah/eek"));
  }

  @Test
  public void testPathWithSpaces2() throws Exception {
    assertEquals("/foo%20blah/eek", Utils.normalizePath("/foo%20blah/eek"));
  }

  @Test
  public void testDodgyPath1() throws Exception {
    assertEquals("/blah", Utils.normalizePath("/foo/../../blah"));
  }

  @Test
  public void testDodgyPath2() throws Exception {
    assertEquals("/blah", Utils.normalizePath("/foo/../../../blah"));
  }

  @Test
  public void testDodgyPath3() throws Exception {
    assertEquals("/blah", Utils.normalizePath("/foo/../blah"));
  }

  @Test
  public void testDodgyPath4() throws Exception {
    assertEquals("/blah", Utils.normalizePath("/../blah"));
  }

  @Test
  public void testMultipleSlashPath1() throws Exception {
    assertEquals("/blah", Utils.normalizePath("//blah"));
  }

  @Test
  public void testMultipleSlashPath2() throws Exception {
    assertEquals("/blah", Utils.normalizePath("///blah"));
  }

  @Test
  public void testMultipleSlashPath3() throws Exception {
    assertEquals("/foo/blah", Utils.normalizePath("/foo//blah"));
  }

  @Test
  public void testMultipleSlashPath4() throws Exception {
    assertEquals("/foo/blah/", Utils.normalizePath("/foo//blah///"));
  }

  @Test
  public void testSlashesAndDodgyPath1() throws Exception {
    assertEquals("/blah", Utils.normalizePath("//../blah"));
  }

  @Test
  public void testSlashesAndDodgyPath2() throws Exception {
    assertEquals("/blah", Utils.normalizePath("/..//blah"));
  }

  @Test
  public void testSlashesAndDodgyPath3() throws Exception {
    assertEquals("/blah", Utils.normalizePath("//..//blah"));
  }

  @Test
  public void testDodgyPathEncoded() throws Exception {
    assertEquals("/..%2Fblah", Utils.normalizePath("/%2E%2E%2Fblah"));
  }

  @Test
  public void testTrailingSlash() throws Exception {
    assertEquals("/blah/", Utils.normalizePath("/blah/"));
  }

  @Test
  public void testMultipleTrailingSlashes1() throws Exception {
    assertEquals("/blah/", Utils.normalizePath("/blah//"));
  }

  @Test
  public void testMultipleTrailingSlashes2() throws Exception {
    assertEquals("/blah/", Utils.normalizePath("/blah///"));
  }

  @Test
  public void testBadURL() throws Exception {
    try {
      Utils.normalizePath("/%7B%channel%%7D");
      fail();
    } catch (IllegalArgumentException e) {
      // expected!
    }
  }

  @Test
  public void testDoubleDot() throws Exception {
    assertEquals("/foo/bar/abc..def", Utils.normalizePath("/foo/bar/abc..def"));
  }

  @Test
  public void testSpec() throws Exception {
    assertEquals("/a/g", Utils.normalizePath("/a/b/c/./../../g"));
    assertEquals("/mid/6", Utils.normalizePath("mid/content=5/../6"));
    assertEquals("/~username/", Utils.normalizePath("/%7Eusername/"));
    assertEquals("/b/", Utils.normalizePath("/b/c/.."));
  }

  @Test
  public void testSockJSEscape() throws Exception {
    assertEquals("[\"x\"]", URIDecoder.decodeURIComponent("%5B%22x%22%5D", true));
    assertEquals("[\"abc\"]", URIDecoder.decodeURIComponent("%5B%22abc%22%5D", true));
    assertEquals("[\"x", URIDecoder.decodeURIComponent("%5B%22x", true));
    assertEquals("[\"b\"]", URIDecoder.decodeURIComponent("%5B%22b%22%5D", true));
  }
}
