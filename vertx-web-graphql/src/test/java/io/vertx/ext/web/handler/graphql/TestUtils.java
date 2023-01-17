package io.vertx.ext.web.handler.graphql;

import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;

public class TestUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);

  public static Response sendQueryValidation(String query) {
    return given().basePath("graphql")
      .contentType("application/json")
      .body(query).port(8081)
      .post();
  }

  public static Response sendQueryBasicTypes(String query) {
    return given().basePath("graphql")
      .contentType("application/json")
      .body(query).port(8082)
      .post();
  }

  public static JsonObject createQuery(String query) {
    return
      new JsonObject()
        .put("query", "{" + query + "}");
  }

  public static String peek(String query) {
    LOGGER.debug("{}", query);
    return query;
  }
}

