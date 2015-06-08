require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.ResponseTimeHandler
module VertxWeb
  #  Handler which adds a header `x-response-time` in the response of matching requests containing the time taken
  #  in ms to process the request.
  class ResponseTimeHandler
    # @private
    # @param j_del [::VertxWeb::ResponseTimeHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::ResponseTimeHandler] the underlying java delegate
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
    # @return [::VertxWeb::ResponseTimeHandler] the handler
    def self.create
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::ResponseTimeHandler.java_method(:create, []).call(),::VertxWeb::ResponseTimeHandler)
      end
      raise ArgumentError, "Invalid arguments when calling create()"
    end
  end
end
