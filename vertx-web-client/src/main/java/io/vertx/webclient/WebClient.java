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
package io.vertx.webclient;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.webclient.impl.WebClientImpl;

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
   * Create a web client using the provided {@code vertx} instance.
   *
   * @param vertx the vertx instance
   * @return the created web client
   */
  static WebClient create(Vertx vertx) {
    return new WebClientImpl(vertx.createHttpClient());
  }

  /**
   * Wrap an {@code httpClient} with a web client.
   *
   * @param httpClient the {@link HttpClient} to wrap
   * @return the web client
   */
  static WebClient wrap(HttpClient httpClient) {
    return new WebClientImpl(httpClient);
  }

  /**
   * Create an HTTP request to send to the server at the specified host and port.
   * @param method  the HTTP method
   * @param port  the port
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest request(HttpMethod method, int port, String host, String requestURI);

  /**
   * Create an HTTP request to send to the server at the specified host and default port.
   * @param method  the HTTP method
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest request(HttpMethod method, String host, String requestURI);

  /**
   * Create an HTTP request to send to the server at the default host and port.
   * @param method  the HTTP method
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest request(HttpMethod method, String requestURI);

  /**
   * Create an HTTP request to send to the server using an absolute URI
   * @param method  the HTTP method
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  HttpRequest requestAbs(HttpMethod method, String absoluteURI);

  /**
   * Create an HTTP GET request to send to the server at the default host and port.
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest get(String requestURI);

  /**
   * Create an HTTP GET request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest get(int port, String host, String requestURI);

  /**
   * Create an HTTP GET request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest get(String host, String requestURI);

  /**
   * Create an HTTP GET request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  HttpRequest getAbs(String absoluteURI);

  /**
   * Create an HTTP POST request to send to the server at the default host and port.
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest post(String requestURI);

  /**
   * Create an HTTP POST request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest post(int port, String host, String requestURI);

  /**
   * Create an HTTP POST request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest post(String host, String requestURI);

  /**
   * Create an HTTP POST request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  HttpRequest postAbs(String absoluteURI);

  /**
   * Create an HTTP PUT request to send to the server at the default host and port.
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest put(String requestURI);

  /**
   * Create an HTTP PUT request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest put(int port, String host, String requestURI);

  /**
   * Create an HTTP PUT request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest put(String host, String requestURI);

  /**
   * Create an HTTP PUT request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  HttpRequest putAbs(String absoluteURI);

  /**
   * Create an HTTP DELETE request to send to the server at the default host and port.
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest delete(String requestURI);

  /**
   * Create an HTTP DELETE request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest delete(int port, String host, String requestURI);

  /**
   * Create an HTTP DELETE request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest delete(String host, String requestURI);

  /**
   * Create an HTTP DELETE request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  HttpRequest deleteAbs(String absoluteURI);

  /**
   * Create an HTTP PATCH request to send to the server at the default host and port.
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest patch(String requestURI);

  /**
   * Create an HTTP PATCH request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest patch(int port, String host, String requestURI);

  /**
   * Create an HTTP PATCH request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest patch(String host, String requestURI);

  /**
   * Create an HTTP PATCH request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  HttpRequest patchAbs(String absoluteURI);

  /**
   * Create an HTTP HEAD request to send to the server at the default host and port.
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest head(String requestURI);

  /**
   * Create an HTTP HEAD request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest head(int port, String host, String requestURI);

  /**
   * Create an HTTP HEAD request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI  the relative URI
   * @return  an HTTP client request object
   */
  HttpRequest head(String host, String requestURI);

  /**
   * Create an HTTP HEAD request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  HttpRequest headAbs(String absoluteURI);

  /**
   * Close the client. Closing will close down any pooled connections.
   * Clients should always be closed after use.
   */
  void close();

}
