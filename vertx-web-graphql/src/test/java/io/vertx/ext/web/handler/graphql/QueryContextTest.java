/*
 * Copyright 2021 Red Hat, Inc.
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

import graphql.schema.DataFetchingEnvironment;
import io.vertx.ext.web.RoutingContext;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author Thomas Segismont
 */
@SuppressWarnings("deprecation")
public class QueryContextTest extends GraphQLTestBase {

  private AtomicReference<Object> queryContext = new AtomicReference<>();

  @Override
  protected Object getAllLinks(DataFetchingEnvironment env) {
    if (!queryContext.compareAndSet(null, env.getContext())) {
      throw new IllegalStateException();
    }
    return super.getAllLinks(env);
  }

  @Test
  public void testDefaultQueryContext() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url } }");
    request.send(client, onSuccess(body -> {
      assertThat(queryContext.get(), is(instanceOf(RoutingContext.class)));
      testComplete();
    }));
    await();
  }

  @Test
  public void testCustomQueryContext() throws Exception {
    Object expected = new Object();
    graphQLHandler.queryContext(rc -> expected);
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url } }");
    request.send(client, onSuccess(body -> {
      assertSame(expected, queryContext.get());
      testComplete();
    }));
    await();
  }
}
