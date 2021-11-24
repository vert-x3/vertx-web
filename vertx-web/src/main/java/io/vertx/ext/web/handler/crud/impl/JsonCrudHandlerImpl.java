package io.vertx.ext.web.handler.crud.impl;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.crud.*;
import io.vertx.ext.web.handler.JsonCrudHandler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.vertx.core.http.HttpMethod.*;
import static java.lang.Integer.parseInt;

public class JsonCrudHandlerImpl implements JsonCrudHandler {

  protected static final Logger LOG = LoggerFactory.getLogger(JsonCrudHandler.class);

  // range pattern
  private static final Pattern rangePattern = Pattern.compile("items=(\\d+)-(\\d+)");
  private static final Pattern sortPattern = Pattern.compile("sort\\((.+)\\)");

  private int maxAllowedLength = -1;

  private CreateFunction create;
  private ReadFunction read;
  private UpdateFunction update;
  private DeleteFunction delete;
  private QueryFunction query;
  private CountFunction count;
  private PatchFunction patch;

  public JsonCrudHandlerImpl(Route collectionRoute, Route entityRoute) {

    collectionRoute
      .handler(ctx -> {
        if (GET == ctx.request().method()) {
          queryHandler(ctx);
          return;
        }

        if (POST == ctx.request().method()) {
          createHandler(ctx);
          return;
        }

        // invalid method
        ctx.fail(405);
      });

    entityRoute
      .handler(ctx -> {
        if (GET == ctx.request().method()) {
          readHandler(ctx);
          return;
        }
        if (PUT == ctx.request().method()) {
          updateHandler(ctx);
          return;
        }
        if (DELETE == ctx.request().method()) {
          deleteHandler(ctx);
          return;
        }
        if (PATCH == ctx.request().method()) {
          patchHandler(ctx);
          return;
        }
        // invalid method
        ctx.fail(405);
      });
  }

  @Override
  public JsonCrudHandler maxAllowedLength(int length) {
    this.maxAllowedLength = length;
    return this;
  }

  @Override
  public JsonCrudHandler create(CreateFunction fn) {
    this.create = fn;
    return this;
  }

  private void createHandler(RoutingContext ctx) {
    // do we have a function?
    if (create == null) {
      ctx.fail(405);
      return;
    }

    // did the client sent json?
    if (!ctx.is("application/json")) {
      ctx.fail(415);
      return;
    }

    final JsonObject obj;

    try {
      obj = ctx.getBodyAsJson(maxAllowedLength);
    } catch (RuntimeException e) {
      ctx.fail(400, e);
      return;
    }

    create
      .apply(obj)
      .onFailure(ctx::fail)
      .onSuccess(id ->
        ctx
          .response()
          .putHeader(HttpHeaders.LOCATION, ctx.normalizedPath() + "/" + id)
          .setStatusCode(201)
          .end());
  }

  @Override
  public JsonCrudHandler read(ReadFunction fn) {
    this.read = fn;
    return this;
  }

  private void readHandler(RoutingContext ctx) {
    // do we have a function?
    if (read == null) {
      ctx.fail(405);
      return;
    }

    // does the client accept json?
    if (!ctx.accepts("application/json")) {
      ctx.fail(406);
      return;
    }

    read.apply(ctx.pathParam("entityId"))
      .onFailure(ctx::fail)
      .onSuccess(item -> {
        if (item == null) {
          ctx.fail(404);
        } else {
          ctx.json(item);
        }
      });
  }

  @Override
  public JsonCrudHandler update(UpdateFunction fn) {
    this.update = fn;
    return this;
  }

  private void updateHandler(RoutingContext ctx) {
    // do we have a function?
    if (update == null && patch == null) {
      ctx.fail(405);
      return;
    }

    // did the client sent json?
    if (!ctx.is("application/json")) {
      ctx.fail(415);
      return;
    }

    final JsonObject obj;

    try {
      obj = ctx.getBodyAsJson(maxAllowedLength);
    } catch (RuntimeException e) {
      ctx.fail(400, e);
      return;
    }

    (update == null ?
      patch.apply(ctx.pathParam("entityId"), obj, null) :
      update.apply(ctx.pathParam("entityId"), obj))
      .onFailure(ctx::fail)
      .onSuccess(affectedRows -> {
        // missing or no rows affected
        if (affectedRows == null || 0L == affectedRows) {
          ctx.fail(404);
        } else {
          ctx.response()
            .setStatusCode(204)
            .end();
        }
      });
  }

  @Override
  public JsonCrudHandler delete(DeleteFunction fn) {
    this.delete = fn;
    return this;
  }

