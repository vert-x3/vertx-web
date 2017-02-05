require 'vertx/buffer'
require 'vertx-web/template_engine'
require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.templ.ThymeleafTemplateEngine
module VertxWeb
  #  A template engine that uses the Thymeleaf library.
  class ThymeleafTemplateEngine < ::VertxWeb::TemplateEngine
    # @private
    # @param j_del [::VertxWeb::ThymeleafTemplateEngine] the java delegate
    def initialize(j_del)
      super(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::ThymeleafTemplateEngine] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == ThymeleafTemplateEngine
    end
    def @@j_api_type.wrap(obj)
      ThymeleafTemplateEngine.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxExtWebTempl::ThymeleafTemplateEngine.java_class
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
    # @return [::VertxWeb::ThymeleafTemplateEngine] the engine
    def self.create
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebTempl::ThymeleafTemplateEngine.java_method(:create, []).call(),::VertxWeb::ThymeleafTemplateEngine)
      end
      raise ArgumentError, "Invalid arguments when calling create()"
    end
    #  Set the mode for the engine
    # @param [:HTML,:XML,:TEXT,:JAVASCRIPT,:CSS,:RAW,:HTML5,:LEGACYHTML5,:XHTML,:VALIDXHTML,:VALIDXML] mode the mode
    # @return [::VertxWeb::ThymeleafTemplateEngine] a reference to this for fluency
    def set_mode(mode=nil)
      if mode.class == Symbol && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:setMode, [Java::OrgThymeleafTemplatemode::TemplateMode.java_class]).call(Java::OrgThymeleafTemplatemode::TemplateMode.valueOf(mode.to_s)),::VertxWeb::ThymeleafTemplateEngine)
      end
      raise ArgumentError, "Invalid arguments when calling set_mode(#{mode})"
    end
  end
end
