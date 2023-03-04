package io.vertx.ext.web.templ.extension;

import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by jensklingsporn on 22.02.17.
 */
public class TestExtension extends AbstractExtension {

  @Override
  public Map<String, Function> getFunctions() {
    return Collections.singletonMap("createString", new CreateStringFunction());
  }

  static class CreateStringFunction implements Function {

    @Override
    public Object execute(Map<String, Object> map, PebbleTemplate pebbleTemplate, EvaluationContext evaluationContext, int i) {
      return "TEST";
    }

    @Override
    public List<String> getArgumentNames() {
      return Collections.emptyList();
    }
  }
}
