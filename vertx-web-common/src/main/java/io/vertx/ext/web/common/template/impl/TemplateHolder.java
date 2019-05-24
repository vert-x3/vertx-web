package io.vertx.ext.web.common.template.impl;

import io.vertx.core.shareddata.Shareable;

public class TemplateHolder<T> implements Shareable {

  private final T template;
  private final String baseDir;

  public TemplateHolder(T template) {
    this(template, null);
  }

  public TemplateHolder(T template, String baseDir) {
    this.template = template;
    this.baseDir = baseDir;
  }

  public T template() {
    return template;
  }

  public String baseDir() {
    return baseDir;
  }
}
