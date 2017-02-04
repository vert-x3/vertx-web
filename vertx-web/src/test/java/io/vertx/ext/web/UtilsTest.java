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

import io.vertx.ext.web.impl.Utils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
  public void testURLDecode() throws Exception {
    // verify that the URL is translated as per RFC
    assertEquals(
        "/%20%21%22%23%24%25%26%27%28%29%2A%2B%2C-.%2F0123456789%3A%3B%3C%3D%3E%3F%40ABCDEFGHIJKLMNOPQRSTUVWXYZ%5B%5C%5D%5E_%60abcdefghijklmnopqrstuvwxyz%7B%7C%7D~",
        Utils.normalizePath("/%20%21%22%23%24%25%26%27%28%29%2A%2B%2C%2D%2E%2F%30%31%32%33%34%35%36%37%38%39%3A%3B%3C%3D%3E%3F%40%41%42%43%44%45%46%47%48%49%4A%4B%4C%4D%4E%4F%50%51%52%53%54%55%56%57%58%59%5A%5B%5C%5D%5E%5F%60%61%62%63%64%65%66%67%68%69%6A%6B%6C%6D%6E%6F%70%71%72%73%74%75%76%77%78%79%7A%7B%7C%7D%7E"));
    // when dealing with path params the url decoder should respect the full encoded chars
    assertEquals(
      "/ !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~",
      Utils.urlDecode(Utils.normalizePath("/%20%21%22%23%24%25%26%27%28%29%2A%2B%2C%2D%2E%2F%30%31%32%33%34%35%36%37%38%39%3A%3B%3C%3D%3E%3F%40%41%42%43%44%45%46%47%48%49%4A%4B%4C%4D%4E%4F%50%51%52%53%54%55%56%57%58%59%5A%5B%5C%5D%5E%5F%60%61%62%63%64%65%66%67%68%69%6A%6B%6C%6D%6E%6F%70%71%72%73%74%75%76%77%78%79%7A%7B%7C%7D%7E"), false));
  }

  @Test
  public void testURLDecodeNonLatin() throws Exception {
    // verify that the URL is translated as per RFC
    assertEquals("/foo/%C3%B1/blah/%E5%A9%B4%E5%84%BF%E6%9C%8D%E9%A5%B0/eek/%E0%B8%8C", Utils.normalizePath("/foo/%C3%B1/blah/%E5%A9%B4%E5%84%BF%E6%9C%8D%E9%A5%B0/eek/%E0%B8%8C"));
    // when dealing with path params the url decoder should respect the full encoded chars
    assertEquals("/foo/\u00F1/blah/\u5a74\u513f\u670d\u9970/eek/\u0E0C", Utils.urlDecode(Utils.normalizePath("/foo/%C3%B1/blah/%E5%A9%B4%E5%84%BF%E6%9C%8D%E9%A5%B0/eek/%E0%B8%8C"), false));
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
    assertEquals("[\"x\"]", Utils.urlDecode("%5B%22x%22%5D", true));
    assertEquals("[\"abc\"]", Utils.urlDecode("%5B%22abc%22%5D", true));
    assertEquals("[\"x", Utils.urlDecode("%5B%22x", true));
    assertEquals("[\"b\"]", Utils.urlDecode("%5B%22b%22%5D", true));
  }
}
