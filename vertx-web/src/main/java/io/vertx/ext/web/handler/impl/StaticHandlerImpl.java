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

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.*;
import io.vertx.core.http.impl.HttpUtils;
import io.vertx.core.http.impl.MimeMapping;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.core.internal.net.URIDecoder;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Http2PushMapping;
import io.vertx.ext.web.MIMEHeader;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.StaticHandlerOptions;
import io.vertx.ext.web.impl.LRUCache;
import io.vertx.ext.web.impl.ParsableMIMEValue;
import io.vertx.ext.web.impl.Utils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * Static web server
 * Parts derived from Yoke
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="https://wissel.net">Stephan Wissel</a>
 */
public class StaticHandlerImpl implements StaticHandler {

  private static final Logger LOG = LoggerFactory.getLogger(StaticHandlerImpl.class);

  private final String webRoot;
  private final long maxAgeSeconds;
  private final boolean directoryListing;
  private final String directoryTemplateResource;
  private String directoryTemplate;
  private final boolean includeHidden;
  private final boolean filesReadOnly;
  private final String indexPage;
  private final List<Http2PushMapping> http2PushMappings;
  private final boolean rangeSupport;
  private final boolean sendVaryHeader;
  private final String defaultContentEncoding;

  private final Set<String> compressedMediaTypes;
  private final Set<String> compressedFileSuffixes;

  private final FSTune tune;
  private final FSPropsCache cache;

  public StaticHandlerImpl(boolean allowRootFileSystemAccess, String webRoot, StaticHandlerOptions options) {
    Objects.requireNonNull(webRoot);
    if (!allowRootFileSystemAccess) {
      for (File root : File.listRoots()) {
        if (webRoot.startsWith(root.getAbsolutePath())) {
          throw new IllegalArgumentException("root cannot start with '" + root.getAbsolutePath() + "'");
        }
      }
    }
    this.webRoot = webRoot;
    maxAgeSeconds = options.getMaxAgeSeconds();
    directoryListing = options.isDirectoryListing();
    directoryTemplateResource = Objects.requireNonNullElse(options.getDirectoryTemplate(), StaticHandlerOptions.DEFAULT_DIRECTORY_TEMPLATE);
    includeHidden = options.isIncludeHidden();
    filesReadOnly = options.isFilesReadOnly();
    indexPage = Objects.requireNonNullElse(options.getIndexPage(), StaticHandlerOptions.DEFAULT_INDEX_PAGE);
    List<Http2PushMapping> http2PushMappings = options.getHttp2PushMappings();
    if (http2PushMappings != null) {
      this.http2PushMappings = new ArrayList<>(http2PushMappings.size());
      for (Http2PushMapping mapping : http2PushMappings) {
        this.http2PushMappings.add(new Http2PushMapping(mapping));
      }
    } else {
      this.http2PushMappings = null;
    }
    rangeSupport = options.isEnableRangeSupport();
    sendVaryHeader = options.isSendVaryHeader();
    defaultContentEncoding = Objects.requireNonNullElse(options.getDefaultContentEncoding(), Charset.defaultCharset().name());
    Set<String> compressedMediaTypes = options.getCompressedMediaTypes();
    if (compressedMediaTypes != null) {
      this.compressedMediaTypes = new HashSet<>(compressedMediaTypes);
    } else {
      this.compressedMediaTypes = Collections.emptySet();
    }
    Set<String> compressedFileSuffixes = options.getCompressedFileSuffixes();
    if (compressedFileSuffixes != null) {
      this.compressedFileSuffixes = new HashSet<>(compressedFileSuffixes);
    } else {
      this.compressedFileSuffixes = Collections.emptySet();
    }
    tune = new FSTune(options);
    cache = new FSPropsCache(options);
  }

  private String directoryTemplate(FileSystem fileSystem) {
    if (directoryTemplate == null) {
      directoryTemplate = fileSystem
        .readFileBlocking(directoryTemplateResource)
        .toString(StandardCharsets.UTF_8);
    }
    return directoryTemplate;
  }

