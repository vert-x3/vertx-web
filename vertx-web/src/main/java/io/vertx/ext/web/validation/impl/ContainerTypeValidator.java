package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.RequestParameter;
import io.vertx.ext.web.validation.ContainerDeserializer;
import io.vertx.ext.web.validation.ParameterTypeValidator;
import io.vertx.ext.web.validation.ValidationException;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public abstract class ContainerTypeValidator<DeserializationResult> implements ParameterTypeValidator {

  private ContainerDeserializer containerDeserializer;
  private boolean exploded; //TODO remove, it's useless

  public ContainerTypeValidator(ContainerDeserializer collectionFormat, boolean exploded) {
    this.containerDeserializer = collectionFormat;
    this.exploded = exploded;
  }

  public boolean isExploded() {
    return exploded;
  }

  protected ContainerDeserializer getContainerDeserializer() {
    return this.containerDeserializer;
  }

  protected abstract DeserializationResult deserialize(String serialized) throws ValidationException;

  protected abstract RequestParameter validate(DeserializationResult values) throws ValidationException;

}
