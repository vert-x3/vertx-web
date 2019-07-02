package io.vertx.ext.web.handler.graphql.impl;

import com.fasterxml.jackson.annotation.JsonValue;

public class GraphQLMessage {

  private String id;

  private Type type;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public enum Type {
    CONNECTION_INIT("connection_init"),
    CONNECTION_TERMINATE("connection_terminate"),
    START("start"),
    STOP("stop"),
    CONNECTION_ACK("connection_ack"),
    CONNECTION_ERROR("error"),
    CONNECTION_KEEP_ALIVE("ka"),
    DATA("data"),
    ERROR("error"),
    COMPLETE("complete");

    private String text;

    Type(String text) {
      this.text = text;
    }

    @JsonValue
    public String getText() {
      return text;
    }

  }

}
