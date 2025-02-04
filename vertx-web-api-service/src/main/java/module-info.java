module io.vertx.web.apiservice {
  requires static io.vertx.docgen;
  requires static io.vertx.codegen.json;
  requires static io.vertx.codegen.api;
  requires io.vertx.codegen.processor;
  requires io.vertx.core;
  requires io.vertx.openapi;
  requires io.vertx.serviceproxy;
  requires io.vertx.web.openapi.router;
  requires io.vertx.web.validation;
  requires io.vertx.web;
  requires java.compiler;
  requires io.netty.codec.http;

  exports io.vertx.ext.web.api.service;
  exports io.vertx.ext.web.api.service.generator.model to io.vertx.web.apiservice.tests;

  provides io.vertx.codegen.processor.GeneratorLoader with io.vertx.ext.web.api.service.generator.WebApiProxyGenLoader;

}

