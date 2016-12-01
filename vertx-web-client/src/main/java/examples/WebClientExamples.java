package examples;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.webclient.BodyCodec;
import io.vertx.webclient.HttpRequest;
import io.vertx.webclient.HttpResponse;
import io.vertx.webclient.WebClient;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class WebClientExamples {

  public void create(Vertx vertx) {
    WebClient client = WebClient.create(vertx) ;
  }

  public void createFromOptions(Vertx vertx) {
    HttpClientOptions options = new HttpClientOptions().setKeepAlive(false);
    WebClient client = WebClient.wrap(vertx.createHttpClient(options));
  }

  public void simpleGetAndHead(Vertx vertx) {

    WebClient client = WebClient.create(vertx) ;

    // Send a GET request
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send(ar -> {
        if (ar.succeeded()) {
          // Obtain response
          HttpResponse<Buffer> response = ar.result();

          System.out.println("Received response with status code" + response.statusCode());
        } else {
          System.out.println("Something went wrong " + ar.cause().getMessage());
        }
      });

    // Send a HEAD request
    client
      .head(8080, "myserver.mycompany.com", "/some-uri")
      .send(ar -> {
        if (ar.succeeded()) {
          // Obtain response
          HttpResponse<Buffer> response = ar.result();

          System.out.println("Received response with status code" + response.statusCode());
        } else {
          System.out.println("Something went wrong " + ar.cause().getMessage());
        }
      });
  }

  public void simpleGets(WebClient client) {
    HttpRequest get = client.get(8080, "myserver.mycompany.com", "/some-uri");
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
      .send(BodyCodec.jsonObject(), ar -> {
        if (ar.succeeded()) {
          HttpResponse<JsonObject> resp = ar.result();
          JsonObject body = resp.body();
        }
      });
  }
}
