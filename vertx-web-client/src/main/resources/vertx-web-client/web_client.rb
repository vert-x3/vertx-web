require 'vertx-web-client/http_request_builder'
require 'vertx/http_client'
require 'vertx/util/utils.rb'
# Generated from io.vertx.webclient.WebClient
module VertxWebClient
  class WebClient
    # @private
    # @param j_del [::VertxWebClient::WebClient] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWebClient::WebClient] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == WebClient
    end
    def @@j_api_type.wrap(obj)
      WebClient.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxWebclient::WebClient.java_class
    end
    # @param [::Vertx::HttpClient] client 
    # @return [::VertxWebClient::WebClient]
    def self.create(client=nil)
      if client.class.method_defined?(:j_del) && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxWebclient::WebClient.java_method(:create, [Java::IoVertxCoreHttp::HttpClient.java_class]).call(client.j_del),::VertxWebClient::WebClient)
      end
      raise ArgumentError, "Invalid arguments when calling create(#{client})"
    end
    # @param [Fixnum] port 
    # @param [String] host 
    # @param [String] requestURI 
    # @return [::VertxWebClient::HttpRequestBuilder]
    def get(port=nil,host=nil,requestURI=nil)
      if port.class == Fixnum && host.class == String && requestURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:get, [Java::int.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(port,host,requestURI),::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling get(#{port},#{host},#{requestURI})"
    end
    # @param [Fixnum] port 
    # @param [String] host 
    # @param [String] requestURI 
    # @return [::VertxWebClient::HttpRequestBuilder]
    def post(port=nil,host=nil,requestURI=nil)
      if port.class == Fixnum && host.class == String && requestURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:post, [Java::int.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(port,host,requestURI),::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling post(#{port},#{host},#{requestURI})"
    end
    # @param [Fixnum] port 
    # @param [String] host 
    # @param [String] requestURI 
    # @return [::VertxWebClient::HttpRequestBuilder]
    def put(port=nil,host=nil,requestURI=nil)
      if port.class == Fixnum && host.class == String && requestURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:put, [Java::int.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(port,host,requestURI),::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling put(#{port},#{host},#{requestURI})"
    end
    # @param [Fixnum] port 
    # @param [String] host 
    # @param [String] requestURI 
    # @return [::VertxWebClient::HttpRequestBuilder]
    def delete(port=nil,host=nil,requestURI=nil)
      if port.class == Fixnum && host.class == String && requestURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:delete, [Java::int.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(port,host,requestURI),::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling delete(#{port},#{host},#{requestURI})"
    end
    # @param [Fixnum] port 
    # @param [String] host 
    # @param [String] requestURI 
    # @return [::VertxWebClient::HttpRequestBuilder]
    def patch(port=nil,host=nil,requestURI=nil)
      if port.class == Fixnum && host.class == String && requestURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:patch, [Java::int.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(port,host,requestURI),::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling patch(#{port},#{host},#{requestURI})"
    end
    # @param [Fixnum] port 
    # @param [String] host 
    # @param [String] requestURI 
    # @return [::VertxWebClient::HttpRequestBuilder]
    def head(port=nil,host=nil,requestURI=nil)
      if port.class == Fixnum && host.class == String && requestURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:head, [Java::int.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(port,host,requestURI),::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling head(#{port},#{host},#{requestURI})"
    end
    # @param [String] absoluteURI 
    # @return [::VertxWebClient::HttpRequestBuilder]
    def get_abs(absoluteURI=nil)
      if absoluteURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:getAbs, [Java::java.lang.String.java_class]).call(absoluteURI),::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling get_abs(#{absoluteURI})"
    end
    # @param [String] absoluteURI 
    # @return [::VertxWebClient::HttpRequestBuilder]
    def post_abs(absoluteURI=nil)
      if absoluteURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:postAbs, [Java::java.lang.String.java_class]).call(absoluteURI),::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling post_abs(#{absoluteURI})"
    end
    # @param [String] absoluteURI 
    # @return [::VertxWebClient::HttpRequestBuilder]
    def put_abs(absoluteURI=nil)
      if absoluteURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:putAbs, [Java::java.lang.String.java_class]).call(absoluteURI),::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling put_abs(#{absoluteURI})"
    end
    # @param [String] absoluteURI 
    # @return [::VertxWebClient::HttpRequestBuilder]
    def delete_abs(absoluteURI=nil)
      if absoluteURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:deleteAbs, [Java::java.lang.String.java_class]).call(absoluteURI),::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling delete_abs(#{absoluteURI})"
    end
    # @param [String] absoluteURI 
    # @return [::VertxWebClient::HttpRequestBuilder]
    def patch_abs(absoluteURI=nil)
      if absoluteURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:patchAbs, [Java::java.lang.String.java_class]).call(absoluteURI),::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling patch_abs(#{absoluteURI})"
    end
    # @param [String] absoluteURI 
    # @return [::VertxWebClient::HttpRequestBuilder]
    def head_abs(absoluteURI=nil)
      if absoluteURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:headAbs, [Java::java.lang.String.java_class]).call(absoluteURI),::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling head_abs(#{absoluteURI})"
    end
  end
end
