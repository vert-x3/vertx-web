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

import io.vertx.ext.web.handler.graphql.dataloader.VertxMappedBatchLoader;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderFactory;
import org.dataloader.DataLoaderRegistry;
import org.dataloader.MappedBatchLoaderWithContext;

import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@SuppressWarnings("deprecation")
public class DeprecatedVertxMappedBatchLoaderTest extends VertxMappedBatchLoaderTest {

  @Override
  protected void setUpGraphQLHandler() {
    MappedBatchLoaderWithContext<String, User> userBatchLoader = VertxMappedBatchLoader.create(
      (keys, environment, mapPromise) -> {
        if (batchloaderInvoked.compareAndSet(false, true)) {
          mapPromise.complete(keys
            .stream()
            .map(testData.users::get)
            .collect(toMap(User::getId, Function.identity()))
          );
        } else {
          mapPromise.fail(new IllegalStateException());
        }
      }
    );

    graphQLHandler.dataLoaderRegistry(rc -> {
      DataLoader<String, User> userDataLoader = DataLoaderFactory.newMappedDataLoader(userBatchLoader);
      return new DataLoaderRegistry().register("user", userDataLoader);
    });
  }
}
