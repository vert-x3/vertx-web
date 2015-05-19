require 'vertx/buffer'
require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.templ.TemplateEngine
module VertxWeb
  #  A template engine uses a specific template and the data in a routing context to render a resource into a buffer.
  #  <p>
  #  Concrete implementations exist for several well-known template engines.
  class TemplateEngine
    # @private
    # @param j_del [::VertxWeb::TemplateEngine] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::TemplateEngine] the underlying java delegate
    def j_del
      @j_del
    end
    #  Render
    # @param [::VertxWeb::RoutingContext] context the routing context
    # @param [String] templateFileName the template file name to use
    # @yield the handler that will be called with a result containing the buffer or a failure.
    # @return [void]
    def render(context=nil,templateFileName=nil)
      if context.class.method_defined?(:j_del) && templateFileName.class == String && block_given?
        return @j_del.java_method(:render, [Java::IoVertxExtWeb::RoutingContext.java_class,Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(context.j_del,templateFileName,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Buffer.new(ar.result) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling render(context,templateFileName)"
    end
  end
end