  /**
   * Create all required header so content can be cache by Caching servers or
   * Browsers
   *
   * @param request base HttpServerRequest
   * @param props   file properties
   */
  private void writeCacheHeaders(HttpServerRequest request, FileProps props) {

    MultiMap headers = request.response().headers();

    if (cache.enabled()) {
      // We use cache-control and last-modified
      // We *do not use* etags and expires (since they do the same thing - redundant)
      Utils.addToMapIfAbsent(headers, HttpHeaders.CACHE_CONTROL, "public, immutable, max-age=" + maxAgeSeconds);
      Utils.addToMapIfAbsent(headers, HttpHeaders.LAST_MODIFIED, Utils.formatRFC1123DateTime(props.lastModifiedTime()));
      // We send the vary header (for intermediate caches)
      // (assumes that most will turn on compression when using static handler)
      if (sendVaryHeader && request.headers().contains(HttpHeaders.ACCEPT_ENCODING)) {
        Utils.addToMapIfAbsent(headers, HttpHeaders.VARY, "accept-encoding");
      }
    }
    // date header is mandatory
    headers.set("date", Utils.formatRFC1123DateTime(System.currentTimeMillis()));
  }

  @Override
  public void handle(RoutingContext context) {
    HttpServerRequest request = context.request();

    if (request.method() != HttpMethod.GET && request.method() != HttpMethod.HEAD) {
      if (LOG.isTraceEnabled())
        LOG.trace("Not GET or HEAD so ignoring request");
      context.next();
    } else {
      if (!request.isEnded()) {
        request.pause();
      }
      // decode URL path
      String uriDecodedPath = URIDecoder.decodeURIComponent(context.normalizedPath(), false);
      // if the normalized path is null it cannot be resolved
      if (uriDecodedPath == null) {
        LOG.warn("Invalid path: " + context.request().path());
        context.next();
        return;
      }
      // will normalize and handle all paths as UNIX paths
      String path = HttpUtils.removeDots(uriDecodedPath.replace('\\', '/'));

      // Access fileSystem once here to be safe
      FileSystem fs = context.vertx().fileSystem();

      sendStatic(
        context,
        fs,
        path,
        // only root is known for sure to be a directory. all other directories must be
        // identified as such.
        !directoryListing && "/".equals(path));
    }
  }

  /**
   * Can be called recursive for index pages
   */
  private void sendStatic(RoutingContext context, FileSystem fileSystem, String path, boolean index) {

    String file = null;

    if (!includeHidden) {
      file = getFile(path, context);
      int idx = file.lastIndexOf('/');
      String name = file.substring(idx + 1);
      if (name.length() > 0 && name.charAt(0) == '.') {
        // skip
        if (!context.request().isEnded()) {
          context.request().resume();
        }
        context.next();
        return;
      }
    }

    // Look in cache
    final CacheEntry entry = cache.get(path);

    if (entry != null) {
      if ((filesReadOnly || !entry.isOutOfDate())) {
        // a cache entry can mean 2 things:
        // 1. a miss
        // 2. a hit

        // a miss signals that we should continue the chain
        if (entry.isMissing()) {
          if (!context.request().isEnded()) {
            context.request().resume();
          }
          context.next();
          return;
        }

        // a hit needs to be verified for freshness
        final long lastModified = Utils.secondsFactor(entry.props.lastModifiedTime());

        if (Utils.fresh(context, lastModified)) {
          context.response()
            .setStatusCode(NOT_MODIFIED.code())
            .end();
          return;
        }
      }
    }

    final boolean dirty = cache.enabled() && entry != null;
    final String localFile;

    if (file == null) {
      String ctxFile = getFile(path, context);
      if (index) {
        localFile = ctxFile + indexPage;
      } else {
        localFile = ctxFile;
      }
    } else {
      if (index) {
        localFile = file + indexPage;
      } else {
        localFile = file;
      }
    }

    // verify if the file exists
    fileSystem
      .exists(localFile)
      .onFailure(err -> {
        if (!context.request().isEnded()) {
          context.request().resume();
        }
        context.fail(err);
      })
      .onSuccess(exists -> {
        // file does not exist, continue...
        if (!exists) {
          if (cache.enabled()) {
            cache.put(path, null);
          }
          if (!context.request().isEnded()) {
            context.request().resume();
          }
          context.next();
          return;
        }

        // Need to read the props from the filesystem
        getFileProps(fileSystem, localFile)
          .onSuccess(fprops -> {
            if (fprops == null) {
              // File does not exist
              if (dirty) {
                cache.remove(path);
              }
              if (!context.request().isEnded()) {
                context.request().resume();
              }
              context.next();
            } else if (fprops.isDirectory()) {
              if (index) {
                // file does not exist (well it exists but it's a directory), continue...
                if (cache.enabled()) {
                  cache.put(path, null);
                }
                if (!context.request().isEnded()) {
                  context.request().resume();
                }
                context.next();
              } else {
                if (dirty) {
                  cache.remove(path);
                }
                sendDirectory(context, fileSystem, path, localFile);
              }
            } else {
              if (cache.enabled()) {
                cache.put(path, fprops);

                if (Utils.fresh(context, Utils.secondsFactor(fprops.lastModifiedTime()))) {
                  context.response().setStatusCode(NOT_MODIFIED.code()).end();
                  return;
                }
              }
              sendFile(context, fileSystem, localFile, fprops);
            }
          })
          .onFailure(err -> {
            if (!context.request().isEnded()) {
              context.request().resume();
            }
            context.fail(err);
          });
      });
  }

