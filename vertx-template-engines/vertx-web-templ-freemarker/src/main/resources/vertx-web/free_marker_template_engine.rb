require 'vertx/buffer'
require 'vertx-web/template_engine'
require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.templ.FreeMarkerTemplateEngine
module VertxWeb
  #  A template engine that uses the FreeMarker library.
  class FreeMarkerTemplateEngine < ::VertxWeb::TemplateEngine
    # @private
    # @param j_del [::VertxWeb::FreeMarkerTemplateEngine] the java delegate
    def initialize(j_del)
      super(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::FreeMarkerTemplateEngine] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == FreeMarkerTemplateEngine
    end
    def @@j_api_type.wrap(obj)
      FreeMarkerTemplateEngine.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxExtWebTempl::FreeMarkerTemplateEngine.java_class
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
    # @return [::VertxWeb::FreeMarkerTemplateEngine] the engine
    def self.create
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebTempl::FreeMarkerTemplateEngine.java_method(:create, []).call(),::VertxWeb::FreeMarkerTemplateEngine)
      end
      raise ArgumentError, "Invalid arguments when calling create()"
    end
    #  Set the extension for the engine
    # @param [String] extension the extension
    # @return [::VertxWeb::FreeMarkerTemplateEngine] a reference to this for fluency
    def set_extension(extension=nil)
      if extension.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:setExtension, [Java::java.lang.String.java_class]).call(extension),::VertxWeb::FreeMarkerTemplateEngine)
      end
      raise ArgumentError, "Invalid arguments when calling set_extension(#{extension})"
    end
    #  Set the max cache size for the engine
    # @param [Fixnum] maxCacheSize the maxCacheSize
    # @return [::VertxWeb::FreeMarkerTemplateEngine] a reference to this for fluency
    def set_max_cache_size(maxCacheSize=nil)
      if maxCacheSize.class == Fixnum && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:setMaxCacheSize, [Java::int.java_class]).call(maxCacheSize),::VertxWeb::FreeMarkerTemplateEngine)
      end
      raise ArgumentError, "Invalid arguments when calling set_max_cache_size(#{maxCacheSize})"
    end
  end
end
