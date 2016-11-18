require 'vertx/buffer'
require 'vertx-web/template_engine'
require 'vertx/vertx'
require 'vertx-web/routing_context'
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
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == PebbleTemplateEngine
    end
    def @@j_api_type.wrap(obj)
      PebbleTemplateEngine.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxExtWebTempl::PebbleTemplateEngine.java_class
    end
    # @param [::VertxWeb::RoutingContext] arg0 
    # @param [String] arg1 
    # @yield 
    # @return [void]
    def render(arg0=nil,arg1=nil)
      if arg0.class.method_defined?(:j_del) && arg1.class == String && block_given?
        return @j_del.java_method(:render, [Java::IoVertxExtWeb::RoutingContext.java_class,Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(arg0.j_del,arg1,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result,::Vertx::Buffer) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling render(#{arg0},#{arg1})"
    end
    #  Create a template engine using defaults
    # @param [::Vertx::Vertx] vertx 
    # @return [::VertxWeb::PebbleTemplateEngine] the engine
    def self.create(vertx=nil)
      if vertx.class.method_defined?(:j_del) && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebTempl::PebbleTemplateEngine.java_method(:create, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del),::VertxWeb::PebbleTemplateEngine)
      end
      raise ArgumentError, "Invalid arguments when calling create(#{vertx})"
    end
    #  Set the extension for the engine
    # @param [String] extension the extension
    # @return [::VertxWeb::PebbleTemplateEngine] a reference to this for fluency
    def set_extension(extension=nil)
      if extension.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:setExtension, [Java::java.lang.String.java_class]).call(extension),::VertxWeb::PebbleTemplateEngine)
      end
      raise ArgumentError, "Invalid arguments when calling set_extension(#{extension})"
    end
    #  Set the max cache size for the engine
    # @param [Fixnum] maxCacheSize the maxCacheSize
    # @return [::VertxWeb::PebbleTemplateEngine] a reference to this for fluency
    def set_max_cache_size(maxCacheSize=nil)
      if maxCacheSize.class == Fixnum && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:setMaxCacheSize, [Java::int.java_class]).call(maxCacheSize),::VertxWeb::PebbleTemplateEngine)
      end
      raise ArgumentError, "Invalid arguments when calling set_max_cache_size(#{maxCacheSize})"
    end
  end
end
