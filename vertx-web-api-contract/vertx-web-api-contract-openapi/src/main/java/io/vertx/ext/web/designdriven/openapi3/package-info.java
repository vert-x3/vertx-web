/**
 * = Vert.x-Web OpenAPI 3 support
 * :toc: left
 * <p>
 * == OpenAPI 3
 * You can create your web service based on OpenAPI3 specification with
 * `link:../../apidocs/io/vertx/ext/web/designdriven/OpenAPI3RouterFactory.html[OpenAPI3RouterFactory]`. This class,
 * as name says, is a router factory based on your OpenAPI 3 specification. It enables you to add handlers for
 * specific paths (or operationId), and the factory will care to load the correct security and validation handlers.
 * [source,$lang]
 * ----
 * {@link examples.openapi3.OpenAPI3Examples#mainExample}
 * ----
 * All methods, except `getRouter()` are lazy methods. When you call `getRouter()`, the `Router` will be generated
 * following the path ordering described in specification.
 **/
@Document(fileName = "index.adoc") @ModuleGen(name = "vertx-web-contract-openapi", groupPackage = "io.vertx")
package io.vertx.ext.web.designdriven.openapi3;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;