  /**
   * sibling means that we are being upgraded from a directory to a index
   */
  private void sendDirectory(RoutingContext context, FileSystem fileSystem, String path, String file) {
    // in order to keep caches in a valid state we need to assert that
    // the user is requesting a directory (ends with /)
    if (!path.endsWith("/")) {
      context.response()
        .putHeader(HttpHeaders.LOCATION, path + "/")
        .setStatusCode(301)
        .end();
      return;
    }

    if (directoryListing) {
      sendDirectoryListing(fileSystem, file, context);
    } else if (indexPage != null) {
      // send index page recursive call
      sendStatic(context, fileSystem, path, true);
    } else {
      // Directory listing denied
      if (!context.request().isEnded()) {
        context.request().resume();
      }
      context.fail(FORBIDDEN.code());
    }
  }

  private Future<FileProps> getFileProps(FileSystem fileSystem, String file) {
    if (tune.useAsyncFS()) {
      return fileSystem.props(file);
    } else {
      // Use synchronous access - it might well be faster!
      try {
        final boolean tuneEnabled = tune.enabled();
        final long start = tuneEnabled ? System.nanoTime() : 0;
        FileProps props = fileSystem.propsBlocking(file);
        if (tuneEnabled) {
          tune.update(start, System.nanoTime());
        }
        return Future.succeededFuture(props);
      } catch (RuntimeException e) {
        return Future.failedFuture(e.getCause());
      }
    }
  }

  private static final Pattern RANGE = Pattern.compile("^bytes=(\\d+)-(\\d*)$");

