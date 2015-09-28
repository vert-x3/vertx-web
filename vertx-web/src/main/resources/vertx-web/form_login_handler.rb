require 'vertx-web/routing_context'
require 'vertx-auth-common/auth_provider'
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
    # @param [::VertxAuthCommon::AuthProvider] authProvider the auth service to use
    # @param [String] usernameParam the value of the form attribute which will contain the username
    # @param [String] passwordParam the value of the form attribute which will contain the password
    # @param [String] returnURLParam the value of the session attribute which will contain the return url
    # @param [String] directLoggedInOKURL a url to redirect to if the user logs in directly at the url of the form login handler without being redirected here first
    # @return [::VertxWeb::FormLoginHandler] the handler
    def self.create(authProvider=nil,usernameParam=nil,passwordParam=nil,returnURLParam=nil,directLoggedInOKURL=nil)
      if authProvider.class.method_defined?(:j_del) && !block_given? && usernameParam == nil && passwordParam == nil && returnURLParam == nil && directLoggedInOKURL == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::FormLoginHandler.java_method(:create, [Java::IoVertxExtAuth::AuthProvider.java_class]).call(authProvider.j_del),::VertxWeb::FormLoginHandler)
      elsif authProvider.class.method_defined?(:j_del) && usernameParam.class == String && passwordParam.class == String && returnURLParam.class == String && directLoggedInOKURL.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::FormLoginHandler.java_method(:create, [Java::IoVertxExtAuth::AuthProvider.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(authProvider.j_del,usernameParam,passwordParam,returnURLParam,directLoggedInOKURL),::VertxWeb::FormLoginHandler)
      end
      raise ArgumentError, "Invalid arguments when calling create(authProvider,usernameParam,passwordParam,returnURLParam,directLoggedInOKURL)"
    end
    #  Set the name of the form param used to submit the username
    # @param [String] usernameParam the name of the param
    # @return [::VertxWeb::FormLoginHandler] a reference to this for a fluent API
    def set_username_param(usernameParam=nil)
      if usernameParam.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:setUsernameParam, [Java::java.lang.String.java_class]).call(usernameParam),::VertxWeb::FormLoginHandler)
      end
      raise ArgumentError, "Invalid arguments when calling set_username_param(usernameParam)"
    end
    #  Set the name of the form param used to submit the password
    # @param [String] passwordParam the name of the param
    # @return [::VertxWeb::FormLoginHandler] a reference to this for a fluent API
    def set_password_param(passwordParam=nil)
      if passwordParam.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:setPasswordParam, [Java::java.lang.String.java_class]).call(passwordParam),::VertxWeb::FormLoginHandler)
      end
      raise ArgumentError, "Invalid arguments when calling set_password_param(passwordParam)"
    end
    #  Set the name of the session attrioute used to specify the return url
    # @param [String] returnURLParam the name of the param
    # @return [::VertxWeb::FormLoginHandler] a reference to this for a fluent API
    def set_return_url_param(returnURLParam=nil)
      if returnURLParam.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:setReturnURLParam, [Java::java.lang.String.java_class]).call(returnURLParam),::VertxWeb::FormLoginHandler)
      end
      raise ArgumentError, "Invalid arguments when calling set_return_url_param(returnURLParam)"
    end
    #  Set the url to redirect to if the user logs in directly at the url of the form login handler
    #  without being redirected here first
    # @param [String] directLoggedInOKURL the URL to redirect to
    # @return [::VertxWeb::FormLoginHandler] a reference to this for a fluent API
    def set_direct_logged_in_okurl(directLoggedInOKURL=nil)
      if directLoggedInOKURL.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:setDirectLoggedInOKURL, [Java::java.lang.String.java_class]).call(directLoggedInOKURL),::VertxWeb::FormLoginHandler)
      end
      raise ArgumentError, "Invalid arguments when calling set_direct_logged_in_okurl(directLoggedInOKURL)"
    end
  end
end
