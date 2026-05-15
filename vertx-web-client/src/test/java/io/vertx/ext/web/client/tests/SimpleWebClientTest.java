package io.vertx.ext.web.client.tests;

import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.test.fakeresolver.FakeAddress;
import io.vertx.test.fakeresolver.FakeAddressResolver;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

/**
 * Tests that do not require a server.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SimpleWebClientTest extends WebClientTestBase {

  @Test
  public void testRequestOptionsHeaders() {
    String headerKey = "header1", headerValue = "value1";
    RequestOptions options = new RequestOptions().addHeader(headerKey, headerValue);

    HttpRequest<Buffer> request = webClient.request(HttpMethod.GET, options);
    Assertions.assertThat(request.headers().get(headerKey)).isEqualTo(headerValue);

    request = webClient.request(HttpMethod.GET, SocketAddress.inetSocketAddress(8080, "localhost"), options);
    Assertions.assertThat(request.headers().get(headerKey)).isEqualTo(headerValue);

    request = webClient.request(HttpMethod.GET, new RequestOptions());
    Assertions.assertThat(request.headers().get(headerKey)).isNull();
  }

  @Test
  public void testFromRequestOptions() {
    ProxyOptions proxyOptions = new ProxyOptions().setHost("proxy-host");
    RequestOptions options = new RequestOptions().setHost("another-host").setPort(8080).setSsl(true)
      .setURI("/test").setTimeout(500).setProxyOptions(proxyOptions).setFollowRedirects(true);
    HttpRequest<Buffer> request = webClient.request(HttpMethod.GET, options);

    Assertions.assertThat(request.host()).isEqualTo("another-host");
    Assertions.assertThat(request.port()).isEqualTo(8080);
    Assertions.assertThat(request.ssl()).isEqualTo(true);
    Assertions.assertThat(request.uri()).isEqualTo("/test");
    Assertions.assertThat(request.timeout()).isEqualTo(500l);
    Assertions.assertThat(request.followRedirects()).isEqualTo(true);
    Assertions.assertThat(request.proxy()).isNotEqualTo(proxyOptions);
    Assertions.assertThat(request.proxy().getHost()).isEqualTo("proxy-host");
  }

  @Test
  public void testMalformedURLExceptionNotSwallowed() {
    Assertions.assertThatThrownBy(() -> webClient.requestAbs(HttpMethod.POST, "blah://foo@bar"))
      .isInstanceOf(VertxException.class)
      .hasCauseInstanceOf(MalformedURLException.class);
  }

  @Test
  public void testCannotResolveAddress() throws Exception {
    client.close().await();
    FakeAddressResolver<?> resolver = new FakeAddressResolver<>();
    client = vertx.httpClientBuilder().with(createBaseClientOptions()).withAddressResolver(resolver).build();
    webClient = WebClient.wrap(client);

    // This test verifies the address resolver is actually used by the WebClient
    Assertions.assertThatThrownBy(() ->
      webClient.request(HttpMethod.GET, new RequestOptions().setServer(new FakeAddress("mars")))
        .send()
        .await()
    ).hasMessageContaining("resolve").hasMessageContaining("mars");
  }
}
