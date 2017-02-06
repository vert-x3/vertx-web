require 'vertx/buffer'
require 'vertx/vertx'
require 'vertx/http_client'
require 'vertx-web-client/http_request'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.client.WebClient
module VertxWebClient
  #  An asynchronous HTTP / HTTP/2 client called <code>WebClient</code>.
  #  <p>
  #  The web client makes easy to do HTTP request/response interactions with a web server, and provides advanced
  #  features like:
  #  <ul>
  #    <li>Json body encoding / decoding</li>
  #    <li>request/response pumping</li>
  #    <li>error handling</li>
  #  </ul>
  #  <p>
  #  The web client does not deprecate the , it is actually based on it and therefore inherits
  #  its configuration and great features like pooling. The <code>HttpClient</code> should be used when fine grained control over the HTTP
  #  requests/response is necessary.
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
      Java::IoVertxExtWebClient::WebClient.java_class
    end
    #  Create a web client using the provided <code>vertx</code> instance.
    # @param [::Vertx::Vertx] vertx the vertx instance
    # @param [Hash] options the Web Client options
    # @return [::VertxWebClient::WebClient] the created web client
    def self.create(vertx=nil,options=nil)
      if vertx.class.method_defined?(:j_del) && !block_given? && options == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebClient::WebClient.java_method(:create, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del),::VertxWebClient::WebClient)
      elsif vertx.class.method_defined?(:j_del) && options.class == Hash && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebClient::WebClient.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::IoVertxExtWebClient::WebClientOptions.java_class]).call(vertx.j_del,Java::IoVertxExtWebClient::WebClientOptions.new(::Vertx::Util::Utils.to_json_object(options))),::VertxWebClient::WebClient)
      end
      raise ArgumentError, "Invalid arguments when calling create(#{vertx},#{options})"
    end
    #  Wrap an <code>httpClient</code> with a web client and default options.
    #  <p>
    #  Only the specific web client portion of the <code>options</code> is used, the {Hash}
    #  of the <code>httpClient</code> is reused.
    # @param [::Vertx::HttpClient] httpClient the  to wrap
    # @param [Hash] options the Web Client options
    # @return [::VertxWebClient::WebClient] the web client
    def self.wrap(httpClient=nil,options=nil)
      if httpClient.class.method_defined?(:j_del) && !block_given? && options == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebClient::WebClient.java_method(:wrap, [Java::IoVertxCoreHttp::HttpClient.java_class]).call(httpClient.j_del),::VertxWebClient::WebClient)
      elsif httpClient.class.method_defined?(:j_del) && options.class == Hash && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebClient::WebClient.java_method(:wrap, [Java::IoVertxCoreHttp::HttpClient.java_class,Java::IoVertxExtWebClient::WebClientOptions.java_class]).call(httpClient.j_del,Java::IoVertxExtWebClient::WebClientOptions.new(::Vertx::Util::Utils.to_json_object(options))),::VertxWebClient::WebClient)
      end
      raise ArgumentError, "Invalid arguments when calling wrap(#{httpClient},#{options})"
    end
    #  Create an HTTP request to send to the server at the specified host and port.
    # @overload request(method,requestURI)
    #   @param [:OPTIONS,:GET,:HEAD,:POST,:PUT,:DELETE,:TRACE,:CONNECT,:PATCH,:OTHER] method the HTTP method
    #   @param [String] requestURI the relative URI
    # @overload request(method,options)
    #   @param [:OPTIONS,:GET,:HEAD,:POST,:PUT,:DELETE,:TRACE,:CONNECT,:PATCH,:OTHER] method the HTTP method
    #   @param [Hash] options the request options
    # @overload request(method,host,requestURI)
    #   @param [:OPTIONS,:GET,:HEAD,:POST,:PUT,:DELETE,:TRACE,:CONNECT,:PATCH,:OTHER] method the HTTP method
    #   @param [String] host the host
    #   @param [String] requestURI the relative URI
    # @overload request(method,port,host,requestURI)
    #   @param [:OPTIONS,:GET,:HEAD,:POST,:PUT,:DELETE,:TRACE,:CONNECT,:PATCH,:OTHER] method the HTTP method
    #   @param [Fixnum] port the port
    #   @param [String] host the host
    #   @param [String] requestURI the relative URI
    # @return [::VertxWebClient::HttpRequest] an HTTP client request object
    def request(param_1=nil,param_2=nil,param_3=nil,param_4=nil)
      if param_1.class == Symbol && param_2.class == String && !block_given? && param_3 == nil && param_4 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:request, [Java::IoVertxCoreHttp::HttpMethod.java_class,Java::java.lang.String.java_class]).call(Java::IoVertxCoreHttp::HttpMethod.valueOf(param_1.to_s),param_2),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      elsif param_1.class == Symbol && param_2.class == Hash && !block_given? && param_3 == nil && param_4 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:request, [Java::IoVertxCoreHttp::HttpMethod.java_class,Java::IoVertxCoreHttp::RequestOptions.java_class]).call(Java::IoVertxCoreHttp::HttpMethod.valueOf(param_1.to_s),Java::IoVertxCoreHttp::RequestOptions.new(::Vertx::Util::Utils.to_json_object(param_2))),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      elsif param_1.class == Symbol && param_2.class == String && param_3.class == String && !block_given? && param_4 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:request, [Java::IoVertxCoreHttp::HttpMethod.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(Java::IoVertxCoreHttp::HttpMethod.valueOf(param_1.to_s),param_2,param_3),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      elsif param_1.class == Symbol && param_2.class == Fixnum && param_3.class == String && param_4.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:request, [Java::IoVertxCoreHttp::HttpMethod.java_class,Java::int.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(Java::IoVertxCoreHttp::HttpMethod.valueOf(param_1.to_s),param_2,param_3,param_4),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling request(#{param_1},#{param_2},#{param_3},#{param_4})"
    end
    #  Create an HTTP request to send to the server using an absolute URI
    # @param [:OPTIONS,:GET,:HEAD,:POST,:PUT,:DELETE,:TRACE,:CONNECT,:PATCH,:OTHER] method the HTTP method
    # @param [String] absoluteURI the absolute URI
    # @return [::VertxWebClient::HttpRequest] an HTTP client request object
    def request_abs(method=nil,absoluteURI=nil)
      if method.class == Symbol && absoluteURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:requestAbs, [Java::IoVertxCoreHttp::HttpMethod.java_class,Java::java.lang.String.java_class]).call(Java::IoVertxCoreHttp::HttpMethod.valueOf(method.to_s),absoluteURI),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling request_abs(#{method},#{absoluteURI})"
    end
    #  Create an HTTP GET request to send to the server at the specified host and port.
    # @overload get(requestURI)
    #   @param [String] requestURI the relative URI
    # @overload get(host,requestURI)
    #   @param [String] host the host
    #   @param [String] requestURI the relative URI
    # @overload get(port,host,requestURI)
    #   @param [Fixnum] port the port
    #   @param [String] host the host
    #   @param [String] requestURI the relative URI
    # @return [::VertxWebClient::HttpRequest] an HTTP client request object
    def get(param_1=nil,param_2=nil,param_3=nil)
      if param_1.class == String && !block_given? && param_2 == nil && param_3 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:get, [Java::java.lang.String.java_class]).call(param_1),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      elsif param_1.class == String && param_2.class == String && !block_given? && param_3 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:get, [Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(param_1,param_2),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      elsif param_1.class == Fixnum && param_2.class == String && param_3.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:get, [Java::int.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(param_1,param_2,param_3),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling get(#{param_1},#{param_2},#{param_3})"
    end
    #  Create an HTTP GET request to send to the server using an absolute URI, specifying a response handler to receive
    #  the response
    # @param [String] absoluteURI the absolute URI
    # @return [::VertxWebClient::HttpRequest] an HTTP client request object
    def get_abs(absoluteURI=nil)
      if absoluteURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:getAbs, [Java::java.lang.String.java_class]).call(absoluteURI),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling get_abs(#{absoluteURI})"
    end
    #  Create an HTTP POST request to send to the server at the specified host and port.
    # @overload post(requestURI)
    #   @param [String] requestURI the relative URI
    # @overload post(host,requestURI)
    #   @param [String] host the host
    #   @param [String] requestURI the relative URI
    # @overload post(port,host,requestURI)
    #   @param [Fixnum] port the port
    #   @param [String] host the host
    #   @param [String] requestURI the relative URI
    # @return [::VertxWebClient::HttpRequest] an HTTP client request object
    def post(param_1=nil,param_2=nil,param_3=nil)
      if param_1.class == String && !block_given? && param_2 == nil && param_3 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:post, [Java::java.lang.String.java_class]).call(param_1),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      elsif param_1.class == String && param_2.class == String && !block_given? && param_3 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:post, [Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(param_1,param_2),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      elsif param_1.class == Fixnum && param_2.class == String && param_3.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:post, [Java::int.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(param_1,param_2,param_3),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling post(#{param_1},#{param_2},#{param_3})"
    end
    #  Create an HTTP POST request to send to the server using an absolute URI, specifying a response handler to receive
    #  the response
    # @param [String] absoluteURI the absolute URI
    # @return [::VertxWebClient::HttpRequest] an HTTP client request object
    def post_abs(absoluteURI=nil)
      if absoluteURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:postAbs, [Java::java.lang.String.java_class]).call(absoluteURI),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling post_abs(#{absoluteURI})"
    end
    #  Create an HTTP PUT request to send to the server at the specified host and port.
    # @overload put(requestURI)
    #   @param [String] requestURI the relative URI
    # @overload put(host,requestURI)
    #   @param [String] host the host
    #   @param [String] requestURI the relative URI
    # @overload put(port,host,requestURI)
    #   @param [Fixnum] port the port
    #   @param [String] host the host
    #   @param [String] requestURI the relative URI
    # @return [::VertxWebClient::HttpRequest] an HTTP client request object
    def put(param_1=nil,param_2=nil,param_3=nil)
      if param_1.class == String && !block_given? && param_2 == nil && param_3 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:put, [Java::java.lang.String.java_class]).call(param_1),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      elsif param_1.class == String && param_2.class == String && !block_given? && param_3 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:put, [Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(param_1,param_2),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      elsif param_1.class == Fixnum && param_2.class == String && param_3.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:put, [Java::int.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(param_1,param_2,param_3),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling put(#{param_1},#{param_2},#{param_3})"
    end
    #  Create an HTTP PUT request to send to the server using an absolute URI, specifying a response handler to receive
    #  the response
    # @param [String] absoluteURI the absolute URI
    # @return [::VertxWebClient::HttpRequest] an HTTP client request object
    def put_abs(absoluteURI=nil)
      if absoluteURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:putAbs, [Java::java.lang.String.java_class]).call(absoluteURI),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling put_abs(#{absoluteURI})"
    end
    #  Create an HTTP DELETE request to send to the server at the specified host and port.
    # @overload delete(requestURI)
    #   @param [String] requestURI the relative URI
    # @overload delete(host,requestURI)
    #   @param [String] host the host
    #   @param [String] requestURI the relative URI
    # @overload delete(port,host,requestURI)
    #   @param [Fixnum] port the port
    #   @param [String] host the host
    #   @param [String] requestURI the relative URI
    # @return [::VertxWebClient::HttpRequest] an HTTP client request object
    def delete(param_1=nil,param_2=nil,param_3=nil)
      if param_1.class == String && !block_given? && param_2 == nil && param_3 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:delete, [Java::java.lang.String.java_class]).call(param_1),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      elsif param_1.class == String && param_2.class == String && !block_given? && param_3 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:delete, [Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(param_1,param_2),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      elsif param_1.class == Fixnum && param_2.class == String && param_3.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:delete, [Java::int.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(param_1,param_2,param_3),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling delete(#{param_1},#{param_2},#{param_3})"
    end
    #  Create an HTTP DELETE request to send to the server using an absolute URI, specifying a response handler to receive
    #  the response
    # @param [String] absoluteURI the absolute URI
    # @return [::VertxWebClient::HttpRequest] an HTTP client request object
    def delete_abs(absoluteURI=nil)
      if absoluteURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:deleteAbs, [Java::java.lang.String.java_class]).call(absoluteURI),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling delete_abs(#{absoluteURI})"
    end
    #  Create an HTTP PATCH request to send to the server at the specified host and port.
    # @overload patch(requestURI)
    #   @param [String] requestURI the relative URI
    # @overload patch(host,requestURI)
    #   @param [String] host the host
    #   @param [String] requestURI the relative URI
    # @overload patch(port,host,requestURI)
    #   @param [Fixnum] port the port
    #   @param [String] host the host
    #   @param [String] requestURI the relative URI
    # @return [::VertxWebClient::HttpRequest] an HTTP client request object
    def patch(param_1=nil,param_2=nil,param_3=nil)
      if param_1.class == String && !block_given? && param_2 == nil && param_3 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:patch, [Java::java.lang.String.java_class]).call(param_1),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      elsif param_1.class == String && param_2.class == String && !block_given? && param_3 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:patch, [Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(param_1,param_2),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      elsif param_1.class == Fixnum && param_2.class == String && param_3.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:patch, [Java::int.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(param_1,param_2,param_3),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling patch(#{param_1},#{param_2},#{param_3})"
    end
    #  Create an HTTP PATCH request to send to the server using an absolute URI, specifying a response handler to receive
    #  the response
    # @param [String] absoluteURI the absolute URI
    # @return [::VertxWebClient::HttpRequest] an HTTP client request object
    def patch_abs(absoluteURI=nil)
      if absoluteURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:patchAbs, [Java::java.lang.String.java_class]).call(absoluteURI),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling patch_abs(#{absoluteURI})"
    end
    #  Create an HTTP HEAD request to send to the server at the specified host and port.
    # @overload head(requestURI)
    #   @param [String] requestURI the relative URI
    # @overload head(host,requestURI)
    #   @param [String] host the host
    #   @param [String] requestURI the relative URI
    # @overload head(port,host,requestURI)
    #   @param [Fixnum] port the port
    #   @param [String] host the host
    #   @param [String] requestURI the relative URI
    # @return [::VertxWebClient::HttpRequest] an HTTP client request object
    def head(param_1=nil,param_2=nil,param_3=nil)
      if param_1.class == String && !block_given? && param_2 == nil && param_3 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:head, [Java::java.lang.String.java_class]).call(param_1),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      elsif param_1.class == String && param_2.class == String && !block_given? && param_3 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:head, [Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(param_1,param_2),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      elsif param_1.class == Fixnum && param_2.class == String && param_3.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:head, [Java::int.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(param_1,param_2,param_3),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling head(#{param_1},#{param_2},#{param_3})"
    end
    #  Create an HTTP HEAD request to send to the server using an absolute URI, specifying a response handler to receive
    #  the response
    # @param [String] absoluteURI the absolute URI
    # @return [::VertxWebClient::HttpRequest] an HTTP client request object
    def head_abs(absoluteURI=nil)
      if absoluteURI.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:headAbs, [Java::java.lang.String.java_class]).call(absoluteURI),::VertxWebClient::HttpRequest,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling head_abs(#{absoluteURI})"
    end
    #  Close the client. Closing will close down any pooled connections.
    #  Clients should always be closed after use.
    # @return [void]
    def close
      if !block_given?
        return @j_del.java_method(:close, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling close()"
    end
  end
end
