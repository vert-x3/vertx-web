package io.vertx.ext.web.handler.logger;

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

public class QueryParameterTest {

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
    when(ctx.request()).thenReturn(req);
  }

  @Test
  public void testQueryParameter_0() {
    String given = "param=value&vertx=cool";
    when(req.query()).thenReturn(given);

    QueryParameter test = new QueryParameter();
    assertThat(test.getValue(ctx, false), equalTo("?" + given));
  }

  @Test
  public void testQueryParameter_1() {
    when(req.query()).thenReturn("");

    QueryParameter test = new QueryParameter();
    assertThat(test.getValue(ctx, false), equalTo(""));

    when(req.query()).thenReturn(null);
    assertThat(test.getValue(ctx, false), equalTo(""));
  }


}
