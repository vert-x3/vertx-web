require 'vertx-web/routing_context'
require 'vertx-auth-common/auth_provider'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.UserSessionHandler
module VertxWeb
  # 
  #  This handler should be used if you want to store the User object in the Session so it's available between
  #  different requests, without you having re-authenticate each time.
  # 
  #  It requires that the session handler is already present on previous matching routes.
  # 
  #  It requires an Auth provider so, if the user is deserialized from a clustered session it knows which Auth provider
  #  to associate the session with.
  class UserSessionHandler
    # @private
    # @param j_del [::VertxWeb::UserSessionHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::UserSessionHandler] the underlying java delegate
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
    #  Create a new handler
    # @param [::VertxAuthCommon::AuthProvider] authProvider The auth provider to use
    # @return [::VertxWeb::UserSessionHandler] the handler
    def self.create(authProvider=nil)
      if authProvider.class.method_defined?(:j_del) && !block_given?
        return ::VertxWeb::UserSessionHandler.new(Java::IoVertxExtWebHandler::UserSessionHandler.java_method(:create, [Java::IoVertxExtAuth::AuthProvider.java_class]).call(authProvider.j_del))
      end
      raise ArgumentError, "Invalid arguments when calling create(authProvider)"
    end
  end
end
