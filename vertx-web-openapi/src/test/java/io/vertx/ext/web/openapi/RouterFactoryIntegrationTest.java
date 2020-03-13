package io.vertx.ext.web.openapi;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.ext.web.validation.*;
import io.vertx.ext.web.validation.impl.ParameterLocation;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vertx.ext.web.validation.testutils.ValidationTestUtils.*;
import static io.vertx.ext.web.validation.testutils.ValidationTestUtils.badParameterResponse;
import static io.vertx.ext.web.validation.testutils.TestRequest.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * This tests are about RouterFactory behaviours
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@SuppressWarnings("unchecked")
@ExtendWith(VertxExtension.class)
@Timeout(1000)
public class RouterFactoryIntegrationTest extends BaseRouterFactoryTest {

  public static final String VALIDATION_SPEC = "src/test/resources/specs/validation_test.yaml";

  private Future<Void> startFileServer(Vertx vertx, VertxTestContext testContext) {
    Router router = Router.router(vertx);
    router.route().handler(StaticHandler.create("src/test/resources"));
    return testContext.assertComplete(
      vertx.createHttpServer()
        .requestHandler(router)
        .listen(9001)
        .mapEmpty()
    );
  }

  private Future<Void> startSecuredFileServer(Vertx vertx, VertxTestContext testContext) {
    Router router = Router.router(vertx);
    router.route()
      .handler((RoutingContext ctx) -> {
        if (ctx.request().getHeader("Authorization") == null) ctx.fail(HttpResponseStatus.FORBIDDEN.code());
        else ctx.next();
      })
      .handler(StaticHandler.create("src/test/resources"));
    return testContext.assertComplete(
      vertx.createHttpServer()
        .requestHandler(router)
        .listen(9001)
        .mapEmpty()
    );
  }

  @Test
  public void loadSpecFromFile(Vertx vertx, VertxTestContext testContext) {
    RouterFactory.create(vertx, "src/test/resources/specs/router_factory_test.yaml",
      routerFactoryAsyncResult -> {
        assertThat(routerFactoryAsyncResult.succeeded()).isTrue();
        assertThat(routerFactoryAsyncResult.result()).isNotNull();
        testContext.completeNow();
      });
  }

  @Test
  public void failLoadSpecFromFile(Vertx vertx, VertxTestContext testContext) {
    RouterFactory.create(vertx, "src/test/resources/specs/aaa.yaml",
      routerFactoryAsyncResult -> {
        assertThat(routerFactoryAsyncResult.failed()).isTrue();
        assertThat(routerFactoryAsyncResult.cause().getClass())
          .isEqualTo(RouterFactoryException.class);
        assertThat(((RouterFactoryException) routerFactoryAsyncResult.cause()).type())
          .isEqualTo(RouterFactoryException.ErrorType.INVALID_FILE);
        testContext.completeNow();
      });
  }

  @Test
  public void loadWrongSpecFromFile(Vertx vertx, VertxTestContext testContext) {
    RouterFactory.create(vertx, "src/test/resources/specs/bad_spec.yaml",
      routerFactoryAsyncResult -> {
        assertThat(routerFactoryAsyncResult.failed()).isTrue();
        assertThat(routerFactoryAsyncResult.cause().getClass())
          .isEqualTo(RouterFactoryException.class);
        assertThat(((RouterFactoryException) routerFactoryAsyncResult.cause()).type())
          .isEqualTo(RouterFactoryException.ErrorType.INVALID_FILE);
        testContext.completeNow();
      });
  }

  @Test
  public void loadSpecFromURL(Vertx vertx, VertxTestContext testContext) {
    startFileServer(vertx, testContext).onComplete(h -> {
      RouterFactory.create(vertx, "http://localhost:9001/specs/router_factory_test.yaml",
        routerFactoryAsyncResult -> {
          testContext.verify(() -> {
            assertThat(routerFactoryAsyncResult.succeeded())
              .isTrue();
            assertThat(routerFactoryAsyncResult.result())
              .isNotNull();
          });
          testContext.completeNow();
        });
    });
  }

  @Test
  public void loadSpecFromURLWithAuthorizationValues(Vertx vertx, VertxTestContext testContext) {
    startSecuredFileServer(vertx, testContext).onComplete(h -> {
      RouterFactory.create(
        vertx,
        "http://localhost:9001/specs/router_factory_test.yaml",
        new OpenAPILoaderOptions()
          .putAuthHeader("Authorization", "Bearer xx.yy.zz"),
        routerFactoryAsyncResult -> {
          assertThat(routerFactoryAsyncResult.succeeded()).isTrue();
          assertThat(routerFactoryAsyncResult.result()).isNotNull();
          testContext.completeNow();
        });
    });
  }

  @Test
  public void failLoadSpecFromURL(Vertx vertx, VertxTestContext testContext) {
    startFileServer(vertx, testContext).onComplete(h -> {
      RouterFactory.create(vertx, "http://localhost:9001/specs/does_not_exist.yaml",
        routerFactoryAsyncResult -> {
          assertThat(routerFactoryAsyncResult.failed()).isTrue();
          assertThat(routerFactoryAsyncResult.cause().getClass()).isEqualTo(RouterFactoryException.class);
          assertThat(((RouterFactoryException) routerFactoryAsyncResult.cause()).type()).isEqualTo(RouterFactoryException.ErrorType.INVALID_FILE);
          testContext.completeNow();
        });
    });
  }

  private RouterFactoryOptions HANDLERS_TESTS_OPTIONS = new RouterFactoryOptions()
    .setMountNotImplementedHandler(false)
    .setRequireSecurityHandlers(false);

