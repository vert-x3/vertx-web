package io.vertx.webclient.impl;

import io.vertx.core.VertxException;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.webclient.HttpRequestBuilder;
import io.vertx.webclient.WebClient;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class WebClientImpl implements WebClient {

  private final HttpClient client;

  public WebClientImpl(HttpClient client) {
    this.client = client;
  }

  @Override
  public HttpRequestBuilder get(int port, String host, String requestURI) {
    HttpRequestBuilderImpl builder = new HttpRequestBuilderImpl(client, HttpMethod.GET);
    return createRequestBuilder(port, host, requestURI, builder);
  }

  @Override
  public HttpRequestBuilder post(int port, String host, String requestURI) {
    HttpRequestBuilderImpl builder = new HttpRequestBuilderImpl(client, HttpMethod.POST);
    return createRequestBuilder(port, host, requestURI, builder);
  }

  @Override
  public HttpRequestBuilder put(int port, String host, String requestURI) {
    HttpRequestBuilderImpl builder = new HttpRequestBuilderImpl(client, HttpMethod.PUT);
    return createRequestBuilder(port, host, requestURI, builder);
  }

  @Override
  public HttpRequestBuilder delete(int port, String host, String requestURI) {
    HttpRequestBuilderImpl builder = new HttpRequestBuilderImpl(client, HttpMethod.DELETE);
    return createRequestBuilder(port, host, requestURI, builder);
  }

  @Override
  public HttpRequestBuilder patch(int port, String host, String requestURI) {
    HttpRequestBuilderImpl builder = new HttpRequestBuilderImpl(client, HttpMethod.PATCH);
    return createRequestBuilder(port, host, requestURI, builder);
  }

  @Override
  public HttpRequestBuilder head(int port, String host, String requestURI) {
    HttpRequestBuilderImpl builder = new HttpRequestBuilderImpl(client, HttpMethod.HEAD);
    return createRequestBuilder(port, host, requestURI, builder);
  }

  @Override
  public HttpRequestBuilder getAbs(String absoluteURI) {
    HttpRequestBuilderImpl builder = new HttpRequestBuilderImpl(client, HttpMethod.GET);
    URL url = parseUrl(absoluteURI);

    return createRequestBuilder(url.getPort(), url.getHost(), url.getFile(), builder);
  }

  @Override
  public HttpRequestBuilder postAbs(String absoluteURI) {
    HttpRequestBuilderImpl builder = new HttpRequestBuilderImpl(client, HttpMethod.POST);
    URL url = parseUrl(absoluteURI);

    return createRequestBuilder(url.getPort(), url.getHost(), url.getFile(), builder);
  }

  @Override
  public HttpRequestBuilder putAbs(String absoluteURI) {
    HttpRequestBuilderImpl builder = new HttpRequestBuilderImpl(client, HttpMethod.PUT);
    URL url = parseUrl(absoluteURI);

    return createRequestBuilder(url.getPort(), url.getHost(), url.getFile(), builder);
  }

  @Override
  public HttpRequestBuilder deleteAbs(String absoluteURI) {
    HttpRequestBuilderImpl builder = new HttpRequestBuilderImpl(client, HttpMethod.DELETE);
    URL url = parseUrl(absoluteURI);

    return createRequestBuilder(url.getPort(), url.getHost(), url.getFile(), builder);
  }

  @Override
  public HttpRequestBuilder patchAbs(String absoluteURI) {
    HttpRequestBuilderImpl builder = new HttpRequestBuilderImpl(client, HttpMethod.PATCH);
    URL url = parseUrl(absoluteURI);

    return createRequestBuilder(url.getPort(), url.getHost(), url.getFile(), builder);
  }

  @Override
  public HttpRequestBuilder headAbs(String absoluteURI) {
    HttpRequestBuilderImpl builder = new HttpRequestBuilderImpl(client, HttpMethod.HEAD);
    URL url = parseUrl(absoluteURI);

    return createRequestBuilder(url.getPort(), url.getHost(), url.getFile(), builder);
  }

  private URL parseUrl(String surl) {
    // Note - parsing a URL this way is slower than specifying host, port and relativeURI
    try {
      return new URL(surl);
    } catch (MalformedURLException e) {
      throw new VertxException("Invalid url: " + surl);
    }
  }

  private HttpRequestBuilder createRequestBuilder(int port, String host, String requestURI, HttpRequestBuilderImpl get) {
    get.port = port;
    get.host = host;
    get.requestURI = requestURI;
    return get;
  }
}
