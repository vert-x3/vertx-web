package io.vertx.ext.web.handler.logger;

import static io.vertx.core.http.HttpVersion.HTTP_1_0;
import static io.vertx.core.http.HttpVersion.HTTP_1_1;
import static io.vertx.core.http.HttpVersion.HTTP_2;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class FirstRequestLineParameterTest {

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
  public void testFirstRequestLineParameter_0() {
    when(req.rawMethod()).thenReturn("GET");
    when(req.path()).thenReturn("/foo");
    when(req.query()).thenReturn("a=b&c=d");
    when(req.version()).thenReturn(HTTP_1_1);

    FirstRequestLineParameter test = new FirstRequestLineParameter();
    assertThat(test.getValue(ctx, false), equalTo("GET /foo?a=b&c=d HTTP/1.1"));
  }

  @Test
  public void testFirstRequestLineParameter_1() {
    when(req.rawMethod()).thenReturn("POST");
    when(req.path()).thenReturn("/foo");
    when(req.version()).thenReturn(HTTP_2);

    FirstRequestLineParameter test = new FirstRequestLineParameter();
    assertThat(test.getValue(ctx, false), equalTo("POST /foo HTTP/2.0"));
  }

  @Test
  public void testFirstRequestLineParameter_2() {
    when(req.rawMethod()).thenReturn("CUSTOM");
    when(req.path()).thenReturn("/bar");
    when(req.version()).thenReturn(HTTP_1_0);

    FirstRequestLineParameter test = new FirstRequestLineParameter();
    assertThat(test.getValue(ctx, false), equalTo("CUSTOM /bar HTTP/1.0"));
  }
}
