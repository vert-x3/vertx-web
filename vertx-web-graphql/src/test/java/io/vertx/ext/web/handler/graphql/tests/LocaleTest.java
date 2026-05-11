/*
 * Copyright 2023 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.ext.web.handler.graphql.tests;

import graphql.GraphQL;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.tests.WebTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static io.vertx.core.http.HttpMethod.GET;
import static org.junit.jupiter.api.Assertions.*;

public class LocaleTest extends WebTestBase {

  private static final String LOCALE = "el-CY";


  protected TestData testData = new TestData();
  protected GraphQLHandler graphQLHandler;

  @Override
  @BeforeEach
  public void setUp(Vertx vertx) throws Exception {
    super.setUp(vertx);
    setUpGraphQLHandler();
  }

  protected void setUpGraphQLHandler() {
    graphQLHandler = GraphQLHandler.builder(graphQL()).beforeExecute(bwc -> {
      for (LanguageHeader acceptableLocale : bwc.context().acceptableLanguages()) {
        try {
          bwc.builder().locale(Locale.forLanguageTag(acceptableLocale.value()));
          break;
        } catch (RuntimeException ignored) {
        }
      }
    }).build();
    router.route("/graphql").order(100).handler(graphQLHandler);
  }

  protected GraphQL graphQL() {
    String schema = vertx.fileSystem().readFileBlocking("locale.graphqls").toString();

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type("Query", builder -> builder.dataFetcher("locale", this::getLocale))
      .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema)
      .build();
  }

  private String getLocale(DataFetchingEnvironment env) {
    Locale locale = env.getLocale();
    if (locale == null)
      return null;
    return locale.toLanguageTag();
  }

  @Test
  public void testLocale() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setLocale(LOCALE)
      .setGraphQLQuery("query { locale }");
    JsonObject body = request.send(webClient);
    assertEquals(LOCALE, body.getJsonObject("data").getString("locale"), body.toString());
  }

  @Test
  public void testEmptyLocaleDefaulsToSystemLocale() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setLocale("")
      .setGraphQLQuery("query { locale }");
    JsonObject body = request.send(webClient);
    Locale expectedLocale = Locale.getDefault();
    String actualLocale = body.getJsonObject("data").getString("locale");
    assertEquals(expectedLocale.toLanguageTag(), actualLocale);
  }

  @Test
  public void testMultipleLocale() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setLocale(LOCALE + ",en-GB")
      .setGraphQLQuery("query { locale }");
    JsonObject body = request.send(webClient);
    assertEquals(LOCALE, body.getJsonObject("data").getString("locale"), body.toString());
  }

  @Test
  public void testMultipleWrongLocales() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setLocale(",,,," + LOCALE)
      .setGraphQLQuery("query { locale }");
    JsonObject body = request.send(webClient);
    assertEquals(LOCALE, body.getJsonObject("data").getString("locale"), body.toString());
  }
}
