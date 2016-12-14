require 'vertx-web/route'
require 'vertx-web/auth_handler'
require 'vertx-web/routing_context'
require 'vertx-auth-oauth2/o_auth2_auth'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.OAuth2AuthHandler
module VertxWeb
  #  An auth handler that provides OAuth2 Authentication support. This handler is suitable for AuthCode flows.
  class OAuth2AuthHandler
    include ::VertxWeb::AuthHandler
    # @private
    # @param j_del [::VertxWeb::OAuth2AuthHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::OAuth2AuthHandler] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == OAuth2AuthHandler
    end
    def @@j_api_type.wrap(obj)
      OAuth2AuthHandler.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxExtWebHandler::OAuth2AuthHandler.java_class
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
    #  Create a OAuth2 auth handler with host pinning
    # @param [::VertxAuthOauth2::OAuth2Auth] authProvider the auth provider to use
    # @param [String] callbackURL the callback URL you entered in your provider admin console, usually it should be something like: `https://myserver:8888/callback`
    # @return [::VertxWeb::OAuth2AuthHandler] the auth handler
    def self.create(authProvider=nil,callbackURL=nil)
      if authProvider.class.method_defined?(:j_del) && callbackURL.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::OAuth2AuthHandler.java_method(:create, [Java::IoVertxExtAuthOauth2::OAuth2Auth.java_class,Java::java.lang.String.java_class]).call(authProvider.j_del,callbackURL),::VertxWeb::OAuth2AuthHandler)
      end
      raise ArgumentError, "Invalid arguments when calling create(#{authProvider},#{callbackURL})"
    end
    #  Extra parameters needed to be passed while requesting a token.
    # @param [Hash{String => Object}] extraParams extra optional parameters.
    # @return [self]
    def extra_params(extraParams=nil)
      if extraParams.class == Hash && !block_given?
        @j_del.java_method(:extraParams, [Java::IoVertxCoreJson::JsonObject.java_class]).call(::Vertx::Util::Utils.to_json_object(extraParams))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling extra_params(#{extraParams})"
    end
    #  add the callback handler to a given route.
    # @param [::VertxWeb::Route] route a given route e.g.: `/callback`
    # @return [self]
    def setup_callback(route=nil)
      if route.class.method_defined?(:j_del) && !block_given?
        @j_del.java_method(:setupCallback, [Java::IoVertxExtWeb::Route.java_class]).call(route.j_del)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling setup_callback(#{route})"
    end
  end
end
