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
 * * request parameters
 * * unified error handling
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
 * You create an {@link io.vertx.webclient.WebClient} instance with default options as follows
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#create}
 * ----
 *
 * If you want to configure options for the client, you create it as follows
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#createFromOptions}
 * ----
 *
 * == Making requests
 *
 * === Simple requests with no body
 *
 * Often, you’ll want to make HTTP requests with no request body. This is usually the case with HTTP GET, OPTIONS
 * and HEAD requests
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#simpleGetAndHead}
 * ----
 *
 * You can add query parameters to the request URI in a fluent fashion
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#simpleGetWithParams(io.vertx.webclient.WebClient)}
 * ----
 *
 * Any request URI parameter will pre-populate the request
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#simpleGetWithInitialParams(io.vertx.webclient.WebClient)}
 * ----
 *
 * === Writing request bodies
 *
 * When you need to make a request with a body, you use the same API and call then `sendXXX` methods
 * that expects a body to send.
 *
 * Use {@link io.vertx.webclient.HttpRequest#sendBuffer} to send a body buffer
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#sendBuffer(io.vertx.webclient.WebClient, io.vertx.core.buffer.Buffer)}
 * ----
 *
 * Sending a single buffer is useful but often you don't want to load fully the content in memory, for this
 * purpose the web client can send `ReadStream<Buffer>` (for example a {@link io.vertx.core.file.AsyncFile}
 * is a ReadStream<Buffer>`) with the {@link io.vertx.webclient.HttpRequest#sendStream} method
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#sendStreamChunked(io.vertx.webclient.WebClient, io.vertx.core.streams.ReadStream)}
 * ----
 *
 * The web client takes care of setting up the transfer Pump for you. The request will use chunked transfer
 * encoding as the length of the stream is not know.
 *
 * When you know the size of the stream, you shall specify before using the `content-length` header
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#sendStream(io.vertx.webclient.WebClient, io.vertx.core.file.FileSystem)}
 * ----
 *
 * ==== Json bodies
 *
 * Often you’ll want to write requests which have a Json body. To send a {@link io.vertx.core.json.JsonObject}
 * use the {@link io.vertx.webclient.HttpRequest#sendJsonObject(io.vertx.core.json.JsonObject, io.vertx.core.Handler)}
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#sendJsonObject(io.vertx.webclient.WebClient)}
 * ----
 *
 * In Java, Groovy or Kotlin, you can use the {@link io.vertx.webclient.HttpRequest#sendJson} method that maps
 * a POJO (Plain Old Java Object) to a Json object using {@link io.vertx.core.json.Json#encode(java.lang.Object)}
 * method
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#sendJson(io.vertx.webclient.WebClient)}
 * ----
 *
 * NOTE: the {@link io.vertx.core.json.Json#encode(java.lang.Object)} uses the Jackson mapper to encode the object
 * to Json.
 *
 * === Writing request headers
 *
 * You can write headers to a request using the headers multi-map as follows:
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#sendHeaders1(io.vertx.webclient.WebClient)}
 * ----
 *
 * The headers are an instance of {@link io.vertx.core.MultiMap} which provides operations for adding,
 * setting and removing entries. Http headers allow more than one value for a specific key.
 *
 * You can also write headers using putHeader
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#sendHeaders2(io.vertx.webclient.WebClient)}
 * ----
 *
 * === Reusing requests
 *
 * The {@link io.vertx.webclient.HttpRequest#send(io.vertx.core.Handler)} method can be called multiple times
 * safely, making it very easy to configure http requests and reuse them
 *
 * [source,java]
 * ----
 * {@link examples.WebClientExamples#multiGet(io.vertx.webclient.WebClient)}
 * ----
 *
 * == Handling http responses
 *
 * todo
 *
 * === Decoding responses
 *
 * todo
 *
 * === RxJava API
 *
 * The RxJava {@link io.vertx.rxjava.webclient.HttpRequest} provides an rx-ified version of the original API,
 * the {@link io.vertx.rxjava.webclient.HttpRequest#rxSend()} method returns a `Single<HttpResponse<Buffer>>` that
 * makes the HTTP request upon subscription, as consequence, the {@code Single} can be subscribed many times.
 *
 * [source,java]
 * ----
 * {@link examples.RxWebClientExamples#simpleGet(io.vertx.rxjava.webclient.WebClient)}
 * ----
 *
 * The obtained {@code Single} can be composed and chained naturally with the RxJava API
 *
 * [source,java]
 * ----
 * {@link examples.RxWebClientExamples#flatMap(io.vertx.rxjava.webclient.WebClient)}
 * ----
 *
 *
 *
 *
 */
@Document(fileName = "index.adoc")
@ModuleGen(name = "vertx-web-client", groupPackage = "io.vertx")
package io.vertx.webclient;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;
