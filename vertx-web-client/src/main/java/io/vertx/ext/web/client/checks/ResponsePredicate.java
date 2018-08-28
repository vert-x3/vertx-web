package io.vertx.ext.web.client.checks;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.impl.NoStackTraceThrowable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@FunctionalInterface
@VertxGen
public interface ResponsePredicate extends Predicate<HttpClientResponse> {

  /**
   * Any 1XX informational response
   */
  ResponsePredicate SC_INFORMATIONAL_RESPONSE = new StatusCheck(100, 200);

  /**
   * 100 Continue
   */
  ResponsePredicate SC_CONTINUE = new StatusCheck(100);

  /**
   * 101 Switching Protocols
   */
  ResponsePredicate SC_SWITCHING_PROTOCOLS = new StatusCheck(101);

  /**
   * 102 Processing (WebDAV, RFC2518)
   */
  ResponsePredicate SC_PROCESSING = new StatusCheck(102);

  /**
   * 200 OK
   */
  ResponsePredicate SC_EARLY_HINTS = new StatusCheck(103);

  /**
   * Any 2XX success
   */
  ResponsePredicate SC_SUCCESS = new StatusCheck(200, 300);

  /**
   * 200 OK
   */
  ResponsePredicate SC_OK = new StatusCheck(200);

  /**
   * 201 Created
   */
  ResponsePredicate SC_CREATED = new StatusCheck(201);

  /**
   * 202 Accepted
   */
  ResponsePredicate SC_ACCEPTED = new StatusCheck(202);

  /**
   * 203 Non-Authoritative Information (since HTTP/1.1)
   */
  ResponsePredicate SC_NON_AUTHORITATIVE_INFORMATION = new StatusCheck(203);

  /**
   * 204 No Content
   */
  ResponsePredicate SC_NO_CONTENT = new StatusCheck(204);

  /**
   * 205 Reset Content
   */
  ResponsePredicate SC_RESET_CONTENT = new StatusCheck(205);

  /**
   * 206 Partial Content
   */
  ResponsePredicate SC_PARTIAL_CONTENT = new StatusCheck(206);

  /**
   * 207 Multi-Status (WebDAV, RFC2518)
   */
  ResponsePredicate SC_MULTI_STATUS = new StatusCheck(207);

  /**
   * Any 3XX redirection
   */
  ResponsePredicate SC_REDIRECTION = new StatusCheck(300, 400);

  /**
   * 300 Multiple Choices
   */
  ResponsePredicate SC_MULTIPLE_CHOICES = new StatusCheck(300);

  /**
   * 301 Moved Permanently
   */
  ResponsePredicate SC_MOVED_PERMANENTLY = new StatusCheck(301);

  /**
   * 302 Found
   */
  ResponsePredicate SC_FOUND = new StatusCheck(302);

  /**
   * 303 See Other (since HTTP/1.1)
   */
  ResponsePredicate SC_SEE_OTHER = new StatusCheck(303);

  /**
   * 304 Not Modified
   */
  ResponsePredicate SC_NOT_MODIFIED = new StatusCheck(304);

  /**
   * 305 Use Proxy (since HTTP/1.1)
   */
  ResponsePredicate SC_USE_PROXY = new StatusCheck(305);

  /**
   * 307 Temporary Redirect (since HTTP/1.1)
   */
  ResponsePredicate SC_TEMPORARY_REDIRECT = new StatusCheck(307);

  /**
   * 308 Permanent Redirect (RFC7538)
   */
  ResponsePredicate SC_PERMANENT_REDIRECT = new StatusCheck(308);

  /**
   * Any 4XX client error
   */
  ResponsePredicate SC_CLIENT_ERRORS = new StatusCheck(400, 500);

  /**
   * 400 Bad Request
   */
  ResponsePredicate SC_BAD_REQUEST = new StatusCheck(400);

  /**
   * 401 Unauthorized
   */
  ResponsePredicate SC_UNAUTHORIZED = new StatusCheck(401);

  /**
   * 402 Payment Required
   */
  ResponsePredicate SC_PAYMENT_REQUIRED = new StatusCheck(402);

  /**
   * 403 Forbidden
   */
  ResponsePredicate SC_FORBIDDEN = new StatusCheck(403);

  /**
   * 404 Not Found
   */
  ResponsePredicate SC_NOT_FOUND = new StatusCheck(404);

  /**
   * 405 Method Not Allowed
   */
  ResponsePredicate SC_METHOD_NOT_ALLOWED = new StatusCheck(405);

  /**
   * 406 Not Acceptable
   */
  ResponsePredicate SC_NOT_ACCEPTABLE = new StatusCheck(406);

  /**
   * 407 Proxy Authentication Required
   */
  ResponsePredicate SC_PROXY_AUTHENTICATION_REQUIRED = new StatusCheck(407);

  /**
   * 408 Request Timeout
   */
  ResponsePredicate SC_REQUEST_TIMEOUT = new StatusCheck(408);

  /**
   * 409 Conflict
   */
  ResponsePredicate SC_CONFLICT = new StatusCheck(409);

  /**
   * 410 Gone
   */
  ResponsePredicate SC_GONE = new StatusCheck(410);

  /**
   * 411 Length Required
   */
  ResponsePredicate SC_LENGTH_REQUIRED = new StatusCheck(411);

  /**
   * 412 Precondition Failed
   */
  ResponsePredicate SC_PRECONDITION_FAILED = new StatusCheck(412);

  /**
   * 413 Request Entity Too Large
   */
  ResponsePredicate SC_REQUEST_ENTITY_TOO_LARGE = new StatusCheck(413);

