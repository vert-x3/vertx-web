require 'vertx-web/auth_handler'
require 'vertx-web/routing_context'
require 'vertx-auth-common/auth_provider'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.BasicAuthHandler
module VertxWeb
  #  An auth handler that provides HTTP Basic Authentication support.
  class BasicAuthHandler
    include ::VertxWeb::AuthHandler
    # @private
    # @param j_del [::VertxWeb::BasicAuthHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::BasicAuthHandler] the underlying java delegate
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
    #  Add a required authority for this auth handler
    # @param [String] authority the authority
    # @return [self]
    def add_authority(authority=nil)
      if authority.class == String && !block_given?
        @j_del.java_method(:addAuthority, [Java::java.lang.String.java_class]).call(authority)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling add_authority(authority)"
    end
    #  Add a set of required authorities for this auth handler
    # @param [Set<String>] authorities the set of authorities
    # @return [self]
    def add_authorities(authorities=nil)
      if authorities.class == Set && !block_given?
        @j_del.java_method(:addAuthorities, [Java::JavaUtil::Set.java_class]).call(Java::JavaUtil::LinkedHashSet.new(authorities.map { |element| element }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling add_authorities(authorities)"
    end
    #  Create a basic auth handler, specifying realm
    # @param [::VertxAuthCommon::AuthProvider] authProvider the auth service to use
    # @param [String] realm the realm to use
    # @return [::VertxWeb::AuthHandler] the auth handler
    def self.create(authProvider=nil,realm=nil)
      if authProvider.class.method_defined?(:j_del) && !block_given? && realm == nil
        return ::VertxWeb::AuthHandlerImpl.new(Java::IoVertxExtWebHandler::BasicAuthHandler.java_method(:create, [Java::IoVertxExtAuth::AuthProvider.java_class]).call(authProvider.j_del))
      elsif authProvider.class.method_defined?(:j_del) && realm.class == String && !block_given?
        return ::VertxWeb::AuthHandlerImpl.new(Java::IoVertxExtWebHandler::BasicAuthHandler.java_method(:create, [Java::IoVertxExtAuth::AuthProvider.java_class,Java::java.lang.String.java_class]).call(authProvider.j_del,realm))
      end
      raise ArgumentError, "Invalid arguments when calling create(authProvider,realm)"
    end
  end
end
