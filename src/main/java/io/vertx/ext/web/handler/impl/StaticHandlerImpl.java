/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.handler.impl;

import io.vertx.core.*;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.FileSystemException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.impl.LRUCache;
import io.vertx.ext.web.impl.Utils;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

/**
 * Static web server
 * Parts derived from Yoke
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public class StaticHandlerImpl implements StaticHandler {

  private static final Logger log = LoggerFactory.getLogger(StaticHandlerImpl.class);

  private final DateFormat dateTimeFormatter = Utils.createISODateTimeFormatter();
  private Map<String, CacheEntry> propsCache;
  private String webRoot = DEFAULT_WEB_ROOT;
  private long maxAgeSeconds = DEFAULT_MAX_AGE_SECONDS; // One day
  private boolean directoryListing = DEFAULT_DIRECTORY_LISTING;
  private String directoryTemplateResource = DEFAULT_DIRECTORY_TEMPLATE;
  private String directoryTemplate;
  private boolean includeHidden = DEFAULT_INCLUDE_HIDDEN;
  private boolean filesReadOnly = DEFAULT_FILES_READ_ONLY;
  private boolean cachingEnabled = DEFAULT_CACHING_ENABLED;
  private long cacheEntryTimeout = DEFAULT_CACHE_ENTRY_TIMEOUT;
  private String indexPage = DEFAULT_INDEX_PAGE;
  private int maxCacheSize = DEFAULT_MAX_CACHE_SIZE;

  // These members are all related to auto tuning of synchronous vs asynchronous file system access
  private static int NUM_SERVES_TUNING_FS_ACCESS = 1000;
  private boolean alwaysAsyncFS = DEFAULT_ALWAYS_ASYNC_FS;
  private long maxAvgServeTimeNanoSeconds = DEFAULT_MAX_AVG_SERVE_TIME_NS;
  private boolean tuning = DEFAULT_ENABLE_FS_TUNING;
  private long totalTime;
  private long numServesBlocking;
  private boolean useAsyncFS;
  private long nextAvgCheck = NUM_SERVES_TUNING_FS_ACCESS;

  public StaticHandlerImpl(String root) {
    setRoot(root);
  }

  public StaticHandlerImpl() {
  }

  private String directoryTemplate(Vertx vertx) {
    if (directoryTemplate == null) {
      directoryTemplate = Utils.readFileToString(vertx, directoryTemplateResource);
    }
    return directoryTemplate;
  }

  /**
   * Create all required header so content can be cache by Caching servers or Browsers
   *
   * @param request
   * @param props
   */
  private void writeCacheHeaders(HttpServerRequest request, FileProps props) {

    MultiMap headers = request.response().headers();

    if (cachingEnabled) {
      // We use cache-control and last-modified
      // We *do not use* etags and expires (since they do the same thing - redundant)
      headers.set("cache-control", "public, max-age=" + maxAgeSeconds);
      headers.set("last-modified", dateTimeFormatter.format(props.lastModifiedTime()));
    }

    // date header is mandatory
    headers.set("date", dateTimeFormatter.format(new Date()));
  }

  @Override
  public void handle(RoutingContext context) {
    HttpServerRequest request = context.request();
    if (request.method() != HttpMethod.GET && request.method() != HttpMethod.HEAD) {
      if (log.isTraceEnabled()) log.trace("Not GET or HEAD so ignoring request");
      context.next();
    } else {
      String path = context.normalisedPath();
      // if the normalized path is null it cannot be resolved
      if (path == null) {
        log.warn("Invalid path: " + context.request().path() + " so returning 404");
        context.fail(404);
        return;
      }

      if (!directoryListing && "/".equals(path)) {
        path = indexPage;
      }

      String file = null;

      if (!includeHidden) {
        file = getFile(path, context);
        int idx = file.lastIndexOf('/');
        String name = file.substring(idx + 1);
        if (name.length() > 0 && name.charAt(0) == '.') {
          context.fail(404);
          return;
        }
      }

      // Look in cache
      CacheEntry entry = null;
      if (cachingEnabled) {
        entry = propsCache().get(path);
        if (entry != null) {
          if ((filesReadOnly || !entry.isOutOfDate()) && entry.shouldUseCached(request)) {
            context.response().setStatusCode(304).end();
            return;
          }
        }
      }

      if (file == null) {
        file = getFile(path, context);
      }

      FileProps props;
      if (filesReadOnly && entry != null) {
        props = entry.props;
        sendFile(context, file, props);
      } else {
        // Need to read the props from the filesystem
        String spath = path;
        String sfile = file;
        getFileProps(context, file, res -> {
          if (res.succeeded()) {
            FileProps fprops = res.result();
            if (fprops == null) {
              // File does not exist
              context.fail(404);
            } else if (fprops.isDirectory()) {
              if (directoryListing) {
                sendDirectory(sfile, context);
              } else {
                // Directory listing denied
                context.fail(403);
              }
            } else {
              propsCache().put(spath, new CacheEntry(fprops, System.currentTimeMillis()));
              sendFile(context, sfile, fprops);
            }
          } else {
            if (res.cause() instanceof NoSuchFileException) {
              context.fail(404);
            } else {
              context.fail(res.cause());
            }
          }
        });

      }

    }
  }

  private synchronized void getFileProps(RoutingContext context, String file, Handler<AsyncResult<FileProps>> resultHandler) {
    FileSystem fs = context.vertx().fileSystem();
    if (alwaysAsyncFS  || useAsyncFS) {
      fs.props(file, resultHandler);
    } else {
      // Use synchronous access - it might well be faster!
      long start = 0;
      if (tuning) {
        start = System.nanoTime();
      }
      try {
        FileProps props = fs.propsBlocking(file);
        if (tuning) {
          long end = System.nanoTime();
          long dur = end - start;
          totalTime += dur;
          numServesBlocking++;
          if (numServesBlocking == Long.MAX_VALUE) {
            // Unlikely.. but...
            resetTuning();
          } else if (numServesBlocking == nextAvgCheck) {
            double avg = (double) totalTime / numServesBlocking;
            if (avg > maxAvgServeTimeNanoSeconds) {
              useAsyncFS = true;
              log.info("Switching to async file system access in static file server as fs access is slow! (Average access time of " + avg + " ns)");
              tuning = false;
            }
            nextAvgCheck += NUM_SERVES_TUNING_FS_ACCESS;
          }
        }
        resultHandler.handle(Future.succeededFuture(props));
      } catch (FileSystemException e) {
        resultHandler.handle(Future.failedFuture(e.getCause()));
      }
    }
  }

  private void resetTuning() {
    // Reset
    nextAvgCheck = NUM_SERVES_TUNING_FS_ACCESS;
    totalTime = 0;
    numServesBlocking = 0;
  }

  private void sendFile(RoutingContext context, String file, FileProps fileProps) {
    HttpServerRequest request = context.request();
    writeCacheHeaders(request, fileProps);

    if (request.method() == HttpMethod.HEAD) {
      request.response().end();
    } else {
      request.response().sendFile(file, res2 -> {
        if (res2.failed()) {
          context.fail(res2.cause());
        }
      });
    }
  }

  @Override
  public StaticHandler setWebRoot(String webRoot) {
    setRoot(webRoot);
    return this;
  }

  @Override
  public StaticHandler setFilesReadOnly(boolean readOnly) {
    this.filesReadOnly = readOnly;
    return this;
  }

  @Override
  public StaticHandler setMaxAgeSeconds(long maxAgeSeconds) {
    if (maxAgeSeconds < 0) {
      throw new IllegalArgumentException("timeout must be >= 0");
    }
    this.maxAgeSeconds = maxAgeSeconds;
    return this;
  }

  @Override
  public StaticHandler setMaxCacheSize(int maxCacheSize) {
    if (maxCacheSize < 1) {
      throw new IllegalArgumentException("maxCacheSize must be >= 1");
    }
    this.maxCacheSize = maxCacheSize;
    return this;
  }

  @Override
  public StaticHandler setCachingEnabled(boolean enabled) {
    this.cachingEnabled = enabled;
    return this;
  }

  @Override
  public StaticHandler setDirectoryListing(boolean directoryListing) {
    this.directoryListing = directoryListing;
    return this;
  }

  @Override
  public StaticHandler setDirectoryTemplate(String directoryTemplate) {
    this.directoryTemplateResource = directoryTemplate;
    this.directoryTemplate = null;
    return this;
  }

  @Override
  public StaticHandler setIncludeHidden(boolean includeHidden) {
    this.includeHidden = includeHidden;
    return this;
  }

  @Override
  public StaticHandler setCacheEntryTimeout(long timeout) {
    if (timeout < 1) {
      throw new IllegalArgumentException("timeout must be >= 1");
    }
    this.cacheEntryTimeout = timeout;
    return this;
  }

  @Override
  public StaticHandler setIndexPage(String indexPage) {
    Objects.requireNonNull(indexPage);
    if (!indexPage.startsWith("/")) {
      indexPage = "/" + indexPage;
    }
    this.indexPage = indexPage;
    return this;
  }

  @Override
  public StaticHandler setAlwaysAsyncFS(boolean alwaysAsyncFS) {
    this.alwaysAsyncFS = alwaysAsyncFS;
    return this;
  }

  @Override
  public synchronized StaticHandler setEnableFSTuning(boolean enableFSTuning) {
    this.tuning = enableFSTuning;
    if (!tuning) {
      resetTuning();
    }
    return this;
  }

  @Override
  public StaticHandler setMaxAvgServeTimeNs(long maxAvgServeTimeNanoSeconds) {
    this.maxAvgServeTimeNanoSeconds = maxAvgServeTimeNanoSeconds;
    return this;
  }

  private Map<String, CacheEntry> propsCache() {
    if (propsCache == null) {
      propsCache = new LRUCache<>(maxCacheSize);
    }
    return propsCache;
  }

  private Date parseDate(String header) {
    try {
      return dateTimeFormatter.parse(header);
    } catch (ParseException e) {
      throw new VertxException(e);
    }
  }

  private String getFile(String path, RoutingContext context) {
    String file = webRoot + Utils.pathOffset(path, context);
    if (log.isTraceEnabled()) log.trace("File to serve is " + file);
    return file;
  }

  private void setRoot(String webRoot) {
    Objects.requireNonNull(webRoot);
    if (webRoot.startsWith("/")) {
      throw new IllegalArgumentException("root cannot start with '/'");
    }
    this.webRoot = webRoot;
  }

  private void sendDirectory(String dir, RoutingContext context) {
    FileSystem fileSystem = context.vertx().fileSystem();
    HttpServerRequest request = context.request();

    fileSystem.readDir(dir, asyncResult -> {
      if (asyncResult.failed()) {
        context.fail(asyncResult.cause());
      } else {

        String accept = request.headers().get("accept");
        if (accept == null) {
          accept = "text/plain";
        }

        if (accept.contains("html")) {
          String normalizedDir = dir.substring(webRoot.length());
          if (!normalizedDir.endsWith("/")) {
            normalizedDir += "/";
          }

          String file;
          StringBuilder files = new StringBuilder("<ul id=\"files\">");

          List<String> list = asyncResult.result();
          Collections.sort(list);

          for (String s : list) {
            file = s.substring(s.lastIndexOf(File.separatorChar) + 1);
            // skip dot files
            if (!includeHidden && file.charAt(0) == '.') {
              continue;
            }
            files.append("<li><a href=\"");
            files.append(normalizedDir);
            files.append(file);
            files.append("\" title=\"");
            files.append(file);
            files.append("\">");
            files.append(file);
            files.append("</a></li>");
          }

          files.append("</ul>");

          // link to parent dir
          int slashPos = 0;
          for (int i = dir.length() - 2; i > 0; i--) {
            if (dir.charAt(i) == '/') {
              slashPos = i;
              break;
            }
          }

          String parent = "<a href=\"/" + dir.substring(0, slashPos + 1) + "\">..</a>";

          request.response().putHeader("content-type", "text/html");
          request.response().end(
            directoryTemplate(context.vertx()).replace("{directory}", normalizedDir)
              .replace("{parent}", parent)
              .replace("{files}", files.toString()));
        } else if (accept.contains("json")) {
          String file;
          JsonArray json = new JsonArray();

          for (String s : asyncResult.result()) {
            file = s.substring(s.lastIndexOf(File.separatorChar) + 1);
            // skip dot files
            if (!includeHidden && file.charAt(0) == '.') {
              continue;
            }
            json.add(file);
          }
          request.response().putHeader("content-type", "application/json");
          request.response().end(json.encode());
        } else {
          String file;
          StringBuilder buffer = new StringBuilder();

          for (String s : asyncResult.result()) {
            file = s.substring(s.lastIndexOf(File.separatorChar) + 1);
            // skip dot files
            if (!includeHidden && file.charAt(0) == '.') {
              continue;
            }
            buffer.append(file);
            buffer.append('\n');
          }

          request.response().putHeader("content-type", "text/plain");
          request.response().end(buffer.toString());
        }
      }
    });
  }

  // TODO make this static and use Java8 DateTimeFormatter
  private final class CacheEntry {
    final FileProps props;
    long createDate;

    private CacheEntry(FileProps props, long createDate) {
      this.props = props;
      this.createDate = createDate;
    }

    // return true if there are conditional headers present and they match what is in the entry
    boolean shouldUseCached(HttpServerRequest request) {
      String ifModifiedSince = request.headers().get("if-modified-since");
      if (ifModifiedSince == null) {
        // Not a conditional request
        return false;
      }
      Date ifModifiedSinceDate = parseDate(ifModifiedSince);
      boolean modifiedSince = Utils.secondsFactor(props.lastModifiedTime()) > ifModifiedSinceDate.getTime();
      return !modifiedSince;
    }

    boolean isOutOfDate() {
      boolean outOfDate = System.currentTimeMillis() - createDate > cacheEntryTimeout;
      return outOfDate;
    }

  }


}
