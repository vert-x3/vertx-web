require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.CookieHandler
module VertxWeb
  #  A handler which decodes cookies from the request, makes them available in the {::VertxWeb::RoutingContext}
  #  and writes them back in the response.
  class CookieHandler
    # @private
    # @param j_del [::VertxWeb::CookieHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::CookieHandler] the underlying java delegate
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
    #  Create a cookie handler
    # @return [::VertxWeb::CookieHandler] the cookie handler
    def self.create
      if !block_given?
        return ::VertxWeb::CookieHandler.new(Java::IoVertxExtWebHandler::CookieHandler.java_method(:create, []).call())
      end
      raise ArgumentError, "Invalid arguments when calling create()"
    end
  end
end
