package examples;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.webclient.HttpRequestTemplate;
import io.vertx.webclient.HttpResponse;
import io.vertx.webclient.WebClient;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class WebClientExamples {

  public void simpleGet(WebClient client) {
    client
      .get(8080, "localhost", "/something")
      .send(ar -> {
        if (ar.succeeded()) {
          // Obtain response
          HttpClientResponse resp = ar.result();
        }
      });
  }

  public void multipleGet(WebClient client) {
    HttpRequestTemplate get = client.get(8080, "localhost", "/something");
    get.send(ar -> {
    });
    // Same request again
    get.send(ar -> {
    });
  }

  public void sendStream(WebClient client, FileSystem fs) {
    AsyncFile file = fs.openBlocking("content.txt", new OpenOptions());
    client
      .get(8080, "localhost", "/something")
      .sendStream(file, resp -> {
      });
  }

  public void bufferBody(WebClient client) {
    client
      .get(8080, "localhost", "/something")
      .bufferBody()
      .send(ar -> {
        if (ar.succeeded()) {
          HttpResponse<Buffer> resp = ar.result();
          JsonObject body = resp.bodyAsJsonObject();
        }
      });
  }

  public void bufferBodyDecodeAsJsonObject(WebClient client) {
    client
      .get(8080, "localhost", "/something")
      .bufferBody()
      .asJsonObject()
      .send(ar -> {
        if (ar.succeeded()) {
          HttpResponse<JsonObject> resp = ar.result();
          JsonObject body = resp.body();
        }
      });
  }
}
