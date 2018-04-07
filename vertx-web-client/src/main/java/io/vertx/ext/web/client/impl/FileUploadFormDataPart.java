package io.vertx.ext.web.client.impl;

import io.vertx.ext.web.client.FormDataPart;

public class FileUploadFormDataPart implements FormDataPart {
  private final String name;
  private final String filename;
  private final String mediaType;
  private final String pathname;
  private final boolean isText;

  public FileUploadFormDataPart(String name, String filename, String pathname, String mediaType, boolean isText) {
    this.name = name;
    this.filename = filename;
    this.pathname = pathname;
    this.mediaType = mediaType;
    this.isText = isText;
  }

  public String getName() {
    return name;
  }

  public String getFilename() {
    return filename;
  }

  public String getPathname() {
    return pathname;
  }

  public String getMediaType() {
    return mediaType;
  }

  public boolean isText() {
    return isText;
  }
}
