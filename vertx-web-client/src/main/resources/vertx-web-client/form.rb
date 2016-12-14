require 'vertx/buffer'
require 'vertx/read_stream'
require 'vertx/multi_map'
require 'vertx/util/utils.rb'
# Generated from io.vertx.webclient.Form
module VertxWebClient
  class Form
    # @private
    # @param j_del [::VertxWebClient::Form] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWebClient::Form] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == Form
    end
    def @@j_api_type.wrap(obj)
      Form.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxWebclient::Form.java_class
    end
    # @return [::VertxWebClient::Form]
    def self.create
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxWebclient::Form.java_method(:create, []).call(), ::VertxWebClient::Form)
      end
      raise ArgumentError, "Invalid arguments when calling create()"
    end
    # @return [::VertxWebClient::Form]
    def self.multipart
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxWebclient::Form.java_method(:multipart, []).call(),::VertxWebClient::Form)
      end
      raise ArgumentError, "Invalid arguments when calling multipart()"
    end
    #  Adds a new value with the specified name and value.
    # @param [String] name The name
    # @param [String] value The value being added
    # @return [self]
    def add_attr(name=nil,value=nil)
      if name.class == String && value.class == String && !block_given?
        @j_del.java_method(:addAttr, [Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(name,value)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling add_attr(#{name},#{value})"
    end
    # @param [String] name
    # @param [::Vertx::ReadStream] file
    # @return [self]
    def add_file(name=nil,file=nil)
      if name.class == String && file.class.method_defined?(:j_del) && !block_given?
        @j_del.java_method(:addFile, [Java::java.lang.String.java_class,Java::IoVertxCoreStreams::ReadStream.java_class]).call(name,file.j_del)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling add_file(#{name},#{file})"
    end
    #  Adds all the entries from another MultiMap to this one
    # @param [::Vertx::MultiMap] map
    # @return [self]
    def add_all(map=nil)
      if map.class.method_defined?(:j_del) && !block_given?
        @j_del.java_method(:addAll, [Java::IoVertxCore::MultiMap.java_class]).call(map.j_del)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling add_all(#{map})"
    end
    #  Sets a value under the specified name.
    #  <p>
    #  If there is an existing header with the same name, it is removed.
    # @param [String] name The name
    # @param [String] value The value
    # @return [self]
    def set(name=nil,value=nil)
      if name.class == String && value.class == String && !block_given?
        @j_del.java_method(:set, [Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(name,value)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set(#{name},#{value})"
    end
    #  Cleans this instance.
    # @param [::Vertx::MultiMap] map
    # @return [self]
    def set_all(map=nil)
      if map.class.method_defined?(:j_del) && !block_given?
        @j_del.java_method(:setAll, [Java::IoVertxCore::MultiMap.java_class]).call(map.j_del)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_all(#{map})"
    end
    # @return [::Vertx::ReadStream]
    def bilto
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:bilto, []).call(),::Vertx::ReadStreamImpl,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling bilto()"
    end
  end
end