  /**
   * 414 Request-URI Too Long
   */
  ResponsePredicate SC_REQUEST_URI_TOO_LONG = new StatusCheck(414);

  /**
   * 415 Unsupported Media Type
   */
  ResponsePredicate SC_UNSUPPORTED_MEDIA_TYPE = new StatusCheck(415);

  /**
   * 416 Requested Range Not Satisfiable
   */
  ResponsePredicate SC_REQUESTED_RANGE_NOT_SATISFIABLE = new StatusCheck(416);

  /**
   * 417 Expectation Failed
   */
  ResponsePredicate SC_EXPECTATION_FAILED = new StatusCheck(417);

  /**
   * 421 Misdirected Request
   */
  ResponsePredicate SC_MISDIRECTED_REQUEST = new StatusCheck(421);

  /**
   * 422 Unprocessable Entity (WebDAV, RFC4918)
   */
  ResponsePredicate SC_UNPROCESSABLE_ENTITY = new StatusCheck(422);

  /**
   * 423 Locked (WebDAV, RFC4918)
   */
  ResponsePredicate SC_LOCKED = new StatusCheck(423);

  /**
   * 424 Failed Dependency (WebDAV, RFC4918)
   */
  ResponsePredicate SC_FAILED_DEPENDENCY = new StatusCheck(424);

  /**
   * 425 Unordered Collection (WebDAV, RFC3648)
   */
  ResponsePredicate SC_UNORDERED_COLLECTION = new StatusCheck(425);

  /**
   * 426 Upgrade Required (RFC2817)
   */
  ResponsePredicate SC_UPGRADE_REQUIRED = new StatusCheck(426);

  /**
   * 428 Precondition Required (RFC6585)
   */
  ResponsePredicate SC_PRECONDITION_REQUIRED = new StatusCheck(428);

  /**
   * 429 Too Many Requests (RFC6585)
   */
  ResponsePredicate SC_TOO_MANY_REQUESTS = new StatusCheck(429);

  /**
   * 431 Request Header Fields Too Large (RFC6585)
   */
  ResponsePredicate SC_REQUEST_HEADER_FIELDS_TOO_LARGE = new StatusCheck(431);

  /**
   * Any 5XX server error
   */
  ResponsePredicate SC_SERVER_ERRORS = new StatusCheck(500, 600);

  /**
   * 500 Internal Server Error
   */
  ResponsePredicate SC_INTERNAL_SERVER_ERROR = new StatusCheck(500);

  /**
   * 501 Not Implemented
   */
  ResponsePredicate SC_NOT_IMPLEMENTED = new StatusCheck(501);

  /**
   * 502 Bad Gateway
   */
  ResponsePredicate SC_BAD_GATEWAY = new StatusCheck(502);

  /**
   * 503 Service Unavailable
   */
  ResponsePredicate SC_SERVICE_UNAVAILABLE = new StatusCheck(503);

  /**
   * 504 Gateway Timeout
   */
  ResponsePredicate SC_GATEWAY_TIMEOUT = new StatusCheck(504);

  /**
   * 505 HTTP Version Not Supported
   */
  ResponsePredicate SC_HTTP_VERSION_NOT_SUPPORTED = new StatusCheck(505);

  /**
   * 506 Variant Also Negotiates (RFC2295)
   */
  ResponsePredicate SC_VARIANT_ALSO_NEGOTIATES = new StatusCheck(506);

  /**
   * 507 Insufficient Storage (WebDAV, RFC4918)
   */
  ResponsePredicate SC_INSUFFICIENT_STORAGE = new StatusCheck(507);

  /**
   * 510 Not Extended (RFC2774)
   */
  ResponsePredicate SC_NOT_EXTENDED = new StatusCheck(510);

  /**
   * 511 Network Authentication Required (RFC6585)
   */
  ResponsePredicate SC_NETWORK_AUTHENTICATION_REQUIRED = new StatusCheck(511);

  /**
   * Return a predicate asserting that the status response code is in the {@code [min,max[} range.
   * @param min the lower (inclusive) accepted status code
   * @param max the highest (exclusive) accepted status code
   * @return the expectation
   */
  static ResponsePredicate status(int min, int max) {
    return new StatusCheck(min, max);
  }

  /**
   * Predicate matching the HTTP response declares a JSON content type.
   */
  ResponsePredicate JSON = contentType("application/json");

  /**
   * Create a predicate validating the response has a {@code content-type} header matching
   * the {@code mimeType}.
   *
   * @param mimeType the mime type
   * @return the predicate
   */
  static ResponsePredicate contentType(String mimeType) {
    return new ContentTypePredicate(mimeType);
  }

  /**
   * Create a predicate validating the response has a {@code content-type} header matching
   * one of the {@code mimeTypes}.
   *
   * @param mimeTypes the list of mime types
   * @return the predicate
   */
  static ResponsePredicate contentType(List<String> mimeTypes) {
    return new ContentTypePredicate(mimeTypes);
  }

  /**
   * Evaluate the {@code HttpClientResponse} validity.
   *
   * @param response the response
   * @return {@code true} if the response is valid
   */
  @Override
  boolean test(HttpClientResponse response);

  /**
   * Map the http response to a {@code Throwable} describing the error.
   *
   * The default implementation returns a {@code NoStackTraceThrowable} with a vanilla message.
   *
   * @param response the response
   * @return the mapped error
   */
  default Throwable mapToError(HttpClientResponse response) {
    return new NoStackTraceThrowable("Invalid http response");
  }
}
