require 'vertx-web/template_engine'
require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.TemplateHandler
module VertxWeb
  # 
  #  A handler which renders responses using a template engine and where the template name is selected from the URI
  #  path.
  class TemplateHandler
    # @private
    # @param j_del [::VertxWeb::TemplateHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::TemplateHandler] the underlying java delegate
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
    #  Create a handler
    # @param [::VertxWeb::TemplateEngine] engine the template engine
    # @param [String] templateDirectory the template directory where templates will be looked for
    # @param [String] contentType the content type header to be used in the response
    # @return [::VertxWeb::TemplateHandler] the handler
    def self.create(engine=nil,templateDirectory=nil,contentType=nil)
      if engine.class.method_defined?(:j_del) && !block_given? && templateDirectory == nil && contentType == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::TemplateHandler.java_method(:create, [Java::IoVertxExtWebTempl::TemplateEngine.java_class]).call(engine.j_del),::VertxWeb::TemplateHandler)
      elsif engine.class.method_defined?(:j_del) && templateDirectory.class == String && contentType.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::TemplateHandler.java_method(:create, [Java::IoVertxExtWebTempl::TemplateEngine.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(engine.j_del,templateDirectory,contentType),::VertxWeb::TemplateHandler)
      end
      raise ArgumentError, "Invalid arguments when calling create(engine,templateDirectory,contentType)"
    end
  end
end
