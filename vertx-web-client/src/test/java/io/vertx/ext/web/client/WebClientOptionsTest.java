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

import io.vertx.core.impl.launcher.commands.VersionCommand;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Thomas Segismont
 */
public class WebClientOptionsTest {

  @Test
  public void testFromJson() {
    JsonObject json = new JsonObject()
      .put("defaultPort", 4848)
      .put("userAgentEnabled", false)
      .put("maxPoolSize", 50);
    WebClientOptions options = new WebClientOptions(json);
    assertEquals(4848, options.getDefaultPort());
    assertFalse(options.isUserAgentEnabled());
    assertEquals("Vert.x-WebClient/" + VersionCommand.getVersion(), options.getUserAgent());
    assertEquals(50, options.getMaxPoolSize());
  }

  @Test
  public void testToJson() {
    WebClientOptions options = new WebClientOptions()
      .setDefaultPort(4848)
      .setMaxPoolSize(50)
      .setUserAgentEnabled(false);
    JsonObject json = options.toJson();
    assertEquals(4848, (int) json.getInteger("defaultPort"));
    assertEquals(50, (int) json.getInteger("maxPoolSize"));
    assertEquals(false, json.getBoolean("userAgentEnabled"));
  }
}
