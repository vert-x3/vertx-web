/**
 * = Vert.x-Web API Contract
 * :toc: left
 *
 * Vert.x-Web API Contract brings to Vert.x two features to help you to develop you API:
 *
 * * HTTP Requests validation
 * * OpenAPI 3 Support with automatic requests validation
 *
 * == Using Vert.x API Contract
 *
 * To use Vert.x API Contract, add the following dependency to the _dependencies_ section of your build descriptor:
 *
 * * Maven (in your `pom.xml`):
 *
 * [source,xml,subs="+attributes"]
 * ----
 * <dependency>
 *   <groupId>${maven.groupId}</groupId>
 *   <artifactId>${maven.artifactId}</artifactId>
 *   <version>${maven.version}</version>
 * </dependency>
 * ----
 *
 * * Gradle (in your `build.gradle` file):
 *
 * [source,groovy,subs="+attributes"]
 * ----
 * dependencies {
 *   compile '${maven.groupId}:${maven.artifactId}:${maven.version}'
 * }
 * ----
 *
 * == HTTP Requests validation
 *
 * Vert.x provides a validation framework that will validate requests for you and will put results of validation inside a container. To define a {@link io.vertx.ext.web.api.validation.HTTPRequestValidationHandler}:
 * [source,$lang]
 * ----
 * {@link examples.ValidationExamples#example1}
 * ----
 *
 * Then you can mount your validation handler:
 * [source,$lang]
 * ----
 * {@link examples.ValidationExamples#example2}
 * ----
 *
 * If validation succeeds, It returns request parameters inside {@link io.vertx.ext.web.api.RequestParameters}, otherwise It will throw a {@link io.vertx.ext.web.api.validation.ValidationException}
 *
 * === Types of request parameters
 * Every parameter has a type validator, a class that describes the expected type of parameter.
 * A type validator validates the value, casts it in required language type and then loads it inside a {@link io.vertx.ext.web.api.RequestParameter} object. There are three ways to describe the type of your parameter:
 *
 * * There is a set of prebuilt types that you can use: {@link io.vertx.ext.web.api.validation.ParameterType}
 * * You can instantiate a custom instance of prebuilt type validators using static methods of {@link io.vertx.ext.web.api.validation.ParameterTypeValidator} and then load it into {@link io.vertx.ext.web.api.validation.HTTPRequestValidationHandler} using functions ending with `WithCustomTypeValidator`
 * * You can create your own `ParameterTypeValidator` implementing {@link io.vertx.ext.web.api.validation.ParameterTypeValidator} interface
 *
 * === Handling parameters
 * Now you can handle parameter values:
 *
 * [source,$lang]
 * ----
 * {@link examples.ValidationExamples#example3}
 * ----
 *
 * As you can see, every parameter is mapped in respective language objects. You can also get a json body:
 *
 * [source,$lang]
 * ----
 * {@link examples.ValidationExamples#example4}
 * ----
 *
 * == OpenAPI 3 support
 *
 * Vert.x allows you to use your OpenApi 3 specification directly inside your code using the design first approach. Vert.x-Web provides:
 *
 * * OpenAPI 3 compliant API specification validation with automatic **loading of external Json schemas**
 * * Automatic request validation
 * * Automatic mount of security validation handlers
 * * Automatic 501 response for not implemented operations
 * * Router factory to provide all these features to users
 *
 * You can also use the community project https://github.com/pmlopes/slush-vertx[`slush-vertx`] to generate server code from your OpenAPI 3 specification.
 *
 * === The router factory
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
 * === Create a new router factory
 * To create a new router factory, you can use methods inside {@link io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory}:
 *
 * * {@link io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory#createRouterFactoryFromFile(io.vertx.core.Vertx, java.lang.String, io.vertx.core.Handler)}  to create a router factory from local file
 * * {@link io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory#createRouterFactoryFromURL(io.vertx.core.Vertx, java.lang.String, io.vertx.core.Handler)}  to create a router factory from url
 *
 * For example:
 * [source,$lang]
 * ----
 * {@link examples.OpenAPI3Examples#constructRouterFactory}
 * ----
 *
 * === Mount the handlers
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
 * {@link examples.OpenAPI3Examples#addRoute}
 * ----
 *
 * .Add operations with operationId
 * IMPORTANT: Usage of combination of path and HTTP method is allowed, but it's better to add operations handlers with operationId, for performance reasons and to avoid paths nomenclature errors
 *
 * Now you can use parameter values as described above
 *
 * == Define security handlers
 * A security handler is defined by a combination of schema name and scope. You can mount only one security handler for a combination.
 * For example:
 *
 * [source,$lang]
 * ----
 * {@link examples.OpenAPI3Examples#addSecurityHandler}
 * ----
 *
 * You can of course use included Vert.x security handlers, for example:
 *
 * [source,$lang]
 * ----
 * {@link examples.OpenAPI3Examples#addJWT}
 * ----
 *
 * === Error handling
 * The router factory allows you to manage errors efficiently:
 *
 * * It automatically mounts a 501 `Not Implemented` handler for operations where you haven't mounted any handler
 * * It automatically mounts a 400 `Bad Request` handler that manages `ValidationException` (You can enable/disable this feature via {@link io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory#enableValidationFailureHandler(boolean)})
 *
 * === Generate the router
 * When you are ready, generate the router and use it:
 *
 * [source,$lang]
 * ----
 * {@link examples.OpenAPI3Examples#generateRouter}
 * ----
 *
 */
@Document(fileName = "index.adoc")
@ModuleGen(name = "vertx-web-api-contract", groupPackage = "io.vertx")
package io.vertx.ext.web.api;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;
