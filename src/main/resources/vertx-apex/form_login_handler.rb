require 'vertx-apex/routing_context'
require 'vertx-auth/auth_provider'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.apex.handler.FormLoginHandler
module VertxApex
  #  Handler that handles login from a form on a custom login page.
  #  <p>
  #  Used in conjunction with the {::VertxApex::RedirectAuthHandler}.
  class FormLoginHandler
    # @private
    # @param j_del [::VertxApex::FormLoginHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxApex::FormLoginHandler] the underlying java delegate
    def j_del
      @j_del
    end
    # @param [::VertxApex::RoutingContext] arg0
    # @return [void]
    def handle(arg0=nil)
      if arg0.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:handle, [Java::IoVertxExtApex::RoutingContext.java_class]).call(arg0.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling handle(arg0)"
    end
    #  Create a handler
    # @param [::VertxAuth::AuthProvider] authProvider the auth service to use
    # @param [String] usernameParam the value of the form attribute which will contain the username
    # @param [String] passwordParam the value of the form attribute which will contain the password
    # @param [String] returnURLParam the value of the form attribute which will contain the return url
    # @return [::VertxApex::FormLoginHandler] the handler
    def self.create(authProvider=nil,usernameParam=nil,passwordParam=nil,returnURLParam=nil)
      if authProvider.class.method_defined?(:j_del) && !block_given? && usernameParam == nil && passwordParam == nil && returnURLParam == nil
        return ::VertxApex::FormLoginHandler.new(Java::IoVertxExtApexHandler::FormLoginHandler.java_method(:create, [Java::IoVertxExtAuth::AuthProvider.java_class]).call(authProvider.j_del))
      elsif authProvider.class.method_defined?(:j_del) && usernameParam.class == String && passwordParam.class == String && returnURLParam.class == String && !block_given?
        return ::VertxApex::FormLoginHandler.new(Java::IoVertxExtApexHandler::FormLoginHandler.java_method(:create, [Java::IoVertxExtAuth::AuthProvider.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(authProvider.j_del,usernameParam,passwordParam,returnURLParam))
      end
      raise ArgumentError, "Invalid arguments when calling create(authProvider,usernameParam,passwordParam,returnURLParam)"
    end
  end
end
