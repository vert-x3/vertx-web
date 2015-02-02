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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import io.vertx.ext.apex.core.impl.Utils;

import java.util.List;

import org.junit.Test;

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
  public void testDodgyPath1() throws Exception {
    assertEquals("/foo/blah", Utils.normalisePath("/foo/../../blah"));
  }

  @Test
  public void testDodgyPath2() throws Exception {
    assertEquals("/foo/blah", Utils.normalisePath("/foo/../../../blah"));
  }

  @Test
  public void testDodgyPath3() throws Exception {
    assertEquals("/foo/blah", Utils.normalisePath("/foo/../blah"));
  }

  @Test
  public void testDodgyPath4() throws Exception {
    assertEquals("/blah", Utils.normalisePath("/../blah"));
  }

  @Test
  public void testURLDecode() throws Exception {
    assertEquals(
        "/ !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~",
        Utils
            .normalisePath("/%20%21%22%23%24%25%26%27%28%29%2A%2B%2C%2D%2E%2F%30%31%32%33%34%35%36%37%38%39%3A%3B%3C%3D%3E%3F%40%41%42%43%44%45%46%47%48%49%4A%4B%4C%4D%4E%4F%50%51%52%53%54%55%56%57%58%59%5A%5B%5C%5D%5E%5F%60%61%62%63%64%65%66%67%68%69%6A%6B%6C%6D%6E%6F%70%71%72%73%74%75%76%77%78%79%7A%7B%7C%7D%7E"));
  }

  @Test
  public void testSortedAcceptableMimeTypes1() {
    String accept = "text/html";
    List<String> types = Utils.getSortedAcceptableMimeTypes(accept);
    assertEquals(1, types.size());
    assertEquals("text/html", types.get(0));
  }

  @Test
  public void testSortedAcceptableMimeTypes2() {
    String accept = "text/html, application/json";
    List<String> types = Utils.getSortedAcceptableMimeTypes(accept);
    assertEquals(2, types.size());
    assertEquals("text/html", types.get(0));
    assertEquals("application/json", types.get(1));
  }

  @Test
  public void testSortedAcceptableMimeTypes3() {
    String accept = "text/html,application/json";
    List<String> types = Utils.getSortedAcceptableMimeTypes(accept);
    assertEquals(2, types.size());
    assertEquals("text/html", types.get(0));
    assertEquals("application/json", types.get(1));
  }

  @Test
  public void testSortedAcceptableMimeTypes4() {
    String accept = "text/html; q=0.8,application/json; q=0.9";
    List<String> types = Utils.getSortedAcceptableMimeTypes(accept);
    assertEquals(2, types.size());
    assertEquals("application/json", types.get(0));
    assertEquals("text/html", types.get(1));
  }

  @Test
  public void testSortedAcceptableMimeTypes5() {
    String accept = "text/html;q=0.8,application/json;q=0.9";
    List<String> types = Utils.getSortedAcceptableMimeTypes(accept);
    assertEquals(2, types.size());
    assertEquals("application/json", types.get(0));
    assertEquals("text/html", types.get(1));
  }

  @Test
  public void testSortedAcceptableMimeTypes6() {
    String accept = "text/html; q=0.8,application/json; q=0.9, text/plain";
    List<String> types = Utils.getSortedAcceptableMimeTypes(accept);
    assertEquals(3, types.size());
    assertEquals("text/plain", types.get(0));
    assertEquals("application/json", types.get(1));
    assertEquals("text/html", types.get(2));
  }

  @Test
  public void testSortedAcceptableMimeTypes7() {
    String accept = "text/html;q=0.8,application/json;q=0.9,text/plain";
    List<String> types = Utils.getSortedAcceptableMimeTypes(accept);
    assertEquals(3, types.size());
    assertEquals("text/plain", types.get(0));
    assertEquals("application/json", types.get(1));
    assertEquals("text/html", types.get(2));
  }
}
