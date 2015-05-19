require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.Cookie
module VertxWeb
  #  Represents an HTTP Cookie.
  #  <p>
  #  All cookies must have a name and a value and can optionally have other fields set such as path, domain, etc.
  #  <p>
  #  (Derived from io.netty.handler.codec.http.Cookie)
  class Cookie
    # @private
    # @param j_del [::VertxWeb::Cookie] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::Cookie] the underlying java delegate
    def j_del
      @j_del
    end
    #  Create a new cookie
    # @param [String] name the name of the cookie
    # @param [String] value the cookie value
    # @return [::VertxWeb::Cookie] the cookie
    def self.cookie(name=nil,value=nil)
      if name.class == String && value.class == String && !block_given?
        return ::VertxWeb::Cookie.new(Java::IoVertxExtWeb::Cookie.java_method(:cookie, [Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(name,value))
      end
      raise ArgumentError, "Invalid arguments when calling cookie(name,value)"
    end
    #  @return the name of this cookie
    # @return [String]
    def get_name
      if !block_given?
        return @j_del.java_method(:getName, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling get_name()"
    end
    #  @return the value of this cookie
    # @return [String]
    def get_value
      if !block_given?
        return @j_del.java_method(:getValue, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling get_value()"
    end
    #  Sets the value of this cookie
    # @param [String] value The value to set
    # @return [self]
    def set_value(value=nil)
      if value.class == String && !block_given?
        @j_del.java_method(:setValue, [Java::java.lang.String.java_class]).call(value)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_value(value)"
    end
    #  Sets the domain of this cookie
    # @param [String] domain The domain to use
    # @return [self]
    def set_domain(domain=nil)
      if domain.class == String && !block_given?
        @j_del.java_method(:setDomain, [Java::java.lang.String.java_class]).call(domain)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_domain(domain)"
    end
    #  @return  the domain for the cookie
    # @return [String]
    def get_domain
      if !block_given?
        return @j_del.java_method(:getDomain, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling get_domain()"
    end
    #  Sets the path of this cookie.
    # @param [String] path The path to use for this cookie
    # @return [self]
    def set_path(path=nil)
      if path.class == String && !block_given?
        @j_del.java_method(:setPath, [Java::java.lang.String.java_class]).call(path)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_path(path)"
    end
    # @return [String] the path for this cookie
    def get_path
      if !block_given?
        return @j_del.java_method(:getPath, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling get_path()"
    end
    #  Sets the maximum age of this cookie in seconds.
    #  If an age of <code>0</code> is specified, this cookie will be
    #  automatically removed by browser because it will expire immediately.
    #  If  is specified, this cookie will be removed when the
    #  browser is closed.
    #  If you don't set this the cookie will be a session cookie and be removed when the browser is closed.
    # @param [Fixnum] maxAge The maximum age of this cookie in seconds
    # @return [::VertxWeb::Cookie]
    def set_max_age(maxAge=nil)
      if maxAge.class == Fixnum && !block_given?
        return ::VertxWeb::Cookie.new(@j_del.java_method(:setMaxAge, [Java::long.java_class]).call(maxAge))
      end
      raise ArgumentError, "Invalid arguments when calling set_max_age(maxAge)"
    end
    #  Sets the security getStatus of this cookie
    # @param [true,false] secure True if this cookie is to be secure, otherwise false
    # @return [self]
    def set_secure(secure=nil)
      if (secure.class == TrueClass || secure.class == FalseClass) && !block_given?
        @j_del.java_method(:setSecure, [Java::boolean.java_class]).call(secure)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_secure(secure)"
    end
    #  Determines if this cookie is HTTP only.
    #  If set to true, this cookie cannot be accessed by a client
    #  side script. However, this works only if the browser supports it.
    #  For for information, please look
    #  <a href="http://www.owasp.org/index.php/HTTPOnly">here</a>.
    # @param [true,false] httpOnly True if the cookie is HTTP only, otherwise false.
    # @return [::VertxWeb::Cookie]
    def set_http_only(httpOnly=nil)
      if (httpOnly.class == TrueClass || httpOnly.class == FalseClass) && !block_given?
        return ::VertxWeb::Cookie.new(@j_del.java_method(:setHttpOnly, [Java::boolean.java_class]).call(httpOnly))
      end
      raise ArgumentError, "Invalid arguments when calling set_http_only(httpOnly)"
    end
    #  Set the version of the cookie
    # @param [Fixnum] version 0 or 1
    # @return [self]
    def set_version(version=nil)
      if version.class == Fixnum && !block_given?
        @j_del.java_method(:setVersion, [Java::int.java_class]).call(version)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_version(version)"
    end
    #  Encode the cookie to a string. This is what is used in the Set-Cookie header
    # @return [String] the encoded cookie
    def encode
      if !block_given?
        return @j_del.java_method(:encode, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling encode()"
    end
    #  Has the cookie been changed? Changed cookies will be saved out in the response and sent to the browser.
    # @return [true,false] true if changed
    def changed?
      if !block_given?
        return @j_del.java_method(:isChanged, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling changed?()"
    end
    #  Set the cookie as being changed. Changed will be true for a cookie just created, false by default if just
    #  read from the request
    # @param [true,false] changed true if changed
    # @return [void]
    def set_changed(changed=nil)
      if (changed.class == TrueClass || changed.class == FalseClass) && !block_given?
        return @j_del.java_method(:setChanged, [Java::boolean.java_class]).call(changed)
      end
      raise ArgumentError, "Invalid arguments when calling set_changed(changed)"
    end
  end
end
