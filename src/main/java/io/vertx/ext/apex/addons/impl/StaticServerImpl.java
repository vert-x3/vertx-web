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

package io.vertx.ext.apex.addons.impl;

import io.vertx.core.MultiMap;
import io.vertx.core.VertxException;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.addons.StaticServer;
import io.vertx.ext.apex.core.RoutingContext;
import io.vertx.ext.apex.core.impl.LRUCache;
import io.vertx.ext.apex.core.impl.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Static web server
 * Parts derived from Yoke
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public class StaticServerImpl implements StaticServer {

  private static final Logger log = LoggerFactory.getLogger(StaticServerImpl.class);

  private static final String directoryTemplate = Utils.readResourceToBuffer("apex-directory.html").toString();

  private final DateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
  {
    DATE_TIME_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
  }
  private Map<String, CacheEntry> propsCache;
  private String webRoot = DEFAULT_WEB_ROOT;
  private long maxAgeSeconds = DEFAULT_MAX_AGE_SECONDS; // One day
  private boolean directoryListing = DEFAULT_DIRECTORY_LISTING;
  private boolean includeHidden = DEFAULT_INCLUDE_HIDDEN;
  private boolean filesReadOnly = DEFAULT_FILES_READ_ONLY;
  private boolean cachingEnabled = DEFAULT_CACHING_ENABLED;
  private long cacheEntryTimeout = DEFAULT_CACHE_ENTRY_TIMEOUT;
  private String indexPage = DEFAULT_INDEX_PAGE;
  private int maxCacheSize = DEFAULT_MAX_CACHE_SIZE;

  public StaticServerImpl(String root) {
    setRoot(root);
  }

  public StaticServerImpl() {
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
      headers.set("last-modified", DATE_TIME_FORMATTER.format(props.lastModifiedTime()));
    }

    // date header is mandatory
    headers.set("date", DATE_TIME_FORMATTER.format(new Date()));
  }

  @Override
  public void handle(RoutingContext context) {
    HttpServerRequest request = context.request();
    if (request.method() != HttpMethod.GET && request.method() != HttpMethod.HEAD) {
      context.next();
    } else {
      String path = context.normalisedPath();
      // if the normalized path is null it cannot be resolved
      if (path == null) {
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

      FileProps props;
      if (filesReadOnly && entry != null) {
        props = entry.props;
      } else {
        // Need to read the props from the filesystem

        if (file == null) {
          file = getFile(path, context);
        }

        // TODO blocking code
        FileSystem filesystem = context.vertx().fileSystem();
        boolean exists = filesystem.existsBlocking(file);
        if (exists) {
          props = filesystem.propsBlocking(file);
          if (props.isDirectory()) {
            if (directoryListing) {
              sendDirectory(file, context);
              return;
            } else {
              // Directory listing denied
              context.fail(403);
              return;
            }
          } else {
            propsCache().put(path, new CacheEntry(props, System.currentTimeMillis()));
          }
        } else {
          context.fail(404);
          return;
        }
      }

      writeCacheHeaders(request, props);

      if (request.method() == HttpMethod.HEAD) {
        request.response().end();
      } else {
        if (file == null) {
          file = getFile(path, context);
        }
        request.response().sendFile(file, res -> {
          if (res.failed()) {
            log.error("Failed to send file", res.cause());
            context.fail(404);
          }
        });
      }
    }
  }

  @Override
  public StaticServer setWebRoot(String webRoot) {
    setRoot(webRoot);
    return this;
  }

  @Override
  public StaticServer setFilesReadOnly(boolean readOnly) {
    this.filesReadOnly = readOnly;
    return this;
  }

  @Override
  public StaticServer setMaxAgeSeconds(long maxAgeSeconds) {
    if (maxAgeSeconds < 0) {
      throw new IllegalArgumentException("timeout must be >= 0");
    }
    this.maxAgeSeconds = maxAgeSeconds;
    return this;
  }

  @Override
  public StaticServer setMaxCacheSize(int maxCacheSize) {
    if (maxCacheSize < 1) {
      throw new IllegalArgumentException("maxCacheSize must be >= 1");
    }
    this.maxCacheSize = maxCacheSize;
    return this;
  }

  @Override
  public StaticServer setCachingEnabled(boolean enabled) {
    this.cachingEnabled = enabled;
    return this;
  }

  @Override
  public StaticServer setDirectoryListing(boolean directoryListing) {
    this.directoryListing = directoryListing;
    return this;
  }

  @Override
  public StaticServer setIncludeHidden(boolean includeHidden) {
    this.includeHidden = includeHidden;
    return this;
  }

  @Override
  public StaticServer setCacheEntryTimeout(long timeout) {
    if (timeout < 1) {
      throw new IllegalArgumentException("timeout must be >= 1");
    }
    this.cacheEntryTimeout = timeout;
    return this;
  }

  @Override
  public StaticServer setIndexPage(String indexPage) {
    Objects.requireNonNull(indexPage);
    if (!indexPage.startsWith("/")) {
      indexPage = "/" + indexPage;
    }
    this.indexPage = indexPage;
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
      return DATE_TIME_FORMATTER.parse(header);
    } catch (ParseException e) {
      throw new VertxException(e);
    }
  }

  private String getFile(String path, RoutingContext context) {
    // map file path from the request
    // the final path is, root + request.path excluding mount
    String mountPoint = context.mountPoint();
    return mountPoint != null ? webRoot + path.substring(mountPoint.length()) : webRoot + path;
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

          for (String s : asyncResult.result()) {
            file = s.substring(s.lastIndexOf('/') + 1);
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

          StringBuilder directory = new StringBuilder();
          // define access to root
          directory.append("<a href=\"/\">/</a> ");

          StringBuilder expandingPath = new StringBuilder();
          String[] dirParts = normalizedDir.split("/");
          for (int i = 1; i < dirParts.length; i++) {
            // dynamic expansion
            expandingPath.append("/");
            expandingPath.append(dirParts[i]);
            // anchor building
            if (i > 1) {
              directory.append(" / ");
            }
            directory.append("<a href=\"");
            directory.append(expandingPath.toString());
            directory.append("\">");
            directory.append(dirParts[i]);
            directory.append("</a>");
          }

          request.response().putHeader("content-type", "text/html");
          request.response().end(
            directoryTemplate.replace("{title}", context.get("title")).replace("{directory}", normalizedDir)
              .replace("{linked-path}", directory.toString())
              .replace("{files}", files.toString()));
        } else if (accept.contains("json")) {
          String file;
          JsonArray json = new JsonArray();

          for (String s : asyncResult.result()) {
            file = s.substring(s.lastIndexOf('/') + 1);
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
            file = s.substring(s.lastIndexOf('/') + 1);
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
      boolean modifiedSince = props.lastModifiedTime() > ifModifiedSinceDate.getTime();
      return !modifiedSince;
    }

    boolean isOutOfDate() {
      boolean outOfDate = System.currentTimeMillis() - createDate > cacheEntryTimeout;
      return outOfDate;
    }

  }


}
