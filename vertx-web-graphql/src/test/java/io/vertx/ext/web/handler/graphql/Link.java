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

/**
 * @author Thomas Segismont
 */
public class Link {
  private final String url;
  private final String description;
  private final String userId;

  public Link(String url, String description, String userId) {
    this.url = url;
    this.description = description;
    this.userId = userId;
  }

  public String getUrl() {
    return url;
  }

  public String getDescription() {
    return description;
  }

  public String getUserId() {
    return userId;
  }
}
