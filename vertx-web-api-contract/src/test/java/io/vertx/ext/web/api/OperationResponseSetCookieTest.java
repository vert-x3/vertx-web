package io.vertx.ext.web.api;

import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OperationResponseSetCookieTest {

  @Test
  public void hasMultipleSetCookies() {
    final MultiMap headers = new CaseInsensitiveHeaders();
    headers.add("Set-Cookie", "cookie1=val1");
    headers.add("Set-Cookie", "cookie2=val2");

    final OperationResponse originalResponse = new OperationResponse().setHeaders(headers);

    final JsonObject serializedResponse = originalResponse.toJson();
    final OperationResponse deserializedResponse = new OperationResponse(serializedResponse);

    assertEquals(originalResponse.getHeaders().getAll("Set-Cookie"), deserializedResponse.getHeaders().getAll("Set-Cookie"));
  }

  @Test
  public void normalHeadersOnlyAppearOnce() {
    final MultiMap headers = new CaseInsensitiveHeaders();
    headers.add("header", "val1");
    headers.add("header", "val2");

    final OperationResponse originalResponse = new OperationResponse().setHeaders(headers);

    final JsonObject serializedResponse = originalResponse.toJson();
    final OperationResponse deserializedResponse = new OperationResponse(serializedResponse);

    List<String> singleHeader = new LinkedList<>();
    singleHeader.add("val2");

    assertEquals(singleHeader, deserializedResponse.getHeaders().getAll("header"));
  }

  @Test
  public void testNoSetCookieHeaders() {
    final MultiMap headers = new CaseInsensitiveHeaders();
    headers.add("header1", "val1");
    headers.add("header2", "val2");

    final OperationResponse originalResponse = new OperationResponse().setHeaders(headers);

    final JsonObject serializedResponse = originalResponse.toJson();
    final OperationResponse deserializedResponse = new OperationResponse(serializedResponse);

    assertEquals(originalResponse.getHeaders().getAll("Set-Cookie"), deserializedResponse.getHeaders().getAll("Set-Cookie"));
  }
}
