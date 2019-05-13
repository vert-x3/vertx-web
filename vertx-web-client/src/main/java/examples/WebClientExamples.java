/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package examples;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.predicate.ErrorConverter;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.client.predicate.ResponsePredicateResult;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.multipart.MultipartForm;

import java.util.function.Function;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class WebClientExamples {

  public void create(Vertx vertx) {
    WebClient client = WebClient.create(vertx);
  }

  public void createFromOptions(Vertx vertx) {
    WebClientOptions options = new WebClientOptions()
      .setUserAgent("My-App/1.2.3");
    options.setKeepAlive(false);
    WebClient client = WebClient.create(vertx, options);
  }

  public void wrap(HttpClient httpClient) {
    WebClient client = WebClient.wrap(httpClient);
  }

  public void simpleGetAndHead(Vertx vertx) {

    WebClient client = WebClient.create(vertx);

    // Send a GET request
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send()
      .onSuccess(response -> System.out
        .println("Received response with status code" + response.statusCode()))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));

    // Send a HEAD request
    client
      .head(8080, "myserver.mycompany.com", "/some-uri")
      .send()
      .onSuccess(response -> System.out
        .println("Received response with status code" + response.statusCode()))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void simpleGetWithParams(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .addQueryParam("param", "param_value")
      .send()
      .onSuccess(response -> System.out
        .println("Received response with status code" + response.statusCode()))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void simpleGetWithInitialParams(WebClient client) {
    HttpRequest<Buffer> request = client
      .get(
        8080,
        "myserver.mycompany.com",
        "/some-uri?param1=param1_value&param2=param2_value");

    // Add param3
    request.addQueryParam("param3", "param3_value");

    // Overwrite param2
    request.setQueryParam("param2", "another_param2_value");
  }

  public void simpleGetOverwritePreviousParams(WebClient client) {
    HttpRequest<Buffer> request = client
      .get(8080, "myserver.mycompany.com", "/some-uri");

    // Add param1
    request.addQueryParam("param1", "param1_value");

    // Overwrite param1 and add param2
    request.uri("/some-uri?param1=param1_value&param2=param2_value");
  }

  public void pathTemplate(WebClient client) {
    PathTemplate template = PathTemplate.parse("/users/:username");

    HttpRequest<Buffer> request = client.get(
      template, PathParameters.create().param("username", "slinkydeveloper")
    );

    // This will render to "/users/slinkydeveloper"
  }

  public void multiGet(WebClient client) {
    HttpRequest<Buffer> get = client
      .get(8080, "myserver.mycompany.com", "/some-uri");

    get
      .send()
      .onSuccess(res -> {
        // OK
      });

    // Same request again
    get
      .send()
      .onSuccess(res -> {
        // OK
      });
  }

  public void multiGetCopy(WebClient client) {
    HttpRequest<Buffer> get = client
      .get(8080, "myserver.mycompany.com", "/some-uri");

    get
      .send()
      .onSuccess(res -> {
        // OK
      });

    // The "get" request instance remains unmodified
    get
      .copy()
      .putHeader("a-header", "with-some-value")
      .send()
      .onSuccess(res -> {
        // OK
      });
  }

  public void timeout(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .timeout(5000)
      .send()
      .onSuccess(res -> {
        // OK
      })
      .onFailure(err -> {
        // Might be a timeout when cause is java.util.concurrent.TimeoutException
      });
  }

  public void sendBuffer(WebClient client, Buffer buffer) {
    // Send a buffer to the server using POST, the content-length
    // header will be set for you
    client
      .post(8080, "myserver.mycompany.com", "/some-uri")
      .sendBuffer(buffer)
      .onSuccess(res -> {
        // OK
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
          .sendStream(fileStream)
          .onSuccess(res -> {
            // OK
          })
        ;
      }
    });
  }

  public void sendStreamChunked(WebClient client, ReadStream<Buffer> stream) {
    // When the stream len is unknown sendStream sends the file to the
    // server using chunked transfer encoding
    client
      .post(8080, "myserver.mycompany.com", "/some-uri")
      .sendStream(stream)
      .onSuccess(res -> {
        // OK
      });
  }

  public void sendJsonObject(WebClient client) {
    client
      .post(8080, "myserver.mycompany.com", "/some-uri")
      .sendJsonObject(
        new JsonObject()
          .put("firstName", "Dale")
          .put("lastName", "Cooper"))
      .onSuccess(res -> {
        // OK
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
      .sendJson(new User("Dale", "Cooper"))
      .onSuccess(res -> {
        // OK
      });
  }

  public void sendForm(WebClient client) {
    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.set("firstName", "Dale");
    form.set("lastName", "Cooper");

    // Submit the form as a form URL encoded body
    client
      .post(8080, "myserver.mycompany.com", "/some-uri")
      .sendForm(form)
      .onSuccess(res -> {
        // OK
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
      .sendForm(form)
      .onSuccess(res -> {
        // OK
      });
  }

  public void sendMultipartWithFileUpload(WebClient client) {
    MultipartForm form = MultipartForm.create()
      .attribute("imageDescription", "a very nice image")
      .binaryFileUpload(
        "imageFile",
        "image.jpg",
        "/path/to/image",
        "image/jpeg");

    // Submit the form as a multipart form body
    client
      .post(8080, "myserver.mycompany.com", "/some-uri")
      .sendMultipartForm(form)
      .onSuccess(res -> {
        // OK
      });
  }

  public void sendHeaders1(WebClient client) {
    HttpRequest<Buffer> request = client
      .get(8080, "myserver.mycompany.com", "/some-uri");

    MultiMap headers = request.headers();
    headers.set("content-type", "application/json");
    headers.set("other-header", "foo");
  }

  public void sendHeaders2(WebClient client) {
    HttpRequest<Buffer> request = client
      .get(8080, "myserver.mycompany.com", "/some-uri");

    request.putHeader("content-type", "application/json");
    request.putHeader("other-header", "foo");
  }

  public void addBasicAccessAuthentication(WebClient client) {
    HttpRequest<Buffer> request = client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .authentication(new UsernamePasswordCredentials("myid", "mypassword"));
  }

  public void addBearerTokenAuthentication(WebClient client) {
    HttpRequest<Buffer> request = client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .authentication(new TokenCredentials("myBearerToken"));
  }

  public void receiveResponse(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send()
      .onSuccess(res ->
        System.out.println("Received response with status code" + res.statusCode()))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void receiveResponseAsJsonObject(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .as(BodyCodec.jsonObject())
      .send()
      .onSuccess(res -> {
        JsonObject body = res.body();

        System.out.println(
          "Received response with status code" +
            res.statusCode() +
            " with body " +
            body);
      })
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void receiveResponseAsJsonPOJO(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .as(BodyCodec.json(User.class))
      .send()
      .onSuccess(res -> {
        User user = res.body();

        System.out.println(
          "Received response with status code" +
            res.statusCode() +
            " with body " +
            user.getFirstName() +
            " " +
            user.getLastName());
      })
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void receiveResponseAsWriteStream(WebClient client, WriteStream<Buffer> writeStream) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .as(BodyCodec.pipe(writeStream))
      .send()
      .onSuccess(res ->
        System.out.println("Received response with status code" + res.statusCode()))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void receiveResponseAsJsonStream(WebClient client) {
    JsonParser parser = JsonParser.newParser().objectValueMode();
    parser.handler(event -> {
      JsonObject object = event.objectValue();
      System.out.println("Got " + object.encode());
    });
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .as(BodyCodec.jsonStream(parser))
      .send()
      .onSuccess(res ->
        System.out.println("Received response with status code" + res.statusCode()))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void receiveResponseAndDiscard(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .as(BodyCodec.none())
      .send()
      .onSuccess(res ->
        System.out.println("Received response with status code" + res.statusCode()))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void receiveResponseAsBufferDecodeAsJsonObject(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send()
      .onSuccess(res -> {
        // Decode the body as a json object
        JsonObject body = res.bodyAsJsonObject();

        System.out.println(
          "Received response with status code" +
            res.statusCode() +
            " with body " +
            body);
      })
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void manualSanityChecks(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send()
      .onSuccess(res -> {
        if (
          res.statusCode() == 200 &&
            res.getHeader("content-type").equals("application/json")) {
          // Decode the body as a json object
          JsonObject body = res.bodyAsJsonObject();

          System.out.println(
            "Received response with status code" +
              res.statusCode() +
              " with body " +
              body);
        } else {
          System.out.println("Something went wrong " + res.statusCode());
        }
      })
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void usingPredicates(WebClient client) {

    // Check CORS header allowing to do POST
    Function<HttpResponse<Void>, ResponsePredicateResult> methodsPredicate =
      resp -> {
        String methods = resp.getHeader("Access-Control-Allow-Methods");
        if (methods != null) {
          if (methods.contains("POST")) {
            return ResponsePredicateResult.success();
          }
        }
        return ResponsePredicateResult.failure("Does not work");
      };

    // Send pre-flight CORS request
    client
      .request(
        HttpMethod.OPTIONS,
        8080,
        "myserver.mycompany.com",
        "/some-uri")
      .putHeader("Origin", "Server-b.com")
      .putHeader("Access-Control-Request-Method", "POST")
      .expect(methodsPredicate)
      .send()
      .onSuccess(res -> {
        // Process the POST request now
      })
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void usingPredefinedPredicates(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .expect(ResponsePredicate.SC_SUCCESS)
      .expect(ResponsePredicate.JSON)
      .send()
      .onSuccess(res -> {
        // Safely decode the body as a json object
        JsonObject body = res.bodyAsJsonObject();
        System.out.println(
          "Received response with status code" +
            res.statusCode() +
            " with body " +
            body);
      })
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void usingSpecificStatus(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .expect(ResponsePredicate.status(200, 202))
      .send()
      .onSuccess(res -> {
        // ....
      });
  }

  public void usingSpecificContentType(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .expect(ResponsePredicate.contentType("some/content-type"))
      .send()
      .onSuccess(res -> {
        // ....
      });
  }

  private static class MyCustomException extends Exception {
    private final String code;

    public MyCustomException(String message) {
      super(message);
      code = null;
    }

    public MyCustomException(String code, String message) {
      super(message);
      this.code = code;
    }
  }

  public void predicateCustomError() {
    ResponsePredicate predicate = ResponsePredicate.create(
      ResponsePredicate.SC_SUCCESS,
      result -> new MyCustomException(result.message()));
  }

  public void predicateCustomErrorWithBody() {
    ErrorConverter converter = ErrorConverter.createFullBody(result -> {

      // Invoked after the response body is fully received
      HttpResponse<Buffer> response = result.response();

      if (response
        .getHeader("content-type")
        .equals("application/json")) {

        // Error body is JSON data
        JsonObject body = response.bodyAsJsonObject();

        return new MyCustomException(
          body.getString("code"),
          body.getString("message"));
      }

      // Fallback to defaut message
      return new MyCustomException(result.message());
    });

    ResponsePredicate predicate = ResponsePredicate
      .create(ResponsePredicate.SC_SUCCESS, converter);
  }

  public void testClientDisableFollowRedirects(Vertx vertx) {

    // Change the default behavior to not follow redirects
    WebClient client = WebClient
      .create(vertx, new WebClientOptions().setFollowRedirects(false));
  }

  public void testClientChangeMaxRedirects(Vertx vertx) {

    // Follow at most 5 redirections
    WebClient client = WebClient
      .create(vertx, new WebClientOptions().setMaxRedirects(5));
  }

  public void testClientChangeMaxRedirects(WebClient client) {

    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .followRedirects(false)
      .send()
      .onSuccess(res -> {
        // Obtain response
        System.out.println("Received response with status code" + res.statusCode());
      })
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void testOverrideRequestSSL(WebClient client) {

    client
      .get(443, "myserver.mycompany.com", "/some-uri")
      .ssl(true)
      .send()
      .onSuccess(res ->
        System.out.println("Received response with status code" + res.statusCode()))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void testAbsRequestSSL(WebClient client) {

    client
      .getAbs("https://myserver.mycompany.com:4043/some-uri")
      .send()
      .onSuccess(res ->
        System.out.println("Received response with status code" + res.statusCode()))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void testSocketAddress(WebClient client) {

    // Creates the unix domain socket address to access the Docker API
    SocketAddress serverAddress = SocketAddress
      .domainSocketAddress("/var/run/docker.sock");

    // We still need to specify host and port so the request
    // HTTP header will be localhost:8080
    // otherwise it will be a malformed HTTP request
    // the actual value does not matter much for this example
    client
      .request(
        HttpMethod.GET,
        serverAddress,
        8080,
        "localhost",
        "/images/json")
      .expect(ResponsePredicate.SC_ACCEPTED)
      .as(BodyCodec.jsonObject())
      .send()
      .onSuccess(res ->
        System.out.println("Current Docker images" + res.body()))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }
}
