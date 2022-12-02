package io.vertx.ext.web.handler.graphql.it;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;

public class TestUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);

  public static Response sendQuery(String query) {
    return given().basePath("graphql")
      .contentType("application/json")
      .body(query)
      .post();
  }

  public static String createQuery(String query) {
    return new StringBuilder()
      .append('{')
      .append('"')
      .append("query")
      .append('"')
      .append(':')
      .append('"')
      .append('{')
      .append(query)
      .append("}")
      .append('"')
      .append("}")
      .toString();
  }

  public static String peek(String query) {
    LOGGER.debug("{}", query);
    return query;
  }
}

