package io.vertx.ext.web.openapi.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.openapi.OpenAPILoaderOptions;
import io.vertx.ext.web.openapi.asserts.MyAssertions;
import io.vertx.json.schema.ValidationException;
import io.vertx.json.schema.common.URIUtils;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static io.vertx.ext.web.openapi.asserts.MyAssertions.assertThat;
import static io.vertx.ext.web.openapi.asserts.MyAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
@Timeout(value = 1, timeUnit = TimeUnit.SECONDS)
public class OpenAPIHolderTest {

  private HttpServer schemaServer;

  private static final Handler<RoutingContext> queryParamAuthMock = rc -> {
    if (rc.queryParam("francesco") == null || !"slinky".equals(rc.queryParam("francesco").get(0)))
      rc.fail(401);
    else
      rc.next();
  };
  private static final Handler<RoutingContext> headerAuthMock = BasicAuthHandler.create((jsonObject, handler) -> {
    if ("francesco".equals(jsonObject.getString("username")) && "slinky".equals(jsonObject.getString("password")))
      handler.handle(Future.succeededFuture(new AbstractUser() {
        @Override
        protected void doIsPermitted(String s, Handler<AsyncResult<Boolean>> handler) {
          handler.handle(Future.succeededFuture(true));
        }

        @Override
        public JsonObject attributes() {
          return null;
        }

        @Override
        public User isAuthorized(Authorization authorization, Handler<AsyncResult<Boolean>> handler) {
          return null;
        }

        @Override
        public JsonObject principal() {
          return jsonObject;
        }

        @Override
        public void setAuthProvider(AuthProvider authProvider) {
        }
      }));
    else
      handler.handle(Future.failedFuture("Not match"));
  });

  @AfterEach
  public void tearDown(VertxTestContext testContext) throws Exception {
    stopSchemaServer(testContext.succeedingThenComplete());
  }

  @Nested
  class Valid {

    @Nested
    class FromFile {

      @Test
      public void simpleSpec(Vertx vertx, VertxTestContext testContext) {
        OpenAPIHolderImpl parser = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
          new OpenAPILoaderOptions());

        parser.loadOpenAPI("src/test/resources/yaml/valid/simple_spec.yaml").onComplete(testContext.succeeding(container -> {
          testContext.verify(() -> {
            assertThat(container)
              .extracting(JsonPointer.from("/info/title"))
              .isEqualTo("Simple spec no $refs");

            assertThat(container)
              .extracting(JsonPointer.create()
                .append("paths")
                .append("/simple")
                .append("post")
                .append("requestBody")
                .append("content")
                .append("multipart/form-data")
                .append("encoding")
                .append("fileName")
                .append("contentType")
              )
              .isEqualTo("text/plain");

            assertThat(container)
              .extracting(JsonPointer.create()
                .append("paths")
                .append("/simple")
                .append("post")
                .append("responses")
                .append("default")
                .append("description")
              )
              .isEqualTo("unexpected error");
          });
          testContext.completeNow();
        }));
      }

      @Test
      public void innerRefs(Vertx vertx, VertxTestContext testContext) {
        OpenAPIHolderImpl parser = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
          new OpenAPILoaderOptions());

