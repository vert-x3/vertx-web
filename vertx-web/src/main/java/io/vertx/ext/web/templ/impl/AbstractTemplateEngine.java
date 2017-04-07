package io.vertx.ext.web.templ.impl;

import io.vertx.ext.web.templ.TemplateEngine;

public abstract class AbstractTemplateEngine implements TemplateEngine {
  private String contentType = DEFAULT_CONTENT_TYPE;

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
}
