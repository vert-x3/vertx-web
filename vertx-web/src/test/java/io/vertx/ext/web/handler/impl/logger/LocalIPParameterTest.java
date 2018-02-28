package io.vertx.ext.web.handler.impl.logger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class LocalIPParameterTest {

  @Mock
  RoutingContext ctx;
  @Mock
  HttpServerRequest req;
  @Mock
  SocketAddress address;

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Before
  public void before() {
    when(ctx.request()).thenReturn(req);
  }

  @Test
  public void testLocalIpParameter_0() {
    when(req.localAddress()).thenReturn(address);
    when(address.host()).thenReturn("1.2.3.4");

    LocalIPParameter test = new LocalIPParameter();
    assertThat(test.getValue(ctx), equalTo("1.2.3.4"));
  }

  @Test
  public void testLocalIpParameter_1() {
    when(req.localAddress()).thenReturn(null);

    LocalIPParameter test = new LocalIPParameter();
    assertThat(test.getValue(ctx), is(nullValue()));
  }
}
