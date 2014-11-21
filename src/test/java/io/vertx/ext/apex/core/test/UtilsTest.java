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

package io.vertx.ext.apex.core.test;

import io.vertx.ext.apex.core.impl.Utils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class UtilsTest {

  @Test
  public void testNoLeadingSlash() throws Exception {
    assertNull(Utils.normalisePath("path/with/no/leading/slash"));
  }

  @Test
  public void testNullPath() throws Exception {
    assertNull(Utils.normalisePath(null));
  }

  @Test
  public void testPathWithSpaces1() throws Exception {
    assertEquals("/foo blah/eek", Utils.normalisePath("/foo+blah/eek"));
  }

  @Test
  public void testPathWithSpaces2() throws Exception {
    assertEquals("/foo blah/eek", Utils.normalisePath("/foo%20blah/eek"));
  }

  @Test
  public void testPathWithDot() throws Exception {
    assertEquals("/foo/blah/eek", Utils.normalisePath("/foo/blah/./eek"));
  }

  @Test
  public void testPathWithExtraSlashes1() throws Exception {
    assertEquals("/foo/blah/eek", Utils.normalisePath("/foo//blah//eek"));
  }

  @Test
  public void testPathWithExtraSlashes2() throws Exception {
    assertEquals("/foo/blah/eek", Utils.normalisePath("//foo/blah/eek//"));
  }

  @Test
  public void testDodgyPath1() throws Exception {
    assertEquals("/blah", Utils.normalisePath("/foo/../../blah"));
  }

  @Test
  public void testDodgyPath2() throws Exception {
    assertEquals("/blah", Utils.normalisePath("/foo/../../../blah"));
  }

  @Test
  public void testDodgyPath3() throws Exception {
    assertEquals("/blah", Utils.normalisePath("/foo/../blah"));
  }

  @Test
  public void testDodgyPath4() throws Exception {
    assertEquals("/blah", Utils.normalisePath("/../blah"));
  }
}
