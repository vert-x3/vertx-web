require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.AuthHandler
module VertxWeb
  module AuthHandler
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
  end
  class AuthHandlerImpl
    include AuthHandler
    # @private
    # @param j_del [::VertxWeb::AuthHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::AuthHandler] the underlying java delegate
    def j_del
      @j_del
    end
  end
end
