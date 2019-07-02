package io.vertx.ext.web.handler.graphql.impl;

public class GraphQLMessageWithError extends GraphQLMessage {

  private Error payload;

  public Error getPayload() {
    return payload;
  }

  public void setPayload(Error payload) {
    this.payload = payload;
  }

  public static class Error {

    private String message;

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }

}
