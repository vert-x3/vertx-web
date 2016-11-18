require 'vertx-web/auth_handler'
require 'vertx-web/routing_context'
require 'vertx-auth-common/auth_provider'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.RedirectAuthHandler
module VertxWeb
  #  An auth handler that's used to handle auth by redirecting user to a custom login page.
  class RedirectAuthHandler
    include ::VertxWeb::AuthHandler
    # @private
    # @param j_del [::VertxWeb::RedirectAuthHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::RedirectAuthHandler] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == RedirectAuthHandler
    end
    def @@j_api_type.wrap(obj)
      RedirectAuthHandler.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxExtWebHandler::RedirectAuthHandler.java_class
    end
    # @param [::VertxWeb::RoutingContext] arg0 
    # @return [void]
    def handle(arg0=nil)
      if arg0.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:handle, [Java::IoVertxExtWeb::RoutingContext.java_class]).call(arg0.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling handle(#{arg0})"
    end
    #  Add a required authority for this auth handler
    # @param [String] authority the authority
    # @return [self]
    def add_authority(authority=nil)
      if authority.class == String && !block_given?
        @j_del.java_method(:addAuthority, [Java::java.lang.String.java_class]).call(authority)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling add_authority(#{authority})"
    end
    #  Add a set of required authorities for this auth handler
    # @param [Set<String>] authorities the set of authorities
    # @return [self]
    def add_authorities(authorities=nil)
      if authorities.class == Set && !block_given?
        @j_del.java_method(:addAuthorities, [Java::JavaUtil::Set.java_class]).call(Java::JavaUtil::LinkedHashSet.new(authorities.map { |element| element }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling add_authorities(#{authorities})"
    end
    #  Create a handler
    # @param [::VertxAuthCommon::AuthProvider] authProvider the auth service to use
    # @param [String] loginRedirectURL the url to redirect the user to
    # @param [String] returnURLParam the name of param used to store return url information in session
    # @return [::VertxWeb::AuthHandler] the handler
    def self.create(authProvider=nil,loginRedirectURL=nil,returnURLParam=nil)
      if authProvider.class.method_defined?(:j_del) && !block_given? && loginRedirectURL == nil && returnURLParam == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::RedirectAuthHandler.java_method(:create, [Java::IoVertxExtAuth::AuthProvider.java_class]).call(authProvider.j_del),::VertxWeb::AuthHandlerImpl)
      elsif authProvider.class.method_defined?(:j_del) && loginRedirectURL.class == String && !block_given? && returnURLParam == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::RedirectAuthHandler.java_method(:create, [Java::IoVertxExtAuth::AuthProvider.java_class,Java::java.lang.String.java_class]).call(authProvider.j_del,loginRedirectURL),::VertxWeb::AuthHandlerImpl)
      elsif authProvider.class.method_defined?(:j_del) && loginRedirectURL.class == String && returnURLParam.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::RedirectAuthHandler.java_method(:create, [Java::IoVertxExtAuth::AuthProvider.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(authProvider.j_del,loginRedirectURL,returnURLParam),::VertxWeb::AuthHandlerImpl)
      end
      raise ArgumentError, "Invalid arguments when calling create(#{authProvider},#{loginRedirectURL},#{returnURLParam})"
    end
  end
end
