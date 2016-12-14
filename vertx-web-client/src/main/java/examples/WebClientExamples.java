package examples;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.webclient.BodyCodec;
import io.vertx.webclient.HttpRequest;
import io.vertx.webclient.HttpResponse;
import io.vertx.webclient.WebClient;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class WebClientExamples {

  public void create(Vertx vertx) {
    WebClient client = WebClient.create(vertx);
  }

  public void createFromOptions(Vertx vertx) {
    HttpClientOptions options = new HttpClientOptions().setKeepAlive(false);
    WebClient client = WebClient.wrap(vertx.createHttpClient(options));
  }

  public void simpleGetAndHead(Vertx vertx) {

    WebClient client = WebClient.create(vertx);

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

  public void simpleGetWithParams(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .addQueryParam("param", "param_value")
      .send(ar -> {});
  }

  public void simpleGetWithInitialParams(WebClient client) {
    HttpRequest request = client.get(8080, "myserver.mycompany.com", "/some-uri?param1=param1_value&param2=param2_value");

    // Add param3
    request.addQueryParam("param3", "param3_value");

    // Overwrite param2
    request.setQueryParam("param2", "another_param2_value");
  }

  public void simpleGetOverwritePreviousParams(WebClient client) {
    HttpRequest request = client.get(8080, "myserver.mycompany.com", "/some-uri");

    // Add param1
    request.addQueryParam("param1", "param1_value");

    // Overwrite param1 and add param2
    request.uri("/some-uri?param1=param1_value&param2=param2_value");
  }

  public void multiGet(WebClient client) {
    HttpRequest get = client.get(8080, "myserver.mycompany.com", "/some-uri");
    get.send(ar -> {
      if (ar.succeeded()) {
        // Ok
      }
    });

    // Same request again
    get.send(ar -> {
      if (ar.succeeded()) {
        // Ok
      }
    });
  }

  public void multiGetCopy(WebClient client) {
    HttpRequest get = client.get(8080, "myserver.mycompany.com", "/some-uri");
    get.send(ar -> {
      if (ar.succeeded()) {
        // Ok
      }
    });

    // Same request again
    get.putHeader("an-header", "with-some-value")
      .send(ar -> {
      if (ar.succeeded()) {
        // Ok
      }
    });
  }

  public void timeout(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .timeout(5000)
      .send(ar -> {
        if (ar.succeeded()) {
          // Ok
        } else {
          // Might be a timeout when cause is java.util.concurrent.TimeoutException
        }
      });
  }

  public void sendBuffer(WebClient client, Buffer buffer) {
    // Send a buffer to the server using POST, the content-length header will be set for you
    client
      .post(8080, "myserver.mycompany.com", "/some-uri")
      .sendBuffer(buffer, ar -> {
        if (ar.succeeded()) {
          // Ok
        }
      });
  }

  public void sendStream(WebClient client, FileSystem fs) {
    fs.open("content.txt", new OpenOptions(), fileRes -> {
      if (fileRes.succeeded()) {
        ReadStream<Buffer> fileStream = fileRes.result();

        String fileLen = "1024";

        // Send the file to the server using POST
        client
          .post(8080, "myserver.mycompany.com", "/some-uri")
          .putHeader("content-length", fileLen)
          .sendStream(fileStream, ar -> {
            if (ar.succeeded()) {
              // Ok
            }
          });
      }
    });
  }

  public void sendStreamChunked(WebClient client, ReadStream<Buffer> stream) {
    // When the stream len is unknown sendStream sends the file to the server using chunked transfer encoding
    client
      .post(8080, "myserver.mycompany.com", "/some-uri")
      .sendStream(stream, resp -> {});
  }

  public void sendJsonObject(WebClient client) {
    client
      .post(8080, "myserver.mycompany.com", "/some-uri")
      .sendJsonObject(new JsonObject()
        .put("firstName", "Dale")
        .put("lastName", "Cooper"), ar -> {
        if (ar.succeeded()) {
          // Ok
        }
      });
  }

  static class User {
    String firstName;
    String lastName;
    public User(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }
    public String getFirstName() {
      return firstName;
    }
    public String getLastName() {
      return lastName;
    }
  }

  public void sendJsonPOJO(WebClient client) {
    client
      .post(8080, "myserver.mycompany.com", "/some-uri")
      .sendJson(new User("Dale", "Cooper"), ar -> {
        if (ar.succeeded()) {
          // Ok
        }
      });
  }

  public void sendForm(WebClient client) {
    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.set("firstName", "Dale");
    form.set("lastName", "Cooper");

    // Submit the form as a form URL encoded body
    client
      .post(8080, "myserver.mycompany.com", "/some-uri")
      .sendForm(form, ar -> {
        if (ar.succeeded()) {
          // Ok
        }
      });
  }

  public void sendMultipart(WebClient client) {
    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.set("firstName", "Dale");
    form.set("lastName", "Cooper");

    // Submit the form as a multipart form body
    client
      .post(8080, "myserver.mycompany.com", "/some-uri")
      .putHeader("content-type", "multipart/form-data")
      .sendForm(form, ar -> {
        if (ar.succeeded()) {
          // Ok
        }
      });
  }

  public void sendHeaders1(WebClient client) {
    HttpRequest request = client.get(8080, "myserver.mycompany.com", "/some-uri");
    MultiMap headers = request.headers();
    headers.set("content-type", "application/json");
    headers.set("other-header", "foo");
  }

  public void sendHeaders2(WebClient client) {
    HttpRequest request = client.get(8080, "myserver.mycompany.com", "/some-uri");
    request.putHeader("content-type", "application/json");
    request.putHeader("other-header", "foo");
  }

  public void receiveResponse(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send(ar -> {
        if (ar.succeeded()) {

          HttpResponse<Buffer> response = ar.result();

          System.out.println("Received response with status code" + response.statusCode());
        } else {
          System.out.println("Something went wrong " + ar.cause().getMessage());
        }
      });
  }

  public void receiveResponseAsJsonObject(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send(BodyCodec.jsonObject(), ar -> {
        if (ar.succeeded()) {
          HttpResponse<JsonObject> response = ar.result();

          JsonObject body = response.body();

          System.out.println("Received response with status code" + response.statusCode() + " with body " + body);
        } else {
          System.out.println("Something went wrong " + ar.cause().getMessage());
        }
      });
  }

  public void receiveResponseAsJsonPOJO(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send(BodyCodec.json(User.class), ar -> {
        if (ar.succeeded()) {
          HttpResponse<User> response = ar.result();

          User user = response.body();

          System.out.println("Received response with status code" + response.statusCode() + " with body " +
            user.getFirstName() + " " + user.getLastName());
        } else {
          System.out.println("Something went wrong " + ar.cause().getMessage());
        }
      });
  }

  public void receiveResponseAsWriteStream(WebClient client, WriteStream<Buffer> writeStream) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send(BodyCodec.pipe(writeStream), ar -> {
        if (ar.succeeded()) {

          HttpResponse<Void> response = ar.result();

          System.out.println("Received response with status code" + response.statusCode());
        } else {
          System.out.println("Something went wrong " + ar.cause().getMessage());
        }
      });
  }

  public void receiveResponseAndDiscard(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send(BodyCodec.none(), ar -> {
        if (ar.succeeded()) {

          HttpResponse<Void> response = ar.result();

          System.out.println("Received response with status code" + response.statusCode());
        } else {
          System.out.println("Something went wrong " + ar.cause().getMessage());
        }
      });
  }

  public void receiveResponseAsBufferDecodeAsJsonObject(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send(ar -> {
        if (ar.succeeded()) {

          HttpResponse<Buffer> response = ar.result();

          // Decode the body as a json object
          JsonObject body = response.bodyAsJsonObject();

          System.out.println("Received response with status code" + response.statusCode() + " with body " + body);
        } else {
          System.out.println("Something went wrong " + ar.cause().getMessage());
        }
      });
  }
}
