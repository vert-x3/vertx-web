/*
 * Copyright 2019 Red Hat, Inc.
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

package io.vertx.ext.web.handler.graphql;

import graphql.GraphQL;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.BodyHandler;
import org.junit.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;
import static java.util.stream.Collectors.toList;

/**
 * @author Thomas Segismont
 */
public class GraphQLHandlerTest extends WebTestBase {

  private Map<String, String> links = new HashMap<>();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    createData();
    router.route("/graphql").order(100).handler(GraphQLHandler.create(graphQL()));
  }

  private void createData() {
    links.put("https://vertx.io", "Vert.x project");
    links.put("https://www.eclipse.org", "Eclipse Foundation");
    links.put("http://reactivex.io", "ReactiveX libraries");
  }

  @Test
  public void testSimpleGet() throws Exception {
    GraphQLRequestOptions options = new GraphQLRequestOptions()
      .setMethod(GET)
      .setQuery("query { allLinks { url } }");
    send(options, onSuccess(body -> {
      checkLinkUrls(body);
      testComplete();
    }));
    await();
  }

  private void checkLinkUrls(JsonObject body) {
    String bodyAsString = body.toString();
    assertFalse(bodyAsString, body.containsKey("errors"));
    JsonObject data = body.getJsonObject("data");
    List<String> urls = data.getJsonArray("allLinks").stream()
      .map(JsonObject.class::cast)
      .map(json -> json.getString("url"))
      .collect(toList());
    assertTrue(bodyAsString, urls.containsAll(links.keySet()) && links.keySet().containsAll(urls));
  }

  @Test
  public void testGetNoQuery() throws Exception {
    client.get("/graphql")
      .handler(onSuccess(response -> {
        assertEquals(400, response.statusCode());
        testComplete();
      })).end();
    await();
  }

  @Test
  public void testSimplePost() throws Exception {
    GraphQLRequestOptions options = new GraphQLRequestOptions()
      .setQuery("query { allLinks { url } }");
    send(options, onSuccess(body -> {
      checkLinkUrls(body);
      testComplete();
    }));
    await();
  }

  @Test
  public void testSimplePostNoContentType() throws Exception {
    GraphQLRequestOptions options = new GraphQLRequestOptions()
      .setQuery("query { allLinks { url } }")
      .setContentType(null);
    send(options, onSuccess(body -> {
      checkLinkUrls(body);
      testComplete();
    }));
    await();
  }

  @Test
  public void testSimplePostWithBodyHandler() throws Exception {
    router.route("/graphql").order(99).handler(BodyHandler.create());
    GraphQLRequestOptions options = new GraphQLRequestOptions()
      .setQuery("query { allLinks { url } }");
    send(options, onSuccess(body -> {
      checkLinkUrls(body);
      testComplete();
    }));
    await();
  }

  @Test
  public void testSimplePostQueryInParam() throws Exception {
    GraphQLRequestOptions options = new GraphQLRequestOptions()
      .setQuery("query { allLinks { url } }")
      .setQueryParam(true);
    send(options, onSuccess(body -> {
      checkLinkUrls(body);
      testComplete();
    }));
    await();
  }

  @Test
  public void testSimplePostQueryAsBody() throws Exception {
    GraphQLRequestOptions options = new GraphQLRequestOptions()
      .setQuery("query { allLinks { url } }")
      .setContentType(GraphQLRequestOptions.GRAPHQL);
    send(options, onSuccess(body -> {
      checkLinkUrls(body);
      testComplete();
    }));
    await();
  }

  @Test
  public void testPostNoQuery() throws Exception {
    client.post("/graphql")
      .handler(onSuccess(response -> {
        assertEquals(400, response.statusCode());
        testComplete();
      })).end(new JsonObject().put("foo", "bar").toBuffer());
    await();
  }

  @Test
  public void testPostInvalidJson() throws Exception {
    client.post("/graphql")
      .handler(onSuccess(response -> {
        assertEquals(400, response.statusCode());
        testComplete();
      })).end(new JsonArray().add("foo").add("bar").toBuffer());
    await();
  }

  @Test
  public void testUnsupportedMethod() throws Exception {
    client.put("/graphql")
      .handler(onSuccess(response -> {
        assertEquals(405, response.statusCode());
        testComplete();
      })).end();
    await();
  }

  @Test
  public void testUnsupportedMediaType() throws Exception {
    client.post("/graphql")
      .putHeader(HttpHeaders.CONTENT_TYPE, "text/html")
      .handler(onSuccess(response -> {
        assertEquals(415, response.statusCode());
        testComplete();
      })).end("<h1>Hello world!</h1>");
    await();
  }

  private void send(GraphQLRequestOptions options, Handler<AsyncResult<JsonObject>> handler) throws Exception {
    StringBuilder uri = new StringBuilder("/graphql");
    if (options.method == GET || options.queryParam) {
      uri.append("?query=").append(URLEncoder.encode(options.query, StandardCharsets.UTF_8.name()));
    }
    Future<JsonObject> future = Future.future();
    RequestOptions requestOptions = new RequestOptions()
      .setPort(8080)
      .setURI(uri.toString());
    HttpClientRequest request = client.request(options.method, requestOptions)
      .exceptionHandler(future::fail)
      .handler(ar -> {
        if (ar.succeeded()) {
          HttpClientResponse response = ar.result();
          if (response.statusCode() == 200) {
            response.bodyHandler(buffer -> future.complete(new JsonObject(buffer)));
          } else {
            future.fail("Status: " + response.statusCode());
          }
        } else {
          future.fail(ar.cause());
        }
      });
    if (options.contentType != null) {
      request.putHeader(HttpHeaders.CONTENT_TYPE, options.contentType);
    }
    if (options.method == GET || options.queryParam) {
      request.end();
    } else if (GraphQLRequestOptions.GRAPHQL.equalsIgnoreCase(options.contentType)) {
      request.end(options.query);
    } else {
      request.end(new JsonObject().put("query", options.query).toBuffer());
    }
    future.setHandler(handler);
  }

  private static class GraphQLRequestOptions {
    static final String JSON = "application/json";
    static final String GRAPHQL = "application/graphql";

    HttpMethod method = POST;
    String query;
    boolean queryParam;
    String contentType = JSON;

    GraphQLRequestOptions setMethod(HttpMethod method) {
      this.method = method;
      return this;
    }

    GraphQLRequestOptions setQuery(String query) {
      this.query = query;
      return this;
    }

    GraphQLRequestOptions setQueryParam(boolean queryParam) {
      this.queryParam = queryParam;
      return this;
    }

    GraphQLRequestOptions setContentType(String contentType) {
      this.contentType = contentType;
      return this;
    }
  }

  private GraphQL graphQL() {
    String schema = vertx.fileSystem().readFileBlocking("schema.graphqls").toString();

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type("Query", builder -> builder.dataFetcher("allLinks", this::getAllLinks))
      .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema)
      .build();
  }

  private List<Link> getAllLinks(DataFetchingEnvironment dataFetchingEnvironment) {
    return links.entrySet().stream()
      .map(e -> new Link(e.getKey(), e.getValue()))
      .collect(toList());
  }
}
