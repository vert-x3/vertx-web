package io.vertx.ext.web.client.tests;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.*;
import io.vertx.test.core.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class HttpResponseExpectationsTest extends WebClientTestBase {

  @Test
  public void testExpectFail_2() throws Exception {
    testExpectation(true,
      value -> false,
      HttpServerResponse::end);
  }

  @Test
  public void testExpectPass_2() throws Exception {
    testExpectation(false,
      value -> true,
      HttpServerResponse::end);
  }

  @Test
  public void testExpectStatusFail_2() throws Exception {
    testExpectation(true,
      HttpResponseExpectation.status(200),
      resp -> resp.setStatusCode(201).end());
  }

  @Test
  public void testExpectStatusPass_2() throws Exception {
    testExpectation(false,
      HttpResponseExpectation.status(200),
      resp -> resp.setStatusCode(200).end());
  }

  @Test
  public void testExpectStatusRangeFail_2() throws Exception {
    testExpectation(true,
      HttpResponseExpectation.SC_SUCCESS,
      resp -> resp.setStatusCode(500).end());
  }

  @Test
  public void testExpectStatusRangePass1_2() throws Exception {
    testExpectation(false,
      HttpResponseExpectation.SC_SUCCESS,
      resp -> resp.setStatusCode(200).end());
  }

  @Test
  public void testExpectStatusRangePass2_2() throws Exception {
    testExpectation(false,
      HttpResponseExpectation.SC_SUCCESS,
      resp -> resp.setStatusCode(299).end());
  }

  @Test
  public void testExpectContentTypeFail_2() throws Exception {
    testExpectation(true,
      HttpResponseExpectation.JSON,
      HttpServerResponse::end);
  }

  @Test
  public void testExpectOneOfContentTypesFail_2() throws Exception {
    testExpectation(true,
      HttpResponseExpectation.contentType(Arrays.asList("text/plain", "text/csv")),
      httpServerResponse -> httpServerResponse.putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML).end());
  }

  @Test
  public void testExpectContentTypePass_2() throws Exception {
    testExpectation(false,
      HttpResponseExpectation.JSON,
      resp -> resp.putHeader("content-type", "application/JSON").end());
  }

  @Test
  public void testExpectContentTypeWithEncodingPass_2() throws Exception {
    testExpectation(false,
      HttpResponseExpectation.JSON,
      resp -> resp.putHeader("content-type", "application/JSON;charset=UTF-8").end());
  }

  @Test
  public void testExpectOneOfContentTypesPass_2() throws Exception {
    testExpectation(false,
      HttpResponseExpectation.contentType(Arrays.asList("text/plain", "text/HTML")),
      httpServerResponse -> httpServerResponse.putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML).end());
  }

  @Test
  public void testExpectCustomException_2() throws Exception {
    Expectation<HttpResponseHead> expectation = ((Expectation<HttpResponseHead>) value -> false)
      .wrappingFailure((head, err) -> new CustomException("boom"));
    testExpectation(true, expectation, HttpServerResponse::end, cause -> {
      Assertions.assertThat(cause).isInstanceOf(CustomException.class);
      CustomException customException = (CustomException) cause;
      assertEquals("boom", customException.getMessage());
    });
  }

  @Test
  public void testExpectCustomExceptionWithResponseBody_2() throws Exception {
    UUID uuid = UUID.randomUUID();
    Expectation<HttpResponseHead> expectation = HttpResponseExpectation.SC_SUCCESS.wrappingFailure((head, err) -> {
      JsonObject body = ((HttpResponse<?>) head).bodyAsJsonObject();
      return new CustomException(UUID.fromString(body.getString("tag")), body.getString("message"));
    });
    testExpectation(true, expectation, httpServerResponse -> {
      httpServerResponse
        .setStatusCode(400)
        .end(new JsonObject().put("tag", uuid.toString()).put("message", "tilt").toBuffer());
    }, cause -> {
      Assertions.assertThat(cause).isInstanceOf(CustomException.class);
      CustomException customException = (CustomException) cause;
      assertEquals("tilt", customException.getMessage());
      assertEquals(uuid, customException.tag);
    });
  }

  @Test
  public void testExpectCustomExceptionWithStatusCode_2() throws Exception {
    UUID uuid = UUID.randomUUID();
    int statusCode = 400;

    Expectation<HttpResponseHead> expectation = HttpResponseExpectation.SC_SUCCESS
      .wrappingFailure((head, err) -> new CustomException(uuid, String.valueOf(head.statusCode())));

    testExpectation(true, expectation, httpServerResponse -> {
      httpServerResponse
        .setStatusCode(statusCode)
        .end(TestUtils.randomBuffer(2048));
    }, cause -> {
      Assertions.assertThat(cause).isInstanceOf(CustomException.class);
      CustomException customException = (CustomException) cause;
      assertEquals(String.valueOf(statusCode), customException.getMessage());
      assertEquals(uuid, customException.tag);
    });
  }

  @Test
  public void testExpectFunctionThrowsException_2() throws Exception {
    Expectation<HttpResponseHead> expectation = value -> {
      throw new IndexOutOfBoundsException("boom");
    };

    testExpectation(true, expectation, HttpServerResponse::end, cause -> {
      Assertions.assertThat(cause).isInstanceOf(IndexOutOfBoundsException.class);
    });
  }

  @Test
  public void testErrorConverterThrowsException_2() throws Exception {
    Expectation<HttpResponseHead> expectation = ((Expectation<HttpResponseHead>) value -> false).wrappingFailure((head, err) -> {
      throw new IndexOutOfBoundsException();
    });

    testExpectation(true, expectation, HttpServerResponse::end, cause -> {
      Assertions.assertThat(cause).isInstanceOf(IndexOutOfBoundsException.class);
    });
  }

  @Test
  public void testErrorConverterReturnsNull_2() throws Exception {
    Expectation<HttpResponseHead> expectation = ((Expectation<HttpResponseHead>) value -> false)
      .wrappingFailure((head, err) -> null);

    testExpectation(true, expectation, HttpServerResponse::end, cause -> {
      Assertions.assertThat(cause).isNotInstanceOf(NullPointerException.class);
    });
  }

  private void testExpectation(boolean shouldFail,
                               Expectation<HttpResponseHead> expectation,
                               Consumer<HttpServerResponse> bilto) throws Exception {
    testExpectation(shouldFail, expectation, bilto, ignore -> {});
  }

  private void testExpectation(boolean shouldFail,
                               Expectation<HttpResponseHead> expectation,
                               Consumer<HttpServerResponse> bilto,
                               Consumer<Throwable> failureTest) throws Exception {
    server.requestHandler(request -> bilto.accept(request.response()));
    startServer();
    HttpRequest<Buffer> request = webClient
      .get("/test");
    if (shouldFail) {
      Assertions
        .assertThatThrownBy(() -> request.send().expecting(expectation).await())
        .satisfies(failureTest);
    } else {
      request.send().expecting(expectation).await();
    }
  }

  private static class CustomException extends Exception {

    UUID tag;

    CustomException(String message) {
      super(message);
    }

    CustomException(UUID tag, String message) {
      super(message);
      this.tag = tag;
    }
  }
}
