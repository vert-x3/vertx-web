require 'vertx-apex/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.apex.handler.BodyHandler
module VertxApex
  #  A handler which gathers the entire request body and sets it on the {::VertxApex::RoutingContext}.
  #  <p>
  #  It also handles HTTP file uploads and can be used to limit body sizes.
  class BodyHandler
    # @private
    # @param j_del [::VertxApex::BodyHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxApex::BodyHandler] the underlying java delegate
    def j_del
      @j_del
    end
    # @param [::VertxApex::RoutingContext] arg0
    # @return [void]
    def handle(arg0=nil)
      if arg0.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:handle, [Java::IoVertxExtApex::RoutingContext.java_class]).call(arg0.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling handle(arg0)"
    end
    #  Create a body handler with defaults
    # @return [::VertxApex::BodyHandler] the body handler
    def self.create
      if !block_given?
        return ::VertxApex::BodyHandler.new(Java::IoVertxExtApexHandler::BodyHandler.java_method(:create, []).call())
      end
      raise ArgumentError, "Invalid arguments when calling create()"
    end
    #  Set the maximum body size -1 means unlimited
    # @param [Fixnum] bodyLimit the max size
    # @return [self]
    def set_body_limit(bodyLimit=nil)
      if bodyLimit.class == Fixnum && !block_given?
        @j_del.java_method(:setBodyLimit, [Java::long.java_class]).call(bodyLimit)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_body_limit(bodyLimit)"
    end
    #  Set the uploads directory to use
    # @param [String] uploadsDirectory the uploads directory
    # @return [self]
    def set_uploads_directory(uploadsDirectory=nil)
      if uploadsDirectory.class == String && !block_given?
        @j_del.java_method(:setUploadsDirectory, [Java::java.lang.String.java_class]).call(uploadsDirectory)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_uploads_directory(uploadsDirectory)"
    end
    #  Set whether form attributes will be added to the request parameters
    # @param [true,false] mergeFormAttributes true if they should be merged
    # @return [self]
    def set_merge_form_attributes(mergeFormAttributes=nil)
      if (mergeFormAttributes.class == TrueClass || mergeFormAttributes.class == FalseClass) && !block_given?
        @j_del.java_method(:setMergeFormAttributes, [Java::boolean.java_class]).call(mergeFormAttributes)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_merge_form_attributes(mergeFormAttributes)"
    end
  end
end
