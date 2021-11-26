package io.vertx.ext.web.handler.crud.impl;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.CrudHandler;
import io.vertx.ext.web.handler.crud.*;
import io.vertx.ext.web.impl.RoutingContextInternal;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.vertx.core.http.HttpMethod.*;
import static io.vertx.ext.web.impl.RoutingContextInternal.BODY_HANDLER;
import static io.vertx.ext.web.impl.RoutingContextInternal.CORS_HANDLER;
import static java.lang.Integer.parseInt;

public class JsonCrudHandlerImpl<T> implements CrudHandler<T> {

  protected static final Logger LOG = LoggerFactory.getLogger(CrudHandler.class);

  // range pattern
  private static final Pattern rangePattern = Pattern.compile("items=(\\d+)-(\\d+)");
  private static final Pattern sortPattern = Pattern.compile("sort\\((.+)\\)");

  private final Class<T> pojoClass;

  private int maxAllowedLength = -1;

  private CreateHandler<T> create;
  private ReadHandler<T> read;
  private UpdateHandler<T> update;
  private DeleteHandler delete;
  private QueryHandler<T> query;
  private CountHandler count;
  private PatchHandler<T> patch;

  public JsonCrudHandlerImpl() {
    pojoClass = null;
  }

  public JsonCrudHandlerImpl(Class<T> clazz) {
    Objects.requireNonNull(clazz, "POJO type cannot be null");
    pojoClass = clazz;
  }

  @SuppressWarnings("unchecked")
  private T getBody(RoutingContext ctx) {
    if (pojoClass == null) {
      return (T) ctx.getBodyAsJson(maxAllowedLength);
    } else {
      Buffer body = ctx.getBody();
      if (body != null) {
        if (maxAllowedLength >= 0 && body.length() > maxAllowedLength) {
          throw new IllegalStateException("RoutingContext body size exceeds the allowed limit");
        }
        return Json.decodeValue(ctx.getBody(), pojoClass);
      } else {
        if (!((RoutingContextInternal) ctx).seenHandler(BODY_HANDLER)) {
          if (LOG.isWarnEnabled()) {
            LOG.warn("BodyHandler in not enabled on this route: RoutingContext.getBodyAsJson() in always be NULL");
          }
        }
      }
      return null;
    }
  }

  @Override
  public void handle(RoutingContext ctx) {
    String rest = ctx.pathParam("*");
    String id = null;
    if (rest != null) {
      int len = rest.length();
      if (len > 0) {
        boolean slash = false;
        int i = 0;
        for (; i < len; i++) {
          if (rest.charAt(i) == '/') {
            slash = true;
            break;
          }
        }

        if (slash) {
          if (i == len - 1) {
            // OK
            id = rest;
          } else {
            ctx.next();
            return;
          }
        } else {
          // no slash found, the rest is the id
          id = rest;
        }
      }
    }

    if (id != null) {
      // there is rest
      if (GET == ctx.request().method()) {
        readHandler(ctx, id);
        return;
      }
      if (PUT == ctx.request().method()) {
        updateHandler(ctx, id);
        return;
      }
      if (DELETE == ctx.request().method()) {
        deleteHandler(ctx, id);
        return;
      }
      if (PATCH == ctx.request().method()) {
        patchHandler(ctx, id);
        return;
      }
      if (OPTIONS == ctx.request().method()) {
        optionsHandler(ctx, id);
        return;
      }
    } else {
      if (GET == ctx.request().method()) {
        queryHandler(ctx);
        return;
      }
      if (POST == ctx.request().method()) {
        createHandler(ctx);
        return;
      }
      if (OPTIONS == ctx.request().method()) {
        optionsHandler(ctx);
        return;
      }
    }

    // invalid method, continue with the router
    ctx.next();
  }

  @Override
  public CrudHandler<T> maxAllowedLength(int length) {
    this.maxAllowedLength = length;
    return this;
  }

