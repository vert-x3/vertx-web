package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.BranchingHandlerImpl;

import java.util.function.Predicate;

/**
 * A handler that performs conditional branching based on a {@link RoutingContext} {@link Predicate}.
 */
@VertxGen
public interface BranchingHandler extends Handler<RoutingContext> {

  /**
   * Creates a branching handler that performs conditional branching.
   *
   * @param test the predicate to evaluate the {@link RoutingContext}
   * @param trueBranch the handler to execute if the predicate evaluates to {@code true}
   * @param falseBranch the handler to execute if the predicate evaluates to {@code false}
   * @return a new instance of {@link BranchingHandler} configured with the specified predicate and handlers
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static BranchingHandler create(Predicate<RoutingContext> test, Handler<RoutingContext> trueBranch, Handler<RoutingContext> falseBranch) {
    return new BranchingHandlerImpl(test, trueBranch, falseBranch);
  }

  /**
   * Creates a branching handler that performs conditional branching where the {@code false} branch is a call to
   * {@link RoutingContext#next()}.
   *
   * This is useful to define a route predicate, but let further {@link io.vertx.ext.web.Router} handlers take over the
   * handling duties.
   *
   * @param test the predicate to evaluate the {@link RoutingContext}
   * @param trueBranch the handler to execute if the predicate evaluates to {@code true}
   * @return a new instance of {@link BranchingHandler} configured with the specified predicate and handler
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static BranchingHandler create(Predicate<RoutingContext> test, Handler<RoutingContext> trueBranch) {
    return new BranchingHandlerImpl(test, trueBranch);
  }
}
