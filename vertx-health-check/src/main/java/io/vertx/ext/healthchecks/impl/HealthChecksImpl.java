package io.vertx.ext.healthchecks.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;

import java.util.Objects;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class HealthChecksImpl implements HealthChecks {

  private final Vertx vertx;
  private CompositeProcedure root = new DefaultCompositeProcedure();

  public HealthChecksImpl(Vertx vertx) {
    this.vertx = Objects.requireNonNull(vertx);
  }

  @Override
  public HealthChecks register(String name, Handler<Future<Status>> procedure) {
    Objects.requireNonNull(name);
    if (name.isEmpty()) {
      throw new IllegalArgumentException("The name must not be empty");
    }
    Objects.requireNonNull(procedure);
    String[] segments = name.split("/");
    CompositeProcedure parent = traverseAndCreate(segments);
    String lastSegment = segments[segments.length - 1];
    parent.add(lastSegment,
      new DefaultProcedure(vertx, lastSegment, 1000, procedure));
    return this;
  }

  private CompositeProcedure traverseAndCreate(String[] segments) {
    int i;
    CompositeProcedure parent = root;
    for (i = 0; i < segments.length - 1; i++) {
      Procedure c = parent.get(segments[i]);
      if (c == null) {
        DefaultCompositeProcedure composite = new DefaultCompositeProcedure();
        parent.add(segments[i], composite);
        parent = composite;
      } else if (c instanceof CompositeProcedure) {
        parent = (CompositeProcedure) c;
      } else {
        // Illegal.
        throw new IllegalArgumentException("Unable to find the procedure `" + segments[i] + "`, `"
          + segments[i] + "` is not a composite.");
      }
    }

    return parent;
  }

  @Override
  public HealthChecks unregister(String name) {
    Objects.requireNonNull(name);
    if (name.isEmpty()) {
      throw new IllegalArgumentException("The name must not be empty");
    }

    String[] segments = name.split("/");
    CompositeProcedure parent = findLastParent(segments);
    if (parent != null) {
      String lastSegment = segments[segments.length - 1];
      parent.remove(lastSegment);
    }
    return this;
  }


  @Override
  public HealthChecks invoke(Handler<JsonObject> resultHandler) {
    compute(root, resultHandler);
    return this;
  }

  @Override
  public HealthChecks invoke(String name, Handler<AsyncResult<JsonObject>> resultHandler) {
    if (name == null || name.isEmpty() || name.equals("/")) {
      return invoke(json -> resultHandler.handle(Future.succeededFuture(json)));
    } else {
      String[] segments = name.split("/");
      Procedure check = root;
      for (String segment : segments) {
        if (segment.trim().isEmpty()) {
          continue;
        }
        if (check instanceof CompositeProcedure) {
          check = ((CompositeProcedure) check).get(segment);
          if (check == null) {
            // Not found
            resultHandler.handle(Future.failedFuture("Not found"));
            return this;
          }
          // Else continue...
        } else {
          // Not a composite
          resultHandler.handle(Future.failedFuture("'" + segment + "' is not a composite"));
          return this;
        }
      }

      if (check == null) {
        resultHandler.handle(null);
        return this;
      }
      compute(check, json -> resultHandler.handle(Future.succeededFuture(json)));
    }
    return this;
  }


  private CompositeProcedure findLastParent(String[] segments) {
    int i;
    CompositeProcedure parent = root;
    for (i = 0; i < segments.length - 1; i++) {
      Procedure c = parent.get(segments[i]);
      if (c instanceof CompositeProcedure) {
        parent = (CompositeProcedure) c;
      } else {
        return null;
      }
    }
    return parent;
  }

  private void compute(Procedure procedure, Handler<JsonObject> resultHandler) {
    procedure.check(resultHandler);
  }


}
