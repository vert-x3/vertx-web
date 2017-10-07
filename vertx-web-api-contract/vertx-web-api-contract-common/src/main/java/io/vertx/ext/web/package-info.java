/**
 * = Vert.x-Web API Contract
 * :toc: left
 *
 * == Validate the requests
 * Vert.x provide a validation framework that will validate requests for you and will put results of validation inside a container. To define a {@link io.vertx.ext.web.validation.HTTPRequestValidationHandler}:
 * [source,$lang]
 * ----
 * {@link examples.WebExamples#example63}
 * ----
 *
 * Then you can mount your validation handler:
 * [source,$lang]
 * ----
 * {@link examples.WebExamples#example64}
 * ----
 *
 * If validation succeeds, It returns request parameters inside {@link io.vertx.ext.web.RequestParameters}, otherwise It will throw a {@link io.vertx.ext.web.validation.ValidationException}
 *
 * === Types of request parameters
 * Every parameter has a type validator, a class that describes the expected type of parameter.
 * A type validator validates the value, casts it in required language type and then loads it inside a {@link io.vertx.ext.web.RequestParameter} object. There are three ways to describe the type of your parameter:
 *
 * * There is a set of prebuilt types that you can use: {@link io.vertx.ext.web.validation.ParameterType}
 * * You can instantiate a custom instance of prebuilt type validators using static methods of {@link io.vertx.ext.web.validation.ParameterTypeValidator} and then load it into {@link io.vertx.ext.web.validation.HTTPRequestValidationHandler} using functions ending with `WithCustomTypeValidator`
 * * You can create your own `ParameterTypeValidator` implementing {@link io.vertx.ext.web.validation.ParameterTypeValidator} interface
 *
 * === Handling parameters
 * Now you can handle parameter values:
 *
 * [source,$lang]
 * ----
 * {@link examples.WebExamples#example65}
 * ----
 *
 * As you can see, every parameter is mapped in respective language objects. You can also get a json body:
 *
 * [source,$lang]
 * ----
 * {@link examples.WebExamples#example66}
 * ----
 *
 */
@Document(fileName = "index.adoc")
@ModuleGen(name = "vertx-web-api-contract", groupPackage = "io.vertx")
package io.vertx.ext.web;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;
