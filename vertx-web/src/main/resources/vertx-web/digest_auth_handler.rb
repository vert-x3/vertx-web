require 'vertx-auth-htdigest/htdigest_auth'
require 'vertx-web/auth_handler'
require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.DigestAuthHandler
module VertxWeb
  #  An auth handler that provides HTTP Basic Authentication support.
  class DigestAuthHandler
    include ::VertxWeb::AuthHandler
    # @private
    # @param j_del [::VertxWeb::DigestAuthHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::DigestAuthHandler] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == DigestAuthHandler
    end
    def @@j_api_type.wrap(obj)
      DigestAuthHandler.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxExtWebHandler::DigestAuthHandler.java_class
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
    #  Create a digest auth handler, specifying the expire timeout for nonces.
    # @param [::VertxAuthHtdigest::HtdigestAuth] authProvider the auth service to use
    # @param [Fixnum] nonceExpireTimeout the nonce expire timeout in milliseconds.
    # @return [::VertxWeb::DigestAuthHandler] the auth handler
    def self.create(authProvider=nil,nonceExpireTimeout=nil)
      if authProvider.class.method_defined?(:j_del) && !block_given? && nonceExpireTimeout == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::DigestAuthHandler.java_method(:create, [Java::IoVertxExtAuthHtdigest::HtdigestAuth.java_class]).call(authProvider.j_del),::VertxWeb::DigestAuthHandler)
      elsif authProvider.class.method_defined?(:j_del) && nonceExpireTimeout.class == Fixnum && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::DigestAuthHandler.java_method(:create, [Java::IoVertxExtAuthHtdigest::HtdigestAuth.java_class,Java::long.java_class]).call(authProvider.j_del,nonceExpireTimeout),::VertxWeb::DigestAuthHandler)
      end
      raise ArgumentError, "Invalid arguments when calling create(#{authProvider},#{nonceExpireTimeout})"
    end
  end
end
