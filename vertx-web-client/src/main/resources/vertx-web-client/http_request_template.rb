require 'vertx/buffer'
require 'vertx/read_stream'
require 'vertx-web-client/http_response_template'
require 'vertx-web-client/http_response'
require 'vertx/util/utils.rb'
# Generated from io.vertx.webclient.HttpRequestTemplate
module VertxWebClient
  #  A template for configuring client-side HTTP requests.
  #  <p>
  #  Instances are created by an  instance, via one of the methods <code>createXXX</code> corresponding to the
  #  specific HTTP methods.
  #  <p>
  #  The request template shall be configured prior making a request, the template is immutable and when a configuration method
  #  is called, a new template is returned allowing to expose the template and apply further customization.
  #  <p>
  #  After the request template has been configured, the methods
  #  <ul>
  #    <li>{::VertxWebClient::HttpRequestTemplate#send}</li>
  #    <li>{::VertxWebClient::HttpRequestTemplate#send_stream}</li>
  #    <li>{::VertxWebClient::HttpRequestTemplate#buffer_body}</li>
  #  </ul>
  #  can be called.
  #  <p>
  #  The <code>#bufferBody</code> configures the template to buffer the entire HTTP response body and returns a
  #  {::VertxWebClient::HttpResponseTemplate} for configuring the response body.
  #  <p>
  #  The <code>send</code> methods perform the actual request, they can be used multiple times to perform HTTP requests.
  class HttpRequestTemplate
    # @private
    # @param j_del [::VertxWebClient::HttpRequestTemplate] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWebClient::HttpRequestTemplate] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == HttpRequestTemplate
    end
    def @@j_api_type.wrap(obj)
      HttpRequestTemplate.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxWebclient::HttpRequestTemplate.java_class
    end
    #  Configure the template to use a new method <code>value</code>.
    # @param [:OPTIONS,:GET,:HEAD,:POST,:PUT,:DELETE,:TRACE,:CONNECT,:PATCH,:OTHER] value
    # @return [::VertxWebClient::HttpRequestTemplate] a new <code>HttpRequestTemplate</code> instance with the specified method <code>value</code>
    def method(value=nil)
      if value.class == Symbol && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:method, [Java::IoVertxCoreHttp::HttpMethod.java_class]).call(Java::IoVertxCoreHttp::HttpMethod.valueOf(value)),::VertxWebClient::HttpRequestTemplate)
      end
      raise ArgumentError, "Invalid arguments when calling method(#{value})"
    end
    #  Configure the template to use a new port <code>value</code>.
    # @param [Fixnum] value
    # @return [::VertxWebClient::HttpRequestTemplate] a new <code>HttpRequestTemplate</code> instance with the specified port <code>value</code>
    def port(value=nil)
      if value.class == Fixnum && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:port, [Java::int.java_class]).call(value),::VertxWebClient::HttpRequestTemplate)
      end
      raise ArgumentError, "Invalid arguments when calling port(#{value})"
    end
    #  Configure the template to use a new host <code>value</code>.
    # @param [String] value
    # @return [::VertxWebClient::HttpRequestTemplate] a new <code>HttpRequestTemplate</code> instance with the specified host <code>value</code>
    def host(value=nil)
      if value.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:host, [Java::java.lang.String.java_class]).call(value),::VertxWebClient::HttpRequestTemplate)
      end
      raise ArgumentError, "Invalid arguments when calling host(#{value})"
    end
    #  Configure the template to use a new request URI <code>value</code>.
    # @param [String] value
    # @return [::VertxWebClient::HttpRequestTemplate] a new <code>HttpRequestTemplate</code> instance with the specified request URI <code>value</code>
    def request_uri(value=nil)
      if value.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:uri, [Java::java.lang.String.java_class]).call(value), ::VertxWebClient::HttpRequestTemplate)
      end
      raise ArgumentError, "Invalid arguments when calling request_uri(#{value})"
    end
    #  Configure the template to add a new HTTP header.
    # @param [String] name the header name
    # @param [String] value the header value
    # @return [::VertxWebClient::HttpRequestTemplate] a new <code>HttpRequestTemplate</code> instance with the specified header
    def put_header(name=nil,value=nil)
      if name.class == String && value.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:putHeader, [Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(name,value),::VertxWebClient::HttpRequestTemplate)
      end
      raise ArgumentError, "Invalid arguments when calling put_header(#{name},#{value})"
    end
    #  Configures the amount of time in milliseconds after which if the request does not return any data within the timeout
    #  period an TimeoutException fails the request.
    #  <p>
    #  Setting zero or a negative <code>value</code> disables the timeout.
    # @param [Fixnum] value The quantity of time in milliseconds.
    # @return [::VertxWebClient::HttpRequestTemplate] a new <code>HttpRequestTemplate</code> instance with the specified timeout
    def timeout(value=nil)
      if value.class == Fixnum && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:timeout, [Java::long.java_class]).call(value),::VertxWebClient::HttpRequestTemplate)
      end
      raise ArgumentError, "Invalid arguments when calling timeout(#{value})"
    end
    #  Like {::VertxWebClient::HttpRequestTemplate#send} but with an HTTP request <code>body</code> stream.
    # @param [::Vertx::ReadStream] body the body
    # @yield
    # @return [void]
    def send_stream(body=nil)
      if body.class.method_defined?(:j_del) && block_given?
        return @j_del.java_method(:sendStream, [Java::IoVertxCoreStreams::ReadStream.java_class,Java::IoVertxCore::Handler.java_class]).call(body.j_del,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result,::VertxWebClient::HttpResponse, nil) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling send_stream(#{body})"
    end
    #  Like {::VertxWebClient::HttpRequestTemplate#send} but with an HTTP request <code>body</code> buffer.
    # @param [::Vertx::Buffer] body the body
    # @yield
    # @return [void]
    def send_buffer(body=nil)
      if body.class.method_defined?(:j_del) && block_given?
        return @j_del.java_method(:sendBuffer, [Java::IoVertxCoreBuffer::Buffer.java_class, Java::IoVertxCore::Handler.java_class]).call(body.j_del, (Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result, ::VertxWebClient::HttpResponse, nil) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling send_buffer(#{body})"
    end
    #  Like {::VertxWebClient::HttpRequestTemplate#send} but with an HTTP request <code>body</code> object encoded as json and the content type
    #  set to <code>application/json</code>.
    # @param [Object] body the body
    # @yield
    # @return [void]
    def send_json(body=nil)
      if ::Vertx::Util::unknown_type.accept?(body) && block_given?
        return @j_del.java_method(:sendJsonPOJO, [Java::java.lang.Object.java_class, Java::IoVertxCore::Handler.java_class]).call(::Vertx::Util::Utils.to_object(body), (Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result, ::VertxWebClient::HttpResponse, nil) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling send_json(#{body})"
    end
    #  Send a request, the <code>handler</code> will receive the response as an .
    # @yield
    # @return [void]
    def send
      if block_given?
        return @j_del.java_method(:send, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result,::VertxWebClient::HttpResponse, nil) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling send()"
    end
    #  Configure to buffer the body and returns a {::VertxWebClient::HttpResponseTemplate< Buffer >} for further configuration of
    #  the response or {::VertxWebClient::HttpResponseTemplate#send(Handler) sending} the request.
    # @return [::VertxWebClient::HttpResponseTemplate]
    def buffer_body
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:receiveBody, []).call(), ::VertxWebClient::HttpResponseTemplate, ::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling buffer_body()"
    end
  end
end
