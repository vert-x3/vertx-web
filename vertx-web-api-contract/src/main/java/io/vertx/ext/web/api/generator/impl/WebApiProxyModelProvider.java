package io.vertx.ext.web.api.generator.impl;

import io.vertx.codegen.Model;
import io.vertx.codegen.ModelProvider;
import io.vertx.ext.web.api.annotations.WebApiProxyGen;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

public class WebApiProxyModelProvider implements ModelProvider {
  @Override
  public Model getModel(ProcessingEnvironment env, TypeElement elt) {
    if (elt.getAnnotation(WebApiProxyGen.class) != null) {
      WebApiProxyModel model = new WebApiProxyModel(env, elt);
      return model;
    } else {
      return null;
    }
  }
}