        parser.loadOpenAPI("src/test/resources/yaml/valid/inner_refs.yaml").onComplete(testContext.succeeding(openapi -> {
          testContext.verify(() -> {
            assertThat(openapi)
              .extracting(JsonPointer.create()
                .append("paths")
                .append("/simple")
                .append("post")
                .append("operationId")
              )
              .isEqualTo("simple");

            assertThat(openapi)
              .extracting(JsonPointer.create()
                .append("paths")
                .append("/simple")
                .append("post")
                .append("requestBody")
                .append("content")
                .append("multipart/form-data")
                .append("schema")
                .append("$ref")
              )
              .isEqualTo(resolveAbsoluteURIFromFS(
                "src/test/resources/yaml/valid/inner_refs.yaml#/components/schemas/Simple"
              ).toString())
              .satisfies(ref ->
                assertThat(parser)
                  .hasCached(URI.create((String) ref))
                  .extracting(JsonPointer.create().append("properties").append("fileName").append("type"))
                  .isEqualTo("string")
              );

            assertThat(parser)
              .hasCached(URI.create("#/components/schemas/Simple"))
              .extracting(JsonPointer.create().append("properties").append("fileName").append("type"))
              .isEqualTo("string");

            assertThat(parser)
              .extractingWithRefSolveFrom(openapi, JsonPointer.create()
                .append("paths")
                .append("/simple")
                .append("post")
                .append("requestBody")
                .append("content")
                .append("multipart/form-data")
                .append("schema")
                .append("properties")
                .append("fileName")
                .append("type")
              ).isEqualTo("string");
          });
          testContext.completeNow();
        }));
      }

      @Test
      public void localRefs(Vertx vertx, VertxTestContext testContext) {
        OpenAPIHolderImpl loader = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
          new OpenAPILoaderOptions());

