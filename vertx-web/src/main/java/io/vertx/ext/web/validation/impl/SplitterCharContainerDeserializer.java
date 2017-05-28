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
public class SplitterCharContainerDeserializer implements ContainerDeserializer {

  private String separator;

  public SplitterCharContainerDeserializer(String separator) {
    this.separator = separator;
  }

  @Override
  public List<String> deserializeArray(String serialized) throws ValidationException {
    List<String> values = new ArrayList<>();
    for (String v : serialized.split(separator, -1)) {
      values.add(v);
    }
    return values;
  }

  @Override
  public Map<String, String> deserializeObject(String serialized) throws ValidationException {
    Map<String, String> result = new HashMap<>();
    String[] values = serialized.split(separator, -1);
    // Key value pairs -> odd length not allowed
    if (values.length % 2 != 0)
      throw ValidationException.generateDeserializationError("DeserializationError: Key value pair Object must have odd fields");
    for (int i = 0; i < values.length; i += 2) {
      // empty key not allowed!
      if (values[i].length() == 0) {
        throw ValidationException.generateDeserializationError("DeserializationError: Empty key not allowed");
      } else {
        result.put(values[i], values[i + 1]);
      }
    }
    return result;
  }

}
