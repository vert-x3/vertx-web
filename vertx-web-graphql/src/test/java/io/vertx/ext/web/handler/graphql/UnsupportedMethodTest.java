/*
 * Copyright 2019 Red Hat, Inc.
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

package io.vertx.ext.web.handler.graphql;

import org.junit.Test;

import static io.vertx.core.http.HttpMethod.PUT;

/**
 * @author Thomas Segismont
 */
public class UnsupportedMethodTest extends GraphQLTestBase {

  @Test
  public void testUnsupportedMethod() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(PUT)
      .setGraphQLQuery("query { allLinks { url } }");
    request.send(client, 405, onSuccess(v -> {
      testComplete();
    }));
    await();
  }
}
