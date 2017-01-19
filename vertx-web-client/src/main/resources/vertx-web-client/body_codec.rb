require 'vertx/buffer'
require 'vertx/write_stream'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.client.BodyCodec
module VertxWebClient
  #  A codec for encoding and decoding HTTP bodies.
  class BodyCodec
    # @private
    # @param j_del [::VertxWebClient::BodyCodec] the java delegate
    def initialize(j_del, j_arg_T=nil)
      @j_del = j_del
      @j_arg_T = j_arg_T != nil ? j_arg_T : ::Vertx::Util::unknown_type
    end
    # @private
    # @return [::VertxWebClient::BodyCodec] the underlying java delegate
    def j_del
      @j_del
    end
    #  A codec for strings using a specific <code>encoding</code>.
    # @param [String] encoding the encoding
    # @return [::VertxWebClient::BodyCodec] the codec
    def self.string(encoding=nil)
      if !block_given? && encoding == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebClient::BodyCodec.java_method(:string, []).call(),::VertxWebClient::BodyCodec, nil)
      elsif encoding.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebClient::BodyCodec.java_method(:string, [Java::java.lang.String.java_class]).call(encoding),::VertxWebClient::BodyCodec, nil)
      end
      raise ArgumentError, "Invalid arguments when calling string(#{encoding})"
    end
    # @return [::VertxWebClient::BodyCodec] the  codec
    def self.buffer
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebClient::BodyCodec.java_method(:buffer, []).call(),::VertxWebClient::BodyCodec,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling buffer()"
    end
    # @return [::VertxWebClient::BodyCodec] the  codec
    def self.json_object
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebClient::BodyCodec.java_method(:jsonObject, []).call(),::VertxWebClient::BodyCodec, nil)
      end
      raise ArgumentError, "Invalid arguments when calling json_object()"
    end
    # @return [::VertxWebClient::BodyCodec] a codec that simply discards the response
    def self.none
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebClient::BodyCodec.java_method(:none, []).call(),::VertxWebClient::BodyCodec, nil)
      end
      raise ArgumentError, "Invalid arguments when calling none()"
    end
    #  Create a codec that buffers the entire body and then apply the <code>decode</code> function and returns the result.
    # @yield the decode function
    # @return [::VertxWebClient::BodyCodec] the created codec
    def self.create(decode=nil)
      if block_given? && decode == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebClient::BodyCodec.java_method(:create, [Java::JavaUtilFunction::Function.java_class]).call((Proc.new { |event| ::Vertx::Util::Utils.to_object(yield(::Vertx::Util::Utils.safe_create(event,::Vertx::Buffer))) })),::VertxWebClient::BodyCodec, nil)
      end
      raise ArgumentError, "Invalid arguments when calling create(#{decode})"
    end
    #  A body codec that pipes the body to a write stream.
    # @param [::Vertx::WriteStream] stream the destination tream
    # @return [::VertxWebClient::BodyCodec] the body codec for a write stream
    def self.pipe(stream=nil)
      if stream.class.method_defined?(:j_del) && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebClient::BodyCodec.java_method(:pipe, [Java::IoVertxCoreStreams::WriteStream.java_class]).call(stream.j_del),::VertxWebClient::BodyCodec, nil)
      end
      raise ArgumentError, "Invalid arguments when calling pipe(#{stream})"
    end
  end
end
