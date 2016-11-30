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
    Single<HttpResponse<Buffer>> single = client
      .get(8080, "localhost", "/somepath")
      .rxSend();
    single.subscribe(resp -> {
      System.out.println(resp.statusCode());
      System.out.println(resp.body());;
    });
  }

  public void decodeAsJson(WebClient client) {
    Single<HttpResponse<JsonObject>> single = client
      .get(8080, "localhost", "/somepath")
      .rxSend(BodyCodec.jsonObject());
    single.subscribe(resp -> {
      System.out.println(resp.statusCode());
      System.out.println(resp.body());;
    });
  }
}
