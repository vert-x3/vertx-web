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

  HttpRequest get(int port, String host, String requestURI);

  HttpRequest post(int port, String host, String requestURI);

  HttpRequest put(int port, String host, String requestURI);

  HttpRequest delete(int port, String host, String requestURI);

  HttpRequest patch(int port, String host, String requestURI);

  HttpRequest head(int port, String host, String requestURI);

  HttpRequest getAbs(String absoluteURI);

  HttpRequest postAbs(String absoluteURI);

  HttpRequest putAbs(String absoluteURI);

  HttpRequest deleteAbs(String absoluteURI);

  HttpRequest patchAbs(String absoluteURI);

  HttpRequest headAbs(String absoluteURI);

}
