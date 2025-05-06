module io.vertx.web.sstore.caffeine {

  requires static io.vertx.codegen.api;

  requires io.vertx.core;
  requires io.vertx.web;
  requires io.vertx.auth.common;

  requires com.github.benmanes.caffeine;

  exports io.vertx.ext.web.sstore.caffeine;

  provides io.vertx.ext.web.sstore.SessionStore with io.vertx.ext.web.sstore.caffeine.impl.CaffeineSessionStoreImpl;

}
