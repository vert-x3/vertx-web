require 'vertx-web/template_engine'
require 'vertx/vertx'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.templ.PebbleTemplateEngine
module VertxWeb
  #  A template engine that uses the Pebble library.
  class PebbleTemplateEngine < ::VertxWeb::TemplateEngine
    # @private
    # @param j_del [::VertxWeb::PebbleTemplateEngine] the java delegate
    def initialize(j_del)
      super(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::PebbleTemplateEngine] the underlying java delegate
    def j_del
      @j_del
    end
    #  Create a template engine using defaults
    # @param [::Vertx::Vertx] vertx 
    # @return [::VertxWeb::PebbleTemplateEngine] the engine
    def self.create(vertx=nil)
      if vertx.class.method_defined?(:j_del) && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebTempl::PebbleTemplateEngine.java_method(:create, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del),::VertxWeb::PebbleTemplateEngine)
      end
      raise ArgumentError, "Invalid arguments when calling create(vertx)"
    end
    #  Set the extension for the engine
    # @param [String] extension the extension
    # @return [::VertxWeb::PebbleTemplateEngine] a reference to this for fluency
    def set_extension(extension=nil)
      if extension.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:setExtension, [Java::java.lang.String.java_class]).call(extension),::VertxWeb::PebbleTemplateEngine)
      end
      raise ArgumentError, "Invalid arguments when calling set_extension(extension)"
    end
    #  Set the max cache size for the engine
    # @param [Fixnum] maxCacheSize the maxCacheSize
    # @return [::VertxWeb::PebbleTemplateEngine] a reference to this for fluency
    def set_max_cache_size(maxCacheSize=nil)
      if maxCacheSize.class == Fixnum && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:setMaxCacheSize, [Java::int.java_class]).call(maxCacheSize),::VertxWeb::PebbleTemplateEngine)
      end
      raise ArgumentError, "Invalid arguments when calling set_max_cache_size(maxCacheSize)"
    end
  end
end
