/*
 * Copyright 2018 Bosch Software Innovations GmbH.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.handler;

import static org.mockito.Mockito.*;

import org.junit.Test;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.AuthHandlerImpl;
import io.vertx.ext.web.handler.impl.HttpStatusException;

public class CustomAuthHandlerTest extends AuthHandlerTestBase {

  @Override
  protected AuthHandler createAuthHandler(AuthProvider authProvider) {
    return newAuthHandler(authProvider, null);
  }

  private AuthHandler newAuthHandler(AuthProvider authProvider, Handler<Throwable> exceptionProcessor) {
    return new AuthHandlerImpl(authProvider) {

      @Override
      public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {
        handler.handle(Future.succeededFuture(new JsonObject()));
      }

      @Override
      public void processException(RoutingContext ctx, Throwable exception) {
        if (exceptionProcessor != null) {
            exceptionProcessor.handle(exception);
        }
        super.processException(ctx, exception);
      }
    };
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCredentialsValidationErrorPropagation() throws Exception {

    Handler<RoutingContext> handler = rc -> {
      fail("should not get here");
      rc.response().end("Welcome to the protected resource!");
    };

    Throwable rootCause = new IllegalArgumentException("validation of credentials failed");
    AuthProvider authProvider = mock(AuthProvider.class);
    doAnswer(invocation -> {
      final Handler<AsyncResult<User>> resultHandler = invocation.getArgument(1);
      resultHandler.handle(Future.failedFuture(rootCause));
      return null;
    }).when(authProvider).authenticate(any(JsonObject.class), any(Handler.class));

    router.route("/protected/*").handler(newAuthHandler(authProvider, exception -> {
      assertTrue(exception instanceof HttpStatusException);
      assertEquals(rootCause, ((HttpStatusException) exception).getCause());
    }));

    router.route("/protected/somepage").handler(handler);

    testRequest(HttpMethod.GET, "/protected/somepage", 401, "Unauthorized");
  }
}