  @Override
  public CrudHandler<T> createHandler(CreateHandler<T> fn) {
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

    final T obj;

    try {
      obj = getBody(ctx);
    } catch (RuntimeException e) {
      ctx.fail(400, e);
      return;
    }

    create
      .handle(obj)
      .onFailure(ctx::fail)
      .onSuccess(id ->
        ctx
          .response()
          .putHeader(HttpHeaders.LOCATION, ctx.normalizedPath() + "/" + id)
          .setStatusCode(201)
          .end());
  }

  @Override
  public CrudHandler<T> readHandler(ReadHandler<T> fn) {
    this.read = fn;
    return this;
  }

  private void readHandler(RoutingContext ctx, String entityId) {
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

    read.handle(entityId)
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
  public CrudHandler<T> updateHandler(UpdateHandler<T> fn) {
    this.update = fn;
    return this;
  }

  private void updateHandler(RoutingContext ctx, String entityId) {
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

    final T obj;

    try {
      obj = getBody(ctx);
    } catch (RuntimeException e) {
      ctx.fail(400, e);
      return;
    }

    (update == null ?
      patch.handle(entityId, obj, null) :
      update.handle(entityId, obj))
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
  public CrudHandler<T> deleteHandler(DeleteHandler fn) {
    this.delete = fn;
    return this;
  }

  private void deleteHandler(RoutingContext ctx, String entityId) {
    // do we have a function?
    if (delete == null) {
      ctx.fail(405);
      return;
    }

    delete.handle(entityId)
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
  public CrudHandler<T> queryHandler(QueryHandler<T> fn) {
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

    this.query.handle(queryArgs)
      .onFailure(ctx::fail)
      .onSuccess(items -> {
        if (items == null) {
          ctx.fail(404);
        } else {
          if (count != null && validRange) {
            // need to send the content-range with totals
            count.handle(queryArgs)
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
  public CrudHandler<T> countHandler(CountHandler fn) {
    this.count = fn;
    return this;
  }

  @Override
  public CrudHandler<T> updateHandler(PatchHandler<T> fn) {
    this.patch = fn;
    return this;
  }

  private void patchHandler(RoutingContext ctx, String entityId) {
    if (read == null || (patch == null && update == null)) {
      ctx.fail(405);
    }

    if (!ctx.is("application/json")) {
      ctx.fail(415);
      return;
    }

    final T obj;

    try {
      obj = getBody(ctx);
    } catch (RuntimeException e) {
      ctx.fail(400, e);
      return;
    }

    read.handle(entityId)
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
            // there is no patch handler **but** we're dealing with plain JsonObject
            // we can merge the 2 objects as use the update function
            if (patch == null && pojoClass == null) {
              try {
                item = (T) ((JsonObject) item).mergeIn(((JsonObject) obj));
              } catch (RuntimeException e) {
                ctx.fail(e);
                return;
              }
            } else {
              ctx.fail(405);
              return;
            }

            // update back to the db
            (patch == null ?
              update.handle(entityId, item) :
              patch.handle(entityId, obj, item))
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

  private void optionsHandler(RoutingContext ctx, String id) {

    if (((RoutingContextInternal) ctx).seenHandler(CORS_HANDLER)) {
      ctx.next();
      return;
    }

    List<String> verbs = new ArrayList<>();

    verbs.add("OPTIONS");

    if (read != null) {
      verbs.add("GET");
    }
    if (update != null || patch != null) {
      verbs.add("PUT");
      verbs.add("PATCH");
    }
    if (delete != null) {
      verbs.add("DELETE");
    }

    ctx
      .response()
      .putHeader(HttpHeaders.ALLOW, String.join(", ", verbs))
      .setStatusCode(204)
      .end();
  }

  private void optionsHandler(RoutingContext ctx) {

    if (((RoutingContextInternal) ctx).seenHandler(CORS_HANDLER)) {
      ctx.next();
      return;
    }

    List<String> verbs = new ArrayList<>();

    verbs.add("OPTIONS");

    if (query != null) {
      verbs.add("GET");
    }
    if (create != null) {
      verbs.add("POST");
    }

    ctx
      .response()
      .putHeader(HttpHeaders.ALLOW, String.join(", ", verbs))
      .setStatusCode(204)
      .end();
  }
}
