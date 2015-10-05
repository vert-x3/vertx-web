require 'vertx-web/auth_handler'
require 'vertx-web/routing_context'
require 'vertx-auth-common/auth_provider'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.JWTAuthHandler
module VertxWeb
  #  An auth handler that provides JWT Authentication support.
  class JWTAuthHandler
    include ::VertxWeb::AuthHandler
    # @private
    # @param j_del [::VertxWeb::JWTAuthHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::JWTAuthHandler] the underlying java delegate
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
    #  Create a JWT auth handler
    # @param [::VertxAuthCommon::AuthProvider] authProvider the auth provider to use.
    # @param [String] skip 
    # @return [::VertxWeb::JWTAuthHandler] the auth handler
    def self.create(authProvider=nil,skip=nil)
      if authProvider.class.method_defined?(:j_del) && !block_given? && skip == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::JWTAuthHandler.java_method(:create, [Java::IoVertxExtAuth::AuthProvider.java_class]).call(authProvider.j_del),::VertxWeb::JWTAuthHandler)
      elsif authProvider.class.method_defined?(:j_del) && skip.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::JWTAuthHandler.java_method(:create, [Java::IoVertxExtAuth::AuthProvider.java_class,Java::java.lang.String.java_class]).call(authProvider.j_del,skip),::VertxWeb::JWTAuthHandler)
      end
      raise ArgumentError, "Invalid arguments when calling create(authProvider,skip)"
    end
    #  Set the audience list
    # @param [Array<String>] audience the audience list
    # @return [self]
    def set_audience(audience=nil)
      if audience.class == Array && !block_given?
        @j_del.java_method(:setAudience, [Java::JavaUtil::List.java_class]).call(audience.map { |element| element })
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_audience(audience)"
    end
    #  Set the issuer
    # @param [String] issuer the issuer
    # @return [self]
    def set_issuer(issuer=nil)
      if issuer.class == String && !block_given?
        @j_del.java_method(:setIssuer, [Java::java.lang.String.java_class]).call(issuer)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_issuer(issuer)"
    end
    #  Set whether expiration is ignored
    # @param [true,false] ignoreExpiration whether expiration is ignored
    # @return [self]
    def set_ignore_expiration(ignoreExpiration=nil)
      if (ignoreExpiration.class == TrueClass || ignoreExpiration.class == FalseClass) && !block_given?
        @j_del.java_method(:setIgnoreExpiration, [Java::boolean.java_class]).call(ignoreExpiration)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_ignore_expiration(ignoreExpiration)"
    end
  end
end
