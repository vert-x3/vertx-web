require 'vertx-apex/session_store'
require 'vertx-auth/auth_provider'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.apex.Session
module VertxApex
  #  Represents a browser session.
  #  <p>
  #  Sessions persist between HTTP requests for a single browser session. They are deleted when the browser is closed, or
  #  they time-out. Session cookies are used to maintain sessions using a secure UUID.
  #  <p>
  #  Sessions can be used to maintain data for a browser session, e.g. a shopping basket.
  #  <p>
  #  The context must have first been routed to a {::VertxApex::SessionHandler}
  #  for sessions to be available.
  class Session
    # @private
    # @param j_del [::VertxApex::Session] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxApex::Session] the underlying java delegate
    def j_del
      @j_del
    end
    #  @return The unique ID of the session. This is generated using a random secure UUID.
    # @return [String]
    def id
      if !block_given?
        return @j_del.java_method(:id, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling id()"
    end
    #  Put some data in a session
    # @param [String] key the key for the data
    # @param [Object] obj the data
    # @return [self]
    def put(key=nil,obj=nil)
      if key.class == String && (obj.class == String  || obj.class == Hash || obj.class == Array || obj.class == NilClass || obj.class == TrueClass || obj.class == FalseClass || obj.class == Fixnum || obj.class == Float) && !block_given?
        @j_del.java_method(:put, [Java::java.lang.String.java_class,Java::java.lang.Object.java_class]).call(key,::Vertx::Util::Utils.to_object(obj))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling put(key,obj)"
    end
    #  Get some data from the session
    # @param [String] key the key of the data
    # @return [Object] the data
    def get(key=nil)
      if key.class == String && !block_given?
        return ::Vertx::Util::Utils.from_object(@j_del.java_method(:get, [Java::java.lang.String.java_class]).call(key))
      end
      raise ArgumentError, "Invalid arguments when calling get(key)"
    end
    #  Remove some data from the session
    # @param [String] key the key of the data
    # @return [Object] the data that was there or null if none there
    def remove(key=nil)
      if key.class == String && !block_given?
        return ::Vertx::Util::Utils.from_object(@j_del.java_method(:remove, [Java::java.lang.String.java_class]).call(key))
      end
      raise ArgumentError, "Invalid arguments when calling remove(key)"
    end
    #  @return the time the session was last accessed
    # @return [Fixnum]
    def last_accessed
      if !block_given?
        return @j_del.java_method(:lastAccessed, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling last_accessed()"
    end
    #  Destroy the session
    # @return [void]
    def destroy
      if !block_given?
        return @j_del.java_method(:destroy, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling destroy()"
    end
    #  @return has the session been destroyed?
    # @return [true,false]
    def destroyed?
      if !block_given?
        return @j_del.java_method(:isDestroyed, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling destroyed?()"
    end
    #  @return  true if the user is logged in.
    # @return [true,false]
    def logged_in?
      if !block_given?
        return @j_del.java_method(:isLoggedIn, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling logged_in?()"
    end
    #  Set the principal (the unique user id) of the user -this signifies the user is logged in
    # @param [Hash{String => Object}] principal the principal
    # @return [void]
    def set_principal(principal=nil)
      if principal.class == Hash && !block_given?
        return @j_del.java_method(:setPrincipal, [Java::IoVertxCoreJson::JsonObject.java_class]).call(::Vertx::Util::Utils.to_json_object(principal))
      end
      raise ArgumentError, "Invalid arguments when calling set_principal(principal)"
    end
    #  Get the principal
    # @return [Hash{String => Object}] the principal or null if not logged in
    def get_principal
      if !block_given?
        return @j_del.java_method(:getPrincipal, []).call() != nil ? JSON.parse(@j_del.java_method(:getPrincipal, []).call().encode) : nil
      end
      raise ArgumentError, "Invalid arguments when calling get_principal()"
    end
    #  Does the logged in user have the specified role?  Information is cached for the lifetime of the session
    # @param [String] role the role
    # @yield will be called with a result true/false
    # @return [void]
    def has_role(role=nil)
      if role.class == String && block_given?
        return @j_del.java_method(:hasRole, [Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(role,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling has_role(role)"
    end
    #  Does the logged in user have the specified permissions?  Information is cached for the lifetime of the session
    # @param [String] permission the permission
    # @yield will be called with a result true/false
    # @return [void]
    def has_permission(permission=nil)
      if permission.class == String && block_given?
        return @j_del.java_method(:hasPermission, [Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(permission,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling has_permission(permission)"
    end
    #  Does the logged in user have the specified roles?  Information is cached for the lifetime of the session
    # @param [Set<String>] roles the roles
    # @yield will be called with a result true/false
    # @return [void]
    def has_roles(roles=nil)
      if roles.class == Set && block_given?
        return @j_del.java_method(:hasRoles, [Java::JavaUtil::Set.java_class,Java::IoVertxCore::Handler.java_class]).call(Java::JavaUtil::LinkedHashSet.new(roles.map { |element| element }),(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling has_roles(roles)"
    end
    #  Does the logged in user have the specified permissions?  Information is cached for the lifetime of the session
    # @param [Set<String>] permissions the permissions
    # @yield will be called with a result true/false
    # @return [void]
    def has_permissions(permissions=nil)
      if permissions.class == Set && block_given?
        return @j_del.java_method(:hasPermissions, [Java::JavaUtil::Set.java_class,Java::IoVertxCore::Handler.java_class]).call(Java::JavaUtil::LinkedHashSet.new(permissions.map { |element| element }),(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling has_permissions(permissions)"
    end
    #  Logout the user.
    # @return [void]
    def logout
      if !block_given?
        return @j_del.java_method(:logout, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling logout()"
    end
    #  @return the amount of time in ms, after which the session will expire, if not accessed.
    # @return [Fixnum]
    def timeout
      if !block_given?
        return @j_del.java_method(:timeout, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling timeout()"
    end
    #  @return the store for the session
    # @return [::VertxApex::SessionStore]
    def session_store
      if !block_given?
        return ::VertxApex::SessionStore.new(@j_del.java_method(:sessionStore, []).call())
      end
      raise ArgumentError, "Invalid arguments when calling session_store()"
    end
    #  Mark the session as being accessed.
    # @return [void]
    def set_accessed
      if !block_given?
        return @j_del.java_method(:setAccessed, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling set_accessed()"
    end
    #  Set the auth provider
    # @param [::VertxAuth::AuthProvider] authProvider the auth provider
    # @return [void]
    def set_auth_provider(authProvider=nil)
      if authProvider.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:setAuthProvider, [Java::IoVertxExtAuth::AuthProvider.java_class]).call(authProvider.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling set_auth_provider(authProvider)"
    end
  end
end
