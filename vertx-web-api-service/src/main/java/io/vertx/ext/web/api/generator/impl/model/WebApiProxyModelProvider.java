package io.vertx.ext.web.api.generator.impl.model;

import io.vertx.codegen.Model;
import io.vertx.codegen.ModelProvider;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
public class WebApiProxyModelProvider implements ModelProvider {
  @Override
  public Model getModel(ProcessingEnvironment env, TypeElement elt) {
    if (elt.getAnnotation(WebApiServiceGen.class) != null) {
      WebApiProxyModel model = new WebApiProxyModel(env, elt);
      return model;
    } else {
      return null;
    }
  }
}
