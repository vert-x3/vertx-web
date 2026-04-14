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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions;
import io.vertx.ext.web.handler.graphql.UploadScalar;
import io.vertx.ext.web.tests.WebTestBase2;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.handler.BodyHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Result {
  private final String id;

  Result(final String id) {
    this.id = id;
  }
}

public class MultipartRequestTest extends WebTestBase2 {
  @Override
  @BeforeEach
  public void setUp(io.vertx.core.Vertx vertx, io.vertx.junit5.VertxTestContext testContext) throws Exception {
    super.setUp(vertx, testContext);
    GraphQLHandler graphQLHandler = GraphQLHandler.create(graphQL(), createOptions());
    router.route().handler(BodyHandler.create());
    router.route("/graphql").order(100).handler(graphQLHandler);
  }

  private GraphQLHandlerOptions createOptions() {
    return new GraphQLHandlerOptions().setRequestMultipartEnabled(true)
      .setRequestBatchingEnabled(true);
  }

  private GraphQL graphQL() {
    final String schema = vertx.fileSystem().readFileBlocking("upload.graphqls").toString();
    final String emptyQueryschema = vertx.fileSystem().readFileBlocking("emptyQuery.graphqls").toString();

    final SchemaParser schemaParser = new SchemaParser();
    final TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema)
      .merge(schemaParser.parse(emptyQueryschema));

    final RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
      .scalar(UploadScalar.build())
      .type("Mutation", builder -> {
        builder.dataFetcher("singleUpload", this::singleUpload);
        builder.dataFetcher("multipleUpload", this::multipleUpload);
        return builder;
      }).build();

    final SchemaGenerator schemaGenerator = new SchemaGenerator();
    final GraphQLSchema graphQLSchema = schemaGenerator
      .makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema).build();
  }

  @Test
  public void testSingleUploadMutation() {
    final Buffer bodyBuffer = vertx.fileSystem().readFileBlocking("singleUpload.txt");

    HttpResponse<Buffer> response = webClient.post(8080, "localhost", "/graphql")
      .putHeader("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundaryBpwmk50wSJmsTPAH")
      .putHeader("accept", "application/json")
      .timeout(10000)
      .sendBuffer(bodyBuffer)
      .await();
    final JsonObject json = response
      .bodyAsJsonObject()
      .getJsonObject("data")
      .getJsonObject("singleUpload");
    assertEquals("a.txt", json.getString("id"));
  }

  @Test
  public void testMultipleUploadMutation() {
    final Buffer bodyBuffer = vertx.fileSystem().readFileBlocking("multipleUpload.txt");

    HttpResponse<Buffer> response = webClient.post(8080, "localhost", "/graphql")
      .putHeader("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundaryhvb6BzAACEqQKt0Z")
      .putHeader("accept", "application/json")
      .timeout(10000)
      .sendBuffer(bodyBuffer).await();
    final JsonObject json = response
      .bodyAsJsonObject()
      .getJsonObject("data")
      .getJsonObject("multipleUpload");
    assertEquals("b.txt c.txt", json.getString("id"));
  }

  @Test
  public void testBatchUploadMutation() {
    final Buffer bodyBuffer = vertx.fileSystem().readFileBlocking("batchUpload.txt");

    HttpResponse<Buffer> response = webClient.post(8080, "localhost", "/graphql")
      .putHeader("Content-Type", "multipart/form-data; boundary=------------------------560b6209af099a26")
      .putHeader("accept", "application/json")
      .timeout(10000)
      .sendBuffer(bodyBuffer).await();
    final JsonObject result = new JsonObject("{ \"array\":" + response.bodyAsString() + "}");
    assertEquals("a.txt", result.getJsonArray("array")
      .getJsonObject(0).getJsonObject("data")
      .getJsonObject("singleUpload").getString("id")
    );

    assertEquals("b.txt c.txt", result.getJsonArray("array")
      .getJsonObject(1).getJsonObject("data")
      .getJsonObject("multipleUpload").getString("id")
    );
  }

  private Object singleUpload(DataFetchingEnvironment env) {
    final FileUpload file = env.getArgument("file");
    return new Result(file.fileName());
  }

  private Object multipleUpload(DataFetchingEnvironment env) {
    final List<FileUpload> files = env.getArgument("files");
    return new Result(files.get(0).fileName() + " " + files.get(1).fileName());
  }
}