        loader.loadOpenAPI("src/test/resources/yaml/valid/local_refs.yaml").onComplete(testContext.succeeding(openapi -> {
          testContext.verify(() -> {
            assertThat(openapi)
              .extracting(JsonPointer.create()
                .append("paths")
                .append("/simple")
                .append("post")
                .append("operationId")
              )
              .isEqualTo("simple");


            assertThat(openapi)
              .extracting(JsonPointer.create()
                .append("paths")
                .append("/simple")
                .append("post")
                .append("requestBody")
                .append("content")
                .append("multipart/form-data")
                .append("schema")
                .append("$ref")
              )
              .isEqualTo(resolveAbsoluteURIFromFS(
                "src/test/resources/yaml/valid/local_refs.yaml#/components/schemas/Simple"
              ).toString());

            assertThat(loader)
              .hasCached(resolveAbsoluteURIFromFS("src/test/resources/yaml/valid/refs/Simple.yaml"));

            assertThat(loader)
              .hasCached(resolveAbsoluteURIFromFS("src/test/resources/yaml/valid/refs/fileName.json"));

            assertThat(loader)
              .extractingWithRefSolve(JsonPointer.create()
                .append("paths")
                .append("/simple")
                .append("post")
                .append("requestBody")
                .append("content")
                .append("multipart/form-data")
                .append("schema")
                .append("properties")
                .append("fileName")
                .append("type")
              ).isEqualTo("string");
          });
          testContext.completeNow();
        }));
      }

      @Test
      public void debtsManager(Vertx vertx, VertxTestContext testContext) {
        OpenAPIHolderImpl loader = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
          new OpenAPILoaderOptions());

        loader.loadOpenAPI("src/test/resources/json/valid/debts_manager_api.json").onComplete(testContext.succeeding(openapi -> {
          testContext.verify(() -> {
            assertThat(loader)
              .extractingWithRefSolveFrom(openapi, JsonPointer.create()
                .append("paths")
                .append("/transactions")
                .append("get")
                .append("responses")
                .append("200")
                .append("content")
                .append("application/json")
                .append("schema")
                .append("items")
                .append("allOf")
                .append("0")
                .append("allOf")
                .append("0")
                .append("properties")
                .append("to")
                .append("minLength")
              ).isEqualTo(5);
          });
          testContext.completeNow();
        }));
      }

      @Test
      public void localCircularRefs(Vertx vertx, VertxTestContext testContext) {
        OpenAPIHolderImpl loader = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
          new OpenAPILoaderOptions());

        loader.loadOpenAPI("src/test/resources/yaml/valid/local_circular_refs.yaml").onComplete(testContext.succeeding(openapi -> {
          testContext.verify(() -> {
            assertThat(loader)
              .hasCached(resolveAbsoluteURIFromFS("src/test/resources/yaml/valid/refs/Circular.yaml"));

            assertThat(loader)
              .extractingWithRefSolve(JsonPointer.create()
                .append("paths")
                .append("/simple")
                .append("post")
                .append("requestBody")
                .append("content")
                .append("application/json")
                .append("schema")
                .append("properties")
                .append("parent")
                .append("properties")
                .append("childs")
                .append("items")
                .append("properties")
                .append("value")
                .append("type")
              ).isEqualTo("string");
          });
          testContext.completeNow();
        }));
      }

    }

    @Nested
    class FromRemote {

      @Test
      public void withoutAuth(Vertx vertx, VertxTestContext testContext) {
        remoteCircularTest(vertx, testContext, new OpenAPILoaderOptions(), Collections.emptyList());
      }

      @Test
      public void withQueryAuth(Vertx vertx, VertxTestContext testContext) {
        remoteCircularTest(
          vertx,
          testContext,
          new OpenAPILoaderOptions().putAuthQueryParam("francesco", "slinky"),
          Collections.singletonList(queryParamAuthMock)
        );
      }

      @Test
      public void withHeaderAuth(Vertx vertx, VertxTestContext testContext) {
        remoteCircularTest(
          vertx,
          testContext,
          new OpenAPILoaderOptions().putAuthHeader("Authorization", "Basic ZnJhbmNlc2NvOnNsaW5reQ=="),
          Collections.singletonList(headerAuthMock)
        );
      }

      @Test
      public void withBothAuth(Vertx vertx, VertxTestContext testContext) {
        remoteCircularTest(
          vertx,
          testContext,
          new OpenAPILoaderOptions()
            .putAuthQueryParam("francesco", "slinky")
            .putAuthHeader("Authorization", "Basic ZnJhbmNlc2NvOnNsaW5reQ=="),
          Arrays.asList(headerAuthMock, queryParamAuthMock)
        );
      }

    }

    @Nested
    class FromClasspath {

      @Test
      public void simpleSpec(Vertx vertx, VertxTestContext testContext) {
        OpenAPIHolderImpl parser = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
          new OpenAPILoaderOptions());

        parser.loadOpenAPI("yaml/valid/simple_spec.yaml").onComplete(testContext.succeeding(container -> {
          testContext.verify(() ->
            assertThat(container)
              .extracting(JsonPointer.from("/info/title"))
              .isEqualTo("Simple spec no $refs")
          );
          testContext.completeNow();
        }));
      }

      @Test
      public void innerRefs(Vertx vertx, VertxTestContext testContext) {
        OpenAPIHolderImpl parser = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
          new OpenAPILoaderOptions());

        parser.loadOpenAPI("yaml/valid/inner_refs.yaml").onComplete(testContext.succeeding(container -> {
          testContext.verify(() -> {
            assertThat(container)
              .extracting(JsonPointer.from("/info/title"))
              .isEqualTo("Local inner ref spec");

            MyAssertions.assertThat(container)
              .extracting("paths", "/simple", "post", "requestBody", "content", "multipart/form-data", "schema", "$ref")
              .isEqualTo(resolveAbsoluteURIFromClasspath(
                "yaml/valid/inner_refs.yaml#/components/schemas/Simple"
              ).toString())
              .satisfies(ref ->
                assertThat(parser)
                  .hasCached(URI.create((String) ref))
                  .extracting(JsonPointer.create().append("properties").append("fileName").append("type"))
                  .isEqualTo("string")
              );
          });
          testContext.completeNow();
        }));
      }

      @Test
      public void localRefs(Vertx vertx, VertxTestContext testContext) {
        OpenAPIHolderImpl parser = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
          new OpenAPILoaderOptions());

        parser.loadOpenAPI("yaml/valid/local_refs.yaml").onComplete(testContext.succeeding(container -> {
          testContext.verify(() ->
            assertThat(container)
              .extracting(JsonPointer.from("/info/title"))
              .isEqualTo("Local relative refs")
          );
          testContext.completeNow();
        }));
      }

      @Test
      public void circularRefs(Vertx vertx, VertxTestContext testContext) {
        OpenAPIHolderImpl parser = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
          new OpenAPILoaderOptions());

        parser.loadOpenAPI("yaml/valid/local_circular_refs.yaml").onComplete(testContext.succeeding(container -> {
          testContext.verify(() ->
            assertThat(container)
              .extracting(JsonPointer.from("/info/title"))
              .isEqualTo("Local circular relative refs")
          );
          testContext.completeNow();
        }));
      }

    }

    @Nested
    class FromJars {

      @Test
      public void simpleSpec(Vertx vertx, VertxTestContext testContext) {
        OpenAPIHolderImpl parser = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
          new OpenAPILoaderOptions());

        parser.loadOpenAPI("my_fancy_resources/simple_spec.yaml").onComplete(testContext.succeeding(container -> {
          testContext.verify(() ->
            assertThat(container)
              .extracting(JsonPointer.from("/info/title"))
              .isEqualTo("Simple spec no $refs")
          );
          testContext.completeNow();
        }));
      }

      @Test
      public void innerRefs(Vertx vertx, VertxTestContext testContext) {
        OpenAPIHolderImpl parser = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
          new OpenAPILoaderOptions());

        parser.loadOpenAPI("my_fancy_resources/inner_refs.yaml").onComplete(testContext.succeeding(container -> {
          testContext.verify(() ->
            assertThat(container)
              .extracting(JsonPointer.from("/info/title"))
              .isEqualTo("Local inner ref spec")
          );
          testContext.completeNow();
        }));
      }

      @Test
      public void localRefsWithRootOpenapiInClasspathAndSchemasInJar(Vertx vertx, VertxTestContext testContext) {
        OpenAPIHolderImpl parser = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
          new OpenAPILoaderOptions());

        parser.loadOpenAPI("local_refs_in_jar.yaml").onComplete(testContext.succeeding(container -> {
          testContext.verify(() ->
            assertThat(container)
              .extracting(JsonPointer.from("/info/title"))
              .isEqualTo("Local relative refs")
          );
          testContext.completeNow();
        }));
      }

      @Test
      public void localRefs(Vertx vertx, VertxTestContext testContext) {
        OpenAPIHolderImpl parser = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
          new OpenAPILoaderOptions());

        parser.loadOpenAPI("my_fancy_resources/local_refs.yaml").onComplete(testContext.succeeding(container -> {
          testContext.verify(() ->
            assertThat(container)
              .extracting(JsonPointer.from("/info/title"))
              .isEqualTo("Local relative refs")
          );
          testContext.completeNow();
        }));
      }

      @Test
      public void circularRefs(Vertx vertx, VertxTestContext testContext) {
        OpenAPIHolderImpl parser = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
          new OpenAPILoaderOptions());

        parser.loadOpenAPI("my_fancy_resources/local_circular_refs.yaml").onComplete(testContext.succeeding(container -> {
          testContext.verify(() ->
            assertThat(container)
              .extracting(JsonPointer.from("/info/title"))
              .isEqualTo("Local circular relative refs")
          );
          testContext.completeNow();
        }));
      }

    }

  }

  @Nested
  class Invalid {

    @Test
    public void simpleSpec(Vertx vertx, VertxTestContext testContext) {
      OpenAPIHolderImpl parser = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
        new OpenAPILoaderOptions());

      parser.loadOpenAPI("yaml/invalid/simple_spec.yaml").onComplete(testContext.failing(err -> {
        testContext.verify(() -> {
          assertThat(err).isInstanceOf(ValidationException.class);
        });
        testContext.completeNow();
      }));
    }

    @Test
    public void innerRefs(Vertx vertx, VertxTestContext testContext) {
      OpenAPIHolderImpl parser = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
        new OpenAPILoaderOptions());

      parser.loadOpenAPI("yaml/invalid/inner_refs.yaml").onComplete(testContext.failing(err -> {
        testContext.verify(() -> {
          assertThat(err)
            .isInstanceOf(ValidationException.class);
        });
        testContext.completeNow();
      }));
    }

    @Test
    public void localRefs(Vertx vertx, VertxTestContext testContext) {
      OpenAPIHolderImpl loader = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
        new OpenAPILoaderOptions());

      loader.loadOpenAPI("yaml/invalid/local_refs.yaml").onComplete(testContext.failing(err -> {
        testContext.verify(() -> {
          assertThat(err)
            .isInstanceOf(ValidationException.class);
        });
        testContext.completeNow();
      }));
    }

    @Test
    public void debtsManager(Vertx vertx, VertxTestContext testContext) {
      OpenAPIHolderImpl loader = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
        new OpenAPILoaderOptions());

      loader.loadOpenAPI("json/invalid/debts_manager_api.json").onComplete(testContext.failing(err -> {
        testContext.verify(() -> {
          assertThat(err)
            .isInstanceOf(ValidationException.class);
        });
        testContext.completeNow();
      }));
    }

    @Test
    public void remote(Vertx vertx, VertxTestContext testContext) {
      OpenAPIHolderImpl loader = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
        new OpenAPILoaderOptions());
      testContext.assertFailure(
        startSchemaServer(vertx, "src/test/resources/yaml/invalid", Collections.emptyList(), 9000)
          .compose(v -> loader.loadOpenAPI("http://localhost:9000/local_refs.yaml"))
      ).onComplete(ar -> {
        testContext.verify(() -> {
          assertThat(ar.cause())
            .isInstanceOf(ValidationException.class);
        });
        testContext.completeNow();
      });
    }

  }

  @Test
  public void schemaNormalization(Vertx vertx, VertxTestContext testContext) {
    OpenAPIHolderImpl loader = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
      new OpenAPILoaderOptions());

    testContext.assertComplete(
      startSchemaServer(vertx, "./src/test/resources/specs/schemas", Collections.emptyList(), 8081)
        .compose(v -> loader.loadOpenAPI("specs/schemas_test_spec.yaml"))
    ).onComplete(l -> {
      testContext.verify(() -> {
        JsonPointer schemaPointer = JsonPointer.create().append(Arrays.asList(
          "paths", "/test10", "post", "requestBody", "content", "application/json", "schema"
        ));
        JsonObject resolved = (JsonObject) schemaPointer.query(loader.getOpenAPI(),
          new JsonPointerIteratorWithLoader(loader));

        assertThatJson(resolved.getString("$ref")).isEqualTo("http://localhost:8081/tree.yaml#/tree");

        Map<JsonPointer, JsonObject> additionalSchemasToRegister = new HashMap<>();
        Map.Entry<JsonPointer, JsonObject> normalizedEntry = loader.normalizeSchema(resolved, schemaPointer,
          additionalSchemasToRegister);
        JsonObject normalized = normalizedEntry.getValue();

        assertThat(additionalSchemasToRegister).hasSize(1);
        String treeObjectUri = additionalSchemasToRegister.keySet().iterator().next().toURI().toString();
        JsonObject treeObject = additionalSchemasToRegister.values().iterator().next();

        assertThat(normalized.getString("$ref")).isEqualTo(treeObjectUri);

        assertThatJson(treeObject)
          .extracting(JsonPointer.create().append("properties").append("childs").append("items").append("$ref"))
          .asString()
          .isEqualTo("#");

        testContext.completeNow();
      });
    });
  }

  @Test
  public void schemaNormalizationOnlyReferenceToMyself(Vertx vertx, VertxTestContext testContext) {
    OpenAPIHolderImpl loader = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(),
      new OpenAPILoaderOptions());

    testContext.assertComplete(
      startSchemaServer(vertx, "./src/test/resources/specs/schemas", Collections.emptyList(), 8081)
        .compose(v -> loader.loadOpenAPI("specs/schemas_test_spec.yaml"))
    ).onComplete(l -> {
      testContext.verify(() -> {
        JsonPointer schemaPointer = JsonPointer.fromURI(resolveAbsoluteURIFromClasspath(
          "specs/schemas_test_spec.yaml"
        )).append(Arrays.asList(
          "paths", "/test8", "post", "requestBody", "content", "application/json", "schema"
        ));
        JsonObject resolved = (JsonObject) schemaPointer.query(loader.getOpenAPI(),
          new JsonPointerIteratorWithLoader(loader));

        Map<JsonPointer, JsonObject> additionalSchemasToRegister = new HashMap<>();
        Map.Entry<JsonPointer, JsonObject> normalizedEntry = loader.normalizeSchema(
          resolved,
          schemaPointer,
          additionalSchemasToRegister
        );
        JsonObject normalized = normalizedEntry.getValue();

        assertThat(additionalSchemasToRegister)
          .hasSize(0);

        assertThatJson(normalized)
          .extracting(JsonPointer.create().append("properties").append("parent").append("$ref"))
          .asString()
          .isEqualTo("#");

        assertThatJson(normalized)
          .extracting(JsonPointer.create().append("properties").append("children").append("items").append("$ref"))
          .asString()
          .isEqualTo("#");

        testContext.completeNow();
      });
    });
  }

  private void remoteCircularTest(Vertx vertx, VertxTestContext testContext, OpenAPILoaderOptions options,
                                  List<Handler<RoutingContext>> authHandlers) {
    OpenAPIHolderImpl loader = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(), options);
    startSchemaServer(vertx, "src/test/resources/yaml/valid", authHandlers, 9000)
      .compose(v -> loader.loadOpenAPI("http://localhost:9000/local_circular_refs.yaml"))
      .onFailure(testContext::failNow)
      .onSuccess(ar -> {
        testContext.verify(() -> {
          assertThat(loader)
            .hasCached(URI.create("http://localhost:9000/local_circular_refs.yaml"));

          assertThat(loader)
            .hasCached(URI.create("http://localhost:9000/refs/Circular.yaml"));

          assertThat(loader)
            .extractingWithRefSolve(JsonPointer.create()
              .append("paths")
              .append("/simple")
              .append("post")
              .append("requestBody")
              .append("content")
              .append("application/json")
              .append("schema")
              .append("properties")
              .append("parent")
              .append("properties")
              .append("childs")
              .append("items")
              .append("properties")
              .append("value")
              .append("type")
            ).isEqualTo("string");
          testContext.completeNow();
        });
      });
  }

  private Future<Void> startSchemaServer(Vertx vertx, String path, List<Handler<RoutingContext>> authHandlers,
                                         int port) {
    Router r = Router.router(vertx);

    Route route = r.route("/*")
      .produces("application/yaml");
    authHandlers.forEach(route::handler);
    route.handler(StaticHandler.create(path).setCachingEnabled(true));

    schemaServer = vertx.createHttpServer(new HttpServerOptions().setPort(port))
      .requestHandler(r);
    return schemaServer.listen().mapEmpty();
  }

  private void stopSchemaServer(Handler<AsyncResult<Void>> completion) {
    if (schemaServer != null) {
      try {
        schemaServer.close((asyncResult) -> {
          completion.handle(Future.succeededFuture());
        });
      } catch (IllegalStateException e) { // Server is already open
        completion.handle(Future.succeededFuture());
      }
    } else
      completion.handle(Future.succeededFuture());
  }

  private URI resolveAbsoluteURIFromFS(String relative) {
    URI relativeURI = URI.create(relative);
    String fragment = relativeURI.getFragment();
    return URIUtils.replaceFragment(
      Paths.get(relativeURI.getPath()).toAbsolutePath().toUri(),
      fragment
    );
  }

  private URI resolveAbsoluteURIFromClasspath(String relative) {
    URI relativeURI = URI.create(relative);
    String fragment = relativeURI.getFragment();
    return URIUtils.replaceFragment(
      OpenAPIHolderImpl.getResourceAbsoluteURIFromClasspath(URIUtils.removeFragment(relativeURI)),
      fragment
    );
  }

}
