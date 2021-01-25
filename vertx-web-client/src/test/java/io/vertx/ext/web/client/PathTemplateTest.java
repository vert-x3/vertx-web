package io.vertx.ext.web.client;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PathTemplateTest {

  @Test
  public void testParam() {
    UriTemplate template = PathTemplate.parse("/:x");
    assertEquals(
      "/1",
      template.expand(UriParameters.create().param("x", 1))
    );
  }

  @Test
  public void testArrayParam() {
    UriTemplate template = PathTemplate.parse("/:x/y");
    assertEquals(
      "/a/b/c/y",
      template.expand(UriParameters.create().param("x", Arrays.asList("a", "b", "c")))
    );
  }

  @Test
  public void testEncodingParam() {
    UriTemplate template = PathTemplate.parse("/:semi/:dot/:comma");
    assertEquals(
      "/%3B/./%2C",
      template.expand(UriParameters.create().param("semi", ";").param("dot", ".").param("comma", ","))
    );
  }

  @Test
  public void testAlreadyEscapedParam1() {
    UriTemplate template = PathTemplate.parse("/:semi/:dot/:comma");
    assertEquals(
      "/%3B/./%2C",
      template.expand(UriParameters.create().param("semi", ";").param("dot", ".").escapedParam("comma", "%2C"))
    );
  }

  @Test
  public void testAlreadyEscapedArrayParam() {
    UriTemplate template = PathTemplate.parse("/:symbol");
    assertEquals(
      "/%3B/%2C/.",
      template.expand(UriParameters.create().escapedParam("symbol", Arrays.asList("%3B", "%2C")).param("symbol", "."))
    );
  }

  @Test
  public void testAlreadyEscapedParam2() {
    UriTemplate template = PathTemplate.parse("/:semi/:dot/:comma");
    assertEquals(
      "/%3B/./,",
      template.expand(UriParameters.create().param("semi", ";").param("dot", ".").escapedParam("comma", ","))
    );
  }

  @Test
  public void testPrependPostpendParam() {
    UriTemplate template = PathTemplate.parse("/a/x=:x/:y/b");
    assertEquals(
      "/a/x=1/2/b",
      template.expand(UriParameters.create().param("x", 1).param("y", 2))
    );
  }

  @Test
  public void testDontEraseQueryParams() {
    UriTemplate template = PathTemplate.parse("/a/:x/:y/b?hello=world:a");
    assertEquals(
      "/a/1/2/b?hello=world:a",
      template.expand(UriParameters.create().param("x", 1).param("y", 2))
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingParameter() {
    UriTemplate template = PathTemplate.parse("/a/:x/:y/b?hello=world");
    template.expand(UriParameters.create().param("x", 1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadTemplate1() {
    PathTemplate.parse("a/:y/b?hello=world");
  }

  @Test
  public void testEmptyTemplate1() {
    UriTemplate template = PathTemplate.parse("/");
    assertEquals(
      "/",
      template.expand(UriParameters.create())
    );
  }

  @Test
  public void testEmptyTemplate2() {
    UriTemplate template = PathTemplate.parse("");
    assertEquals(
      "",
      template.expand(UriParameters.create())
    );
  }

  @Test
  public void testEmptyTemplate3() {
    UriTemplate template = PathTemplate.parse("/hello/world");
    assertEquals(
      "/hello/world",
      template.expand(UriParameters.create())
    );
  }

}
