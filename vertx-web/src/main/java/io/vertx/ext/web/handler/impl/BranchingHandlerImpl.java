package io.vertx.ext.web.handler.impl;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BranchingHandler;

import java.util.function.Predicate;

public class BranchingHandlerImpl implements BranchingHandler {

  private final Predicate<RoutingContext> test;
  private final Handler<RoutingContext> trueBranch;
  private final Handler<RoutingContext> falseBranch;

  public BranchingHandlerImpl(Predicate<RoutingContext> test, Handler<RoutingContext> trueBranch, Handler<RoutingContext> falseBranch) {
    this.test = test;
    this.trueBranch = trueBranch;
    this.falseBranch = falseBranch;
  }

  public BranchingHandlerImpl(Predicate<RoutingContext> test, Handler<RoutingContext> trueBranch) {
    this(test, trueBranch, RoutingContext::next);
  }

  @Override
  public void handle(RoutingContext event) {
    if (test.test(event)) {
      trueBranch.handle(event);
    } else {
      falseBranch.handle(event);
    }
  }
}
