require 'vertx-web/routing_context'
require 'vertx-auth/auth_provider'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.FormLoginHandler
module VertxWeb
  #  Handler that handles login from a form on a custom login page.
  #  <p>
  #  Used in conjunction with the {::VertxWeb::RedirectAuthHandler}.
  class FormLoginHandler
    # @private
    # @param j_del [::VertxWeb::FormLoginHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::FormLoginHandler] the underlying java delegate
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
    # @param [::VertxAuth::AuthProvider] authProvider the auth service to use
    # @param [String] usernameParam the value of the form attribute which will contain the username
    # @param [String] passwordParam the value of the form attribute which will contain the password
    # @param [String] returnURLParam the value of the form attribute which will contain the return url
    # @return [::VertxWeb::FormLoginHandler] the handler
    def self.create(authProvider=nil,usernameParam=nil,passwordParam=nil,returnURLParam=nil)
      if authProvider.class.method_defined?(:j_del) && !block_given? && usernameParam == nil && passwordParam == nil && returnURLParam == nil
        return ::VertxWeb::FormLoginHandler.new(Java::IoVertxExtWebHandler::FormLoginHandler.java_method(:create, [Java::IoVertxExtAuth::AuthProvider.java_class]).call(authProvider.j_del))
      elsif authProvider.class.method_defined?(:j_del) && usernameParam.class == String && passwordParam.class == String && returnURLParam.class == String && !block_given?
        return ::VertxWeb::FormLoginHandler.new(Java::IoVertxExtWebHandler::FormLoginHandler.java_method(:create, [Java::IoVertxExtAuth::AuthProvider.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(authProvider.j_del,usernameParam,passwordParam,returnURLParam))
      end
      raise ArgumentError, "Invalid arguments when calling create(authProvider,usernameParam,passwordParam,returnURLParam)"
    end
  end
end
