/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.client;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.impl.HttpClientImpl;
import io.vertx.ext.web.client.impl.WebClientBase;

/**
 * An asynchronous HTTP / HTTP/2 client called {@code WebClient}.
 * <p>
 * The web client makes easy to do HTTP request/response interactions with a web server, and provides advanced
 * features like:
 * <ul>
 *   <li>Json body encoding / decoding</li>
 *   <li>request/response pumping</li>
 *   <li>error handling</li>
 * </ul>
 * <p>
 * The web client does not deprecate the {@link HttpClient}, it is actually based on it and therefore inherits
 * its configuration and great features like pooling. The {@code HttpClient} should be used when fine grained control over the HTTP
 * requests/response is necessary.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface WebClient {

  /**
   * Create a web client using the provided {@code vertx} instance and default options.
   *
   * @param vertx the vertx instance
   * @return the created web client
   */
  static WebClient create(Vertx vertx) {
    WebClientOptions options = new WebClientOptions();
    return create(vertx, options);
  }

  /**
   * Create a web client using the provided {@code vertx} instance.
   *
   * @param vertx   the vertx instance
   * @param options the Web Client options
   * @return the created web client
   */
  static WebClient create(Vertx vertx, WebClientOptions options) {
    return new WebClientBase(vertx.createHttpClient(options), options);
  }

  /**
   * Wrap an {@code httpClient} with a web client and default options.
   *
   * @param httpClient the {@link HttpClient} to wrap
   * @return the web client
   */
  static WebClient wrap(HttpClient httpClient) {
    return wrap(httpClient, new WebClientOptions());
  }

  /**
   * Wrap an {@code httpClient} with a web client and default options.
   * <p>
   * Only the specific web client portion of the {@code options} is used, the {@link io.vertx.core.http.HttpClientOptions}
   * of the {@code httpClient} is reused.
   *
   * @param httpClient the {@link HttpClient} to wrap
   * @param options    the Web Client options
   * @return the web client
   */
  static WebClient wrap(HttpClient httpClient, WebClientOptions options) {
    WebClientOptions actualOptions = new WebClientOptions(((HttpClientImpl) httpClient).getOptions());
    actualOptions.init(options);
    return new WebClientBase(httpClient, actualOptions);
  }

  /**
   * Create an HTTP request to send to the server at the specified host and port.
   * @param method  the HTTP method
   * @param port  the port
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> request(HttpMethod method, int port, String host, String requestURI);

  /**
   * Create an HTTP request to send to the server at the specified host and default port.
   * @param method  the HTTP method
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> request(HttpMethod method, String host, String requestURI);

  /**
   * Create an HTTP request to send to the server at the default host and port.
   * @param method  the HTTP method
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> request(HttpMethod method, String requestURI);

  /**
   * Create an HTTP request to send to the server at the specified host and port.
   * @param method  the HTTP method
   * @param options  the request options
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> request(HttpMethod method, RequestOptions options);

  /**
   * Create an HTTP request to send to the server using an absolute URI
   * @param method  the HTTP method
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> requestAbs(HttpMethod method, String absoluteURI);

  /**
   * Create an HTTP GET request to send to the server at the default host and port.
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> get(String requestURI);

  /**
   * Create an HTTP GET request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> get(int port, String host, String requestURI);

  /**
   * Create an HTTP GET request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> get(String host, String requestURI);

  /**
   * Create an HTTP GET request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> getAbs(String absoluteURI);

  /**
   * Create an HTTP POST request to send to the server at the default host and port.
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> post(String requestURI);

  /**
   * Create an HTTP POST request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> post(int port, String host, String requestURI);

  /**
   * Create an HTTP POST request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> post(String host, String requestURI);

  /**
   * Create an HTTP POST request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> postAbs(String absoluteURI);

  /**
   * Create an HTTP PUT request to send to the server at the default host and port.
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> put(String requestURI);

  /**
   * Create an HTTP PUT request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> put(int port, String host, String requestURI);

  /**
   * Create an HTTP PUT request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> put(String host, String requestURI);

  /**
   * Create an HTTP PUT request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> putAbs(String absoluteURI);

  /**
   * Create an HTTP DELETE request to send to the server at the default host and port.
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> delete(String requestURI);

  /**
   * Create an HTTP DELETE request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> delete(int port, String host, String requestURI);

  /**
   * Create an HTTP DELETE request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> delete(String host, String requestURI);

  /**
   * Create an HTTP DELETE request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> deleteAbs(String absoluteURI);

  /**
   * Create an HTTP PATCH request to send to the server at the default host and port.
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> patch(String requestURI);

  /**
   * Create an HTTP PATCH request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> patch(int port, String host, String requestURI);

  /**
   * Create an HTTP PATCH request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> patch(String host, String requestURI);

  /**
   * Create an HTTP PATCH request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> patchAbs(String absoluteURI);

  /**
   * Create an HTTP HEAD request to send to the server at the default host and port.
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> head(String requestURI);

  /**
   * Create an HTTP HEAD request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> head(int port, String host, String requestURI);

  /**
   * Create an HTTP HEAD request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> head(String host, String requestURI);

  /**
   * Create an HTTP HEAD request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  HttpRequest<Buffer> headAbs(String absoluteURI);

  /**
   * Close the client. Closing will close down any pooled connections.
   * Clients should always be closed after use.
   */
  void close();
}
