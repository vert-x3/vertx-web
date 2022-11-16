package io.vertx.ext.web.api.service.it;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.restassured.response.Response;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.api.service.it.models.Transaction;
import io.vertx.ext.web.api.service.it.services.WebApiServiceVerticle;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WebApiServiceIT {

  private static final String BASE_ROUTE = "http://localhost:8080/api/transactions";

  private static Transaction commonTransaction;

  @BeforeAll
  public static void deploy(Vertx vertx, VertxTestContext testContext) {
    commonTransaction = new Transaction("my-id", "message", "from@example.com", "to@example.com", Double.MAX_VALUE);
    vertx.deployVerticle(WebApiServiceVerticle.class.getName(), testContext.succeedingThenComplete());
  }

  @Test
  @Order(1)
  void baseRouteTest(VertxTestContext vertxTestContext) {
    final Checkpoint responseCheck = vertxTestContext.checkpoint();
    vertxTestContext.verify(() -> {
      given().port(8080).get(BASE_ROUTE).then().statusCode(200).body("", Matchers.hasSize(0));
      responseCheck.flag();
    });
  }

  @Test
  @Order(2)
  @DisplayName("postSomeDataTest")
  void postSomeDataTest(VertxTestContext vertxTestContext) {
    final Checkpoint responsePostCheck = vertxTestContext.checkpoint();
    vertxTestContext.verify(() -> {
      Response response = given()
        .port(8080)
        .header(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
        .body(commonTransaction.toJson().encode())
        .post(BASE_ROUTE);
      Assertions.assertEquals(201, response.statusCode());
      responsePostCheck.flag();
    });
  }

  @Test
  @Order(3)
  @DisplayName("getJsonResponse")
  void getJsonResponse(VertxTestContext vertxTestContext) {
    final Checkpoint responsePostCheck = vertxTestContext.checkpoint(2);
    String response = given().port(8080).get(BASE_ROUTE).then().statusCode(200).extract().response().body().asString();
    responsePostCheck.flag();
    vertxTestContext.verify(() -> {
      String expectedResponse = "[" + commonTransaction.toJson().encode() + "]";
      System.out.println(" expect transaction: " + expectedResponse);
      System.out.println(" actual response is: " + response);
      assertThat(expectedResponse).isEqualTo(response);
      responsePostCheck.flag();
    });
  }

  @Test
  @Order(4)
  @DisplayName("getDataByIdTest")
  void getDataByIdTest(VertxTestContext vertxTestContext) {
    final Checkpoint flagCheck = vertxTestContext.checkpoint();
    vertxTestContext.verify(() -> {
      String response = given().get(BASE_ROUTE + "/" + commonTransaction.getId()).then().statusCode(200).extract().asString();
      System.out.println(" expect transaction: " + commonTransaction.toJson().encode());
      System.out.println(" actual response is: " + response);
      assertThat(response).isEqualToIgnoringCase(commonTransaction.toJson().encode());
      flagCheck.flag();
    });
  }

  @Test
   @Order(5)
  @DisplayName("editTransaction")
  void editTransaction(VertxTestContext vertxTestContext) {
    final Checkpoint checkpointEdition = vertxTestContext.checkpoint(2);
    Transaction newTransaction = new Transaction("my-id", "messageEdited", "fromEdited@example.com", "toEdited@example.com", Double.MAX_VALUE);
    vertxTestContext.verify(() -> {
      given()
        .get(BASE_ROUTE + "/my-id").then().statusCode(200);
      checkpointEdition.flag();
      given()
        .header(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
        .body(newTransaction.toJson().encode())
        .put(BASE_ROUTE + "/my-id");

      checkpointEdition.flag();
    });
  }

  @Test
   @Order(6)
  @DisplayName("deleteTransaction")
  void deleteTransaction(VertxTestContext vertxTestContext) {
    final Checkpoint checkpointDelete = vertxTestContext.checkpoint();
    vertxTestContext.verify(() -> {
      given().delete(BASE_ROUTE + "/my-id").then().statusCode(200);
      checkpointDelete.flag();
    });
  }
}
