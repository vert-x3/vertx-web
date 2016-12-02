/**
 * = Vert.x Web Client
 *
 * Vert.x Web Client is an asynchronous HTTP / HTTP/2 client.
 *
 * The Web Client makes easy to do HTTP request/response interactions with a web server, and provides advanced
 * features like:
 *
 * * Json body encoding / decoding
 * * request/response pumping
 * * error handling
 *
 * The web client does not deprecate the Vert.x Core {@link io.vertx.core.http.HttpClient}, it is actually based on
 * it and therefore inherits its configuration and great features like pooling. The {@link io.vertx.core.http.HttpClient}
 * should be used when fine grained control over the HTTP requests/response is necessary.
 *
 * == Using the web client
 *
 * To use Vert.x Web Client, add the following dependency to the _dependencies_ section of your build descriptor:
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
 * == Re-cap on Vert.x core HTTP client
 *
 * Vert.x Web Client uses the API from Vert.x core, so it's well worth getting familiar with the basic concepts of using
 * {@link io.vertx.core.http.HttpClient} using Vert.x core, if you're not already.
 *
 * == Creating a web client
 *
 * You create an {@link io.vertx.webclient.WebClient} instance with default options as follows:
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#create}
 * ----
 *
 * If you want to configure options for the client, you create it as follows:
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#createFromOptions}
 * ----
 *
 * == Making requests
 *
 * === Simple requests with no request body
 *
 * Often, you’ll want to make HTTP requests with no request body. This is usually the case with HTTP GET, OPTIONS
 * and HEAD requests:
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#simpleGetAndHead}
 * ----
 *
 * You can add query parameters to the request URI in a fluent fashion:
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#simpleGetWithParams(io.vertx.webclient.WebClient)}
 * ----
 *
 * Any request URI parameter will pre-populate the request:
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#simpleGetWithInitialParams(io.vertx.webclient.WebClient)}
 * ----
 *
 * === Requests with a body
 *
 * todo : show how to send a buffer or stream buffers
 *
 * ==== Sending json
 *
 * ==== Sending forms
 *
 * === Reusing requests
 *
 * The {@link io.vertx.webclient.HttpRequest#send(io.vertx.core.Handler)} method can be called multiple times
 * safely, making it very easy to configure http requests and reuse them:
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#multiGet(io.vertx.webclient.WebClient)}
 * ----
 *
 * === Decoding responses
 *
 * todo
 *
 */
@Document(fileName = "index.adoc")
@ModuleGen(name = "vertx-web-client", groupPackage = "io.vertx")
package io.vertx.webclient;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;
