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

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.abac.Policy;
import io.vertx.ext.auth.abac.PolicyBasedAuthorizationProvider;
import io.vertx.ext.auth.authorization.AuthorizationProvider;
import io.vertx.ext.auth.authorization.WildcardPermissionBasedAuthorization;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

public class ABACHandlerTest extends WebTestBase {

  @Test
  public void testABAC() throws Exception {

    AuthorizationProvider abac = PolicyBasedAuthorizationProvider.create()
      .addPolicy(new Policy()
        .setName("policy1")
        .addSubject("paulo")
        .addAuthorization(WildcardPermissionBasedAuthorization.create("web:GET").setResource("/protected/somepage")));


    router.route()
      .handler(SimpleAuthenticationHandler.create().authenticate(ctx -> Future.succeededFuture(User.fromName("paulo"))))
      .handler(AuthorizationHandler.create().addAuthorizationProvider(abac))
      .handler(ctx -> ctx.response().end("Welcome to the protected resource!"));

    // the attributes are computed from the request and the policy only gives access to /protected/somepage as a GET
    testRequest(HttpMethod.GET, "/protected/somepage", 200, "OK");
    testRequest(HttpMethod.GET, "/", 403, "Forbidden");
    testRequest(HttpMethod.POST, "/protected/somepage", 403, "Forbidden");
  }
}
