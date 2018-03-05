/*
 * Copyright 2016 Red Hat, Inc.
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

package io.vertx.ext.web.templ.freemarker.impl;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author Thomas Segismont
 */
public class VertxWebObjectWrapper extends DefaultObjectWrapper {

  public VertxWebObjectWrapper(Version incompatibleImprovements) {
    super(incompatibleImprovements);
  }

  @Override
  protected TemplateModel handleUnknownType(final Object obj) throws TemplateModelException {
    if (obj instanceof JsonArray) {
      return new JsonArrayAdapter((JsonArray) obj, this);
    }
    if (obj instanceof JsonObject) {
      return new JsonObjectAdapter((JsonObject) obj, this);
    }
    return super.handleUnknownType(obj);
  }

}
