open module io.vertx.web.openapi.router.tests {
  requires io.netty.codec.http;
  requires io.vertx.openapi;
  requires io.vertx.web;
  requires io.vertx.testing.junit5;
  requires io.vertx.web.client;
  requires io.vertx.web.openapi.router;
  requires org.junit.jupiter.params;
  requires truth;
  requires org.mockito;
  requires static io.vertx.auth.oauth2;
  requires static io.vertx.auth.jwt;
  exports io.vertx.router.test.base;
  exports io.vertx.router.test;
}
