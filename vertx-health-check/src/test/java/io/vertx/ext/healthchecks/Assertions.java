package io.vertx.ext.healthchecks;

import io.vertx.core.AsyncResult;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Assertions {

  public static CheckAssert assertThatCheck(JsonObject json) {
    return new CheckAssert(json);
  }

  public static <T> AsyncResultAssert<T> assertThat(AsyncResult<T> ar) {
    return new AsyncResultAssert<>(ar);
  }

}
