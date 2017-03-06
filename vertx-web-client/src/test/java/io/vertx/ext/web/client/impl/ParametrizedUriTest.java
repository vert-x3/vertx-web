package io.vertx.ext.web.client.impl;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:pabloeabad@gmail.com">Pablo Abad</a>
 */
public class ParametrizedUriTest {

  public static final String FIXED = "http://www.vertx.io/index.html?p1=20";
  public static final String ONE = "http://www.vertx.io/:one/index.html?p1=20";
  public static final String TWO = "http://www.vertx.io/:one/:two/index.html?p1=20";

  @Test
  public void testFixedUrlReusesString() {
    ParametrizedUri u = fixed();

    Assert.assertEquals(FIXED, u.toString());
    Assert.assertSame(u.toString(), u.toString());
  }

  @Test
  public void testSimpleSubstitution() {
    ParametrizedUri u = simple();

    u.setParam("one", "main");

    Assert.assertEquals(simpleUrl("main"), u.toString());
  }

  @Test
  public void testSimpleLongSubstitution() {
    ParametrizedUri u = simple();

    u.setParam("one", 123);

    Assert.assertEquals(simpleUrl("123"), u.toString());
  }

  @Test(expected = IllegalStateException.class)
  public void testMissingParameter() {
    ParametrizedUri u = twoParams();

    u.setParam("one", 123);

    u.toString();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnknownParameter() {
    ParametrizedUri u = twoParams();

    u.setParam("other", 123);
  }


  @Test
  public void testMultipleParameters() {
    ParametrizedUri u = twoParams();

    u.setParam("one", 123);
    u.setParam("two", "test");

    Assert.assertEquals(twoParamsUrl("123", "test"), u.toString());
  }

  @Test
  public void testUriEndsInParam() {
    ParametrizedUri u = new ParametrizedUri("http://www.vertx.io/:id");

    u.setParam("id", 123);

    Assert.assertEquals("http://www.vertx.io/123", u.toString());
  }

  @Test
  public void testParameterEscaping() {
    ParametrizedUri u = simple();

    u.setParam("one", "/.+~-_ ");

    Assert.assertEquals(simpleUrl("%2f.+~-_%20"), u.toString());
  }

  @Test
  public void testParameterOverride() {
    ParametrizedUri u = simple();

    u.setParam("one", "test");
    String uri1 = u.toString();
    u.setParam("one", "test2");
    String uri2 = u.toString();

    Assert.assertEquals(simpleUrl("test"), uri1);
    Assert.assertEquals(simpleUrl("test2"), uri2);
  }

  @Test
  public void testParameterReuse() {
    ParametrizedUri u = twoParams();

    u.setParam("one", "test");
    u.setParam("two", 1);
    String uri1 = u.toString();
    u.setParam("two", 2);
    String uri2 = u.toString();

    Assert.assertEquals(twoParamsUrl("test", "1"), uri1);
    Assert.assertEquals(twoParamsUrl("test", "2"), uri2);
  }

  private ParametrizedUri fixed() {
    return new ParametrizedUri(FIXED);
  }

  private ParametrizedUri simple() {
    return new ParametrizedUri(ONE);
  }

  private ParametrizedUri twoParams() {
    return new ParametrizedUri(TWO);
  }

  private String simpleUrl(String one) {
    return ONE.replace(":one", one);
  }

  private String twoParamsUrl(String one, String two) {
    return TWO.replace(":one", one).replace(":two", two);
  }
}
