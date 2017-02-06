/*
 * Copyright 2017 Red Hat, Inc.
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

package io.vertx.ext.web.client;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.impl.UserAgentUtil;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Thomas Segismont
 */
public class WebClientOptionsTest {

  @Test
  public void testFromJson() {
    JsonObject json = new JsonObject()
      .put("userAgentEnabled", false)
      .put("maxPoolSize", 50);
    WebClientOptions options = new WebClientOptions(json);
    assertFalse(options.isUserAgentEnabled());
    assertEquals(UserAgentUtil.loadUserAgent(), options.getUserAgent());
    assertEquals(50, options.getMaxPoolSize());
  }

}
