/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.ext.web.client;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.Oauth2Credentials;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class OAuth2FormRetryTest extends WebClientTestBase {

  @Test
  public void retryUrlEncodedForm() throws Exception {
    testFormRetry(false, false, false);
  }

  @Test
  public void retryMultipartForm() throws Exception {
    testFormRetry(true, false, false);
  }

  @Test
  public void retryLargeUrlEncodedForm() throws Exception {
    testFormRetry(false, false, true);
  }

  @Test
  public void retryLargeMultipartForm() throws Exception {
    testFormRetry(true, false, true);
  }

  @Test
  public void urlEncodedFormStopsAfterSecond401() throws Exception {
    testFormRetry(false, true, false);
  }

  @Test
  public void multipartFormStopsAfterSecond401() throws Exception {
    testFormRetry(true, true, false);
  }

  private void testFormRetry(boolean multipart, boolean alwaysUnauthorized, boolean large) throws Exception {
    AtomicInteger grants = new AtomicInteger();
    AtomicInteger requests = new AtomicInteger();
    String value = "synthetic +&=% token";
    if (large) {
      value = String.join("", Collections.nCopies(256, value));
    }
    MultiMap form = MultiMap.caseInsensitiveMultiMap()
      .add("access_token", value).add("scope", "read").add("scope", "write");

    server.requestHandler(req -> {
      assertEquals(HttpMethod.POST, req.method());
      if ("/oauth/token".equals(req.path())) {
        int grant = grants.incrementAndGet();
        assertTrue(grant <= 2);
        req.response().putHeader("Content-Type", "application/json").end(new JsonObject()
          .put("access_token", "service-token-" + grant)
          .put("token_type", "Bearer").put("expires_in", 3600).encode());
      } else {
        assertEquals("/protected/path", req.path());
        req.setExpectMultipart(true);
        req.endHandler(v -> {
          int attempt = requests.incrementAndGet();
          assertTrue(attempt <= 2);
          assertEquals("Bearer service-token-" + attempt, req.getHeader("Authorization"));
          String contentType = multipart ? "multipart/form-data;" : "application/x-www-form-urlencoded";
          assertTrue(req.getHeader("Content-Type").startsWith(contentType));
          assertEquals(form.get("access_token"), req.getFormAttribute("access_token"));
          assertEquals(Arrays.asList("read", "write"), req.formAttributes().getAll("scope"));
          req.response().setStatusCode(attempt == 1 || alwaysUnauthorized ? 401 : 200).end();
        });
      }
    });
    startServer();

    OAuth2Auth auth = OAuth2Auth.create(vertx, new OAuth2Options()
      .setClientId("client-id").setClientSecret("client-secret").setSite("http://localhost:8080"));
    try {
      OAuth2WebClient oauthClient = OAuth2WebClient.create(webClient, auth,
        new OAuth2WebClientOptions().setRenewTokenOnForbidden(true))
        .withCredentials(new Oauth2Credentials().setFlow(OAuth2FlowType.CLIENT));
      HttpRequest<Buffer> request = oauthClient.post(8080, "localhost", "/protected/path").timeout(5000);
      if (multipart) {
        request.putHeader("Content-Type", "multipart/form-data");
      }
      request.sendForm(form).onComplete(onSuccess(response -> {
        assertEquals(alwaysUnauthorized ? 401 : 200, response.statusCode());
        assertEquals(2, grants.get());
        assertEquals(2, requests.get());
        testComplete();
      }));
      await(10, TimeUnit.SECONDS);
    } finally {
      auth.close();
    }
  }
}