  private void sendFile(RoutingContext context, FileSystem fileSystem, String file, FileProps fileProps) {
    final HttpServerRequest request = context.request();
    final HttpServerResponse response = context.response();

    Long offset = null;
    Long end = null;
    MultiMap headers = null;

    if (response.closed())
      return;

    if (rangeSupport) {
      // check if the client is making a range request
      String range = request.getHeader("Range");
      // end byte is length - 1
      end = fileProps.size() - 1;

      if (range != null) {
        Matcher m = RANGE.matcher(range);
        if (m.matches()) {
          try {
            String part = m.group(1);
            // offset cannot be empty
            offset = Long.parseLong(part);
            // offset must fall inside the limits of the file
            if (offset < 0 || offset >= fileProps.size()) {
              throw new IndexOutOfBoundsException();
            }
            // length can be empty
            part = m.group(2);
            if (part != null && part.length() > 0) {
              // ranges are inclusive
              end = Math.min(end, Long.parseLong(part));
              // end offset must not be smaller than start offset
              if (end < offset) {
                throw new IndexOutOfBoundsException();
              }
            }
          } catch (NumberFormatException | IndexOutOfBoundsException e) {
            context.response().putHeader(HttpHeaders.CONTENT_RANGE, "bytes */" + fileProps.size());
            if (!context.request().isEnded()) {
              context.request().resume();
            }
            context.fail(REQUESTED_RANGE_NOT_SATISFIABLE.code());
            return;
          }
        }
      }

      // notify client we support range requests
      headers = response.headers();
      headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
      // send the content length even for HEAD requests
      headers.set(HttpHeaders.CONTENT_LENGTH, Long.toString(end + 1 - (offset == null ? 0 : offset)));
    }

    writeCacheHeaders(request, fileProps);

    if (request.method() == HttpMethod.HEAD) {
      response.end();
    } else {
      if (rangeSupport && offset != null) {
        // must return content range
        headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + offset + "-" + end + "/" + fileProps.size());
        // return a partial response
        response.setStatusCode(PARTIAL_CONTENT.code());

        final long finalOffset = offset;
        final long finalLength = end + 1 - offset;
        // guess content type
        String contentType = MimeMapping.getMimeTypeForFilename(file);
        if (contentType != null) {
          if (contentType.startsWith("text")) {
            response.putHeader(HttpHeaders.CONTENT_TYPE, contentType + ";charset=" + defaultContentEncoding);
          } else {
            response.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
          }
        }

        response.sendFile(file, finalOffset, finalLength)
          .onFailure(err -> {
            if (!context.request().isEnded()) {
              context.request().resume();
            }
            context.fail(err);
          });
      } else {
        // guess content type
        String extension = getFileExtension(file);
        String contentType = MimeMapping.getMimeTypeForExtension(extension);
        if (compressedMediaTypes.contains(contentType) || compressedFileSuffixes.contains(extension)) {
          response.putHeader(HttpHeaders.CONTENT_ENCODING, HttpHeaders.IDENTITY);
        }
        if (contentType != null) {
          if (contentType.startsWith("text")) {
            response.putHeader(HttpHeaders.CONTENT_TYPE, contentType + ";charset=" + defaultContentEncoding);
          } else {
            response.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
          }
        }

        // http2 pushing support
        if (request.version() == HttpVersion.HTTP_2 && http2PushMappings != null) {
          for (Http2PushMapping dependency : http2PushMappings) {
            if (!dependency.isNoPush()) {
              final String dep = webRoot + "/" + dependency.getFilePath();
              // get the file props
              getFileProps(fileSystem, dep)
                .onSuccess(fprops -> {
                  // push
                  writeCacheHeaders(request, fprops);
                  response
                    .push(HttpMethod.GET, "/" + dependency.getFilePath())
                    .onSuccess(res -> {
                      final String depContentType = MimeMapping.getMimeTypeForExtension(file);
                      if (depContentType != null) {
                        if (depContentType.startsWith("text")) {
                          res.putHeader(HttpHeaders.CONTENT_TYPE, contentType + ";charset=" + defaultContentEncoding);
                        } else {
                          res.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
                        }
                      }
                      res.sendFile(webRoot + "/" + dependency.getFilePath());
                    });
                });
            }
          }

        } else if (http2PushMappings != null) {
          // Link preload when file push is not supported
          List<String> links = new ArrayList<>();
          for (Http2PushMapping dependency : http2PushMappings) {
            final String dep = webRoot + "/" + dependency.getFilePath();
            // get the file props
            getFileProps(fileSystem, dep)
              .onSuccess(fprops -> {
                // push
                writeCacheHeaders(request, fprops);
                links
                  .add("<" + dependency.getFilePath() + ">; rel=preload; as=" + dependency.getExtensionTarget() + (dependency.isNoPush() ? "; nopush" : ""));
              });
          }
          response.putHeader("Link", links);
        }

        response.sendFile(file)
          .onFailure(err -> {
            if (!context.request().isEnded()) {
              context.request().resume();
            }
            context.fail(err);
          });
      }
    }
  }

  private String getFile(String path, RoutingContext context) {
    String file = webRoot + Utils.pathOffset(path, context);
    if (LOG.isTraceEnabled()) {
      LOG.trace("File to serve is " + file);
    }
    return file;
  }

  private static final Collection<MIMEHeader> DIRECTORY_LISTING_ACCEPT = Arrays.asList(
    new ParsableMIMEValue("text/html").forceParse(),
    new ParsableMIMEValue("text/plain").forceParse(),
    new ParsableMIMEValue("application/json").forceParse());

  private void sendDirectoryListing(FileSystem fileSystem, String dir, RoutingContext context) {
    final HttpServerResponse response = context.response();

    fileSystem.readDir(dir)
      .onFailure(err -> {
        if (!context.request().isEnded()) {
          context.request().resume();
        }
        context.fail(err);
      })
      .onSuccess(list -> {

        final List<MIMEHeader> accepts = context.parsedHeaders().accept();
        String accept = "text/plain";
        String file;

        if (accepts != null) {
          MIMEHeader header = context.parsedHeaders()
            .findBestUserAcceptedIn(context.parsedHeaders().accept(), DIRECTORY_LISTING_ACCEPT);

          if (header != null) {
            accept = header.component() + "/" + header.subComponent();
          }
        }

        switch (accept) {
          case "text/html":
            String normalizedDir = context.normalizedPath();
            if (!normalizedDir.endsWith("/")) {
              normalizedDir += "/";
            }

            StringBuilder files = new StringBuilder("<ul id=\"files\">");
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
            for (int i = normalizedDir.length() - 2; i > 0; i--) {
              if (normalizedDir.charAt(i) == '/') {
                slashPos = i;
                break;
              }
            }

            String parent = "<a href=\"" + normalizedDir.substring(0, slashPos + 1) + "\">..</a>";

            response
              .putHeader(HttpHeaders.CONTENT_TYPE, "text/html")
              .end(
                directoryTemplate(fileSystem).replace("{directory}", normalizedDir)
                  .replace("{parent}", parent)
                  .replace("{files}", files.toString()));
            break;
          case "application/json":
            JsonArray json = new JsonArray();

            for (String s : list) {
              file = s.substring(s.lastIndexOf(File.separatorChar) + 1);
              // skip dot files
              if (!includeHidden && file.charAt(0) == '.') {
                continue;
              }
              json.add(file);
            }
            response
              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
              .end(json.encode());
            break;
          default:
            StringBuilder buffer = new StringBuilder();

            for (String s : list) {
              file = s.substring(s.lastIndexOf(File.separatorChar) + 1);
              // skip dot files
              if (!includeHidden && file.charAt(0) == '.') {
                continue;
              }
              buffer.append(file);
              buffer.append('\n');
            }

            response
              .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
              .end(buffer.toString());
        }
    });
  }

  private String getFileExtension(String file) {
    int li = file.lastIndexOf(46);
    if (li != -1 && li != file.length() - 1) {
      return file.substring(li + 1);
    } else {
      return null;
    }
  }

  private static final class CacheEntry {
    final long createDate = System.currentTimeMillis();

    final FileProps props;
    final long cacheEntryTimeout;

    private CacheEntry(FileProps props, long cacheEntryTimeout) {
      this.props = props;
      this.cacheEntryTimeout = cacheEntryTimeout;
    }

    boolean isOutOfDate() {
      return System.currentTimeMillis() - createDate > cacheEntryTimeout;
    }

    public boolean isMissing() {
      return props == null;
    }
  }

  private static class FSTune {
    // These members are all related to auto-tuning of synchronous vs asynchronous file system access
    static final int NUM_SERVES_TUNING_FS_ACCESS = 1000;

    // These variables are read often and should always represent the real value, no caching should be allowed
    volatile boolean enabled;
    volatile boolean useAsyncFS;

    long totalTime;
    long numServesBlocking;
    long nextAvgCheck = NUM_SERVES_TUNING_FS_ACCESS;
    final long maxAvgServeTimeNanoSeconds;
    final boolean alwaysAsyncFS;

    FSTune(StaticHandlerOptions options) {
      enabled = options.isEnableFSTuning();
      alwaysAsyncFS = options.isAlwaysAsyncFS();
      maxAvgServeTimeNanoSeconds = options.getMaxAvgServeTimeNs();
    }

    boolean enabled() {
      return enabled;
    }

    boolean useAsyncFS() {
      return alwaysAsyncFS || useAsyncFS;
    }

    synchronized void update(long start, long end) {
      long dur = end - start;
      totalTime += dur;
      numServesBlocking++;
      if (numServesBlocking == Long.MAX_VALUE) {
        // Unlikely.. but...
        reset();
      } else if (numServesBlocking == nextAvgCheck) {
        double avg = (double) totalTime / numServesBlocking;
        if (avg > maxAvgServeTimeNanoSeconds) {
          useAsyncFS = true;
          if (LOG.isInfoEnabled()) {
            LOG.info(
              "Switching to async file system access in static file server as fs access is slow! (Average access time of "
                + avg + " ns)");
          }
          enabled = false;
        }
        nextAvgCheck += NUM_SERVES_TUNING_FS_ACCESS;
      }
    }

    synchronized void reset() {
      nextAvgCheck = NUM_SERVES_TUNING_FS_ACCESS;
      totalTime = 0;
      numServesBlocking = 0;
    }
  }

  private static class FSPropsCache {
    final Map<String, CacheEntry> propsCache;
    final long cacheEntryTimeout;

    FSPropsCache(StaticHandlerOptions options) {
      if (options.isCachingEnabled()) {
        propsCache = new LRUCache<>(options.getMaxCacheSize());
      } else {
        propsCache = null;
      }
      cacheEntryTimeout = options.getCacheEntryTimeout();
    }

    boolean enabled() {
      return propsCache != null;
    }

    void remove(String path) {
      if (propsCache != null) {
        propsCache.remove(path);
      }
    }

    CacheEntry get(String key) {
      if (propsCache != null) {
        return propsCache.get(key);
      }

      return null;
    }

    void put(String path, FileProps props) {
      if (propsCache != null) {
        CacheEntry now = new CacheEntry(props, cacheEntryTimeout);
        propsCache.put(path, now);
      }
    }
  }
}