  private void deleteHandler(RoutingContext ctx) {
    // do we have a function?
    if (delete == null) {
      ctx.fail(405);
      return;
    }

    delete.apply(ctx.pathParam("entityId"))
      .onFailure(ctx::fail)
      .onSuccess(affectedRows -> {
        // missing or no rows affected
        if (affectedRows == null || 0L == affectedRows) {
          ctx.fail(404);
        } else {
          ctx.response()
            .setStatusCode(204)
            .end();
        }
      });
  }

  @Override
  public JsonCrudHandler query(QueryFunction fn) {
    this.query = fn;
    return this;
  }

  private void queryHandler(RoutingContext ctx) {
    // do we have a function?
    if (query == null) {
      ctx.fail(405);
      return;
    }

    // does the client accept json?
    if (!ctx.accepts("application/json")) {
      ctx.fail(406);
      return;
    }

    // parse ranges
    final String range = ctx.request().getHeader("range");
    final Integer start, end;
    final boolean validRange;
    if (range != null) {
      Matcher m = rangePattern.matcher(range);
      if (m.matches()) {
        start = parseInt(m.group(1));
        end = parseInt(m.group(2));
        validRange = true;
      } else {
        start = null;
        end = null;
        validRange = false;
      }
    } else {
      start = null;
      end = null;
      validRange = false;
    }

    // parse query
    final JsonObject query = new JsonObject();
    final JsonObject sort = new JsonObject();
    for (Map.Entry<String, String> entry : ctx.request().params()) {
      String[] sortArgs;
      // parse sort
      Matcher matcher = sortPattern.matcher(entry.getKey());

      if (matcher.matches()) {
        sortArgs = matcher.group(1).split(",");
        for (String arg : sortArgs) {
          if (arg.charAt(0) == '+' || arg.charAt(0) == ' ') {
            sort.put(arg.substring(1), 1);
          } else if (arg.charAt(0) == '-') {
            sort.put(arg.substring(1), -1);
          }
        }
        continue;
      }

      query.put(entry.getKey(), entry.getValue());
    }

    // perform the query
    final CrudQuery queryArgs = new CrudQuery().setQuery(query).setStart(start).setEnd(end).setSort(sort);

    this.query.apply(queryArgs)
      .onFailure(ctx::fail)
      .onSuccess(items -> {
        if (items == null) {
          ctx.fail(404);
        } else {
          if (count != null && validRange) {
            // need to send the content-range with totals
            count.apply(queryArgs)
              .onFailure(ctx::fail)
              .onSuccess(count -> {
                ctx.response()
                  .putHeader(HttpHeaders.CONTENT_RANGE, "items " + start + "-" + end + "/" + count);

                ctx.json(items);
              });

            return;
          }

          ctx.json(items);
        }
      });
  }

  @Override
  public JsonCrudHandler count(CountFunction fn) {
    this.count = fn;
    return this;
  }

  @Override
  public JsonCrudHandler update(PatchFunction fn) {
    this.patch = fn;
    return this;
  }

  private void patchHandler(RoutingContext ctx) {
    if (read == null || (patch == null && update == null)) {
      ctx.fail(405);
    }

    if (!ctx.is("application/json")) {
      ctx.fail(415);
      return;
    }

    final JsonObject obj;

    try {
      obj = ctx.getBodyAsJson(maxAllowedLength);
    } catch (RuntimeException e) {
      ctx.fail(400, e);
      return;
    }

    // get the real id from the params multimap
    final String id = ctx.pathParam("entityId");

    read.apply(id)
      .onFailure(ctx::fail)
      .onSuccess(item -> {
        final String ifMatch = ctx.request().getHeader("If-Match");
        final String ifNoneMatch = ctx.request().getHeader("If-None-Match");

        // merge existing json with incoming one
        final boolean overwrite =
          // pure PUT, must exist and will be updated
          (ifMatch == null && ifNoneMatch == null) ||
            // must exist and will be updated
            ("*".equals(ifMatch));

        if (item == null) {
          // does not exist but was marked as overwrite
          if (overwrite) {
            // does not exist, returns 412
            ctx.fail(412);
          } else {
            // does not exist, returns 404
            ctx.fail(404);
          }
        } else {
          // does exist but was marked as not overwrite
          if (!overwrite) {
            // does exist, returns 412
            ctx.fail(412);
          } else {
            // update back to the db
            (patch == null ?
              update.apply(ctx.pathParam("entityId"), item.mergeIn(obj)) :
              patch.apply(ctx.pathParam("entityId"), obj, item))
              .onFailure(ctx::fail)
              .onSuccess(affectedItems -> {
                if (affectedItems == null || affectedItems == 0) {
                  // nothing was updated
                  ctx.fail(404);
                } else {
                  ctx.response()
                    .setStatusCode(204)
                    .end();
                }
              });
          }
        }
      });
  }
}
