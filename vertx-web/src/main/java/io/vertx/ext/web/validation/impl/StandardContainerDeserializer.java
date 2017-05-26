package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.validation.ContainerDeserializer;
import io.vertx.ext.web.validation.ValidationException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class StandardContainerDeserializer implements ContainerDeserializer {

  private String separator;

  public StandardContainerDeserializer(String separator) {
    this.separator = separator;
  }

  @Override
  public List<String> deserializeArray(String serialized) throws ValidationException {
    List<String> values = new ArrayList<>();
    for (String v : serialized.split(separator)) {
      values.add(v);
    }
    return values;
  }

  @Override
  public Map<String, String> deserializeObject(String serialized) throws ValidationException {
    Map<String, String> result = new HashMap<>();
    String[] values = serialized.split(separator);
    // Key value pairs -> odd length not allowed
    if (values.length % 2 != 0)
      throw //TODO throw validationException
    for (int i = 0; i < values.length; i += 2) {
      // empty key not allowed!
      if (values[i].length() == 0) {
        //TODO throw ValidationExcpetion
      } else {
        result.put(Utils.urlDecode(values[0], false), values[1]);
      }
    }
    return result;
  }

}
