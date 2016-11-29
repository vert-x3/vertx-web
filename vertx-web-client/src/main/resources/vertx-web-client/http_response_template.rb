require 'vertx/buffer'
require 'vertx/read_stream'
require 'vertx-web-client/http_response'
require 'vertx/util/utils.rb'
# Generated from io.vertx.webclient.HttpResponseTemplate
module VertxWebClient
  #  A template for configuring client-side HTTP responses.
  class HttpResponseTemplate
    # @private
    # @param j_del [::VertxWebClient::HttpResponseTemplate] the java delegate
    def initialize(j_del, j_arg_T=nil)
      @j_del = j_del
      @j_arg_T = j_arg_T != nil ? j_arg_T : ::Vertx::Util::unknown_type
    end
    # @private
    # @return [::VertxWebClient::HttpResponseTemplate] the underlying java delegate
    def j_del
      @j_del
    end
    #  Send a request, the <code>handler</code> will receive the response as an {::VertxWebClient::HttpResponse}.
    # @yield 
    # @return [void]
    def send
      if block_given?
        return @j_del.java_method(:send, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result,::VertxWebClient::HttpResponse, nil) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling send()"
    end
    #  Like {::VertxWebClient::HttpResponseTemplate#send} but with an HTTP request <code>body</code> stream.
    # @param [::Vertx::ReadStream] body the body
    # @yield 
    # @return [void]
    def send_stream(body=nil)
      if body.class.method_defined?(:j_del) && block_given?
        return @j_del.java_method(:sendStream, [Java::IoVertxCoreStreams::ReadStream.java_class,Java::IoVertxCore::Handler.java_class]).call(body.j_del,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result,::VertxWebClient::HttpResponse, nil) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling send_stream(#{body})"
    end
    #  Like {::VertxWebClient::HttpResponseTemplate#send} but with an HTTP request <code>body</code> buffer.
    # @param [::Vertx::Buffer] body the body
    # @yield 
    # @return [void]
    def send_buffer(body=nil)
      if body.class.method_defined?(:j_del) && block_given?
        return @j_del.java_method(:sendBuffer, [Java::IoVertxCoreBuffer::Buffer.java_class,Java::IoVertxCore::Handler.java_class]).call(body.j_del,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result,::VertxWebClient::HttpResponse, nil) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling send_buffer(#{body})"
    end
    #  Like {::VertxWebClient::HttpResponseTemplate#send} but with an HTTP request <code>body</code> json and the content type
    #  set to <code>application/json</code>.
    # @param [Object] body the body
    # @yield 
    # @return [void]
    def send_json(body=nil)
      if ::Vertx::Util::unknown_type.accept?(body) && block_given?
        return @j_del.java_method(:sendJson, [Java::java.lang.Object.java_class,Java::IoVertxCore::Handler.java_class]).call(::Vertx::Util::Utils.to_object(body),(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result,::VertxWebClient::HttpResponse, nil) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling send_json(#{body})"
    end
    #  Like {::VertxWebClient::HttpResponseTemplate#as_string} but with the specified <code>encoding</code> param.
    # @param [String] encoding 
    # @return [::VertxWebClient::HttpResponseTemplate]
    def as_string(encoding=nil)
      if !block_given? && encoding == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:asString, []).call(),::VertxWebClient::HttpResponseTemplate, nil)
      elsif encoding.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:asString, [Java::java.lang.String.java_class]).call(encoding),::VertxWebClient::HttpResponseTemplate, nil)
      end
      raise ArgumentError, "Invalid arguments when calling as_string(#{encoding})"
    end
    #  Configure the template to decode the response as a Json object.
    # @return [::VertxWebClient::HttpResponseTemplate] a new <code>HttpResponseTemplate</code> instance decoding the response as a Json object
    def as_json_object
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:asJsonObject, []).call(),::VertxWebClient::HttpResponseTemplate, nil)
      end
      raise ArgumentError, "Invalid arguments when calling as_json_object()"
    end
  end
end
