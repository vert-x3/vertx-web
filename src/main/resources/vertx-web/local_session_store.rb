require 'vertx-web/session_store'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.sstore.LocalSessionStore
module VertxWeb
  #  A session store which is only available on a single node.
  #  <p>
  #  Can be used when sticky sessions are being used.
  class LocalSessionStore < ::VertxWeb::SessionStore
    # @private
    # @param j_del [::VertxWeb::LocalSessionStore] the java delegate
    def initialize(j_del)
      super(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::LocalSessionStore] the underlying java delegate
    def j_del
      @j_del
    end
    #  Create a session store
    # @param [::Vertx::Vertx] vertx the Vert.x instance
    # @param [String] sessionMapName name for map used to store sessions
    # @param [Fixnum] reaperInterval how often, in ms, to check for expired sessions
    # @return [::VertxWeb::LocalSessionStore] the session store
    def self.create(vertx=nil,sessionMapName=nil,reaperInterval=nil)
      if vertx.class.method_defined?(:j_del) && !block_given? && sessionMapName == nil && reaperInterval == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebSstore::LocalSessionStore.java_method(:create, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del),::VertxWeb::LocalSessionStore)
      elsif vertx.class.method_defined?(:j_del) && sessionMapName.class == String && !block_given? && reaperInterval == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebSstore::LocalSessionStore.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::java.lang.String.java_class]).call(vertx.j_del,sessionMapName),::VertxWeb::LocalSessionStore)
      elsif vertx.class.method_defined?(:j_del) && sessionMapName.class == String && reaperInterval.class == Fixnum && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebSstore::LocalSessionStore.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::java.lang.String.java_class,Java::long.java_class]).call(vertx.j_del,sessionMapName,reaperInterval),::VertxWeb::LocalSessionStore)
      end
      raise ArgumentError, "Invalid arguments when calling create(vertx,sessionMapName,reaperInterval)"
    end
  end
end
