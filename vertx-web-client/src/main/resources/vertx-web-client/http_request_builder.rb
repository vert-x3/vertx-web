require 'vertx/buffer'
require 'vertx-web-client/payload_codec'
require 'vertx/read_stream'
require 'vertx-web-client/http_response'
require 'vertx/util/utils.rb'
# Generated from io.vertx.webclient.HttpRequestBuilder
module VertxWebClient
  #  A builder for configuring client-side HTTP requests.
  #  <p>
  #  Instances are created by an  instance, via one of the methods <code>createXXX</code> corresponding to the
  #  specific HTTP methods.
  #  <p>
  #  The request builder shall be configured prior making a request, the builder is immutable and when a configuration method
  #  is called, a new builder is returned allowing to expose the builder and apply further customization.
  #  <p>
  #  After the request builder has been configured, the methods
  #  <ul>
  #    <li>{::VertxWebClient::HttpRequestBuilder#send}</li>
  #    <li>{::VertxWebClient::HttpRequestBuilder#send_stream}</li>
  #    <li></li>
  #  </ul>
  #  can be called.
  #  <p>
  #  The <code>#bufferBody</code> configures the builder to buffer the entire HTTP response body and returns a
  #  {::VertxWebClient::PayloadCodec} for configuring the response body.
  #  <p>
  #  The <code>send</code> methods perform the actual request, they can be used multiple times to perform HTTP requests.
  class HttpRequestBuilder
    # @private
    # @param j_del [::VertxWebClient::HttpRequestBuilder] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWebClient::HttpRequestBuilder] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == HttpRequestBuilder
    end
    def @@j_api_type.wrap(obj)
      HttpRequestBuilder.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxWebclient::HttpRequestBuilder.java_class
    end
    #  Configure the builder to use a new method <code>value</code>.
    # @param [:OPTIONS,:GET,:HEAD,:POST,:PUT,:DELETE,:TRACE,:CONNECT,:PATCH,:OTHER] value
    # @return [::VertxWebClient::HttpRequestBuilder] a new <code>HttpRequestBuilder</code> instance with the specified method <code>value</code>
    def method(value=nil)
      if value.class == Symbol && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:method, [Java::IoVertxCoreHttp::HttpMethod.java_class]).call(Java::IoVertxCoreHttp::HttpMethod.valueOf(value)),::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling method(#{value})"
    end
    #  Configure the builder to use a new port <code>value</code>.
    # @param [Fixnum] value
    # @return [::VertxWebClient::HttpRequestBuilder] a new <code>HttpRequestBuilder</code> instance with the specified port <code>value</code>
    def port(value=nil)
      if value.class == Fixnum && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:port, [Java::int.java_class]).call(value),::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling port(#{value})"
    end
    #  Configure the builder to use a new host <code>value</code>.
    # @param [String] value
    # @return [::VertxWebClient::HttpRequestBuilder] a new <code>HttpRequestBuilder</code> instance with the specified host <code>value</code>
    def host(value=nil)
      if value.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:host, [Java::java.lang.String.java_class]).call(value),::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling host(#{value})"
    end
    #  Configure the builder to use a new request URI <code>value</code>.
    # @param [String] value
    # @return [::VertxWebClient::HttpRequestBuilder] a new <code>HttpRequestBuilder</code> instance with the specified request URI <code>value</code>
    def request_uri(value=nil)
      if value.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:uri, [Java::java.lang.String.java_class]).call(value), ::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling request_uri(#{value})"
    end
    #  Configure the builder to add a new HTTP header.
    # @param [String] name the header name
    # @param [String] value the header value
    # @return [::VertxWebClient::HttpRequestBuilder] a new <code>HttpRequestBuilder</code> instance with the specified header
    def put_header(name=nil,value=nil)
      if name.class == String && value.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:putHeader, [Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(name,value),::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling put_header(#{name},#{value})"
    end
    #  Configures the amount of time in milliseconds after which if the request does not return any data within the timeout
    #  period an TimeoutException fails the request.
    #  <p>
    #  Setting zero or a negative <code>value</code> disables the timeout.
    # @param [Fixnum] value The quantity of time in milliseconds.
    # @return [::VertxWebClient::HttpRequestBuilder] a new <code>HttpRequestBuilder</code> instance with the specified timeout
    def timeout(value=nil)
      if value.class == Fixnum && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:timeout, [Java::long.java_class]).call(value),::VertxWebClient::HttpRequestBuilder)
      end
      raise ArgumentError, "Invalid arguments when calling timeout(#{value})"
    end
    #  Like {::VertxWebClient::HttpRequestBuilder#send} but with an HTTP request <code>body</code> stream.
    # @param [::Vertx::ReadStream] body the body
    # @yield
    # @return [void]
    def send_stream(body=nil)
      if body.class.method_defined?(:j_del) && block_given?
        return @j_del.java_method(:sendStream, [Java::IoVertxCoreStreams::ReadStream.java_class,Java::IoVertxCore::Handler.java_class]).call(body.j_del,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result,::VertxWebClient::HttpResponse,::Vertx::Buffer.j_api_type) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling send_stream(#{body})"
    end
    #  Like {::VertxWebClient::HttpRequestBuilder#send} but with an HTTP request <code>body</code> buffer.
    # @param [::Vertx::Buffer] body the body
    # @yield
    # @return [void]
    def send_buffer(body=nil)
      if body.class.method_defined?(:j_del) && block_given?
        return @j_del.java_method(:sendBuffer, [Java::IoVertxCoreBuffer::Buffer.java_class, Java::IoVertxCore::Handler.java_class]).call(body.j_del, (Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result, ::VertxWebClient::HttpResponse, ::Vertx::Buffer.j_api_type) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling send_buffer(#{body})"
    end
    #  Like {::VertxWebClient::HttpRequestBuilder#send} but with an HTTP request <code>body</code> object encoded as json and the content type
    #  set to <code>application/json</code>.
    # @param [Object] body the body
    # @yield
    # @return [void]
    def send_json(body=nil)
      if ::Vertx::Util::unknown_type.accept?(body) && block_given?
        return @j_del.java_method(:sendJson, [Java::java.lang.Object.java_class,Java::IoVertxCore::Handler.java_class]).call(::Vertx::Util::Utils.to_object(body),(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result,::VertxWebClient::HttpResponse,::Vertx::Buffer.j_api_type) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling send_json(#{body})"
    end
    #  Send a request, the <code>handler</code> will receive the response as an .
    # @param [::VertxWebClient::PayloadCodec] codec
    # @yield
    # @return [void]
    def send(codec=nil)
      if block_given? && codec == nil
        return @j_del.java_method(:send, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result,::VertxWebClient::HttpResponse,::Vertx::Buffer.j_api_type) : nil) }))
      elsif codec.class.method_defined?(:j_del) && block_given?
        return @j_del.java_method(:send, [Java::IoVertxWebclient::PayloadCodec.java_class,Java::IoVertxCore::Handler.java_class]).call(codec.j_del,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result,::VertxWebClient::HttpResponse, nil) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling send(#{codec})"
    end
  end
end
