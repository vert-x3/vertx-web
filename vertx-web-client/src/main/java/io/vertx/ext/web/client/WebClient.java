/*
 * Copyright 2022 Red Hat, Inc.
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
import io.vertx.core.http.impl.HttpClientInternal;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.client.impl.WebClientBase;
import io.vertx.uritemplate.UriTemplate;

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
    WebClientOptions actualOptions = new WebClientOptions(((HttpClientInternal) httpClient).options());
    actualOptions.init(options);
    return new WebClientBase(httpClient, actualOptions);
  }

  /**
   * Create an HTTP request to send to the server at the specified host and port.
   * @param method  the HTTP method
   * @param port  the port
   * @param host  the host
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> request(HttpMethod method, int port, String host, String requestURI) {
    return request(method, null, port, host, requestURI);
  }

  /**
   * Create an HTTP request to send to the server at the specified host and port.
   * @param method  the HTTP method
   * @param port  the port
   * @param host  the host
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> request(HttpMethod method, int port, String host, UriTemplate requestURI) {
    return request(method, null, port, host, requestURI);
  }

  /**
   * Like {@link #request(HttpMethod, int, String, String)} using the {@code serverAddress} parameter to connect to the
   * server instead of the {@code port} and {@code host} parameters.
   * <p>
   * The request host header will still be created from the {@code port} and {@code host} parameters.
   * <p>
   * Use {@link SocketAddress#domainSocketAddress(String)} to connect to a unix domain socket server.
   */
  HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, int port, String host, String requestURI);

  /**
   * Like {@link #request(HttpMethod, int, String, UriTemplate)} using the {@code serverAddress} parameter to connect to the
   * server instead of the {@code port} and {@code host} parameters.
   * <p>
   * The request host header will still be created from the {@code port} and {@code host} parameters.
   * <p>
   * Use {@link SocketAddress#domainSocketAddress(String)} to connect to a unix domain socket server.
   */
  HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, int port, String host, UriTemplate requestURI);

  /**
   * Create an HTTP request to send to the server at the specified host and default port.
   * @param method  the HTTP method
   * @param host  the host
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> request(HttpMethod method, String host, String requestURI) {
    return request(method, null, host, requestURI);
  }

  /**
   * Create an HTTP request to send to the server at the specified host and default port.
   * @param method  the HTTP method
   * @param host  the host
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> request(HttpMethod method, String host, UriTemplate requestURI) {
    return request(method, null, host, requestURI);
  }

  /**
   * Like {@link #request(HttpMethod, String, String)} using the {@code serverAddress} parameter to connect to the
   * server instead of the default port and {@code host} parameter.
   * <p>
   * The request host header will still be created from the default port and {@code host} parameter.
   * <p>
   * Use {@link SocketAddress#domainSocketAddress(String)} to connect to a unix domain socket server.
   */
  HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String host, String requestURI);

  /**
   * Like {@link #request(HttpMethod, String, UriTemplate)} using the {@code serverAddress} parameter to connect to the
   * server instead of the default port and {@code host} parameter.
   * <p>
   * The request host header will still be created from the default port and {@code host} parameter.
   * <p>
   * Use {@link SocketAddress#domainSocketAddress(String)} to connect to a unix domain socket server.
   */
  HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String host, UriTemplate requestURI);

  /**
   * Create an HTTP request to send to the server at the default host and port.
   * @param method  the HTTP method
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> request(HttpMethod method, String requestURI) {
    return request(method, (SocketAddress) null, requestURI);
  }

  /**
   * Create an HTTP request to send to the server at the default host and port.
   * @param method  the HTTP method
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> request(HttpMethod method, UriTemplate requestURI) {
    return request(method, (SocketAddress) null, requestURI);
  }

  /**
   * Like {@link #request(HttpMethod, String)} using the {@code serverAddress} parameter to connect to the
   * server instead of the default port and default host.
   * <p>
   * The request host header will still be created from the default port and default host.
   * <p>
   * Use {@link SocketAddress#domainSocketAddress(String)} to connect to a unix domain socket server.
   */
  HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String requestURI);

  /**
   * Like {@link #request(HttpMethod, UriTemplate)} using the {@code serverAddress} parameter to connect to the
   * server instead of the default port and default host.
   * <p>
   * The request host header will still be created from the default port and default host.
   * <p>
   * Use {@link SocketAddress#domainSocketAddress(String)} to connect to a unix domain socket server.
   */
  HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, UriTemplate requestURI);

  /**
   * Create an HTTP request to send to the server at the specified host and port.
   * @param method  the HTTP method
   * @param options  the request options
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> request(HttpMethod method, RequestOptions options) {
    return request(method, null, options);
  }

  /**
   * Like {@link #request(HttpMethod, RequestOptions)} using the {@code serverAddress} parameter to connect to the
   * server instead of the {@code options} parameter.
   * <p>
   * The request host header will still be created from the {@code options} parameter.
   * <p>
   * Use {@link SocketAddress#domainSocketAddress(String)} to connect to a unix domain socket server.
   */
  HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, RequestOptions options);

  /**
   * Create an HTTP request to send to the server using an absolute URI
   * @param method  the HTTP method
   * @param absoluteURI the absolute URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> requestAbs(HttpMethod method, String absoluteURI) {
    return requestAbs(method, null, absoluteURI);
  }

  /**
   * Create an HTTP request to send to the server using an absolute URI
   * @param method  the HTTP method
   * @param absoluteURI the absolute URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> requestAbs(HttpMethod method, UriTemplate absoluteURI) {
    return requestAbs(method, null, absoluteURI);
  }

  /**
   * Like {@link #requestAbs(HttpMethod, String)} using the {@code serverAddress} parameter to connect to the
   * server instead of the {@code absoluteURI} parameter.
   * <p>
   * The request host header will still be created from the {@code absoluteURI} parameter.
   * <p>
   * Use {@link SocketAddress#domainSocketAddress(String)} to connect to a unix domain socket server.
   */
  HttpRequest<Buffer> requestAbs(HttpMethod method, SocketAddress serverAddress, String absoluteURI);

  /**
   * Like {@link #requestAbs(HttpMethod, UriTemplate)} using the {@code serverAddress} parameter to connect to the
   * server instead of the {@code absoluteURI} parameter.
   * <p>
   * The request host header will still be created from the {@code absoluteURI} parameter.
   * <p>
   * Use {@link SocketAddress#domainSocketAddress(String)} to connect to a unix domain socket server.
   */
  HttpRequest<Buffer> requestAbs(HttpMethod method, SocketAddress serverAddress, UriTemplate absoluteURI);

  /**
   * Create an HTTP GET request to send to the server at the default host and port.
   * @param requestURI the request URI
   * @return an HTTP client request object
   */
  default HttpRequest<Buffer> get(String requestURI) {
    return request(HttpMethod.GET, requestURI);
  }

  /**
   * Create an HTTP GET request to send to the server at the default host and port.
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return an HTTP client request object
   */
  default HttpRequest<Buffer> get(UriTemplate requestURI) {
    return request(HttpMethod.GET, requestURI);
  }

  /**
   * Create an HTTP GET request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> get(int port, String host, String requestURI) {
    return request(HttpMethod.GET, port, host, requestURI);
  }

  /**
   * Create an HTTP GET request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> get(int port, String host, UriTemplate requestURI) {
    return request(HttpMethod.GET, port, host, requestURI);
  }

  /**
   * Create an HTTP GET request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> get(String host, String requestURI) {
    return request(HttpMethod.GET, host, requestURI);
  }

  /**
   * Create an HTTP GET request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> get(String host, UriTemplate requestURI) {
    return request(HttpMethod.GET, host, requestURI);
  }

  /**
   * Create an HTTP GET request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> getAbs(String absoluteURI) {
    return requestAbs(HttpMethod.GET, absoluteURI);
  }

  /**
   * Create an HTTP GET request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI the absolute URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> getAbs(UriTemplate absoluteURI) {
    return requestAbs(HttpMethod.GET, absoluteURI);
  }

  /**
   * Create an HTTP POST request to send to the server at the default host and port.
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> post(String requestURI) {
    return request(HttpMethod.POST, requestURI);
  }

  /**
   * Create an HTTP POST request to send to the server at the default host and port.
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> post(UriTemplate requestURI) {
    return request(HttpMethod.POST, requestURI);
  }

  /**
   * Create an HTTP POST request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> post(int port, String host, String requestURI) {
    return request(HttpMethod.POST, port, host, requestURI);
  }

  /**
   * Create an HTTP POST request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> post(int port, String host, UriTemplate requestURI) {
    return request(HttpMethod.POST, port, host, requestURI);
  }

  /**
   * Create an HTTP POST request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> post(String host, String requestURI) {
    return request(HttpMethod.POST, host, requestURI);
  }

  /**
   * Create an HTTP POST request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> post(String host, UriTemplate requestURI) {
    return request(HttpMethod.POST, host, requestURI);
  }

  /**
   * Create an HTTP POST request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> postAbs(String absoluteURI) {
    return requestAbs(HttpMethod.POST, absoluteURI);
  }

  /**
   * Create an HTTP POST request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI the absoluate URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> postAbs(UriTemplate absoluteURI) {
    return requestAbs(HttpMethod.POST, absoluteURI);
  }

  /**
   * Create an HTTP PUT request to send to the server at the default host and port.
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> put(String requestURI) {
    return request(HttpMethod.PUT, requestURI);
  }

  /**
   * Create an HTTP PUT request to send to the server at the default host and port.
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> put(UriTemplate requestURI) {
    return request(HttpMethod.PUT, requestURI);
  }

  /**
   * Create an HTTP PUT request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> put(int port, String host, String requestURI) {
    return request(HttpMethod.PUT, port, host, requestURI);
  }

  /**
   * Create an HTTP PUT request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> put(int port, String host, UriTemplate requestURI) {
    return request(HttpMethod.PUT, port, host, requestURI);
  }

  /**
   * Create an HTTP PUT request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> put(String host, String requestURI) {
    return request(HttpMethod.PUT, host, requestURI);
  }

  /**
   * Create an HTTP PUT request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> put(String host, UriTemplate requestURI) {
    return request(HttpMethod.PUT, host, requestURI);
  }

  /**
   * Create an HTTP PUT request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> putAbs(String absoluteURI) {
    return requestAbs(HttpMethod.PUT, absoluteURI);
  }

  /**
   * Create an HTTP PUT request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI the absolute URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> putAbs(UriTemplate absoluteURI) {
    return requestAbs(HttpMethod.PUT, absoluteURI);
  }

  /**
   * Create an HTTP DELETE request to send to the server at the default host and port.
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> delete(String requestURI) {
    return request(HttpMethod.DELETE, requestURI);
  }

  /**
   * Create an HTTP DELETE request to send to the server at the default host and port.
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> delete(UriTemplate requestURI) {
    return request(HttpMethod.DELETE, requestURI);
  }

  /**
   * Create an HTTP DELETE request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> delete(int port, String host, String requestURI) {
    return request(HttpMethod.DELETE, port, host, requestURI);
  }

  /**
   * Create an HTTP DELETE request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> delete(int port, String host, UriTemplate requestURI) {
    return request(HttpMethod.DELETE, port, host, requestURI);
  }

  /**
   * Create an HTTP DELETE request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> delete(String host, String requestURI) {
    return request(HttpMethod.DELETE, host, requestURI);
  }

  /**
   * Create an HTTP DELETE request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> delete(String host, UriTemplate requestURI) {
    return request(HttpMethod.DELETE, host, requestURI);
  }

  /**
   * Create an HTTP DELETE request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> deleteAbs(String absoluteURI) {
    return requestAbs(HttpMethod.DELETE, absoluteURI);
  }

  /**
   * Create an HTTP DELETE request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI the absolute URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> deleteAbs(UriTemplate absoluteURI) {
    return requestAbs(HttpMethod.DELETE, absoluteURI);
  }

  /**
   * Create an HTTP PATCH request to send to the server at the default host and port.
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> patch(String requestURI) {
    return request(HttpMethod.PATCH, requestURI);
  }

  /**
   * Create an HTTP PATCH request to send to the server at the default host and port.
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> patch(UriTemplate requestURI) {
    return request(HttpMethod.PATCH, requestURI);
  }

  /**
   * Create an HTTP PATCH request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> patch(int port, String host, String requestURI) {
    return request(HttpMethod.PATCH, port, host, requestURI);
  }

  /**
   * Create an HTTP PATCH request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> patch(int port, String host, UriTemplate requestURI) {
    return request(HttpMethod.PATCH, port, host, requestURI);
  }

  /**
   * Create an HTTP PATCH request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> patch(String host, String requestURI) {
    return request(HttpMethod.PATCH, host, requestURI);
  }

  /**
   * Create an HTTP PATCH request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> patch(String host, UriTemplate requestURI) {
    return request(HttpMethod.PATCH, host, requestURI);
  }

  /**
   * Create an HTTP PATCH request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> patchAbs(String absoluteURI) {
    return requestAbs(HttpMethod.PATCH, absoluteURI);
  }

  /**
   * Create an HTTP PATCH request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI the absolute URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> patchAbs(UriTemplate absoluteURI) {
    return requestAbs(HttpMethod.PATCH, absoluteURI);
  }

  /**
   * Create an HTTP HEAD request to send to the server at the default host and port.
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> head(String requestURI) {
    return request(HttpMethod.HEAD, requestURI);
  }

  /**
   * Create an HTTP HEAD request to send to the server at the default host and port.
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> head(UriTemplate requestURI) {
    return request(HttpMethod.HEAD, requestURI);
  }

  /**
   * Create an HTTP HEAD request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> head(int port, String host, String requestURI) {
    return request(HttpMethod.HEAD, port, host, requestURI);
  }

  /**
   * Create an HTTP HEAD request to send to the server at the specified host and port.
   * @param port  the port
   * @param host  the host
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> head(int port, String host, UriTemplate requestURI) {
    return request(HttpMethod.HEAD, port, host, requestURI);
  }

  /**
   * Create an HTTP HEAD request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI the request URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> head(String host, String requestURI) {
    return request(HttpMethod.HEAD, host, requestURI);
  }

  /**
   * Create an HTTP HEAD request to send to the server at the specified host and default port.
   * @param host  the host
   * @param requestURI the request URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> head(String host, UriTemplate requestURI) {
    return request(HttpMethod.HEAD, host, requestURI);
  }

  /**
   * Create an HTTP HEAD request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI  the absolute URI
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> headAbs(String absoluteURI) {
    return requestAbs(HttpMethod.HEAD, absoluteURI);
  }

  /**
   * Create an HTTP HEAD request to send to the server using an absolute URI, specifying a response handler to receive
   * the response
   * @param absoluteURI the absolute URI as a {@link UriTemplate}
   * @return  an HTTP client request object
   */
  default HttpRequest<Buffer> headAbs(UriTemplate absoluteURI) {
    return requestAbs(HttpMethod.HEAD, absoluteURI);
  }

  /**
   * Close the client. Closing will close down any pooled connections.
   * Clients should always be closed after use.
   */
  void close();
}
