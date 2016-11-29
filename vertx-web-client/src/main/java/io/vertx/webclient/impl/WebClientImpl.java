package io.vertx.webclient.impl;

import io.vertx.core.VertxException;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.webclient.HttpRequestTemplate;
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
  public HttpRequestTemplate get(int port, String host, String requestURI) {
    HttpRequestTemplateImpl template = new HttpRequestTemplateImpl(client, HttpMethod.GET);
    return createRequestTemplate(port, host, requestURI, template);
  }

  @Override
  public HttpRequestTemplate post(int port, String host, String requestURI) {
    HttpRequestTemplateImpl template = new HttpRequestTemplateImpl(client, HttpMethod.POST);
    return createRequestTemplate(port, host, requestURI, template);
  }

  @Override
  public HttpRequestTemplate put(int port, String host, String requestURI) {
    HttpRequestTemplateImpl template = new HttpRequestTemplateImpl(client, HttpMethod.PUT);
    return createRequestTemplate(port, host, requestURI, template);
  }

  @Override
  public HttpRequestTemplate delete(int port, String host, String requestURI) {
    HttpRequestTemplateImpl template = new HttpRequestTemplateImpl(client, HttpMethod.DELETE);
    return createRequestTemplate(port, host, requestURI, template);
  }

  @Override
  public HttpRequestTemplate patch(int port, String host, String requestURI) {
    HttpRequestTemplateImpl template = new HttpRequestTemplateImpl(client, HttpMethod.PATCH);
    return createRequestTemplate(port, host, requestURI, template);
  }

  @Override
  public HttpRequestTemplate head(int port, String host, String requestURI) {
    HttpRequestTemplateImpl template = new HttpRequestTemplateImpl(client, HttpMethod.HEAD);
    return createRequestTemplate(port, host, requestURI, template);
  }

  @Override
  public HttpRequestTemplate getAbs(String absoluteURI) {
    HttpRequestTemplateImpl template = new HttpRequestTemplateImpl(client, HttpMethod.GET);
    URL url = parseUrl(absoluteURI);

    return createRequestTemplate(url.getPort(), url.getHost(), url.getFile(), template);
  }

  @Override
  public HttpRequestTemplate postAbs(String absoluteURI) {
    HttpRequestTemplateImpl template = new HttpRequestTemplateImpl(client, HttpMethod.POST);
    URL url = parseUrl(absoluteURI);

    return createRequestTemplate(url.getPort(), url.getHost(), url.getFile(), template);
  }

  @Override
  public HttpRequestTemplate putAbs(String absoluteURI) {
    HttpRequestTemplateImpl template = new HttpRequestTemplateImpl(client, HttpMethod.PUT);
    URL url = parseUrl(absoluteURI);

    return createRequestTemplate(url.getPort(), url.getHost(), url.getFile(), template);
  }

  @Override
  public HttpRequestTemplate deleteAbs(String absoluteURI) {
    HttpRequestTemplateImpl template = new HttpRequestTemplateImpl(client, HttpMethod.DELETE);
    URL url = parseUrl(absoluteURI);

    return createRequestTemplate(url.getPort(), url.getHost(), url.getFile(), template);
  }

  @Override
  public HttpRequestTemplate patchAbs(String absoluteURI) {
    HttpRequestTemplateImpl template = new HttpRequestTemplateImpl(client, HttpMethod.PATCH);
    URL url = parseUrl(absoluteURI);

    return createRequestTemplate(url.getPort(), url.getHost(), url.getFile(), template);
  }

  @Override
  public HttpRequestTemplate headAbs(String absoluteURI) {
    HttpRequestTemplateImpl template = new HttpRequestTemplateImpl(client, HttpMethod.HEAD);
    URL url = parseUrl(absoluteURI);

    return createRequestTemplate(url.getPort(), url.getHost(), url.getFile(), template);
  }

  private URL parseUrl(String surl) {
    // Note - parsing a URL this way is slower than specifying host, port and relativeURI
    try {
      return new URL(surl);
    } catch (MalformedURLException e) {
      throw new VertxException("Invalid url: " + surl);
    }
  }

  private HttpRequestTemplate createRequestTemplate(int port, String host, String requestURI, HttpRequestTemplateImpl get) {
    get.port = port;
    get.host = host;
    get.requestURI = requestURI;
    return get;
  }
}
