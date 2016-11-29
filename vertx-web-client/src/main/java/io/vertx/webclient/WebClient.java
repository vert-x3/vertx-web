package io.vertx.webclient;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.http.HttpClient;
import io.vertx.webclient.impl.WebClientImpl;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface WebClient {

  static WebClient create(HttpClient client) {
    return new WebClientImpl(client);
  }

  HttpRequestTemplate get(int port, String host, String requestURI);

  HttpRequestTemplate post(int port, String host, String requestURI);

  HttpRequestTemplate put(int port, String host, String requestURI);

  HttpRequestTemplate delete(int port, String host, String requestURI);

  HttpRequestTemplate patch(int port, String host, String requestURI);

  HttpRequestTemplate head(int port, String host, String requestURI);

  HttpRequestTemplate getAbs(String absoluteURI);

  HttpRequestTemplate postAbs(String absoluteURI);

  HttpRequestTemplate putAbs(String absoluteURI);

  HttpRequestTemplate deleteAbs(String absoluteURI);

  HttpRequestTemplate patchAbs(String absoluteURI);

  HttpRequestTemplate headAbs(String absoluteURI);

}
