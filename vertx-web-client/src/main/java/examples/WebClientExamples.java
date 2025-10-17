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

import io.vertx.core.Expectation;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.endpoint.LoadBalancer;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.uritemplate.ExpandOptions;
import io.vertx.uritemplate.UriTemplate;

import java.util.HashMap;
import java.util.Map;

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

  public void idleTimeout(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .idleTimeout(5000)
      .send()
      .onSuccess(res -> {
        // OK
      })
      .onFailure(err -> {
        // Might be a timeout when cause is java.util.concurrent.TimeoutException
      });
  }

  public void connectTimeout(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .connectTimeout(5000)
      .send()
      .onSuccess(res -> {
        // OK
      })
      .onFailure(err -> {
        // Might be a timeout when cause is java.util.concurrent.TimeoutException
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
    fs.open("content.txt", new OpenOptions())
      .onSuccess(fileStream -> {
        String fileLen = "1024";

        // Send the file to the server using POST
        client
          .post(8080, "myserver.mycompany.com", "/some-uri")
          .putHeader("content-length", fileLen)
          .sendStream(fileStream)
          .onSuccess(res -> {
            // OK
          });
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
    Expectation<HttpResponseHead> methodsPredicate = new Expectation<HttpResponseHead>() {
      @Override
      public boolean test(HttpResponseHead resp) {
        String methods = resp.getHeader("Access-Control-Allow-Methods");
        return methods != null && methods.contains("POST");
      }
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
      .send()
      .expecting(methodsPredicate)
      .onSuccess(res -> {
        // Process the POST request now
      })
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void usingPredefinedPredicates(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send()
      .expecting(HttpResponseExpectation.SC_SUCCESS.and(HttpResponseExpectation.JSON))
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
      .send()
      .expecting(HttpResponseExpectation.status(200, 202))
      .onSuccess(res -> {
        // ....
      });
  }

  public void usingSpecificContentType(WebClient client) {
    client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .send()
      .expecting(HttpResponseExpectation.contentType("some/content-type"))
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
    Expectation<HttpResponseHead> expectation = HttpResponseExpectation.SC_SUCCESS
      .wrappingFailure((resp, err) -> new MyCustomException(err.getMessage()));
  }

  public void predicateCustomErrorWithBody() {
    HttpResponseExpectation.SC_SUCCESS.wrappingFailure((resp, err) -> {
      // Invoked after the response body is fully received
      HttpResponse<?> response =(HttpResponse<?>) resp;

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
      return new MyCustomException(err.getMessage());
    });
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

  public void testUriTemplate1(WebClient client, UriTemplate REQUEST_URI) {
    HttpRequest<Buffer> request = client.get(8080, "myserver.mycompany.com", REQUEST_URI);
  }

  public void testUriTemplate2(HttpRequest<Buffer> request) {
    request.setTemplateParam("param", "param_value");
  }

  public void testUriTemplate3(HttpRequest<Buffer> request) {
    request.send()
      .onSuccess(res ->
        System.out.println("Received response with status code" + res.statusCode()))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public void testUriTemplateFluent(WebClient client, UriTemplate REQUEST_URI) {
    client.get(8080, "myserver.mycompany.com", REQUEST_URI)
      .setTemplateParam("param", "param_value")
      .send()
      .onSuccess(res ->
        System.out.println("Received response with status code" + res.statusCode()))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  private void assertEquals(String a, String b) {
  }

  public void testUriTemplateEncoding(WebClient client, String amount) {
    String euroSymbol = "\u20AC";
    UriTemplate template = UriTemplate.of("/convert?{amount}&{currency}");

    // Request uri: /convert?amount=1234&currency=%E2%82%AC
    Future<HttpResponse<Buffer>> fut = client.get(template)
      .setTemplateParam("amount", amount)
      .setTemplateParam("currency", euroSymbol)
      .send();
  }

  public void testConfigureTemplateExpansion(Vertx vertx) {
    WebClient webClient = WebClient.create(vertx, new WebClientOptions()
      .setTemplateExpandOptions(new ExpandOptions()
        .setAllowVariableMiss(false))
    );
  }

  public void testUriTemplateMapExpansion(WebClient client) {
    Map<String, String> query = new HashMap<>();
    query.put("color", "red");
    query.put("width", "30");
    query.put("height", "50");
    UriTemplate template = UriTemplate.of("/{?query*}");

    // Request uri: /?color=red&width=30&height=50
    Future<HttpResponse<Buffer>> fut = client.getAbs(template)
      .setTemplateParam("query", query)
      .send();
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
      .as(BodyCodec.jsonObject())
      .send()
      .expecting(HttpResponseExpectation.SC_ACCEPTED)
      .onSuccess(res ->
        System.out.println("Current Docker images" + res.body()))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }

  public static void clientSideLoadBalancing(Vertx vertx) {
    WebClient client = WebClient.wrap(vertx
      .httpClientBuilder()
      .withLoadBalancer(LoadBalancer.ROUND_ROBIN)
      .build());
  }

  public static void clientSideConsistentHashing(Vertx vertx, int servicePort) {
    WebClient client = WebClient.wrap(vertx
      .httpClientBuilder()
      .withLoadBalancer(LoadBalancer.CONSISTENT_HASHING)
      .build());

    HttpServer server = vertx.createHttpServer()
      .requestHandler(inboundReq -> {

        // Get a routing key, in this example we will hash the incoming request host/ip
        // it could be anything else, e.g. user id, request id, ...
        String routingKey = inboundReq.remoteAddress().hostAddress();

        client
          .get("/test")
          .host("example.com")
          .routingKey(routingKey)
          .send()
          .expecting(HttpResponseExpectation.SC_OK)
          .onSuccess(res ->
            System.out.println("Received response with status code" + res.statusCode()))
          .onFailure(err ->
            System.out.println("Something went wrong " + err.getMessage()));
      });

    server.listen(servicePort);
  }

  public static void receiveResponseAsServerSentEvents(Vertx vertx, int servicePort) {
    WebClient client = WebClient.create(vertx, new WebClientOptions().setDefaultPort(servicePort).setDefaultHost("localhost"));

    HttpServer server = vertx.createHttpServer()
        .requestHandler(req -> {
          req.response().setChunked(true);
          // set headers
          req.response().headers().add("Content-Type", "text/event-stream;charset=UTF-8");
          req.response().headers().add("Connection", "keep-alive");
          req.response().headers().add("Cache-Control", "no-cache");
          req.response().headers().add("Access-Control-Allow-Origin", "*");
          int count = Integer.parseInt(req.getParam("count"));
          vertx.setPeriodic(50, new Handler<Long>() {
            private int index = 0;

            @Override
            public void handle(Long timerId) {
              if (index < count) {
                String event = String.format("event: event%d\ndata: data%d\nid: %d\n\n", index, index, index);
                index++;
                req.response().write(event);
              } else {
                vertx.cancelTimer(timerId);
                req.response().end();
              }
            }
          });
        });
    server.listen(servicePort);

    client.get("/basic?count=5").as(BodyCodec.sseStream(stream -> {
      stream.handler(v -> System.out.println("Event received " + v));
      stream.endHandler(v ->  System.out.println("End of stream " + v));
      })).send().expecting(HttpResponseExpectation.SC_OK)
          .onSuccess(res ->
            System.out.println("Received response with status code" + res.statusCode()))
          .onFailure(err ->
            System.out.println("Something went wrong " + err.getMessage()));
  }
}
