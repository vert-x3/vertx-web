require 'vertx-web/routing_context'
require 'vertx-auth/auth_provider'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.UserHandler
module VertxWeb
  class UserHandler
    # @private
    # @param j_del [::VertxWeb::UserHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::UserHandler] the underlying java delegate
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
    # @param [::VertxAuth::AuthProvider] authProvider
    # @return [::VertxWeb::UserHandler]
    def self.create(authProvider=nil)
      if authProvider.class.method_defined?(:j_del) && !block_given?
        return ::VertxWeb::UserHandler.new(Java::IoVertxExtWebHandler::UserHandler.java_method(:create, [Java::IoVertxExtAuth::AuthProvider.java_class]).call(authProvider.j_del))
      end
      raise ArgumentError, "Invalid arguments when calling create(authProvider)"
    end
  end
end
