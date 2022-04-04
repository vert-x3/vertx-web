package io.vertx.ext.web.openapi;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class RouteBuilderTest {

  @Test
  public void testLoadFromClassPath(Vertx vertx, VertxTestContext testContext) {
    RouterBuilder.create(vertx, "jar:file:/target/app.jar!/api.yaml")
      .onFailure(err -> {
        assertThat(err).isNotNull();
        assertThat(err.getMessage()).isEqualTo("Cannot load the spec in path jar:file:/target/app.jar!/api.yaml");
        assertThat(err.getCause()).isNotNull();
        assertThat(err.getCause().getMessage()).isEqualTo("Unsupported protocol: jar");
        testContext.completeNow();
      });
  }
}
