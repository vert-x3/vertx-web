require 'vertx/async_file'
require 'vertx/buffer'
require 'vertx/write_stream'
require 'vertx/util/utils.rb'
# Generated from io.vertx.webclient.BodyCodec
module VertxWebClient
  #  A builder for configuring client-side HTTP responses.
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
    # @param [String] enc 
    # @return [::VertxWebClient::BodyCodec]
    def self.string(enc=nil)
      if !block_given? && enc == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxWebclient::BodyCodec.java_method(:string, []).call(),::VertxWebClient::BodyCodec, nil)
      elsif enc.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxWebclient::BodyCodec.java_method(:string, [Java::java.lang.String.java_class]).call(enc),::VertxWebClient::BodyCodec, nil)
      end
      raise ArgumentError, "Invalid arguments when calling string(#{enc})"
    end
    # @return [::VertxWebClient::BodyCodec]
    def self.buffer
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxWebclient::BodyCodec.java_method(:buffer, []).call(),::VertxWebClient::BodyCodec,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling buffer()"
    end
    # @return [::VertxWebClient::BodyCodec]
    def self.json_object
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxWebclient::BodyCodec.java_method(:jsonObject, []).call(),::VertxWebClient::BodyCodec, nil)
      end
      raise ArgumentError, "Invalid arguments when calling json_object()"
    end
    # @return [::VertxWebClient::BodyCodec]
    def self.temp_file
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxWebclient::BodyCodec.java_method(:tempFile, []).call(),::VertxWebClient::BodyCodec,::Vertx::AsyncFile.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling temp_file()"
    end
    #  A body codec that writes the body to a write stream
    # @param [::Vertx::WriteStream] stream the destination tream
    # @return [::VertxWebClient::BodyCodec] the body codec for a write stream
    def self.stream(stream=nil)
      if stream.class.method_defined?(:j_del) && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxWebclient::BodyCodec.java_method(:stream, [Java::IoVertxCoreStreams::WriteStream.java_class]).call(stream.j_del),::VertxWebClient::BodyCodec, nil)
      end
      raise ArgumentError, "Invalid arguments when calling stream(#{stream})"
    end
  end
end
