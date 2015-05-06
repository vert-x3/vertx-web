require 'vertx-apex/session_store'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.apex.sstore.LocalSessionStore
module VertxApex
  #  A session store which is only available on a single node.
  #  <p>
  #  Can be used when sticky sessions are being used.
  class LocalSessionStore < ::VertxApex::SessionStore
    # @private
    # @param j_del [::VertxApex::LocalSessionStore] the java delegate
    def initialize(j_del)
      super(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxApex::LocalSessionStore] the underlying java delegate
    def j_del
      @j_del
    end
    #  Create a session store
    # @param [::Vertx::Vertx] vertx the Vert.x instance
    # @param [String] sessionMapName name for map used to store sessions
    # @param [Fixnum] reaperPeriod how often, in ms, to check for expired sessions
    # @return [::VertxApex::LocalSessionStore] the session store
    def self.create(vertx=nil,sessionMapName=nil,reaperPeriod=nil)
      if vertx.class.method_defined?(:j_del) && !block_given? && sessionMapName == nil && reaperPeriod == nil
        return ::VertxApex::LocalSessionStore.new(Java::IoVertxExtApexSstore::LocalSessionStore.java_method(:create, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del))
      elsif vertx.class.method_defined?(:j_del) && sessionMapName.class == String && !block_given? && reaperPeriod == nil
        return ::VertxApex::LocalSessionStore.new(Java::IoVertxExtApexSstore::LocalSessionStore.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::java.lang.String.java_class]).call(vertx.j_del,sessionMapName))
      elsif vertx.class.method_defined?(:j_del) && sessionMapName.class == String && reaperPeriod.class == Fixnum && !block_given?
        return ::VertxApex::LocalSessionStore.new(Java::IoVertxExtApexSstore::LocalSessionStore.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::java.lang.String.java_class,Java::long.java_class]).call(vertx.j_del,sessionMapName,reaperPeriod))
      end
      raise ArgumentError, "Invalid arguments when calling create(vertx,sessionMapName,reaperPeriod)"
    end
  end
end
