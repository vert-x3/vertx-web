package io.vertx.webclient.impl;

import io.vertx.core.VertxException;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.webclient.HttpRequest;
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
  public HttpRequest get(int port, String host, String requestURI) {
    HttpRequestImpl builder = new HttpRequestImpl(client, HttpMethod.GET);
    return createRequestBuilder(port, host, requestURI, builder);
  }

  @Override
  public HttpRequest post(int port, String host, String requestURI) {
    HttpRequestImpl builder = new HttpRequestImpl(client, HttpMethod.POST);
    return createRequestBuilder(port, host, requestURI, builder);
  }

  @Override
  public HttpRequest put(int port, String host, String requestURI) {
    HttpRequestImpl builder = new HttpRequestImpl(client, HttpMethod.PUT);
    return createRequestBuilder(port, host, requestURI, builder);
  }

  @Override
  public HttpRequest delete(int port, String host, String requestURI) {
    HttpRequestImpl builder = new HttpRequestImpl(client, HttpMethod.DELETE);
    return createRequestBuilder(port, host, requestURI, builder);
  }

  @Override
  public HttpRequest patch(int port, String host, String requestURI) {
    HttpRequestImpl builder = new HttpRequestImpl(client, HttpMethod.PATCH);
    return createRequestBuilder(port, host, requestURI, builder);
  }

  @Override
  public HttpRequest head(int port, String host, String requestURI) {
    HttpRequestImpl builder = new HttpRequestImpl(client, HttpMethod.HEAD);
    return createRequestBuilder(port, host, requestURI, builder);
  }

  @Override
  public HttpRequest getAbs(String absoluteURI) {
    HttpRequestImpl builder = new HttpRequestImpl(client, HttpMethod.GET);
    URL url = parseUrl(absoluteURI);

    return createRequestBuilder(url.getPort(), url.getHost(), url.getFile(), builder);
  }

  @Override
  public HttpRequest postAbs(String absoluteURI) {
    HttpRequestImpl builder = new HttpRequestImpl(client, HttpMethod.POST);
    URL url = parseUrl(absoluteURI);

    return createRequestBuilder(url.getPort(), url.getHost(), url.getFile(), builder);
  }

  @Override
  public HttpRequest putAbs(String absoluteURI) {
    HttpRequestImpl builder = new HttpRequestImpl(client, HttpMethod.PUT);
    URL url = parseUrl(absoluteURI);

    return createRequestBuilder(url.getPort(), url.getHost(), url.getFile(), builder);
  }

  @Override
  public HttpRequest deleteAbs(String absoluteURI) {
    HttpRequestImpl builder = new HttpRequestImpl(client, HttpMethod.DELETE);
    URL url = parseUrl(absoluteURI);

    return createRequestBuilder(url.getPort(), url.getHost(), url.getFile(), builder);
  }

  @Override
  public HttpRequest patchAbs(String absoluteURI) {
    HttpRequestImpl builder = new HttpRequestImpl(client, HttpMethod.PATCH);
    URL url = parseUrl(absoluteURI);

    return createRequestBuilder(url.getPort(), url.getHost(), url.getFile(), builder);
  }

  @Override
  public HttpRequest headAbs(String absoluteURI) {
    HttpRequestImpl builder = new HttpRequestImpl(client, HttpMethod.HEAD);
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

  private HttpRequest createRequestBuilder(int port, String host, String requestURI, HttpRequestImpl get) {
    get.port = port;
    get.host = host;
    get.requestURI = requestURI;
    return get;
  }
}
