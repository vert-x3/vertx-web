package io.vertx.ext.web.impl;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.*;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.vertx.core.http.HttpMethod.*;
import static java.lang.Integer.parseInt;

public class CrudImpl implements Crud {

  private CrudFunction<JsonObject, String> create;
  private CrudFunction<String, JsonObject> read;
  private CrudBiFunction<JsonObject, Long> update;
  private CrudFunction<String, Long> delete;
  private CrudFunction<CrudQuery, JsonArray> query;
  private CrudFunction<CrudQuery, Long> count;

  public CrudImpl(Route collectionRoute, Route entityRoute) {
    // range pattern
    final Pattern rangePattern = Pattern.compile("items=(\\d+)-(\\d+)");
    final Pattern sortPattern = Pattern.compile("sort\\((.+)\\)");

    collectionRoute
      .handler(ctx -> {
        if (GET == ctx.request().method()) {
          if (query == null) {
            ctx.fail(405);
          } else {
            // parse ranges
            final String range = ctx.request().getHeader("range");
            final Integer start, end;
            if (range != null) {
              Matcher m = rangePattern.matcher(range);
              if (m.matches()) {
                start = parseInt(m.group(1));
                end = parseInt(m.group(2));
              } else {
                start = null;
                end = null;
              }
            } else {
              start = null;
              end = null;
            }

            // parse query
            final JsonObject dbquery = new JsonObject();
            final JsonObject dbsort = new JsonObject();
            for (Map.Entry<String, String> entry : ctx.request().params()) {
              String[] sortArgs;
              // parse sort
              Matcher sort = sortPattern.matcher(entry.getKey());

              if (sort.matches()) {
                sortArgs = sort.group(1).split(",");
                for (String arg : sortArgs) {
                  if (arg.charAt(0) == '+' || arg.charAt(0) == ' ') {
                    dbsort.put(arg.substring(1), 1);
                  } else if (arg.charAt(0) == '-') {
                    dbsort.put(arg.substring(1), -1);
                  }
                }
                continue;
              }

              dbquery.put(entry.getKey(), entry.getValue());
            }

            // perform the query
            final CrudQuery queryArgs = new CrudQuery().setQuery(dbquery).setStart(start).setEnd(end).setSort(dbsort);

            query.apply(queryArgs)
              .onFailure(ctx::fail)
              .onSuccess(items -> {
                if (items == null) {
                  ctx.fail(404);
                } else {
                  if (count != null && range != null) {
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
          return;
        }
        if (POST == ctx.request().method()) {
          if (create == null) {
            ctx.fail(405);
          } else {
            create
              .apply(ctx.getBodyAsJson())
              .onFailure(ctx::fail)
              .onSuccess(id -> {
                ctx
                  .response()
                  .putHeader(HttpHeaders.LOCATION, ctx.normalizedPath() + "/" + id)
                  .setStatusCode(201)
                  .end();
              });
          }
          return;
        }
        // invalid method
        ctx.fail(415);
      });

    entityRoute
      .handler(ctx -> {
        if (GET == ctx.request().method()) {
          if (read == null) {
            ctx.fail(405);
          } else {
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
          return;
        }
        if (PUT == ctx.request().method()) {
          if (update == null) {
            ctx.fail(405);
          } else {
            update.apply(ctx.pathParam("entityId"), ctx.getBodyAsJson())
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
          return;
        }
        if (DELETE == ctx.request().method()) {
          if (delete == null) {
            ctx.fail(405);
          } else {
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
          return;
        }
        if (PATCH == ctx.request().method()) {
          if (read == null || update == null) {
            ctx.fail(405);
          } else {
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
                    item.mergeIn(ctx.getBodyAsJson());

                    // update back to the db
                    update.apply(id, item)
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
          return;
        }
        // invalid method
        ctx.fail(415);
      });
  }

  @Override
  public Crud create(CrudFunction<JsonObject, String> fn) {
    this.create = fn;
    return this;
  }

  @Override
  public Crud read(CrudFunction<String, JsonObject> fn) {
    this.read = fn;
    return this;

  }

  @Override
  public Crud update(CrudBiFunction<JsonObject, Long> fn) {
    this.update = fn;
    return this;
  }

  @Override
  public Crud delete(CrudFunction<String, Long> fn) {
    this.delete = fn;
    return this;
  }

  @Override
  public Crud query(CrudFunction<CrudQuery, JsonArray> fn) {
    this.query = fn;
    return this;
  }

  @Override
  public Crud count(CrudFunction<CrudQuery, Long> fn) {
    this.count = fn;
    return this;
  }
}
