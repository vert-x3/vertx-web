package io.vertx.ext.web.handler.impl.logger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class ServerNameParameterTest {
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
  public void testServerNameParameter_noHostHeader() {
    when(req.headers()).thenReturn(MultiMap.caseInsensitiveMultiMap());
    when(req.localAddress()).thenReturn(address);
    when(address.host()).thenReturn("1.2.3.4");

    ServerNameParameter test = new ServerNameParameter();
    assertThat(test.getValue(ctx), equalTo("1.2.3.4"));
  }

  @Test
  public void testServerNameParameter_hostHeader() {
    when(req.headers()).thenReturn(MultiMap.caseInsensitiveMultiMap().add("host", "my.host:1234"));
    when(req.localAddress()).thenReturn(null);

    ServerNameParameter test = new ServerNameParameter();
    assertThat(test.getValue(ctx), equalTo("my.host"));
  }

  @Test
  public void testServerNameParameter_noHostHeader_noLocalAddress() {
    when(req.headers()).thenReturn(MultiMap.caseInsensitiveMultiMap());
    when(req.localAddress()).thenReturn(null);

    ServerNameParameter test = new ServerNameParameter();
    assertThat(test.getValue(ctx), is(nullValue()));
  }
}
