require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.StaticHandler
module VertxWeb
  #  A handler for serving static resources from the file system or classpath.
  class StaticHandler
    # @private
    # @param j_del [::VertxWeb::StaticHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::StaticHandler] the underlying java delegate
    def j_del
      @j_del
    end
    # @param [::VertxWeb::RoutingContext] arg0 
    # @return [void]
    def handle(arg0=nil)
      if arg0.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:handle, [Java::IoVertxExtWeb::RoutingContext.java_class]).call(arg0.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling handle(arg0)"
    end
    #  Create a handler, specifying web-root
    # @param [String] root the web-root
    # @return [::VertxWeb::StaticHandler] the handler
    def self.create(root=nil)
      if !block_given? && root == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::StaticHandler.java_method(:create, []).call(),::VertxWeb::StaticHandler)
      elsif root.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::StaticHandler.java_method(:create, [Java::java.lang.String.java_class]).call(root),::VertxWeb::StaticHandler)
      end
      raise ArgumentError, "Invalid arguments when calling create(root)"
    end
    #  Set the web root
    # @param [String] webRoot the web root
    # @return [self]
    def set_web_root(webRoot=nil)
      if webRoot.class == String && !block_given?
        @j_del.java_method(:setWebRoot, [Java::java.lang.String.java_class]).call(webRoot)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_web_root(webRoot)"
    end
    #  Set whether files are read-only and will never change
    # @param [true,false] readOnly whether files are read-only
    # @return [self]
    def set_files_read_only(readOnly=nil)
      if (readOnly.class == TrueClass || readOnly.class == FalseClass) && !block_given?
        @j_del.java_method(:setFilesReadOnly, [Java::boolean.java_class]).call(readOnly)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_files_read_only(readOnly)"
    end
    #  Set value for max age in caching headers
    # @param [Fixnum] maxAgeSeconds maximum time for browser to cache, in seconds
    # @return [self]
    def set_max_age_seconds(maxAgeSeconds=nil)
      if maxAgeSeconds.class == Fixnum && !block_given?
        @j_del.java_method(:setMaxAgeSeconds, [Java::long.java_class]).call(maxAgeSeconds)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_max_age_seconds(maxAgeSeconds)"
    end
    #  Set whether cache header handling is enabled
    # @param [true,false] enabled true if enabled
    # @return [self]
    def set_caching_enabled(enabled=nil)
      if (enabled.class == TrueClass || enabled.class == FalseClass) && !block_given?
        @j_del.java_method(:setCachingEnabled, [Java::boolean.java_class]).call(enabled)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_caching_enabled(enabled)"
    end
    #  Set whether directory listing is enabled
    # @param [true,false] directoryListing true if enabled
    # @return [self]
    def set_directory_listing(directoryListing=nil)
      if (directoryListing.class == TrueClass || directoryListing.class == FalseClass) && !block_given?
        @j_del.java_method(:setDirectoryListing, [Java::boolean.java_class]).call(directoryListing)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_directory_listing(directoryListing)"
    end
    #  Set whether hidden files should be served
    # @param [true,false] includeHidden true if hidden files should be served
    # @return [self]
    def set_include_hidden(includeHidden=nil)
      if (includeHidden.class == TrueClass || includeHidden.class == FalseClass) && !block_given?
        @j_del.java_method(:setIncludeHidden, [Java::boolean.java_class]).call(includeHidden)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_include_hidden(includeHidden)"
    end
    #  Set the server cache entry timeout when caching is enabled
    # @param [Fixnum] timeout the timeout, in ms
    # @return [self]
    def set_cache_entry_timeout(timeout=nil)
      if timeout.class == Fixnum && !block_given?
        @j_del.java_method(:setCacheEntryTimeout, [Java::long.java_class]).call(timeout)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_cache_entry_timeout(timeout)"
    end
    #  Set the index page
    # @param [String] indexPage the index page
    # @return [self]
    def set_index_page(indexPage=nil)
      if indexPage.class == String && !block_given?
        @j_del.java_method(:setIndexPage, [Java::java.lang.String.java_class]).call(indexPage)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_index_page(indexPage)"
    end
    #  Set the max cache size, when caching is enabled
    # @param [Fixnum] maxCacheSize the max cache size
    # @return [self]
    def set_max_cache_size(maxCacheSize=nil)
      if maxCacheSize.class == Fixnum && !block_given?
        @j_del.java_method(:setMaxCacheSize, [Java::int.java_class]).call(maxCacheSize)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_max_cache_size(maxCacheSize)"
    end
    #  Set whether async filesystem access should always be used
    # @param [true,false] alwaysAsyncFS true for always async FS access
    # @return [self]
    def set_always_async_fs(alwaysAsyncFS=nil)
      if (alwaysAsyncFS.class == TrueClass || alwaysAsyncFS.class == FalseClass) && !block_given?
        @j_del.java_method(:setAlwaysAsyncFS, [Java::boolean.java_class]).call(alwaysAsyncFS)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_always_async_fs(alwaysAsyncFS)"
    end
    #  Set whether async/sync filesystem tuning should enabled
    # @param [true,false] enableFSTuning true to enabled FS tuning
    # @return [self]
    def set_enable_fs_tuning(enableFSTuning=nil)
      if (enableFSTuning.class == TrueClass || enableFSTuning.class == FalseClass) && !block_given?
        @j_del.java_method(:setEnableFSTuning, [Java::boolean.java_class]).call(enableFSTuning)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_enable_fs_tuning(enableFSTuning)"
    end
    #  Set the max serve time in ns, above which serves are considered slow
    # @param [Fixnum] maxAvgServeTimeNanoSeconds max serve time, in ns
    # @return [self]
    def set_max_avg_serve_time_ns(maxAvgServeTimeNanoSeconds=nil)
      if maxAvgServeTimeNanoSeconds.class == Fixnum && !block_given?
        @j_del.java_method(:setMaxAvgServeTimeNs, [Java::long.java_class]).call(maxAvgServeTimeNanoSeconds)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_max_avg_serve_time_ns(maxAvgServeTimeNanoSeconds)"
    end
    #  Set the directory template to be used when directory listing
    # @param [String] directoryTemplate the directory template
    # @return [self]
    def set_directory_template(directoryTemplate=nil)
      if directoryTemplate.class == String && !block_given?
        @j_del.java_method(:setDirectoryTemplate, [Java::java.lang.String.java_class]).call(directoryTemplate)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_directory_template(directoryTemplate)"
    end
  end
end
