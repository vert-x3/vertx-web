package io.vertx.ext.web.client;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PathTemplateTest {

  @Test
  public void testParam() {
    PathTemplate template = PathTemplate.parse("/:x");
    assertEquals(
      "/1",
      template.expand(PathParameters.create().param("x", 1))
    );
  }

  @Test
  public void testArrayParam() {
    PathTemplate template = PathTemplate.parse("/:x/y");
    assertEquals(
      "/a/b/c/y",
      template.expand(PathParameters.create().param("x", Arrays.asList("a", "b", "c")))
    );
  }

  @Test
  public void testEncodingParam() {
    PathTemplate template = PathTemplate.parse("/:semi/:dot/:comma");
    assertEquals(
      "/%3B/./%2C",
      template.expand(PathParameters.create().param("semi", ";").param("dot", ".").param("comma", ","))
    );
  }

  @Test
  public void testAlreadyEscapedParam1() {
    PathTemplate template = PathTemplate.parse("/:semi/:dot/:comma");
    assertEquals(
      "/%3B/./%2C",
      template.expand(PathParameters.create().param("semi", ";").param("dot", ".").escapedParam("comma", "%2C"))
    );
  }

  @Test
  public void testAlreadyEscapedArrayParam() {
    PathTemplate template = PathTemplate.parse("/:symbol");
    assertEquals(
      "/%3B/%2C/.",
      template.expand(PathParameters.create().escapedParam("symbol", Arrays.asList("%3B", "%2C")).param("symbol", "."))
    );
  }

  @Test
  public void testAlreadyEscapedParam2() {
    PathTemplate template = PathTemplate.parse("/:semi/:dot/:comma");
    assertEquals(
      "/%3B/./,",
      template.expand(PathParameters.create().param("semi", ";").param("dot", ".").escapedParam("comma", ","))
    );
  }

  @Test
  public void testPrependPostpendParam() {
    PathTemplate template = PathTemplate.parse("/a/x=:x/:y/b");
    assertEquals(
      "/a/x=1/2/b",
      template.expand(PathParameters.create().param("x", 1).param("y", 2))
    );
  }

  @Test
  public void testDontEraseQueryParams() {
    PathTemplate template = PathTemplate.parse("/a/:x/:y/b?hello=world:a");
    assertEquals(
      "/a/1/2/b?hello=world:a",
      template.expand(PathParameters.create().param("x", 1).param("y", 2))
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingParameter() {
    PathTemplate template = PathTemplate.parse("/a/:x/:y/b?hello=world");
    template.expand(PathParameters.create().param("x", 1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadTemplate1() {
    PathTemplate.parse("a/:y/b?hello=world");
  }

  @Test
  public void testEmptyTemplate1() {
    PathTemplate template = PathTemplate.parse("/");
    assertEquals(
      "/",
      template.expand(PathParameters.create())
    );
  }

  @Test
  public void testEmptyTemplate2() {
    PathTemplate template = PathTemplate.parse("");
    assertEquals(
      "",
      template.expand(PathParameters.create())
    );
  }

  @Test
  public void testEmptyTemplate3() {
    PathTemplate template = PathTemplate.parse("/hello/world");
    assertEquals(
      "/hello/world",
      template.expand(PathParameters.create())
    );
  }

}
