open module io.vertx.web.sstore.redis.tests {

  requires io.vertx.core;
  requires io.vertx.web;
  requires io.vertx.testing.unit;
  requires junit;
  requires com.github.benmanes.caffeine;
  requires testcontainers;
  requires io.vertx.web.sstore.caffeine;
  requires io.vertx.web.tests;
}
