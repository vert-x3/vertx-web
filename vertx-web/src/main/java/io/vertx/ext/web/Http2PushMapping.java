package io.vertx.ext.web;

public class Http2PushMapping {
  private String filePath;
  private String extensionTarget;
  private boolean noPush;

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
