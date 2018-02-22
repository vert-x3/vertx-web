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

/**
 * @author <a href="https://github.com/marcinczeczko">Marcin Czeczko</a>
 */
public class ByteCountParameterTest {

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
  public void testByteCountParameter_NonZero() {
    when(resp.bytesWritten()).thenReturn(123L);
    when(req.headers()).thenReturn(MultiMap.caseInsensitiveMultiMap().add("content-length", "321"));

    ByteCountParameter test = new ByteCountParameter();
    assertThat(test.getValue(ctx, true), equalTo("321"));
    assertThat(test.getValue(ctx, false), equalTo("123"));
  }

  @Test
  public void testByteCountParameter_Zero() {
    when(resp.bytesWritten()).thenReturn(0L);

    ByteCountParameter test = new ByteCountParameter();
    assertThat(test.getValue(ctx, false), equalTo("0"));

    test.setParName('b');
    assertThat(test.getValue(ctx, false), equalTo("-"));
  }
}
