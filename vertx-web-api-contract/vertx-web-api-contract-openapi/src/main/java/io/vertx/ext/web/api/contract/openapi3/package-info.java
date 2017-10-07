/**
 * = Vert.x-Web OpenAPI 3 support
 * :toc: left
 *
 * Vert.x enables you to use your OpenApi 3 specification directly inside your code using the design first approach. Vert.x-Web provides:
 *
 * * OpenAPI 3 compliant API specification validation with automatic **loading of external Json schemas**
 * * Automatic request validation
 * * Automatic mount of security validation handlers
 * * Automatic 501 response for not implemented operations
 * * Router factory to provide all this features to users
 *
 * == The router factory
 * You can create your web service based on OpenAPI3 specification with {@link io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory}.
 * This class, as name says, is a router factory based on your OpenAPI 3 specification.
 * {@link io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory} is intended to give you a really simple user interface to use OpenAPI 3 support. It includes:
 *
 * * Async loading of specification and its schema dependencies
 * * Mount path with operationId or with combination of path and HTTP method
 * * Automatic request parameters validation
 * * Automatic convert OpenAPI style paths to Vert.x style paths
 * * Lazy methods: operations (combination of paths and HTTP methods) are mounted in declaration order inside specification
 * * Automatic mount of security validation handlers
 *
 * == Create a new router factory
 * To create a new router factory, you can use methods inside {@link io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory}:
 *
 * * {@link io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory#createRouterFactoryFromFile(io.vertx.core.Vertx, java.lang.String, io.vertx.core.Handler)}  to create a router factory from local file
 * * {@link io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory#createRouterFactoryFromURL(io.vertx.core.Vertx, java.lang.String, io.vertx.core.Handler)}  to create a router factory from url
 *
 * For example:
 * [source,$lang]
 * ----
 * {@link examples.openapi3.OpenAPI3Examples#constructRouterFactory}
 * ----
 *
 * == Mount the handlers
 * Now load your first path. There are two functions to load the handlers:
 *
 * * {@link io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory#addHandler(io.vertx.core.http.HttpMethod, java.lang.String, io.vertx.core.Handler)}
 * * {@link io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory#addHandlerByOperationId(java.lang.String, io.vertx.core.Handler)}
 *
 * And, of course, two functions to load failure handlers
 *
 * * {@link io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory#addFailureHandler(io.vertx.core.http.HttpMethod, java.lang.String, io.vertx.core.Handler)}
 * * {@link io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory#addFailureHandlerByOperationId(java.lang.String, io.vertx.core.Handler)}
 *
 * You can, of course, **add multiple handlers to same operation**, without overwrite the existing ones.
 *
 * .Path in OpenAPI format
 * IMPORTANT: If you want to use {@link io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory#addHandler(io.vertx.core.http.HttpMethod, java.lang.String, io.vertx.core.Handler)} or {@link io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory#addFailureHandler(io.vertx.core.http.HttpMethod, java.lang.String, io.vertx.core.Handler)} pay attention: You can provide a path only in OpenAPI styles (for example path `/hello/:param` doesn't work)
 *
 * For example:
 * [source,$lang]
 * ----
 * {@link examples.openapi3.OpenAPI3Examples#addRoute}
 * ----
 *
 * .Add operations with operationId
 * IMPORTANT: Usage of combination of path and HTTP method is allowed, but it's better to add operations handlers with operationId, for performance reasons and to avoid paths nomenclature errors
 *
 * Now you can use parameter values as described in http://vertx.io/docs/vertx-web/java/#_andling_parameters[vertx-web documentation]
 *
 * == Define security handlers
 * A security handler is defined by a combination of schema name and scope. You can mount only one security handler for a combination.
 * For example:
 *
 * [source,$lang]
 * ----
 * {@link examples.openapi3.OpenAPI3Examples#addSecurityHandler}
 * ----
 *
 * You can of course use included Vert.x security handlers, for example:
 *
 * [source,$lang]
 * ----
 * {@link examples.openapi3.OpenAPI3Examples#addJWT}
 * ----
 *
 * == Error handling
 * The router factory allows you to manage efficiently errors:
 *
 * * It automatically mounts a 501 `Not Implemented` handler for operations where you haven't mounted any handler
 * * It automatically mounts a 400 `Bad Request` handler that manages `ValidationException` (You can enable/disable this feature via {@link io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory#enableValidationFailureHandler(boolean)})
 *
 * == Generate the router
 * When you are ready, generate the router and use it:
 *
 * [source,$lang]
 * ----
 * {@link examples.openapi3.OpenAPI3Examples#generateRouter}
 * ----
 *
 **/
@Document(fileName = "index.adoc") @ModuleGen(name = "vertx-web-contract-openapi", groupPackage = "io.vertx")
package io.vertx.ext.web.api.contract.openapi3;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;
