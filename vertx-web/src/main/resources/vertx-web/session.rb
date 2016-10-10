require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.Session
module VertxWeb
  class Session
    # @private
    # @param j_del [::VertxWeb::Session] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::Session] the underlying java delegate
    def j_del
      @j_del
    end
    # @return [String]
    def id
      if !block_given?
        return @j_del.java_method(:id, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling id()"
    end
    # @param [String] arg0 
    # @param [Object] arg1 
    # @return [self]
    def put(arg0=nil,arg1=nil)
      if arg0.class == String && (arg1.class == String  || arg1.class == Hash || arg1.class == Array || arg1.class == NilClass || arg1.class == TrueClass || arg1.class == FalseClass || arg1.class == Fixnum || arg1.class == Float) && !block_given?
        @j_del.java_method(:put, [Java::java.lang.String.java_class,Java::java.lang.Object.java_class]).call(arg0,::Vertx::Util::Utils.to_object(arg1))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling put(arg0,arg1)"
    end
    # @param [String] arg0 
    # @return [Object]
    def get(arg0=nil)
      if arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.from_object(@j_del.java_method(:get, [Java::java.lang.String.java_class]).call(arg0))
      end
      raise ArgumentError, "Invalid arguments when calling get(arg0)"
    end
    # @param [String] arg0 
    # @return [Object]
    def remove(arg0=nil)
      if arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.from_object(@j_del.java_method(:remove, [Java::java.lang.String.java_class]).call(arg0))
      end
      raise ArgumentError, "Invalid arguments when calling remove(arg0)"
    end
    # @return [Fixnum]
    def last_accessed
      if !block_given?
        return @j_del.java_method(:lastAccessed, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling last_accessed()"
    end
    # @return [void]
    def destroy
      if !block_given?
        return @j_del.java_method(:destroy, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling destroy()"
    end
    # @return [true,false]
    def destroyed?
      if !block_given?
        return @j_del.java_method(:isDestroyed, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling destroyed?()"
    end
    # @return [Fixnum]
    def timeout
      if !block_given?
        return @j_del.java_method(:timeout, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling timeout()"
    end
    # @return [void]
    def set_accessed
      if !block_given?
        return @j_del.java_method(:setAccessed, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling set_accessed()"
    end
  end
end
