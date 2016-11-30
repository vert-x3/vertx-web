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

  HttpRequestBuilder get(int port, String host, String requestURI);

  HttpRequestBuilder post(int port, String host, String requestURI);

  HttpRequestBuilder put(int port, String host, String requestURI);

  HttpRequestBuilder delete(int port, String host, String requestURI);

  HttpRequestBuilder patch(int port, String host, String requestURI);

  HttpRequestBuilder head(int port, String host, String requestURI);

  HttpRequestBuilder getAbs(String absoluteURI);

  HttpRequestBuilder postAbs(String absoluteURI);

  HttpRequestBuilder putAbs(String absoluteURI);

  HttpRequestBuilder deleteAbs(String absoluteURI);

  HttpRequestBuilder patchAbs(String absoluteURI);

  HttpRequestBuilder headAbs(String absoluteURI);

}