  @Test
  public void mountHandlerTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();
    loadFactoryAndStartServer(vertx, "src/test/resources/specs/router_factory_test.yaml", testContext, routerFactory -> {
      routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);

      routerFactory.operation("listPets").handler(routingContext ->
        routingContext
          .response()
          .setStatusCode(200)
          .end()
      );
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/pets")
        .expect(statusCode(200))
        .send(testContext, checkpoint)
    );
  }

  @Test
  public void mountFailureHandlerTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();
    loadFactoryAndStartServer(vertx, "src/test/resources/specs/router_factory_test.yaml", testContext, routerFactory -> {
      routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);

      routerFactory
        .operation("listPets")
        .handler(routingContext -> routingContext.fail(null))
        .failureHandler(routingContext -> routingContext
          .response()
          .setStatusCode(500)
          .setStatusMessage("ERROR")
          .end()
        );
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/pets")
        .expect(statusCode(500), statusMessage("ERROR"))
        .send(testContext, checkpoint)
    );
  }

  @Test
  public void mountMultipleHandlers(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();
    loadFactoryAndStartServer(vertx, "src/test/resources/specs/router_factory_test.yaml", testContext, routerFactory -> {
      routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);

      routerFactory
        .operation("listPets")
        .handler(routingContext ->
          routingContext.put("message", "A").next()
        )
        .handler(routingContext -> {
          routingContext.put("message", routingContext.get("message") + "B");
          routingContext.fail(500);
        });
      routerFactory
        .operation("listPets")
        .failureHandler(routingContext ->
          routingContext.put("message", routingContext.get("message") + "E").next()
        )
        .failureHandler(routingContext ->
          routingContext
            .response()
            .setStatusCode(500)
            .setStatusMessage(routingContext.get("message"))
            .end()
        );
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/pets")
        .expect(statusCode(500), statusMessage("ABE"))
        .send(testContext, checkpoint)
    );
  }

  @Test
  public void mountSecurityHandlers(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();
    loadFactoryAndStartServer(vertx, "src/test/resources/specs/router_factory_test.yaml", testContext, routerFactory -> {
      routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(true));

      routerFactory.operation("listPetsSecurity").handler(routingContext -> routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage(routingContext.get("first_level") + "-" +
          routingContext.get("second_level") + "-" + routingContext.get("third_level_one") +
          "-" + routingContext.get("third_level_two") + "-Done")
        .end());

      routerFactory.securityHandler("api_key",
        routingContext -> routingContext.put("first_level", "User").next()
      );

      routerFactory.securityHandler("second_api_key", "moderator",
        routingContext -> routingContext.put("second_level", "Moderator").next()
      );

      routerFactory.securityHandler("third_api_key", "admin",
        routingContext -> routingContext.put("third_level_one", "Admin").next()
      );

      routerFactory.securityHandler("third_api_key", "useless",
        routingContext -> routingContext.put("third_level_one", "Wrong!").next()
      );

      routerFactory.securityHandler("third_api_key", "super_admin",
        routingContext -> routingContext.put("third_level_two", "SuperAdmin").next()
      );
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/pets_security_test")
        .expect(statusCode(200), statusMessage("User-Moderator-Admin-SuperAdmin-Done"))
        .send(testContext, checkpoint)
    );
  }

  @Test
  public void mountMultipleSecurityHandlers(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();

    loadFactoryAndStartServer(vertx, "src/test/resources/specs/router_factory_test.yaml", testContext, routerFactory -> {
      routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(true));

      routerFactory.operation("listPetsSecurity").handler(routingContext ->
        routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage("First handler: " + routingContext.get("firstHandler") + ", Second handler: " + routingContext.get("secondHandler") + ", Second api key: " + routingContext.get("secondApiKey") + ", Third api key: " + routingContext.get("thirdApiKey"))
          .end()
      );

      routerFactory.securityHandler("api_key", routingContext -> routingContext.put("firstHandler", "OK").next());
      routerFactory.securityHandler("api_key", routingContext -> routingContext.put("secondHandler", "OK").next());
      routerFactory.securityHandler("second_api_key", routingContext -> routingContext.put("secondApiKey", "OK").next());
      routerFactory.securityHandler("third_api_key", routingContext -> routingContext.put("thirdApiKey", "OK").next());

    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/pets_security_test")
        .expect(statusCode(200), statusMessage("First handler: OK, Second handler: OK, Second api key: OK, Third api key: OK"))
        .send(testContext, checkpoint)
    );
  }

  @Test
  public void requireSecurityHandler(Vertx vertx, VertxTestContext testContext) {
    RouterFactory.create(vertx, "src/test/resources/specs/router_factory_test.yaml", testContext.succeeding(routerFactory -> {
      routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(true));

      routerFactory.operation("listPets").handler(routingContext -> routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage(routingContext.get("message") + "OK")
        .end()
      );

      testContext.verify(() ->
        assertThatCode(routerFactory::createRouter)
          .isInstanceOfSatisfying(RouterFactoryException.class, rfe ->
            assertThat(rfe.type())
              .isEqualTo(RouterFactoryException.ErrorType.MISSING_SECURITY_HANDLER)
          )
      );

      routerFactory.securityHandler("api_key", RoutingContext::next);
      routerFactory.securityHandler("second_api_key", RoutingContext::next);
      routerFactory.securityHandler("third_api_key", RoutingContext::next);

      testContext.verify(() ->
        assertThatCode(routerFactory::createRouter)
          .doesNotThrowAnyException()
      );
      testContext.completeNow();

    }));

  }


  @Test
  public void testGlobalSecurityHandler(Vertx vertx, VertxTestContext testContext) {
    final Handler<RoutingContext> handler = routingContext -> {
      routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage(((routingContext.get("message") != null) ? routingContext.get("message") + "-OK" : "OK"))
        .end();
    };

    Checkpoint checkpoint = testContext.checkpoint(3);

    loadFactoryAndStartServer(vertx, "src/test/resources/specs/global_security_test.yaml", testContext, routerFactory -> {
      routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(true));

      routerFactory.operation("listPetsWithoutSecurity").handler(handler);
      routerFactory.operation("listPetsWithOverride").handler(handler);
      routerFactory.operation("listPetsWithoutOverride").handler(handler);

      testContext.verify(() ->
        assertThatCode(routerFactory::createRouter)
          .isInstanceOfSatisfying(RouterFactoryException.class, rfe ->
            assertThat(rfe.type())
              .isEqualTo(RouterFactoryException.ErrorType.MISSING_SECURITY_HANDLER)
          )
      );

      routerFactory.securityHandler("global_api_key",
        routingContext -> routingContext.put("message", "Global").next()
      );

      routerFactory.securityHandler("api_key",
        routingContext -> routingContext.put("message", "Local").next()
      );

    }).onComplete(h -> {
      testRequest(client, HttpMethod.GET, "/petsWithoutSecurity")
        .expect(statusCode(200), statusMessage("OK"))
        .send(testContext, checkpoint);
      testRequest(client, HttpMethod.GET, "/petsWithOverride")
        .expect(statusCode(200), statusMessage("Local-OK"))
        .send(testContext, checkpoint);
      testRequest(client, HttpMethod.GET, "/petsWithoutOverride")
        .expect(statusCode(200), statusMessage("Global-OK"))
        .send(testContext, checkpoint);
    });
  }

  @Test
  public void notRequireSecurityHandler(Vertx vertx, VertxTestContext testContext) {
    RouterFactory.create(vertx, "src/test/resources/specs/router_factory_test.yaml",
      routerFactoryAsyncResult -> {
        RouterFactory routerFactory = routerFactoryAsyncResult.result();

        routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(false));

        routerFactory.operation("listPets").handler(routingContext -> routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage(routingContext.get("message") + "OK")
          .end()
        );

        testContext.verify(() -> assertThatCode(routerFactory::createRouter).doesNotThrowAnyException());

        testContext.completeNow();
      });
  }

  @Test
  public void mountNotImplementedHandler(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();
    loadFactoryAndStartServer(vertx, "src/test/resources/specs/router_factory_test.yaml", testContext, routerFactory -> {
      routerFactory.setOptions(
        new RouterFactoryOptions()
          .setRequireSecurityHandlers(false)
          .setMountNotImplementedHandler(true)
      );
      routerFactory.operation("showPetById").handler(RoutingContext::next);
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/pets")
        .expect(statusCode(501), statusMessage("Not Implemented"))
        .send(testContext, checkpoint)
    );
  }

  @Test
  public void mountNotAllowedHandler(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();

    loadFactoryAndStartServer(vertx, "src/test/resources/specs/router_factory_test.yaml", testContext, routerFactory -> {
      routerFactory.setOptions(
        new RouterFactoryOptions()
          .setRequireSecurityHandlers(false)
          .setMountNotImplementedHandler(true)
      );

      routerFactory.operation("deletePets").handler(RoutingContext::next);
      routerFactory.operation("createPets").handler(RoutingContext::next);
    }).onComplete(rc ->
      testRequest(client, HttpMethod.GET, "/pets")
        .expect(statusCode(405), statusMessage("Method Not Allowed"))
        .expect(resp ->
          assertThat(new HashSet<>(Arrays.asList(resp.getHeader("Allow").split(Pattern.quote(", ")))))
            .isEqualTo(Stream.of("DELETE", "POST").collect(Collectors.toSet()))
        ).send(testContext, checkpoint)
    );
  }

  @Test
  public void addGlobalHandlersTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();

    loadFactoryAndStartServer(vertx, "src/test/resources/specs/router_factory_test.yaml", testContext, routerFactory -> {
      routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(false));

      routerFactory.rootHandler(rc -> {
        rc.response().putHeader("header-from-global-handler", "some dummy data");
        rc.next();
      });
      routerFactory.rootHandler(rc -> {
        rc.response().putHeader("header-from-global-handler", "some more dummy data");
        rc.next();
      });

      routerFactory.operation("listPets").handler(routingContext -> routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .end());
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/pets")
        .expect(statusCode(200))
        .expect(responseHeader("header-from-global-handler", "some more dummy data"))
        .send(testContext)
    );
  }

  @Test
  public void exposeConfigurationTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();

    loadFactoryAndStartServer(vertx, "src/test/resources/specs/router_factory_test.yaml", testContext, routerFactory -> {
        routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(false).setOperationModelKey("fooBarKey"));

        routerFactory.operation("listPets").handler(routingContext -> {
          JsonObject operation = routingContext.get("fooBarKey");

          routingContext
            .response()
            .setStatusCode(200)
            .setStatusMessage(operation.getString("operationId"))
            .end();
        });
    }).onComplete(h ->
        testRequest(client, HttpMethod.GET, "/pets")
          .expect(statusCode(200), statusMessage("listPets"))
          .send(testContext, checkpoint)
    );
  }

  @Test
  public void consumesTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(4);

    loadFactoryAndStartServer(vertx, "src/test/resources/specs/produces_consumes_test.yaml", testContext, routerFactory -> {
        routerFactory.setOptions(new RouterFactoryOptions().setMountNotImplementedHandler(false));

        routerFactory.operation("consumesTest").handler(routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          if (params.body() != null && params.body().isJsonObject()) {
            routingContext
              .response()
              .setStatusCode(200)
              .putHeader("Content-Type", "application/json")
              .end(params.body().getJsonObject().encode());
          } else {
            routingContext
              .response()
              .setStatusCode(200)
              .end();
          }
        });
    }).onComplete(h -> {
      JsonObject obj = new JsonObject().put("name", "francesco");
      testRequest(client, HttpMethod.POST, "/consumesTest")
        .expect(statusCode(200))
        .expect(jsonBodyResponse(obj))
        .sendJson(obj, testContext, checkpoint);

      MultiMap form = MultiMap.caseInsensitiveMultiMap();
      form.add("name", "francesco");
      testRequest(client, HttpMethod.POST, "/consumesTest")
        .expect(statusCode(200))
        .expect(jsonBodyResponse(obj))
        .sendURLEncodedForm(form, testContext, checkpoint);

      MultipartForm multipartForm = MultipartForm.create();
      form.add("name", "francesco");
      testRequest(client, HttpMethod.POST, "/consumesTest")
        .expect(statusCode(400))
        .expect(badBodyResponse(BodyProcessorException.BodyProcessorErrorType.MISSING_MATCHING_BODY_PROCESSOR))
        .sendMultipartForm(multipartForm, testContext, checkpoint);

      testRequest(client, HttpMethod.POST, "/consumesTest")
        .expect(statusCode(400))
        .expect(failurePredicateResponse())
        .send(testContext, checkpoint);
    });
  }

  @Test
  public void producesTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);

    loadFactoryAndStartServer(vertx, "src/test/resources/specs/produces_consumes_test.yaml", testContext, routerFactory -> {
        routerFactory.setOptions(new RouterFactoryOptions().setMountNotImplementedHandler(false));

        routerFactory.operation("producesTest").handler(routingContext -> {
          if (((RequestParameters) routingContext.get("parsedParameters")).queryParameter("fail").getBoolean())
            routingContext
              .response()
              .putHeader("content-type", "text/plain")
              .setStatusCode(500)
              .end("Hate it");
          else
            routingContext.response().setStatusCode(200).end("{}"); // ResponseContentTypeHandler does the job for me
        });
    }).onComplete(h -> {
      String acceptableContentTypes = String.join(", ", "application/json", "text/plain");
      testRequest(client, HttpMethod.GET, "/producesTest")
        .with(requestHeader("Accept", acceptableContentTypes))
        .expect(statusCode(200), responseHeader("Content-type", "application/json"))
        .send(testContext, checkpoint);
      testRequest(client, HttpMethod.GET, "/producesTest?fail=true")
        .with(requestHeader("Accept", acceptableContentTypes))
        .expect(statusCode(500), responseHeader("Content-type", "text/plain"))
        .send(testContext, checkpoint);
    });
  }

  @Test
  public void mountHandlersOrderTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);

    loadFactoryAndStartServer(vertx, "src/test/resources/specs/test_order_spec.yaml", testContext, routerFactory -> {
        routerFactory.setOptions(new RouterFactoryOptions().setMountNotImplementedHandler(false));

        routerFactory.operation("showSpecialProduct").handler(routingContext ->
          routingContext.response().setStatusMessage("special").end()
        );

        routerFactory.operation("showProductById").handler(routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          routingContext.response().setStatusMessage(params.pathParameter("id").getInteger().toString()).end();
        });

        testContext.completeNow();
    }).onComplete(h -> {
      testRequest(client, HttpMethod.GET, "/product/special")
        .expect(statusCode(200), statusMessage("special"))
        .send(testContext, checkpoint);
      testRequest(client, HttpMethod.GET, "/product/123")
        .expect(statusCode(200), statusMessage("123"))
        .send(testContext, checkpoint);
    });
  }

  @Test
  public void mountHandlerEncodedTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();

    loadFactoryAndStartServer(vertx, "src/test/resources/specs/router_factory_test.yaml", testContext, routerFactory -> {
        routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);

        routerFactory.operation("encodedParamTest").handler(routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          assertThat(params.pathParameter("p1").toString()).isEqualTo("a:b");
          assertThat(params.queryParameter("p2").toString()).isEqualTo("a:b");
          routingContext
            .response()
            .setStatusCode(200)
            .setStatusMessage(params.pathParameter("p1").toString())
            .end();
        });

        testContext.completeNow();
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/foo/a%3Ab?p2=a%3Ab")
        .expect(statusCode(200), statusMessage("a:b"))
        .send(testContext, checkpoint)
    );
  }

  /**
   * Tests that user can supply customised BodyHandler
   *
   * @throws Exception
   */
  @Test
  public void customBodyHandlerTest(Vertx vertx, VertxTestContext testContext) {
    RouterFactory.create(vertx, "src/test/resources/specs/upload_test.yaml", testContext.succeeding(routerFactory -> {
      routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(false));

      BodyHandler bodyHandler = BodyHandler.create("my-uploads");

      routerFactory.bodyHandler(bodyHandler);

      routerFactory.operation("upload").handler(routingContext -> routingContext.response().setStatusCode(201).end());

      testContext.verify(() -> {
        assertThat(routerFactory.createRouter().getRoutes().get(0))
          .extracting("state")
          .extracting("contextHandlers")
          .asList()
          .hasOnlyOneElementSatisfying(b -> assertThat(b).isSameAs(bodyHandler));
      });

      testContext.completeNow();

    }));
  }

  @Test
  public void testSharedRequestBody(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);

    loadFactoryAndStartServer(vertx, "src/test/resources/specs/shared_request_body.yaml", testContext, routerFactory -> {
        routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);

        final Handler<RoutingContext> handler = routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          RequestParameter body = params.body();
          JsonObject jsonBody = body.getJsonObject();
          routingContext
            .response()
            .setStatusCode(200)
            .setStatusMessage("OK")
            .putHeader("Content-Type", "application/json")
            .end(jsonBody.toBuffer());
        };

        routerFactory.operation("thisWayWorks").handler(handler);
        routerFactory.operation("thisWayBroken").handler(handler);
    }).onComplete(h -> {
      JsonObject obj = new JsonObject().put("id", "aaa").put("name", "bla");
      testRequest(client, HttpMethod.POST, "/v1/working")
        .expect(statusCode(200))
        .expect(jsonBodyResponse(obj))
        .sendJson(obj, testContext, checkpoint);
      testRequest(client, HttpMethod.POST, "/v1/notworking")
        .expect(statusCode(200))
        .expect(jsonBodyResponse(obj))
        .sendJson(obj, testContext, checkpoint);
    });
  }

  @Test
  public void pathResolverShouldNotCreateRegex(Vertx vertx, VertxTestContext testContext) {
    RouterFactory.create(vertx, "src/test/resources/specs/produces_consumes_test.yaml", testContext.succeeding(routerFactory -> {
        routerFactory.setOptions(new RouterFactoryOptions().setMountNotImplementedHandler(false));

        routerFactory.operation("consumesTest").handler(routingContext ->
          routingContext
            .response()
            .setStatusCode(200)
            .setStatusMessage("OK")
        );

        testContext.verify(() ->
          assertThat(routerFactory.createRouter().getRoutes())
            .extracting(Route::getPath)
            .anyMatch("/consumesTest"::equals)
        );

        testContext.completeNow();
    }));
  }

  @Test
  public void testJsonEmptyBody(Vertx vertx, VertxTestContext testContext) {
    loadFactoryAndStartServer(vertx, "src/test/resources/specs/router_factory_test.yaml", testContext, routerFactory -> {
        routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(false).setMountNotImplementedHandler(false));

        routerFactory.operation("jsonEmptyBody").handler(routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          RequestParameter body = params.body();
          routingContext
            .response()
            .setStatusCode(200)
            .setStatusMessage("OK")
            .putHeader("Content-Type", "application/json")
            .end(new JsonObject().put("bodyEmpty", body == null).toBuffer());
        });

        testContext.completeNow();
    }).onComplete(h ->
      testRequest(client, HttpMethod.POST, "/jsonBody/empty")
        .expect(statusCode(200), jsonBodyResponse(new JsonObject().put("bodyEmpty", true)))
        .send(testContext)
    );
  }

  @Test
  public void commaSeparatedMultipartEncoding(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    loadFactoryAndStartServer(vertx, "src/test/resources/specs/multipart.yaml", testContext, routerFactory -> {
      routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(false));
      routerFactory.operation("testMultipartMultiple").handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage(params.body().getJsonObject().getString("type"))
          .end();
      });
    }).onComplete(h -> {
      MultipartForm form1 = MultipartForm
        .create()
        .binaryFileUpload("file1", "random-file", "src/test/resources/random-file", "application/octet-stream")
        .attribute("type", "application/octet-stream");
      testRequest(client, HttpMethod.POST, "/testMultipartMultiple")
        .expect(statusCode(200), statusMessage("application/octet-stream"))
        .sendMultipartForm(form1, testContext, checkpoint);

      MultipartForm form2 =
        MultipartForm
          .create()
          .binaryFileUpload("file1", "random.txt", "src/test/resources/random.txt", "text/plain")
          .attribute("type", "text/plain");
      testRequest(client, HttpMethod.POST, "/testMultipartMultiple")
        .expect(statusCode(200), statusMessage("text/plain"))
        .sendMultipartForm(form2, testContext, checkpoint);

      MultipartForm form3 =
        MultipartForm
          .create()
          .binaryFileUpload("file1", "random.txt", "src/test/resources/random.txt", "application/json")
          .attribute("type", "application/json");
      testRequest(client, HttpMethod.POST, "/testMultipartMultiple")
        .expect(statusCode(400))
        .sendMultipartForm(form3, testContext, checkpoint);

    });
  }

  @Test
  public void wildcardMultipartEncoding(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    loadFactoryAndStartServer(vertx, "src/test/resources/specs/multipart.yaml", testContext, routerFactory -> {
      routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(false));
      routerFactory.operation("testMultipartWildcard").handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage(params.body().getJsonObject().getString("type"))
          .end();
      });
    }).onComplete(h -> {
      MultipartForm form1 =
        MultipartForm
          .create()
          .binaryFileUpload("file1", "random.txt", "src/test/resources/random.txt", "text/plain")
          .attribute("type", "text/plain");
      testRequest(client, HttpMethod.POST, "/testMultipartWildcard")
        .expect(statusCode(200), statusMessage("text/plain"))
        .sendMultipartForm(form1, testContext, checkpoint);

      MultipartForm form2 =
        MultipartForm
          .create()
          .binaryFileUpload("file1", "random.csv", "src/test/resources/random.csv", "text/csv")
          .attribute("type", "text/csv");
      testRequest(client, HttpMethod.POST, "/testMultipartWildcard")
        .expect(statusCode(200), statusMessage("text/csv"))
        .sendMultipartForm(form2, testContext, checkpoint);

      MultipartForm form3 =
        MultipartForm
          .create()
          .binaryFileUpload("file1", "random.txt", "src/test/resources/random.txt", "application/json")
          .attribute("type", "application/json");
      testRequest(client, HttpMethod.POST, "/testMultipartWildcard")
        .expect(statusCode(400))
        .sendMultipartForm(form3, testContext, checkpoint);
    });
  }

  @Test
  public void testQueryParamNotRequired(Vertx vertx, VertxTestContext testContext) {
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory
        .operation("listPets")
        .handler(routingContext -> routingContext.response().setStatusMessage("ok").end());
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/pets")
        .expect(statusCode(200), statusMessage("ok"))
        .send(testContext)
    );
  }

  @Test
  public void testPathParameter(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory
        .operation("showPetById")
        .handler(routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          routingContext.response().setStatusMessage(params.pathParameter("petId").toString()).end();
        });
    }).onComplete(h -> {
      testRequest(client, HttpMethod.GET, "/pets/3")
        .expect(statusCode(200), statusMessage("3"))
        .send(testContext, checkpoint);
      testRequest(client, HttpMethod.GET, "/pets/three")
        .expect(statusCode(400))
        .expect(badParameterResponse(ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR, "petId", ParameterLocation.PATH))
        .send(testContext, checkpoint);
    });
  }

  @Test
  public void testQueryParameterArrayExploded(Vertx vertx, VertxTestContext testContext) {
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory
        .operation("arrayTestFormExploded")
        .handler(routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          String serialized = params
            .queryParameter("parameter")
            .getJsonArray()
            .stream()
            .map(Object::toString)
            .collect(Collectors.joining(","));
          routingContext.response().setStatusMessage(serialized).end();
        });
    }).onComplete(h -> {
      QueryStringEncoder encoder = new QueryStringEncoder("/queryTests/arrayTests/formExploded");
      List<String> values = new ArrayList<>();
      values.add("4");
      values.add("2");
      values.add("26");
      for (String s : values) {
        encoder.addParam("parameter", s);
      }
      String serialized = String.join(",", values);

      testRequest(client, HttpMethod.GET, encoder.toString())
        .expect(statusCode(200), statusMessage(serialized))
        .send(testContext);
    });
  }

  @Test
  public void testQueryParameterArrayDefaultStyle(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);
      routerFactory
        .operation("arrayTest")
        .handler(routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          String serialized = params
            .queryParameter("parameter")
            .getJsonArray()
            .stream()
            .map(Object::toString)
            .collect(Collectors.joining(","));
          routingContext.response().setStatusMessage(serialized).end();
        });
    }).onComplete(h -> {
      String serialized = String.join(",", "4", "2", "26");
      testRequest(client, HttpMethod.GET, "/queryTests/arrayTests/default?parameter=" + serialized)
        .expect(statusCode(200), statusMessage(serialized))
        .send(testContext, checkpoint);
      testRequest(client, HttpMethod.GET, "/queryTests/arrayTests/default?parameter=" + String.join(",", "4", "1", "26"))
        .expect(statusCode(400))
        .expect(badParameterResponse(ParameterProcessorException.ParameterProcessorErrorType.VALIDATION_ERROR, "parameter", ParameterLocation.QUERY))
        .send(testContext, checkpoint);
    });
  }

  @Test
  public void testDefaultStringQueryParameter(Vertx vertx, VertxTestContext testContext) {
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory
        .operation("testDefaultString")
        .handler(routingContext ->
          routingContext.response().setStatusMessage(
            ((RequestParameters)routingContext.get("parsedParameters")).queryParameter("parameter").getString()
          ).end()
        );
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/queryTests/defaultString")
        .expect(statusCode(200), statusMessage("aString"))
        .send(testContext)
    );
  }


  @Test
  public void testDefaultIntQueryParameter(Vertx vertx, VertxTestContext testContext) {
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory
        .operation("testDefaultInt")
        .handler(routingContext ->
          routingContext.response().setStatusMessage(
            ((RequestParameters)routingContext.get("parsedParameters")).queryParameter("parameter").getInteger().toString()
          ).end()
        );
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/queryTests/defaultInt")
        .expect(statusCode(200), statusMessage("1"))
        .send(testContext)
    );
  }

  @Test
  public void testDefaultFloatQueryParameter(Vertx vertx, VertxTestContext testContext) {
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory
        .operation("testDefaultFloat")
        .handler(routingContext ->
          routingContext.response().setStatusMessage(
            ((RequestParameters)routingContext.get("parsedParameters")).queryParameter("parameter").getFloat().toString()
          ).end()
        );
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/queryTests/defaultFloat")
        .expect(statusCode(200), statusMessage("1.0"))
        .send(testContext)
    );
  }

  @Test
  public void testDefaultDoubleQueryParameter(Vertx vertx, VertxTestContext testContext) {
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory
        .operation("testDefaultDouble")
        .handler(routingContext ->
          routingContext.response().setStatusMessage(
            ((RequestParameters)routingContext.get("parsedParameters")).queryParameter("parameter").getDouble().toString()
          ).end()
        );
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/queryTests/defaultDouble")
        .expect(statusCode(200), statusMessage("1.0"))
        .send(testContext)
    );
  }

  @Test
  public void testAllowEmptyValueStringQueryParameter(Vertx vertx, VertxTestContext testContext) {
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory
        .operation("testDefaultString")
        .handler(routingContext ->
          routingContext.response().setStatusMessage(
            "" + ((RequestParameters)routingContext.get("parsedParameters")).queryParameter("parameter").getString().length()
          ).end()
        );
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/queryTests/defaultString?parameter")
        .expect(statusCode(200), statusMessage("0"))
        .send(testContext)
    );
  }

  @Test
  public void testAllowEmptyValueBooleanQueryParameter(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory
        .operation("testDefaultBoolean")
        .handler(routingContext ->
          routingContext.response().setStatusMessage(
            "" + ((RequestParameters)routingContext.get("parsedParameters")).queryParameter("parameter").toString()
          ).end()
        );
    }).onComplete(h -> {
      testRequest(client, HttpMethod.GET, "/queryTests/defaultBoolean?parameter")
        .expect(statusCode(200), statusMessage("false"))
        .send(testContext, checkpoint);
      testRequest(client, HttpMethod.GET, "/queryTests/defaultBoolean")
        .expect(statusCode(200), statusMessage("false"))
        .send(testContext, checkpoint);
      testRequest(client, HttpMethod.GET, "/queryTests/defaultBoolean?parameter=true")
        .expect(statusCode(200), statusMessage("true"))
        .send(testContext, checkpoint);
    });
  }

  @Test
  public void testQueryParameterByteFormat(Vertx vertx, VertxTestContext testContext) {
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory
        .operation("byteFormatTest")
        .handler(routingContext ->
          routingContext.response().setStatusMessage(
            "" + ((RequestParameters)routingContext.get("parsedParameters")).queryParameter("parameter").toString()
          ).end()
        );
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/queryTests/byteFormat?parameter=Zm9vYmFyCg==")
        .expect(statusCode(200), statusMessage("Zm9vYmFyCg=="))
        .send(testContext)
    );
  }

  @Test
  public void testFormArrayParameter(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);
      routerFactory
        .operation("formArrayTest")
        .handler(routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          String serialized = params
            .body()
            .getJsonObject()
            .getJsonArray("values")
            .stream()
            .map(Object::toString)
            .collect(Collectors.joining(","));
          routingContext.response().setStatusMessage(
            params.body().getJsonObject().getString("id") + serialized
          ).end();
        });
    }).onComplete(h -> {
      testRequest(client, HttpMethod.POST, "/formTests/arraytest")
        .expect(statusCode(200), statusMessage("a+b+c" + "10,8,4"))
        .sendURLEncodedForm(MultiMap.caseInsensitiveMultiMap().add("id", "a+b+c").add("values", (Iterable<String>) Arrays.asList("10", "8", "4")), testContext, checkpoint);

      testRequest(client, HttpMethod.POST, "/formTests/arraytest")
        .expect(statusCode(400))
        .expect(badBodyResponse(BodyProcessorException.BodyProcessorErrorType.PARSING_ERROR))
        .sendURLEncodedForm(MultiMap.caseInsensitiveMultiMap().add("id", "id").add("values", (Iterable<String>) Arrays.asList("10", "bla", "4")), testContext, checkpoint);
    });
  }

  @Test
  public void testJsonBody(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(4);
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory
        .operation("jsonBodyTest")
        .handler(routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          routingContext
            .response()
            .setStatusCode(200)
            .setStatusMessage("OK")
            .putHeader("Content-Type", "application/json")
            .end(params.body().getJsonObject().encode());
        });
    }).onComplete(h -> {
      JsonObject valid = new JsonObject().put("id", "anId").put("values", new JsonArray().add(5).add(10).add(2));

      testRequest(client, HttpMethod.POST, "/jsonBodyTest/sampleTest")
        .expect(statusCode(200), jsonBodyResponse(valid))
        .sendJson(valid, testContext, checkpoint);

      testRequest(client, HttpMethod.POST, "/jsonBodyTest/sampleTest")
        .with(requestHeader("content-type", "application/json; charset=utf-8"))
        .expect(statusCode(200), jsonBodyResponse(valid))
        .sendBuffer(valid.toBuffer(), testContext, checkpoint);

      testRequest(client, HttpMethod.POST, "/jsonBodyTest/sampleTest")
        .with(requestHeader("content-type", "application/superapplication+json"))
        .expect(statusCode(200), jsonBodyResponse(valid))
        .sendBuffer(valid.toBuffer(), testContext, checkpoint);

      JsonObject invalid = new JsonObject().put("id", "anId").put("values", new JsonArray().add(5).add("bla").add(2));

      testRequest(client, HttpMethod.POST, "/jsonBodyTest/sampleTest")
        .expect(statusCode(400))
        .expect(badBodyResponse(BodyProcessorException.BodyProcessorErrorType.VALIDATION_ERROR))
        .sendJson(invalid, testContext, checkpoint);

    });
  }

  @Test
  public void testRequiredJsonBody(Vertx vertx, VertxTestContext testContext) {
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);
      routerFactory
        .operation("createPets")
        .handler(routingContext ->
          routingContext
            .response()
            .setStatusCode(200)
            .end()
        );
    }).onComplete(h ->
      testRequest(client, HttpMethod.POST, "/pets")
        .expect(statusCode(400))
        .expect(failurePredicateResponse())
        .send(testContext)
    );
  }

  @Test
  public void testAllOfQueryParam(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(4);
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);
      routerFactory
        .operation("alloftest")
        .handler(routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          routingContext
            .response()
            .setStatusMessage("" +
              params.queryParameter("parameter").getJsonObject().getInteger("a") +
              params.queryParameter("parameter").getJsonObject().getBoolean("b")
            ).end();
        });
    }).onComplete(h -> {

      testRequest(client, HttpMethod.GET, "/queryTests/allOfTest?parameter=a,5,b,true")
        .expect(statusCode(200), statusMessage("5true"))
        .send(testContext, checkpoint);

      testRequest(client, HttpMethod.GET, "/queryTests/allOfTest?parameter=a,5,b,")
        .expect(statusCode(400))
        .expect(badParameterResponse(ParameterProcessorException.ParameterProcessorErrorType.VALIDATION_ERROR, "parameter", ParameterLocation.QUERY))
        .send(testContext, checkpoint);

      testRequest(client, HttpMethod.GET, "/queryTests/allOfTest?parameter=a,5")
        .expect(statusCode(200), statusMessage("5false"))
        .send(testContext, checkpoint);

      testRequest(client, HttpMethod.GET, "/queryTests/allOfTest?parameter=a,5,b,bla")
        .expect(statusCode(400))
        .expect(badParameterResponse(ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR, "parameter", ParameterLocation.QUERY))
        .send(testContext, checkpoint);

    });
  }

  @Test
  public void testQueryParameterAnyOf(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(5);
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory
        .operation("anyOfTest")
        .handler(routingContext ->
          routingContext
            .response()
            .setStatusMessage(((RequestParameters)routingContext.get("parsedParameters")).queryParameter("parameter").toString())
            .end()
        );
    }).onComplete(h -> {

      testRequest(client, HttpMethod.GET, "/queryTests/anyOfTest?parameter=true")
        .expect(statusCode(200), statusMessage("true"))
        .send(testContext, checkpoint);

      testRequest(client, HttpMethod.GET, "/queryTests/anyOfTest?parameter=5")
        .expect(statusCode(200), statusMessage("5"))
        .send(testContext, checkpoint);


      testRequest(client, HttpMethod.GET, "/queryTests/anyOfTest?parameter=5,4")
        .expect(statusCode(200), statusMessage(new JsonArray().add(5).add(4).encode()))
        .send(testContext, checkpoint);

      testRequest(client, HttpMethod.GET, "/queryTests/anyOfTest?parameter=a,5")
        .expect(statusCode(200), statusMessage(new JsonObject().put("a", 5).encode()))
        .send(testContext, checkpoint);

      testRequest(client, HttpMethod.GET, "/queryTests/anyOfTest?parameter=bla")
        .expect(statusCode(400))
        .expect(badParameterResponse(ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR, "parameter", ParameterLocation.QUERY))
        .send(testContext, checkpoint);

    });
  }

  @Timeout(2000)
  @Test
  public void testComplexMultipart(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);
      routerFactory
        .operation("complexMultipartRequest")
        .handler(routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          if (params.body() == null) {
            routingContext.response().setStatusCode(200).end();
          } else {
            routingContext
              .response()
              .putHeader("content-type", "application/json")
              .setStatusCode(200)
              .end(params.body().getJsonObject().toBuffer());
          }
        });
    }).onComplete(h -> {

      JsonObject pet = new JsonObject();
      pet.put("id", 14612);
      pet.put("name", "Willy");

      MultipartForm form = MultipartForm.create()
        .textFileUpload("param1", "random.txt", "src/test/resources/random.txt", "text/plain")
        .attribute("param2", pet.encode())
        .textFileUpload("param3", "random.csv", "src/test/resources/random.txt", "text/csv")
        .attribute("param4", "1.2")
        .attribute("param4", "5.2")
        .attribute("param4", "6.2")
        .attribute("param5", "2")
        .binaryFileUpload("param1NotRealBinary", "random-file", "src/test/resources/random-file", "text/plain")
        .binaryFileUpload("param1Binary", "random-file", "src/test/resources/random-file", "application/octet-stream");

      JsonObject expected = new JsonObject()
        .put("param2", pet)
        .put("param4", new JsonArray().add(1.2).add(5.2).add(6.2))
        .put("param5", 2);

      testRequest(client, HttpMethod.POST, "/multipart/complex")
        .expect(statusCode(200))
        .expect(jsonBodyResponse(expected))
        .sendMultipartForm(form, testContext, checkpoint);

      testRequest(client, HttpMethod.POST, "/multipart/complex")
        .expect(statusCode(200))
        .send(testContext, checkpoint);

    });
  }

  @Test
  public void testEmptyParametersNotNull(Vertx vertx, VertxTestContext testContext) {
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory
        .operation("createPets")
        .handler(routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          routingContext.response().setStatusCode(200).setStatusMessage( //Here it should not throw exception (issue #850)
            "" + params.queryParametersNames().size() + params.pathParametersNames().size() +
              params.cookieParametersNames().size() + params.headerParametersNames().size()
          ).end();
        });
    }).onComplete(h -> {
      testRequest(client, HttpMethod.POST, "/pets")
        .expect(statusCode(200), statusMessage("0000"))
        .sendJson(new JsonObject().put("id", 1).put("name", "Willy"), testContext);
    });
  }

  @Test
  public void testQueryExpandedObjectTestOnlyAdditionalProperties(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);
      routerFactory
        .operation("objectTestOnlyAdditionalProperties")
        .handler(routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          testContext.verify(() -> {
            assertThat(params.queryParameter("wellKnownParam").getString()).isEqualTo("hello");

            JsonObject param = params.queryParameter("params").getJsonObject();
            assertThat(param.containsKey("wellKnownParam")).isFalse();
            assertThat(param.getInteger("param1")).isEqualTo(1);
            assertThat(param.getInteger("param2")).isEqualTo(2);
          });
          routingContext.response().setStatusCode(200).end();
        });
    }).onComplete(h -> {
      testRequest(client, HttpMethod.GET, "/queryTests/objectTests/onlyAdditionalProperties?param1=1&param2=2&wellKnownParam=hello")
        .expect(statusCode(200))
        .send(testContext, checkpoint);

      testRequest(client, HttpMethod.GET, "/queryTests/objectTests/onlyAdditionalProperties?param1=1&param2=a&wellKnownParam=hello")
        .expect(statusCode(400))
        .expect(badParameterResponse(ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR, "params", ParameterLocation.QUERY))
        .send(testContext, checkpoint);

      testRequest(client, HttpMethod.GET, "/queryTests/objectTests/onlyAdditionalProperties?param1=1&param2=2&wellKnownParam=a")
        .expect(statusCode(400))
        .expect(badParameterResponse(ParameterProcessorException.ParameterProcessorErrorType.VALIDATION_ERROR, "wellKnownParam", ParameterLocation.QUERY))
        .send(testContext, checkpoint);
    });
  }

  @Test
  public void testJsonBodyWithDate(Vertx vertx, VertxTestContext testContext) {
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory
        .operation("jsonBodyWithDate")
        .handler(routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          routingContext
            .response()
            .setStatusCode(200)
            .setStatusMessage("OK")
            .putHeader("Content-Type", "application/json")
            .end(params.body().getJsonObject().encode());
        });
    }).onComplete(h -> {
      JsonObject obj = new JsonObject();
      obj.put("date", "2018-02-18");
      obj.put("dateTime1", "2018-01-01T10:00:00.0000000000000000000000Z");
      obj.put("dateTime2", "2018-01-01T10:00:00+10:00");
      obj.put("dateTime3", "2018-01-01T10:00:00-10:00");

      testRequest(client, HttpMethod.POST, "/jsonBodyWithDate")
        .expect(statusCode(200))
        .expect(jsonBodyResponse(obj))
        .sendJson(obj, testContext);
    });
  }


  /**
   * Test: query_optional_form_explode_object
   */
  @Test
  public void testQueryOptionalFormExplodeObject(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    loadFactoryAndStartServer(vertx, VALIDATION_SPEC, testContext, routerFactory -> {
      routerFactory
        .operation("query_form_explode_object")
        .handler(routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          routingContext.response()
            .setStatusCode(200)
            .setStatusMessage("OK")
            .putHeader("content-type", "application/json")
            .end(params.queryParameter("color").getJsonObject().encode());
        });
    }).onComplete(h -> {
      testRequest(client, HttpMethod.GET, "/query/form/explode/object?R=100&G=200&B=150&alpha=50")
        .expect(statusCode(200))
        .expect(jsonBodyResponse(new JsonObject("{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\",\"alpha\":50}")))
        .send(testContext, checkpoint);
      testRequest(client, HttpMethod.GET, "/query/form/explode/object?R=100&G=200&B=150&alpha=aaa")
        .expect(statusCode(400))
        .expect(badParameterResponse(ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR, "color", ParameterLocation.QUERY))
        .send(testContext, checkpoint);
    });
  }
}
