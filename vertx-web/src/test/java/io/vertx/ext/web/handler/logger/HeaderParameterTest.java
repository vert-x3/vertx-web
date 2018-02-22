package io.vertx.ext.web.handler.logger;

import static org.hamcrest.CoreMatchers.equalTo;
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

public class HeaderParameterTest {

  private final static boolean REQUEST = true;
  private final static boolean RESPONSE = false;

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
  public void testHeaderParameter_request_single() {
    when(req.headers())
      .thenReturn(MultiMap.caseInsensitiveMultiMap().add("header", "req"));

    HeaderParameter test = new HeaderParameter("header", REQUEST);
    assertThat(test.getValue(ctx, false), equalTo("req"));
  }

  @Test
  public void testHeaderParameter_response_single() {
    when(resp.headers())
      .thenReturn(MultiMap.caseInsensitiveMultiMap().add("header", "resp"));

    HeaderParameter test = new HeaderParameter("header", RESPONSE);
    assertThat(test.getValue(ctx, false), equalTo("resp"));
  }

  @Test
  public void testHeaderParameter_req_multiple() {
    when(req.headers())
      .thenReturn(MultiMap.caseInsensitiveMultiMap()
        .add("header", "1")
        .add("header", "2")
        .add("header", "3"));

    HeaderParameter test = new HeaderParameter("header", REQUEST);
    assertThat(test.getValue(ctx, false), equalTo("1,2,3"));
  }

  @Test
  public void testHeaderParameter_resp_multiple() {
    when(resp.headers())
      .thenReturn(MultiMap.caseInsensitiveMultiMap()
        .add("header", "1")
        .add("header", "2")
        .add("header", "3"));

    HeaderParameter test = new HeaderParameter("header", RESPONSE);
    assertThat(test.getValue(ctx, false), equalTo("1,2,3"));
  }
}
