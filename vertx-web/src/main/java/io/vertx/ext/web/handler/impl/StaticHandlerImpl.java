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

import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_MODIFIED;
import static io.netty.handler.codec.http.HttpResponseStatus.PARTIAL_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.impl.HttpUtils;
import io.vertx.core.http.impl.MimeMapping;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.net.impl.URIDecoder;
import io.vertx.ext.web.Http2PushMapping;
import io.vertx.ext.web.MIMEHeader;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.impl.LRUCache;
import io.vertx.ext.web.impl.ParsableMIMEValue;
import io.vertx.ext.web.impl.Utils;

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

  // TODO change to private final after setWebRoot has been removed
  private String webRoot = DEFAULT_WEB_ROOT;
  private long maxAgeSeconds = DEFAULT_MAX_AGE_SECONDS; // One day
  private boolean directoryListing = DEFAULT_DIRECTORY_LISTING;
  private String directoryTemplateResource = DEFAULT_DIRECTORY_TEMPLATE;
  private String directoryTemplate;
  private boolean includeHidden = DEFAULT_INCLUDE_HIDDEN;
  private boolean filesReadOnly = DEFAULT_FILES_READ_ONLY;
  private String indexPage = DEFAULT_INDEX_PAGE;
  private List<Http2PushMapping> http2PushMappings;
  private boolean rangeSupport = DEFAULT_RANGE_SUPPORT;
  // TODO change to private final after setAllowRootAccess has been removed
  private boolean allowRootFileSystemAccess = DEFAULT_ROOT_FILESYSTEM_ACCESS;
  private boolean sendVaryHeader = DEFAULT_SEND_VARY_HEADER;
  private String defaultContentEncoding = Charset.defaultCharset().name();

  private Set<String> compressedMediaTypes = Collections.emptySet();
  private Set<String> compressedFileSuffixes = Collections.emptySet();

  private final FSTune tune = new FSTune();
  private final FSPropsCache cache = new FSPropsCache();

  /**
   * Constructor called by static factory method
   * 
   * @param visibility          path specified by root is RELATIVE or ROOT
   * @param staticRootDirectory path on host with static file location
   */
  public StaticHandlerImpl(FileSystemAccess visibility, String staticRootDirectory) {

    this.allowRootFileSystemAccess = FileSystemAccess.ROOT.equals(visibility);
    this.setRoot(staticRootDirectory != null ? staticRootDirectory : DEFAULT_WEB_ROOT);
  }

  /**
   * Default constructor with DEFAULT_WEB_ROOT and
   * relative file access only
   */
  public StaticHandlerImpl() {

    this.allowRootFileSystemAccess = false;
    this.setRoot(DEFAULT_WEB_ROOT);
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
        .exists(localFile, exists -> {
          if (exists.failed()) {
            context.fail(exists.cause());
            return;
          }

          // file does not exist, continue...
          if (!exists.result()) {
            if (cache.enabled()) {
              cache.put(path, null);
            }
            context.next();
            return;
          }

          // Need to read the props from the filesystem
          getFileProps(fileSystem, localFile, res -> {
            if (res.succeeded()) {
              FileProps fprops = res.result();
              if (fprops == null) {
                // File does not exist
                if (dirty) {
                  cache.remove(path);
                }
                context.next();
              } else if (fprops.isDirectory()) {
                if (index) {
                  // file does not exist (well it exists but it's a directory), continue...
                  if (cache.enabled()) {
                    cache.put(path, null);
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
            } else {
              context.fail(res.cause());
            }
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
      context.fail(FORBIDDEN.code());
    }
  }

  private void getFileProps(FileSystem fileSystem, String file, Handler<AsyncResult<FileProps>> resultHandler) {
    if (tune.useAsyncFS()) {
      fileSystem.props(file, resultHandler);
    } else {
      // Use synchronous access - it might well be faster!
      try {
        final boolean tuneEnabled = tune.enabled();
        final long start = tuneEnabled ? System.nanoTime() : 0;
        FileProps props = fileSystem.propsBlocking(file);
        if (tuneEnabled) {
          tune.update(start, System.nanoTime());
        }
        resultHandler.handle(Future.succeededFuture(props));
      } catch (RuntimeException e) {
        resultHandler.handle(Future.failedFuture(e.getCause()));
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

        response.sendFile(file, finalOffset, finalLength, res2 -> {
          if (res2.failed()) {
            context.fail(res2.cause());
          }
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
              getFileProps(fileSystem, dep, filePropsAsyncResult -> {
                if (filePropsAsyncResult.succeeded()) {
                  // push
                  writeCacheHeaders(request, filePropsAsyncResult.result());
                  response.push(HttpMethod.GET, "/" + dependency.getFilePath(), pushAsyncResult -> {
                    if (pushAsyncResult.succeeded()) {
                      HttpServerResponse res = pushAsyncResult.result();
                      final String depContentType = MimeMapping.getMimeTypeForExtension(file);
                      if (depContentType != null) {
                        if (depContentType.startsWith("text")) {
                          res.putHeader(HttpHeaders.CONTENT_TYPE, contentType + ";charset=" + defaultContentEncoding);
                        } else {
                          res.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
                        }
                      }
                      res.sendFile(webRoot + "/" + dependency.getFilePath());
                    }
                  });
                }
              });
            }
          }

        } else if (http2PushMappings != null) {
          // Link preload when file push is not supported
          List<String> links = new ArrayList<>();
          for (Http2PushMapping dependency : http2PushMappings) {
            final String dep = webRoot + "/" + dependency.getFilePath();
            // get the file props
            getFileProps(fileSystem, dep, filePropsAsyncResult -> {
              if (filePropsAsyncResult.succeeded()) {
                // push
                writeCacheHeaders(request, filePropsAsyncResult.result());
                links.add("<" + dependency.getFilePath() + ">; rel=preload; as="
                    + dependency.getExtensionTarget() + (dependency.isNoPush() ? "; nopush" : ""));
              }
            });
          }
          response.putHeader("Link", links);
        }

        response.sendFile(file, res2 -> {
          if (res2.failed()) {
            context.fail(res2.cause());
          }
        });
      }
    }
  }

  /**
   * @deprecated Root file system access is set only once in the constructor
   */
  @Override
  @Deprecated
  public StaticHandler setAllowRootFileSystemAccess(boolean allowRootFileSystemAccess) {
    this.allowRootFileSystemAccess = allowRootFileSystemAccess;
    return this;
  }

  /**
   * @deprecated - webroot is set only once in the constructor
   */
  @Override
  @Deprecated
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
    cache.setMaxSize(maxCacheSize);
    return this;
  }

  @Override
  public StaticHandler setCachingEnabled(boolean enabled) {
    cache.setEnabled(enabled);
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
  public StaticHandler setEnableRangeSupport(boolean enableRangeSupport) {
    this.rangeSupport = enableRangeSupport;
    return this;
  }

  @Override
  public StaticHandler setIncludeHidden(boolean includeHidden) {
    this.includeHidden = includeHidden;
    return this;
  }

  @Override
  public StaticHandler setCacheEntryTimeout(long timeout) {
    cache.setCacheEntryTimeout(timeout);
    return this;
  }

  @Override
  public StaticHandler setIndexPage(String indexPage) {
    Objects.requireNonNull(indexPage);
    if (indexPage.charAt(0) == '/') {
      this.indexPage = indexPage.substring(1);
    } else {
      this.indexPage = indexPage;
    }
    return this;
  }

  @Override
  public StaticHandler setAlwaysAsyncFS(boolean alwaysAsyncFS) {
    tune.setAlwaysAsyncFS(alwaysAsyncFS);
    return this;
  }

  @Override
  public StaticHandler setHttp2PushMapping(List<Http2PushMapping> http2PushMap) {
    if (http2PushMap != null) {
      this.http2PushMappings = new ArrayList<>(http2PushMap);
    }
    return this;
  }

  @Override
  public StaticHandler skipCompressionForMediaTypes(Set<String> mediaTypes) {
    if (mediaTypes != null) {
      this.compressedMediaTypes = new HashSet<>(mediaTypes);
    }
    return this;
  }

  @Override
  public StaticHandler skipCompressionForSuffixes(Set<String> fileSuffixes) {
    if (fileSuffixes != null) {
      this.compressedFileSuffixes = new HashSet<>(fileSuffixes);
    }
    return this;
  }

  @Override
  public synchronized StaticHandler setEnableFSTuning(boolean enableFSTuning) {
    tune.setEnabled(enableFSTuning);
    return this;
  }

  @Override
  public StaticHandler setMaxAvgServeTimeNs(long maxAvgServeTimeNanoSeconds) {
    tune.maxAvgServeTimeNanoSeconds = maxAvgServeTimeNanoSeconds;
    return this;
  }

  @Override
  public StaticHandler setSendVaryHeader(boolean sendVaryHeader) {
    this.sendVaryHeader = sendVaryHeader;
    return this;
  }

  @Override
  public StaticHandler setDefaultContentEncoding(String contentEncoding) {
    this.defaultContentEncoding = contentEncoding;
    return this;
  }

  private String getFile(String path, RoutingContext context) {
    String file = webRoot + Utils.pathOffset(path, context);
    if (LOG.isTraceEnabled()) {
      LOG.trace("File to serve is " + file);
    }
    return file;
  }

  private void setRoot(String webRoot) {
    Objects.requireNonNull(webRoot);
    if (!allowRootFileSystemAccess) {
      for (File root : File.listRoots()) {
        if (webRoot.startsWith(root.getAbsolutePath())) {
          throw new IllegalArgumentException("root cannot start with '" + root.getAbsolutePath() + "'");
        }
      }
    }
    this.webRoot = webRoot;
  }

  private static final Collection<MIMEHeader> DIRECTORY_LISTING_ACCEPT = Arrays.asList(
      new ParsableMIMEValue("text/html").forceParse(),
      new ParsableMIMEValue("text/plain").forceParse(),
      new ParsableMIMEValue("application/json").forceParse());

  private void sendDirectoryListing(FileSystem fileSystem, String dir, RoutingContext context) {
    final HttpServerResponse response = context.response();

    fileSystem.readDir(dir, asyncResult -> {
      if (asyncResult.failed()) {
        context.fail(asyncResult.cause());
      } else {

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

            for (String s : asyncResult.result()) {
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

            for (String s : asyncResult.result()) {
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
    // These members are all related to auto tuning of synchronous vs asynchronous
    // file system access
    private static final int NUM_SERVES_TUNING_FS_ACCESS = 1000;

    // these variables are read often and should always represent the
    // real value, no caching should be allowed
    private volatile boolean enabled = DEFAULT_ENABLE_FS_TUNING;
    private volatile boolean useAsyncFS;

    private long totalTime;
    private long numServesBlocking;
    private long nextAvgCheck = NUM_SERVES_TUNING_FS_ACCESS;
    private long maxAvgServeTimeNanoSeconds = DEFAULT_MAX_AVG_SERVE_TIME_NS;
    private boolean alwaysAsyncFS = DEFAULT_ALWAYS_ASYNC_FS;

    boolean enabled() {
      return enabled;
    }

    boolean useAsyncFS() {
      return alwaysAsyncFS || useAsyncFS;
    }

    synchronized void setEnabled(boolean enabled) {
      this.enabled = enabled;
      if (!enabled) {
        reset();
      }
    }

    void setAlwaysAsyncFS(boolean alwaysAsyncFS) {
      this.alwaysAsyncFS = alwaysAsyncFS;
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
    private Map<String, CacheEntry> propsCache;
    private long cacheEntryTimeout = DEFAULT_CACHE_ENTRY_TIMEOUT;
    private int maxCacheSize = DEFAULT_MAX_CACHE_SIZE;

    FSPropsCache() {
      setEnabled(DEFAULT_CACHING_ENABLED);
    }

    boolean enabled() {
      return propsCache != null;
    }

    synchronized void setMaxSize(int maxCacheSize) {
      if (maxCacheSize < 1) {
        throw new IllegalArgumentException("maxCacheSize must be >= 1");
      }
      if (this.maxCacheSize != maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
        // force the creation of the cache with the correct size
        setEnabled(enabled(), true);
      }
    }

    void setEnabled(boolean enable) {
      setEnabled(enable, false);
    }

    private synchronized void setEnabled(boolean enable, boolean force) {
      if (force || enable != enabled()) {
        if (propsCache != null) {
          propsCache.clear();
        }
        if (enable) {
          propsCache = new LRUCache<>(maxCacheSize);
        } else {
          propsCache = null;
        }
      }
    }

    void setCacheEntryTimeout(long timeout) {
      if (timeout < 1) {
        throw new IllegalArgumentException("timeout must be >= 1");
      }
      this.cacheEntryTimeout = timeout;
    }

    private void remove(String path) {
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
