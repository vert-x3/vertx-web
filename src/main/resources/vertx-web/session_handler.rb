require 'vertx-web/session_store'
require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.SessionHandler
module VertxWeb
  #  A handler that maintains a {::VertxWeb::Session} for each browser session.
  #  <p>
  #  It looks up the session for each request based on a session cookie which contains a session ID. It stores the session
  #  when the response is ended in the session store.
  #  <p>
  #  The session is available on the routing context with {::VertxWeb::RoutingContext#session}.
  #  <p>
  #  The session handler requires a {::VertxWeb::CookieHandler} to be on the routing chain before it.
  class SessionHandler
    # @private
    # @param j_del [::VertxWeb::SessionHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::SessionHandler] the underlying java delegate
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
    #  Create a session handler
    # @param [::VertxWeb::SessionStore] sessionStore the session store
    # @return [::VertxWeb::SessionHandler] the handler
    def self.create(sessionStore=nil)
      if sessionStore.class.method_defined?(:j_del) && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::SessionHandler.java_method(:create, [Java::IoVertxExtWebSstore::SessionStore.java_class]).call(sessionStore.j_del),::VertxWeb::SessionHandler)
      end
      raise ArgumentError, "Invalid arguments when calling create(sessionStore)"
    end
    #  Set the session timeout
    # @param [Fixnum] timeout the timeout, in ms.
    # @return [self]
    def set_session_timeout(timeout=nil)
      if timeout.class == Fixnum && !block_given?
        @j_del.java_method(:setSessionTimeout, [Java::long.java_class]).call(timeout)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_session_timeout(timeout)"
    end
    #  Set whether a nagging log warning should be written if the session handler is accessed over HTTP, not
    #  HTTPS
    # @param [true,false] nag true to nag
    # @return [self]
    def set_nag_https(nag=nil)
      if (nag.class == TrueClass || nag.class == FalseClass) && !block_given?
        @j_del.java_method(:setNagHttps, [Java::boolean.java_class]).call(nag)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_nag_https(nag)"
    end
    #  Sets whether the 'secure' flag should be set for the session cookie. When set this flag instructs browsers to only
    #  send the cookie over HTTPS. Note that this will probably stop your sessions working if used without HTTPS (e.g. in development).
    # @param [true,false] secure true to set the secure flag on the cookie
    # @return [self]
    def set_cookie_secure_flag(secure=nil)
      if (secure.class == TrueClass || secure.class == FalseClass) && !block_given?
        @j_del.java_method(:setCookieSecureFlag, [Java::boolean.java_class]).call(secure)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_cookie_secure_flag(secure)"
    end
    #  Sets whether the 'HttpOnly' flag should be set for the session cookie. When set this flag instructs browsers to
    #  prevent Javascript access to the the cookie. Used as a line of defence against the most common XSS attacks.
    # @param [true,false] httpOnly true to set the HttpOnly flag on the cookie
    # @return [self]
    def set_cookie_http_only_flag(httpOnly=nil)
      if (httpOnly.class == TrueClass || httpOnly.class == FalseClass) && !block_given?
        @j_del.java_method(:setCookieHttpOnlyFlag, [Java::boolean.java_class]).call(httpOnly)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_cookie_http_only_flag(httpOnly)"
    end
    #  Set the session cookie name
    # @param [String] sessionCookieName the session cookie name
    # @return [self]
    def set_session_cookie_name(sessionCookieName=nil)
      if sessionCookieName.class == String && !block_given?
        @j_del.java_method(:setSessionCookieName, [Java::java.lang.String.java_class]).call(sessionCookieName)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_session_cookie_name(sessionCookieName)"
    end
  end
end
