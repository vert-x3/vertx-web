package io.vertx.ext.web.handler.graphql;

import org.junit.Test;

import static io.vertx.core.http.HttpMethod.GET;

public class LocaleTest extends GraphQLTestBase {
  private static final String LOCALE = "el-CY";

  @Test
  public void testLocale() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setLocale(LOCALE)
      .setGraphQLQuery("query { locale }");
    request.send(client, onSuccess(body -> {
      if (body.getJsonObject("data").getString("locale").equals(LOCALE)) {
        testComplete();
      } else {
        fail(body.toString());
      }
    }));
    await();
  }

  @Test
  public void testWrongLocale() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setLocale("")
      .setGraphQLQuery("query { locale }");
    request.send(client, onSuccess(body -> {
      if (body.getJsonObject("data").getString("locale") != null) {
        fail(body.toString());
      } else {
        testComplete();
      }
    }));
    await();
  }

  @Test
  public void testMultipleLocale() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setLocale(LOCALE + ",en-GB")
      .setGraphQLQuery("query { locale }");
    request.send(client, onSuccess(body -> {
      if (body.getJsonObject("data").getString("locale").equals(LOCALE)) {
        testComplete();
      } else {
        fail(body.toString());
      }
    }));
    await();
  }

  @Test
  public void testMultipleWrongLocales() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setLocale(",,,," + LOCALE)
      .setGraphQLQuery("query { locale }");
    request.send(client, onSuccess(body -> {
      if (body.getJsonObject("data").getString("locale").equals(LOCALE)) {
        testComplete();
      } else {
        fail(body.toString());
      }
    }));
    await();
  }
}
