package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.ContainerSerializationStyle;
import io.vertx.ext.web.validation.ParameterTypeValidator;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public abstract class ContainerTypeValidator<DeserializationResult> implements ParameterTypeValidator {

  private ContainerSerializationStyle collectionFormat;
  private boolean exploded;

  public ContainerTypeValidator(ContainerSerializationStyle collectionFormat, boolean exploded) {
    this.collectionFormat = collectionFormat;
    this.exploded = exploded;
  }

  public ContainerSerializationStyle getCollectionFormat() {
    return collectionFormat;
  }

  public boolean isExploded() {
    return exploded;
  }

  protected abstract DeserializationResult deserialize(String serialized);
}
