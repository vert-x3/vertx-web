package io.vertx.ext.web.handler.logger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class CookieParameterTest {

  @Mock
  RoutingContext ctx;
  @Mock
  HttpServerRequest req;
  @Mock
  HttpServerResponse resp;

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Before
  public void before() {
    when(ctx.response()).thenReturn(resp);
    when(ctx.request()).thenReturn(req);
  }

  @Test
  public void testCookieParameter_foundCookie0() {
    String givenCookie = "my-cookie=my-value";
    when(req.headers())
      .thenReturn(MultiMap.caseInsensitiveMultiMap().add("cookie", givenCookie));

    CookieParameter test = new CookieParameter("my-cookie");
    assertThat(test.getValue(ctx, false), equalTo(givenCookie));
  }

  @Test
  public void testCookieParameter_foundCookie1() {
    String givenCookie = "my-cookie=my-value";
    when(req.headers())
      .thenReturn(MultiMap.caseInsensitiveMultiMap()
        .add("cookie", "other=value;" + givenCookie + ";foo=bogus"));

    CookieParameter test = new CookieParameter("my-cookie");
    assertThat(test.getValue(ctx, false), equalTo(givenCookie));
  }

  @Test
  public void testCookieParameter_noCookie() {
    when(req.headers())
      .thenReturn(MultiMap.caseInsensitiveMultiMap()
        .add("cookie", "other=value;foo=bogus"));

    CookieParameter test = new CookieParameter("my-cookie");
    assertThat(test.getValue(ctx, false), is(nullValue()));
  }
}
