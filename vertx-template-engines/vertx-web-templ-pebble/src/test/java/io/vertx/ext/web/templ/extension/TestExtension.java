package io.vertx.ext.web.templ.extension;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Function;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by jensklingsporn on 22.02.17.
 */
public class TestExtension extends AbstractExtension{

  @Override
  public Map<String, Function> getFunctions() {
    return Collections.singletonMap("createString",new CreateStringFunction());
  }

  class CreateStringFunction implements Function{

    @Override
    public Object execute(Map<String, Object> args) {
      return "TEST";
    }

    @Override
    public List<String> getArgumentNames() {
      return Collections.emptyList();
    }
  }
}
