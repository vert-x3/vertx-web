require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.BodyHandler
module VertxWeb
  #  A handler which gathers the entire request body and sets it on the .
  #  <p>
  #  It also handles HTTP file uploads and can be used to limit body sizes.
  class BodyHandler
    # @private
    # @param j_del [::VertxWeb::BodyHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::BodyHandler] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == BodyHandler
    end
    def @@j_api_type.wrap(obj)
      BodyHandler.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxExtWebHandler::BodyHandler.java_class
    end
    # @param [::VertxWeb::RoutingContext] arg0 
    # @return [void]
    def handle(arg0=nil)
      if arg0.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:handle, [Java::IoVertxExtWeb::RoutingContext.java_class]).call(arg0.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling handle(#{arg0})"
    end
    #  Create a body handler and use the given upload directory.
    # @param [String] uploadDirectory the uploads directory
    # @return [::VertxWeb::BodyHandler] the body handler
    def self.create(uploadDirectory=nil)
      if !block_given? && uploadDirectory == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::BodyHandler.java_method(:create, []).call(),::VertxWeb::BodyHandler)
      elsif uploadDirectory.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::BodyHandler.java_method(:create, [Java::java.lang.String.java_class]).call(uploadDirectory),::VertxWeb::BodyHandler)
      end
      raise ArgumentError, "Invalid arguments when calling create(#{uploadDirectory})"
    end
    #  Set the maximum body size -1 means unlimited
    # @param [Fixnum] bodyLimit the max size
    # @return [self]
    def set_body_limit(bodyLimit=nil)
      if bodyLimit.class == Fixnum && !block_given?
        @j_del.java_method(:setBodyLimit, [Java::long.java_class]).call(bodyLimit)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_body_limit(#{bodyLimit})"
    end
    #  Set the uploads directory to use
    # @param [String] uploadsDirectory the uploads directory
    # @return [self]
    def set_uploads_directory(uploadsDirectory=nil)
      if uploadsDirectory.class == String && !block_given?
        @j_del.java_method(:setUploadsDirectory, [Java::java.lang.String.java_class]).call(uploadsDirectory)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_uploads_directory(#{uploadsDirectory})"
    end
    #  Set whether form attributes will be added to the request parameters
    # @param [true,false] mergeFormAttributes true if they should be merged
    # @return [self]
    def set_merge_form_attributes(mergeFormAttributes=nil)
      if (mergeFormAttributes.class == TrueClass || mergeFormAttributes.class == FalseClass) && !block_given?
        @j_del.java_method(:setMergeFormAttributes, [Java::boolean.java_class]).call(mergeFormAttributes)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_merge_form_attributes(#{mergeFormAttributes})"
    end
    #  Set whether uploaded files should be removed after handling the request
    # @param [true,false] deleteUploadedFilesOnEnd true if uploaded files should be removed after handling the request
    # @return [self]
    def set_delete_uploaded_files_on_end(deleteUploadedFilesOnEnd=nil)
      if (deleteUploadedFilesOnEnd.class == TrueClass || deleteUploadedFilesOnEnd.class == FalseClass) && !block_given?
        @j_del.java_method(:setDeleteUploadedFilesOnEnd, [Java::boolean.java_class]).call(deleteUploadedFilesOnEnd)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_delete_uploaded_files_on_end(#{deleteUploadedFilesOnEnd})"
    end
  end
end
