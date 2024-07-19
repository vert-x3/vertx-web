package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.impl.CSRFHandlerImpl;

/**
 * This handler adds a CSRF token to requests which mutate state. In order change the state a (XSRF-TOKEN) cookie is set
 * with a unique token, that is expected to be sent back in a (X-XSRF-TOKEN) header.
 * <p>
 * The behavior is to check the request body header and cookie for validity.
 * <p>
 * This Handler requires session support, thus should be added somewhere below Session and Body handlers.
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@VertxGen
public interface CSRFHandler extends InputTrustHandler {

  /**
   * Instantiate a new CSRFHandlerImpl with a secret and default {@link CSRFHandlerOptions}.
   * <p>
   * <pre>
   * CSRFHandler.create("s3cr37")
   * </pre>
   *
   * @param secret server secret to sign the token.
   */
  static CSRFHandler create(Vertx vertx, String secret) {
    return new CSRFHandlerImpl(vertx, secret, new CSRFHandlerOptions());
  }

  /**
   * Like {@link #create(Vertx, String)}, with the given {@link CSRFHandlerOptions}.
   */
  static CSRFHandler create(Vertx vertx, String secret, CSRFHandlerOptions options) {
    return new CSRFHandlerImpl(vertx, secret, options);
  }
}
