package io.vertx.ext.web.handler.graphql.impl;

public class GraphQLMessageWithPayload extends GraphQLMessage {

  private GraphQLQuery payload;

  public GraphQLQuery getPayload() {
    return payload;
  }

  public void setPayload(GraphQLQuery payload) {
    this.payload = payload;
  }

}
