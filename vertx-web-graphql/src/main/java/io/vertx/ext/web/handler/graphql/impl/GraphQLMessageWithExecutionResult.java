package io.vertx.ext.web.handler.graphql.impl;

import graphql.ExecutionResult;

public class GraphQLMessageWithExecutionResult extends GraphQLMessage {

  private ExecutionResult payload;

  public ExecutionResult getPayload() {
    return payload;
  }

  public void setPayload(ExecutionResult payload) {
    this.payload = payload;
  }

}
