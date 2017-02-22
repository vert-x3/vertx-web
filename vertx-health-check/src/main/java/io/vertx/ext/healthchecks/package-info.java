/**
 * = Vert.x Health Checks
 *
 * This component provides a simple way to expose health checks. Health checks are used to express the current state
 * of the application in very simple terms: _UP_ or _DOWN_. The health checks can be used individually, or in
 * combination to Vert.x Web or the event bus.
 *
 * This component provides a Vert.x Web handler on which you
 * can register procedure testing the health of the application. The handler computes the final state and returns the
 * result as JSON.
 *
 * == Using Vert.x Health Checks
 *
 * Notice that you generally need Vert.x Web to use this component. In addition add the following dependency:
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
 * compile '${maven.groupId}:${maven.artifactId}:${maven.version}'
 * ----
 *
 * === Creating the health check object.
 *
 * The central object is {@link io.vertx.ext.healthchecks.HealthChecks}. You can create a new instance using:
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#example1(io.vertx.core.Vertx)}
 * ----
 *
 * Once you have created this object you can register and unregister procedures. See more about this below.
 *
 * === Registering the Vert.x Web handler
 *
 * To create the Vert.x Web handler managing your health check you can either:
 *
 * * using an existing instance of {@link io.vertx.ext.healthchecks.HealthChecks}
 * * let the handler create one instance for you.
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#example2(io.vertx.core.Vertx)}
 * ----
 *
 * Procedure registration can be directly made on the {@link io.vertx.ext.healthchecks.HealthCheckHandler}
 * instance. Alternatively, if you have created the {@link io.vertx.ext.healthchecks.HealthChecks} instance
 * beforehand, you can register the procedure on this object directly. Registrations and unregistrations can be done at
 * anytime, even after the route registration:
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#example2(io.vertx.core.Vertx, io.vertx.ext.web.Router)}
 * ----
 *
 * == Procedures
 *
 * A procedure is a function checking some aspect of the system to deduce the current health. It reports a
 * {@link io.vertx.ext.healthchecks.Status} indicating whether or not the test has passed or failed. This function
 * must not block and report to the given {@link io.vertx.core.Future} whether or not it succeed.
 *
 * When you register a procedure, you give a name, and the function (handler) executing the check.
 *
 * Rules deducing the status are the following
 *
 * * if the future is mark as failed, the check is considered as _KO_
 * * if the future is completed successfully but without a {@link io.vertx.ext.healthchecks.Status}, the check
 * is considered as _OK_.
 * * if the future is completed successfully with a {@link io.vertx.ext.healthchecks.Status} marked as _OK_,
 * the check is considered as _OK_.
 * * if the future is completed successfully with a {@link io.vertx.ext.healthchecks.Status} marked as _KO_,
 * the check is considered as _KO_.
 *
 * {@link io.vertx.ext.healthchecks.Status} can also provide additional data:
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#example4(io.vertx.core.Vertx, io.vertx.ext.web.Router)}
 * ----
 *
 * Procedures can be organised by groups. The procedure name indicates the group. The procedures are organized as a
 * tree and the structure is mapped to HTTP urls (see below).
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#example3(io.vertx.core.Vertx, io.vertx.ext.web.Router)}
 * ----
 *
 * == HTTP responses and JSON Output
 *
 * When using the Vert.x web handler, the overall health check is retrieved using a HTTP GET or POST (depending on
 * the route you registered) on the route given when exposing the
 * {@link io.vertx.ext.healthchecks.HealthCheckHandler}.
 *
 * If no procedure are registered, the response is `204 - NO CONTENT`, indicating that the system is _UP_ but no
 * procedures has been executed. The response does not contain a payload.
 *
 * If there is at least one procedure registered, this procedure is executed and the outcome status is computed. The
 * response would use the following status code:
 *
 * * `200` : Everything is fine
 * * `503` : At least one procedure has reported a non-healthy state
 * * `500` : One procedure has thrown an error or has not reported a status in time
 *
 * The content is a JSON document indicating the overall result (`outcome`). It's either `UP` or `DOWN`. A `checks`
 * array is also given indicating the result of the different executed procedures. If the procedure has reported
 * additional data, the data is also given:
 *
 * [source]
 * ----
 * {
 *  "checks" : [
 *  {
 *    "id" : "A",
 *    "status" : "UP"
 *  },
 *  {
 *    "id" : "B",
 *    "status" : "DOWN",
 *    "data" : {
 *      "some-data" : "some-value"
 *    }
 *  }
 *  ],
 *  "outcome" : "DOWN"
 * }
 * ----
 *
 * In case of groups/ hierarchy, the `checks` array depicts this structure:
 *
 * [source]
 * ----
 * {
 *  "checks" : [
 *  {
 *    "id" : "my-group",
 *    "status" : "UP",
 *    "checks" : [
 *    {
 *      "id" : "check-2",
 *      "status" : "UP",
 *    },
 *    {
 *      "id" : "check-1",
 *      "status" : "UP"
 *    }]
 *  }],
 *  "outcome" : "UP"
 * }
 * ----
 *
 * If a procedure throws an error, reports a failure (exception), the JSON document provides the `cause` in the
 * `data` section. If a procedure does not report back before a timeout, the indicated cause is `Timeout`.
 *
 * == Examples of procedures
 *
 * This section provides example of common health checks.
 *
 * === JDBC
 *
 * This check reports whether or not a connection to the database can be established:
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#jdbc(io.vertx.ext.jdbc.JDBCClient, HealthCheckHandler)}
 * ----
 *
 * === Service availability
 *
 * This check reports whether or not a service (here a HTTP endpoint) is available in the service discovery:
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#service}
 * ----
 *
 * === Event bus
 *
 * This check reports whether a consumer is ready on the event bus. The protocol, in this example, is a simple
 * ping/pong, but it can be more sophisticated. This check can be used to check whether or not a verticle is ready
 * if it's listening on a specific event address.
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#eventbus(io.vertx.core.Vertx, HealthCheckHandler)}
 * ----
 *
 * == Authentication
 *
 * When using the Vert.x web handler, you can pass a {@link io.vertx.ext.auth.AuthProvider} use to authenticate the
 * request. Check <a href="http://vertx.io/docs/#authentication_and_authorisation">Vert.x Auth</a> for more details
 * about available authentication providers.
 *
 * The Vert.x Web handler creates a JSON object containing:
 *
 * * the request headers
 * * the request params
 * * the form param if any
 * * the content as JSON if any and if the request set the content type to `application/json`.
 *
 * The resulting object is passed to the auth provider to authenticate the request. If the authentication failed, it
 * returns a `403 - FORBIDDEN` response.
 *
 * == Exposing health checks on the event bus
 *
 * While exposing the health checks using HTTP with the Vert.x web handler is convenient, it can be useful
 * to expose the data differently. This section gives an example to expose the data on the event bus:
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#publishOnEventBus(io.vertx.core.Vertx, HealthChecks)}
 * ----
 *
 */
@ModuleGen(name = "vertx-health-checks", groupPackage = "io.vertx")
@Document(fileName = "index.adoc")
package io.vertx.ext.healthchecks;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;
