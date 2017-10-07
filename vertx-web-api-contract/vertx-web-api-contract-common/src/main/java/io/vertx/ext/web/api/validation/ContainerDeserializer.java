package io.vertx.ext.web.api.validation;

import io.vertx.codegen.annotations.VertxGen;

import java.util.List;
import java.util.Map;

/**
 * Interface that define methods for deserialization of array and objects
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface ContainerDeserializer {

  List<String> deserializeArray(String serialized) throws ValidationException;

  Map<String, String> deserializeObject(String serialized) throws ValidationException;

}
