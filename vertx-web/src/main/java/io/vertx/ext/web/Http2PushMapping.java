package io.vertx.ext.web;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class Http2PushMapping {
  private String filePath;
  private String extensionTarget;
  private boolean noPush;

  /**
   * Copy constructor
   *
   * @param other the http2PushMapping to copy
   */
  public Http2PushMapping(Http2PushMapping other) {
    this.filePath = other.filePath;
    this.extensionTarget = other.extensionTarget;
    this.noPush = other.noPush;
  }

  /**
   * Default constructor
   */
  public Http2PushMapping() {
  }

  /**
   * Constructor from JSON
   *
   * @param json the JSON
   */
  public Http2PushMapping(JsonObject json) {
    this.filePath = json.getString("filePath");
    this.extensionTarget = json.getString("extensionTarget");
    this.noPush = json.getBoolean("noPush");
  }

  /**
   * Constructor with params for Link preload
   *
   * @param filePath the path of file to preload
   * @param extensionTarget the link header extension
   * @param noPush describes if the file should be pushed
   */
  public Http2PushMapping(String filePath, String extensionTarget, boolean noPush) {
    this.filePath = filePath;
    this.extensionTarget = extensionTarget;
    this.noPush = noPush;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getExtensionTarget() {
    return extensionTarget;
  }

  public void setExtensionTarget(String extensionTarget) {
    this.extensionTarget = extensionTarget;
  }

  public boolean isNoPush() {
    return noPush;
  }

  public void setNoPush(boolean noPush) {
    this.noPush = noPush;
  }
}
