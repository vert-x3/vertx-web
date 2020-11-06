package io.vertx.ext.web.validation;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.validation.testutils.ValidationTestUtils;
import io.vertx.json.schema.SchemaParser;
import io.vertx.json.schema.SchemaRouter;
import io.vertx.json.schema.SchemaRouterOptions;
import io.vertx.json.schema.draft7.Draft7SchemaParser;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public abstract class BaseValidationHandlerTest {

  public SchemaRouter schemaRouter;
  public SchemaParser parser;
  public Router router;
  public HttpServer server;
  public WebClient client;

  @BeforeEach
  public void setUp(Vertx vertx, VertxTestContext testContext) {
    router = Router.router(vertx);
    ValidationTestUtils.mountRouterFailureHandler(router);

    schemaRouter = SchemaRouter.create(vertx, new SchemaRouterOptions());
    parser = Draft7SchemaParser.create(schemaRouter);

    client = WebClient.create(vertx, new WebClientOptions().setDefaultPort(9000).setDefaultHost("localhost"));
    server = vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(9000, testContext.succeedingThenComplete());
  }

  @AfterEach
  public void tearDown(VertxTestContext testContext) {
    if (client != null) client.close();
    if (server != null) server.close(testContext.succeedingThenComplete());
    else testContext.completeNow();
  }

}
