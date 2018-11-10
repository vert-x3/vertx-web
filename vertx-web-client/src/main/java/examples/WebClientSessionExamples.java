package examples;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientSession;

public class WebClientSessionExamples {

  public void create(Vertx vertx) {
    WebClient client = WebClient.create(vertx);
    WebClientSession session = WebClientSession.create(client);
  }
  
  public void setHeaders(WebClient client, String jwtToken) {
    WebClientSession session = WebClientSession.create(client);
    session.addHeader("my-jwt-token", jwtToken);
  }
  
}
