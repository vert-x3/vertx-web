package examples;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.webclient.BodyCodec;
import io.vertx.rxjava.webclient.HttpResponse;
import io.vertx.rxjava.webclient.WebClient;
import rx.Single;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class RxWebClientExamples {

  public void simpleGet(WebClient client) {

    // Create the RxJava single for an HttpRequest
    // at this point no HTTP request has been sent to the server
    Single<HttpResponse<Buffer>> single = client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .rxSend();

    // Send a request upon subscription of the Single
    single.subscribe(response -> {
      System.out.println("Received response with status code" + response.statusCode());
    }, error -> {
      System.out.println("Something went wrong " + error.getMessage());
    });
  }

  public void flatMap(WebClient client) {

    // Obtain an URL Single from myserver.mycompany.com
    Single<String> url = client
      .get(8080, "myserver.mycompany.com", "/some-uri")
      .rxSend()
      .map(HttpResponse::bodyAsString);

    // Use the flatMap operator to make a request on the URL Single
    url
      .flatMap(u -> client.getAbs(u).rxSend())
      .subscribe(response -> {
        System.out.println("Received response with status code" + response.statusCode());
      }, error -> {
        System.out.println("Something went wrong " + error.getMessage());
      });
  }

  public void decodeAsJson(WebClient client) {
    Single<HttpResponse<JsonObject>> single = client
      .get(8080, "localhost", "/somepath")
      .rxSend(BodyCodec.jsonObject());
    single.subscribe(resp -> {
      System.out.println(resp.statusCode());
      System.out.println(resp.body());
      ;
    });
  }
}
