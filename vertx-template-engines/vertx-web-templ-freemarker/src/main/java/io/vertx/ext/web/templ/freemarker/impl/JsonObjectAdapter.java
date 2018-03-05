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

import freemarker.template.AdapterTemplateModel;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleCollection;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.WrappingTemplateModel;
import io.vertx.core.json.JsonObject;

/**
 * @author Thomas Segismont
 */
public class JsonObjectAdapter extends WrappingTemplateModel implements TemplateHashModelEx, AdapterTemplateModel {

  private final JsonObject jsonObject;

  public JsonObjectAdapter(JsonObject jsonObject, ObjectWrapper ow) {
    super(ow);
    this.jsonObject = jsonObject;
  }

  @Override
  public TemplateModel get(String key) throws TemplateModelException {
    Object value = jsonObject.getValue(key);
    return value == null ? null : wrap(value);
  }

  @Override
  public boolean isEmpty() throws TemplateModelException {
    return jsonObject.isEmpty();
  }

  @Override
  public int size() throws TemplateModelException {
    return jsonObject.size();
  }

  @Override
  public TemplateCollectionModel keys() throws TemplateModelException {
    return new SimpleCollection(jsonObject.fieldNames(), getObjectWrapper());
  }

  @Override
  public TemplateCollectionModel values() throws TemplateModelException {
    return new SimpleCollection(jsonObject.getMap().values(), getObjectWrapper());
  }

  @Override
  public Object getAdaptedObject(Class hint) {
    return jsonObject;
  }

}
