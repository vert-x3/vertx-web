/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.ext.web.client.predicate;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.impl.predicate.ResponsePredicateImpl;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * A predicate on {@link HttpResponse}.
 * <p>
 * By default, a Vert.x Web Client request ends with an error only if something wrong happens at the network level.
 * In other words, a {@code 404 Not Found} response, or a response with the wrong content type, are <em>NOT</em> considered as failures.
 * <p>
 * {@link ResponsePredicate Response predicates} can fail a request when the response does not match some criteria.
 * <p>
 * Custom predicate instances can be used with {@link HttpRequest#expect(Function)}.
 * <p>
 * As a convenience, a few predicates for common uses cases are predefined. For example:
 * <ul>
 * <li>{@link #SC_SUCCESS} to verify that the response has a {@code 2xx} code, or</li>
 * <li>{@link #JSON} to verify that the response body contains JSON data.</li>
 * <li>...</li>
 * </ul>
 * Predefined predicates use the default error converter (discarding the body).
 * <p>
 * However, you can create a new {@link ResponsePredicate} instance from an existing one using {@link #create(Function)} or
 * {@link #create(Function, ErrorConverter)} when the body is required to build the validation failure.
 */
@VertxGen
public interface ResponsePredicate extends Function<HttpResponse<Void>, ResponsePredicateResult> {

  /**
   * Any 1XX informational response
   */
  ResponsePredicate SC_INFORMATIONAL_RESPONSE = status(100, 200);

  /**
   * 100 Continue
   */
  ResponsePredicate SC_CONTINUE = status(100);

  /**
   * 101 Switching Protocols
   */
  ResponsePredicate SC_SWITCHING_PROTOCOLS = status(101);

  /**
   * 102 Processing (WebDAV, RFC2518)
   */
  ResponsePredicate SC_PROCESSING = status(102);

  /**
   * 103 Early Hints
   */
  ResponsePredicate SC_EARLY_HINTS = status(103);

  /**
   * Any 2XX success
   */
  ResponsePredicate SC_SUCCESS = status(200, 300);

  /**
   * 200 OK
   */
  ResponsePredicate SC_OK = status(200);

  /**
   * 201 Created
   */
  ResponsePredicate SC_CREATED = status(201);

  /**
   * 202 Accepted
   */
  ResponsePredicate SC_ACCEPTED = status(202);

  /**
   * 203 Non-Authoritative Information (since HTTP/1.1)
   */
  ResponsePredicate SC_NON_AUTHORITATIVE_INFORMATION = status(203);

  /**
   * 204 No Content
   */
  ResponsePredicate SC_NO_CONTENT = status(204);

  /**
   * 205 Reset Content
   */
  ResponsePredicate SC_RESET_CONTENT = status(205);

  /**
   * 206 Partial Content
   */
  ResponsePredicate SC_PARTIAL_CONTENT = status(206);

  /**
   * 207 Multi-Status (WebDAV, RFC2518)
   */
  ResponsePredicate SC_MULTI_STATUS = status(207);

  /**
   * Any 3XX redirection
   */
  ResponsePredicate SC_REDIRECTION = status(300, 400);

  /**
   * 300 Multiple Choices
   */
  ResponsePredicate SC_MULTIPLE_CHOICES = status(300);

  /**
   * 301 Moved Permanently
   */
  ResponsePredicate SC_MOVED_PERMANENTLY = status(301);

  /**
   * 302 Found
   */
  ResponsePredicate SC_FOUND = status(302);

  /**
   * 303 See Other (since HTTP/1.1)
   */
  ResponsePredicate SC_SEE_OTHER = status(303);

  /**
   * 304 Not Modified
   */
  ResponsePredicate SC_NOT_MODIFIED = status(304);

  /**
   * 305 Use Proxy (since HTTP/1.1)
   */
  ResponsePredicate SC_USE_PROXY = status(305);

  /**
   * 307 Temporary Redirect (since HTTP/1.1)
   */
  ResponsePredicate SC_TEMPORARY_REDIRECT = status(307);

  /**
   * 308 Permanent Redirect (RFC7538)
   */
  ResponsePredicate SC_PERMANENT_REDIRECT = status(308);

  /**
   * Any 4XX client error
   */
  ResponsePredicate SC_CLIENT_ERRORS = status(400, 500);

  /**
   * 400 Bad Request
   */
  ResponsePredicate SC_BAD_REQUEST = status(400);

  /**
   * 401 Unauthorized
   */
  ResponsePredicate SC_UNAUTHORIZED = status(401);

  /**
   * 402 Payment Required
   */
  ResponsePredicate SC_PAYMENT_REQUIRED = status(402);

  /**
   * 403 Forbidden
   */
  ResponsePredicate SC_FORBIDDEN = status(403);

  /**
   * 404 Not Found
   */
  ResponsePredicate SC_NOT_FOUND = status(404);

  /**
   * 405 Method Not Allowed
   */
  ResponsePredicate SC_METHOD_NOT_ALLOWED = status(405);

  /**
   * 406 Not Acceptable
   */
  ResponsePredicate SC_NOT_ACCEPTABLE = status(406);

  /**
   * 407 Proxy Authentication Required
   */
  ResponsePredicate SC_PROXY_AUTHENTICATION_REQUIRED = status(407);

  /**
   * 408 Request Timeout
   */
  ResponsePredicate SC_REQUEST_TIMEOUT = status(408);

  /**
   * 409 Conflict
   */
  ResponsePredicate SC_CONFLICT = status(409);

  /**
   * 410 Gone
   */
  ResponsePredicate SC_GONE = status(410);

  /**
   * 411 Length Required
   */
  ResponsePredicate SC_LENGTH_REQUIRED = status(411);

  /**
   * 412 Precondition Failed
   */
  ResponsePredicate SC_PRECONDITION_FAILED = status(412);

  /**
   * 413 Request Entity Too Large
   */
  ResponsePredicate SC_REQUEST_ENTITY_TOO_LARGE = status(413);

  /**
   * 414 Request-URI Too Long
   */
  ResponsePredicate SC_REQUEST_URI_TOO_LONG = status(414);

  /**
   * 415 Unsupported Media Type
   */
  ResponsePredicate SC_UNSUPPORTED_MEDIA_TYPE = status(415);

  /**
   * 416 Requested Range Not Satisfiable
   */
  ResponsePredicate SC_REQUESTED_RANGE_NOT_SATISFIABLE = status(416);

  /**
   * 417 Expectation Failed
   */
  ResponsePredicate SC_EXPECTATION_FAILED = status(417);

  /**
   * 421 Misdirected Request
   */
  ResponsePredicate SC_MISDIRECTED_REQUEST = status(421);

  /**
   * 422 Unprocessable Entity (WebDAV, RFC4918)
   */
  ResponsePredicate SC_UNPROCESSABLE_ENTITY = status(422);

  /**
   * 423 Locked (WebDAV, RFC4918)
   */
  ResponsePredicate SC_LOCKED = status(423);

  /**
   * 424 Failed Dependency (WebDAV, RFC4918)
   */
  ResponsePredicate SC_FAILED_DEPENDENCY = status(424);

  /**
   * 425 Unordered Collection (WebDAV, RFC3648)
   */
  ResponsePredicate SC_UNORDERED_COLLECTION = status(425);

  /**
   * 426 Upgrade Required (RFC2817)
   */
  ResponsePredicate SC_UPGRADE_REQUIRED = status(426);

  /**
   * 428 Precondition Required (RFC6585)
   */
  ResponsePredicate SC_PRECONDITION_REQUIRED = status(428);

  /**
   * 429 Too Many Requests (RFC6585)
   */
  ResponsePredicate SC_TOO_MANY_REQUESTS = status(429);

  /**
   * 431 Request Header Fields Too Large (RFC6585)
   */
  ResponsePredicate SC_REQUEST_HEADER_FIELDS_TOO_LARGE = status(431);

  /**
   * Any 5XX server error
   */
  ResponsePredicate SC_SERVER_ERRORS = status(500, 600);

  /**
   * 500 Internal Server Error
   */
  ResponsePredicate SC_INTERNAL_SERVER_ERROR = status(500);

  /**
   * 501 Not Implemented
   */
  ResponsePredicate SC_NOT_IMPLEMENTED = status(501);

  /**
   * 502 Bad Gateway
   */
  ResponsePredicate SC_BAD_GATEWAY = status(502);

  /**
   * 503 Service Unavailable
   */
  ResponsePredicate SC_SERVICE_UNAVAILABLE = status(503);

  /**
   * 504 Gateway Timeout
   */
  ResponsePredicate SC_GATEWAY_TIMEOUT = status(504);

  /**
   * 505 HTTP Version Not Supported
   */
  ResponsePredicate SC_HTTP_VERSION_NOT_SUPPORTED = status(505);

  /**
   * 506 Variant Also Negotiates (RFC2295)
   */
  ResponsePredicate SC_VARIANT_ALSO_NEGOTIATES = status(506);

  /**
   * 507 Insufficient Storage (WebDAV, RFC4918)
   */
  ResponsePredicate SC_INSUFFICIENT_STORAGE = status(507);

  /**
   * 510 Not Extended (RFC2774)
   */
  ResponsePredicate SC_NOT_EXTENDED = status(510);

  /**
   * 511 Network Authentication Required (RFC6585)
   */
  ResponsePredicate SC_NETWORK_AUTHENTICATION_REQUIRED = status(511);

  /**
   * Creates a predicate asserting that the status response code is equal to {@code statusCode}.
   *
   * @param statusCode the expected status code
   */
  static ResponsePredicate status(int statusCode) {
    return status(statusCode, statusCode + 1);
  }

  /**
   * Creates a predicate asserting that the status response code is in the {@code [min,max[} range.
   *
   * @param min the lower (inclusive) accepted status code
   * @param max the highest (exclusive) accepted status code
   */
  static ResponsePredicate status(int min, int max) {
    return response -> {
      int sc = response.statusCode();
      if (sc >= min && sc < max) {
        return ResponsePredicateResult.success();
      }
      if (max - min == 1) {
        return ResponsePredicateResult.failure("Response status code " + sc + " is not equal to " + min);
      }
      return ResponsePredicateResult.failure("Response status code " + sc + " is not between " + min + " and " + max);
    };
  }

  /**
   * Creates a predicate validating the response {@code content-type} is {@code application/json}.
   */
  ResponsePredicate JSON = contentType("application/json");

  /**
   * Creates a predicate validating the response has a {@code content-type} header matching the {@code mimeType}.
   *
   * @param mimeType the mime type
   */
  static ResponsePredicate contentType(String mimeType) {
    return ResponsePredicate.contentType(Collections.singletonList(mimeType));
  }

  /**
   * Creates a predicate validating the response has a {@code content-type} header matching one of the {@code mimeTypes}.
   *
   * @param mimeTypes the list of mime types
   */
  static ResponsePredicate contentType(List<String> mimeTypes) {
    return response -> {
      String contentType = response.headers().get(HttpHeaders.CONTENT_TYPE);
      if (contentType == null) {
        return ResponsePredicateResult.failure("Missing response content type");
      }
      for (String mimeType : mimeTypes) {
        if (contentType.equalsIgnoreCase(mimeType)) {
          return ResponsePredicateResult.success();
        }
      }
      StringBuilder sb = new StringBuilder("Expect content type ").append(contentType).append(" to be one of ");
      boolean first = true;
      for (String mimeType : mimeTypes) {
        if (!first) {
          sb.append(", ");
        }
        first = false;
        sb.append(mimeType);
      }
      return ResponsePredicateResult.failure(sb.toString());
    };
  }

  /**
   * Creates a new {@link ResponsePredicate}. The default error converter will be used (discarding the body).
   *
   * @param test the function to invoke when the response is received
   */
  static ResponsePredicate create(Function<HttpResponse<Void>, ResponsePredicateResult> test) {
    return test::apply;
  }

  /**
   * Creates a new {@link ResponsePredicate}, using a custom {@code errorConverter}.
   *
   * @param test the function to invoke when the response is received
   * @param errorConverter converts the result of the {@code test} function to a {@link Throwable}
   */
  static ResponsePredicate create(Function<HttpResponse<Void>, ResponsePredicateResult> test, ErrorConverter errorConverter) {
    return new ResponsePredicateImpl(test, errorConverter);
  }

  /**
   * @return the error converter currently used
   */
  default ErrorConverter errorConverter() {
    return ErrorConverter.DEFAULT_CONVERTER;
  }
}
