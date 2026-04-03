/*
 * Copyright 2022 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.ext.web.client.tests;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.impl.WebClientInternal;
import io.vertx.junit5.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

/**
 * JUnit 5 base class for web client tests, replacing the JUnit 4 WebClientTestBase.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@ExtendWith(VertxExtension.class)
@ReportHandlerFailures
public class WebClientJUnit5TestBase {

  public static class VertxProv implements VertxProvider {
    @Override
    public Vertx get() {
      return Vertx.vertx(new VertxOptions()
        .setAddressResolverOptions(new AddressResolverOptions().
        setHostsValue(Buffer.buffer(
          "127.0.0.1 somehost\n" +
            "127.0.0.1 localhost"))));
    }
  }

  protected static final String DEFAULT_HTTP_HOST = "localhost";
  protected static final int DEFAULT_HTTP_PORT = 8080;
  protected static final String DEFAULT_HTTPS_HOST = "localhost";
  protected static final int DEFAULT_HTTPS_PORT = 4043;
  protected static final String DEFAULT_TEST_URI = "some-uri";

  @TempDir
  protected File testFolder;

  protected Vertx vertx;
  protected HttpServer server;
  protected HttpClient client;
  protected WebClientInternal webClient;
  protected SocketAddress testAddress;

  @BeforeEach
  public void setUp(@ProvidedBy(VertxProv.class) Vertx vertx, VertxTestContext testContext) {
    this.vertx = vertx;
    server = vertx.createHttpServer(createBaseServerOptions());
    client = vertx.createHttpClient(createBaseClientOptions());
    webClient = (WebClientInternal) WebClient.wrap(client);
    testAddress = SocketAddress.inetSocketAddress(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST);
    testContext.completeNow();
  }

  protected HttpServerOptions createBaseServerOptions() {
    return new HttpServerOptions().setPort(DEFAULT_HTTP_PORT).setHost(DEFAULT_HTTP_HOST);
  }

  protected HttpClientOptions createBaseClientOptions() {
    return new HttpClientOptions().setDefaultPort(DEFAULT_HTTP_PORT).setDefaultHost(DEFAULT_HTTP_HOST);
  }

  protected void startServer() {
    server.listen().await();
  }

  protected void startServer(HttpServer server) {
    server.listen().await();
  }
}
