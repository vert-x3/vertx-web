require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.Cookie
module VertxWeb
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
    # @param [String] name 
    # @param [String] value 
    # @return [::VertxWeb::Cookie]
    def self.cookie(name=nil,value=nil)
      if name.class == String && value.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWeb::Cookie.java_method(:cookie, [Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(name,value),::VertxWeb::Cookie)
      end
      raise ArgumentError, "Invalid arguments when calling cookie(name,value)"
    end
    # @return [String]
    def get_name
      if !block_given?
        return @j_del.java_method(:getName, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling get_name()"
    end
    # @return [String]
    def get_value
      if !block_given?
        return @j_del.java_method(:getValue, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling get_value()"
    end
    # @param [String] arg0 
    # @return [self]
    def set_value(arg0=nil)
      if arg0.class == String && !block_given?
        @j_del.java_method(:setValue, [Java::java.lang.String.java_class]).call(arg0)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_value(arg0)"
    end
    # @param [String] arg0 
    # @return [self]
    def set_domain(arg0=nil)
      if arg0.class == String && !block_given?
        @j_del.java_method(:setDomain, [Java::java.lang.String.java_class]).call(arg0)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_domain(arg0)"
    end
    # @return [String]
    def get_domain
      if !block_given?
        return @j_del.java_method(:getDomain, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling get_domain()"
    end
    # @param [String] arg0 
    # @return [self]
    def set_path(arg0=nil)
      if arg0.class == String && !block_given?
        @j_del.java_method(:setPath, [Java::java.lang.String.java_class]).call(arg0)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_path(arg0)"
    end
    # @return [String]
    def get_path
      if !block_given?
        return @j_del.java_method(:getPath, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling get_path()"
    end
    # @param [Fixnum] arg0 
    # @return [self]
    def set_max_age(arg0=nil)
      if arg0.class == Fixnum && !block_given?
        @j_del.java_method(:setMaxAge, [Java::long.java_class]).call(arg0)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_max_age(arg0)"
    end
    # @param [true,false] arg0 
    # @return [self]
    def set_secure(arg0=nil)
      if (arg0.class == TrueClass || arg0.class == FalseClass) && !block_given?
        @j_del.java_method(:setSecure, [Java::boolean.java_class]).call(arg0)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_secure(arg0)"
    end
    # @param [true,false] arg0 
    # @return [self]
    def set_http_only(arg0=nil)
      if (arg0.class == TrueClass || arg0.class == FalseClass) && !block_given?
        @j_del.java_method(:setHttpOnly, [Java::boolean.java_class]).call(arg0)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_http_only(arg0)"
    end
    # @return [String]
    def encode
      if !block_given?
        return @j_del.java_method(:encode, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling encode()"
    end
    # @return [true,false]
    def changed?
      if !block_given?
        return @j_del.java_method(:isChanged, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling changed?()"
    end
    # @param [true,false] arg0 
    # @return [void]
    def set_changed(arg0=nil)
      if (arg0.class == TrueClass || arg0.class == FalseClass) && !block_given?
        return @j_del.java_method(:setChanged, [Java::boolean.java_class]).call(arg0)
      end
      raise ArgumentError, "Invalid arguments when calling set_changed(arg0)"
    end
  end
end
