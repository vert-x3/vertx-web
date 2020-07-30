package io.vertx.ext.web.openapi.asserts;

import io.vertx.core.json.pointer.JsonPointer;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.StringAssert;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonAssert extends AbstractAssert<JsonAssert, Object> {
  public JsonAssert(Object actual) {
    super(actual, JsonAssert.class);
  }

  public JsonAssert extracting(JsonPointer pointer) {
    return new JsonAssert(pointer.queryJson(actual));
  }

  public JsonAssert extracting(URI unparsedPointer) {
    return extracting(JsonPointer.fromURI(unparsedPointer));
  }

  public StringAssert asString() {
    assertThat(actual).isInstanceOf(String.class);
    return new StringAssert((String) actual);
  }

}
