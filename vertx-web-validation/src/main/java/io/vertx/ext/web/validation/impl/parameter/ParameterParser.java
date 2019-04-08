package io.vertx.ext.web.validation.impl.parameter;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.ext.web.validation.MalformedValueException;

import java.util.List;
import java.util.Map;

/**
 * This class extracts from parameter map the parameter and converts it to a json representation
 */
public interface ParameterParser extends Comparable<ParameterParser> {

  @Nullable Object parseParameter(Map<String, List<String>> parameterValue) throws MalformedValueException;

}
