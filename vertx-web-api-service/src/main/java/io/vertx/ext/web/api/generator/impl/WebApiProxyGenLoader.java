package io.vertx.ext.web.api.generator.impl;

import io.vertx.codegen.Generator;
import io.vertx.codegen.GeneratorLoader;
import io.vertx.serviceproxy.generator.GeneratorUtils;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.stream.Stream;

/**
 * @author <a href="http://slinkydeveloper.github.io">Francesco Guardiani @slinkydeveloper</a>
 */
public class WebApiProxyGenLoader implements GeneratorLoader {

  @Override
  public Stream<Generator<?>> loadGenerators(ProcessingEnvironment processingEnv) {
    GeneratorUtils utils = new GeneratorUtils();
    return Stream.of(new WebApiProxyHandlerGen(utils));
  }
}
