require 'vertx-apex/file_upload'
require 'vertx/http_server_request'
require 'vertx-apex/route'
require 'vertx/buffer'
require 'vertx/http_server_response'
require 'vertx-apex/cookie'
require 'vertx-apex/session'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.apex.RoutingContext
module VertxApex
  #  Represents the context for the handling of a request in Apex.
  #  <p>
  #  A new instance is created for each HTTP request that is received in the
  #  {::VertxApex::Router#accept} of the router.
  #  <p>
  #  The same instance is passed to any matching request or failure handlers during the routing of the request or
  #  failure.
  #  <p>
  #  The context provides access to the {::Vertx::HttpServerRequest} and {::Vertx::HttpServerResponse}
  #  and allows you to maintain arbitrary data that lives for the lifetime of the context. Contexts are discarded once they
  #  have been routed to the handler for the request.
  #  <p>
  #  The context also provides access to the {::VertxApex::Session}, cookies and body for the request, given the correct handlers
  #  in the application.
  class RoutingContext
    # @private
    # @param j_del [::VertxApex::RoutingContext] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxApex::RoutingContext] the underlying java delegate
    def j_del
      @j_del
    end
    #  @return the HTTP request object
    # @return [::Vertx::HttpServerRequest]
    def request
      if !block_given?
        if @cached_request != nil
          return @cached_request
        end
        return @cached_request = ::Vertx::HttpServerRequest.new(@j_del.java_method(:request, []).call())
      end
      raise ArgumentError, "Invalid arguments when calling request()"
    end
    #  @return the HTTP response object
    # @return [::Vertx::HttpServerResponse]
    def response
      if !block_given?
        if @cached_response != nil
          return @cached_response
        end
        return @cached_response = ::Vertx::HttpServerResponse.new(@j_del.java_method(:response, []).call())
      end
      raise ArgumentError, "Invalid arguments when calling response()"
    end
    #  Tell the router to route this context to the next matching route (if any).
    #  This method, if called, does not need to be called during the execution of the handler, it can be called
    #  some arbitrary time later, if required.
    #  <p>
    #  If next is not called for a handler then the handler should make sure it ends the response or no response
    #  will be sent.
    # @return [void]
    def next
      if !block_given?
        return @j_del.java_method(:next, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling next()"
    end
    #  Fail the context with the specified status code.
    #  <p>
    #  This will cause the router to route the context to any matching failure handlers for the request. If no failure handlers
    #  match a default failure response will be sent.
    # @param [Fixnum] statusCode the HTTP status code
    # @return [void]
    def fail(statusCode=nil)
      if statusCode.class == Fixnum && !block_given?
        return @j_del.java_method(:fail, [Java::int.java_class]).call(statusCode)
      end
      raise ArgumentError, "Invalid arguments when calling fail(statusCode)"
    end
    #  Put some arbitrary data in the context. This will be available in any handlers that receive the context.
    # @param [String] key the key for the data
    # @param [Object] obj the data
    # @return [self]
    def put(key=nil,obj=nil)
      if key.class == String && (obj.class == String  || obj.class == Hash || obj.class == Array || obj.class == NilClass || obj.class == TrueClass || obj.class == FalseClass || obj.class == Fixnum || obj.class == Float) && !block_given?
        @j_del.java_method(:put, [Java::java.lang.String.java_class,Java::java.lang.Object.java_class]).call(key,::Vertx::Util::Utils.to_object(obj))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling put(key,obj)"
    end
    #  Get some data from the context. The data is available in any handlers that receive the context.
    # @param [String] key the key for the data
    # @return [Object] the data
    def get(key=nil)
      if key.class == String && !block_given?
        return ::Vertx::Util::Utils.from_object(@j_del.java_method(:get, [Java::java.lang.String.java_class]).call(key))
      end
      raise ArgumentError, "Invalid arguments when calling get(key)"
    end
    #  @return the Vert.x instance associated to the initiating {::VertxApex::Router} for this context
    # @return [::Vertx::Vertx]
    def vertx
      if !block_given?
        return ::Vertx::Vertx.new(@j_del.java_method(:vertx, []).call())
      end
      raise ArgumentError, "Invalid arguments when calling vertx()"
    end
    #  @return the mount point for this router. It will be null for a top level router. For a sub-router it will be the path
    #  at which the subrouter was mounted.
    # @return [String]
    def mount_point
      if !block_given?
        return @j_del.java_method(:mountPoint, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling mount_point()"
    end
    #  @return the current route this context is being routed through.
    # @return [::VertxApex::Route]
    def current_route
      if !block_given?
        return ::VertxApex::Route.new(@j_del.java_method(:currentRoute, []).call())
      end
      raise ArgumentError, "Invalid arguments when calling current_route()"
    end
    #  Return the normalised path for the request.
    #  <p>
    #  The normalised path is where the URI path has been decoded, i.e. any unicode or other illegal URL characters that
    #  were encoded in the original URL with `%` will be returned to their original form. E.g. `%20` will revert to a space.
    #  Also `+` reverts to a space in a query.
    #  <p>
    #  The normalised path will also not contain any `..` character sequences to prevent resources being accessed outside
    #  of the permitted area.
    #  <p>
    #  It's recommended to always use the normalised path as opposed to {::Vertx::HttpServerRequest#path}
    #  if accessing server resources requested by a client.
    # @return [String] the normalised path
    def normalised_path
      if !block_given?
        return @j_del.java_method(:normalisedPath, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling normalised_path()"
    end
    #  Get the cookie with the specified name. The context must have first been routed to a {::VertxApex::CookieHandler}
    #  for this to work.
    # @param [String] name the cookie name
    # @return [::VertxApex::Cookie] the cookie
    def get_cookie(name=nil)
      if name.class == String && !block_given?
        return ::VertxApex::Cookie.new(@j_del.java_method(:getCookie, [Java::java.lang.String.java_class]).call(name))
      end
      raise ArgumentError, "Invalid arguments when calling get_cookie(name)"
    end
    #  Add a cookie. This will be sent back to the client in the response. The context must have first been routed
    #  to a {::VertxApex::CookieHandler} for this to work.
    # @param [::VertxApex::Cookie] cookie the cookie
    # @return [self]
    def add_cookie(cookie=nil)
      if cookie.class.method_defined?(:j_del) && !block_given?
        @j_del.java_method(:addCookie, [Java::IoVertxExtApex::Cookie.java_class]).call(cookie.j_del)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling add_cookie(cookie)"
    end
    #  Remove a cookie. The context must have first been routed to a {::VertxApex::CookieHandler}
    #  for this to work.
    # @param [String] name the name of the cookie
    # @return [::VertxApex::Cookie] the cookie, if it existed, or null
    def remove_cookie(name=nil)
      if name.class == String && !block_given?
        return ::VertxApex::Cookie.new(@j_del.java_method(:removeCookie, [Java::java.lang.String.java_class]).call(name))
      end
      raise ArgumentError, "Invalid arguments when calling remove_cookie(name)"
    end
    #  @return the number of cookies. The context must have first been routed to a {::VertxApex::CookieHandler}
    #  for this to work.
    # @return [Fixnum]
    def cookie_count
      if !block_given?
        return @j_del.java_method(:cookieCount, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling cookie_count()"
    end
    #  @return a set of all the cookies. The context must have first been routed to a {::VertxApex::CookieHandler}
    #  for this to be populated.
    # @return [Set<::VertxApex::Cookie>]
    def cookies
      if !block_given?
        return ::Vertx::Util::Utils.to_set(@j_del.java_method(:cookies, []).call()).map! { |elt| ::VertxApex::Cookie.new(elt) }
      end
      raise ArgumentError, "Invalid arguments when calling cookies()"
    end
    #  Get the entire HTTP request body as a string, assuming the specified encoding. The context must have first been routed to a
    #  {::VertxApex::BodyHandler} for this to be populated.
    # @param [String] encoding the encoding, e.g. "UTF-16"
    # @return [String] the body
    def get_body_as_string(encoding=nil)
      if !block_given? && encoding == nil
        return @j_del.java_method(:getBodyAsString, []).call()
      elsif encoding.class == String && !block_given?
        return @j_del.java_method(:getBodyAsString, [Java::java.lang.String.java_class]).call(encoding)
      end
      raise ArgumentError, "Invalid arguments when calling get_body_as_string(encoding)"
    end
    #  @return Get the entire HTTP request body as a {::Vertx::JsonObject}. The context must have first been routed to a
    #  {::VertxApex::BodyHandler} for this to be populated.
    # @return [Hash{String => Object}]
    def get_body_as_json
      if !block_given?
        return @j_del.java_method(:getBodyAsJson, []).call() != nil ? JSON.parse(@j_del.java_method(:getBodyAsJson, []).call().encode) : nil
      end
      raise ArgumentError, "Invalid arguments when calling get_body_as_json()"
    end
    #  @return Get the entire HTTP request body as a {::Vertx::Buffer}. The context must have first been routed to a
    #  {::VertxApex::BodyHandler} for this to be populated.
    # @return [::Vertx::Buffer]
    def get_body
      if !block_given?
        return ::Vertx::Buffer.new(@j_del.java_method(:getBody, []).call())
      end
      raise ArgumentError, "Invalid arguments when calling get_body()"
    end
    #  @return a set of fileuploads (if any) for the request. The context must have first been routed to a
    #  {::VertxApex::BodyHandler} for this to work.
    # @return [Set<::VertxApex::FileUpload>]
    def file_uploads
      if !block_given?
        return ::Vertx::Util::Utils.to_set(@j_del.java_method(:fileUploads, []).call()).map! { |elt| ::VertxApex::FileUpload.new(elt) }
      end
      raise ArgumentError, "Invalid arguments when calling file_uploads()"
    end
    #  Get the session. The context must have first been routed to a {::VertxApex::SessionHandler}
    #  for this to be populated.
    #  Sessions live for a browser session, and are maintained by session cookies.
    # @return [::VertxApex::Session] the session.
    def session
      if !block_given?
        return ::VertxApex::Session.new(@j_del.java_method(:session, []).call())
      end
      raise ArgumentError, "Invalid arguments when calling session()"
    end
    #  If the context is being routed to failure handlers after a failure has been triggered by calling
    #  {::VertxApex::RoutingContext#fail}  then this will return that status code.  It can be used by failure handlers to render a response,
    #  e.g. create a failure response page.
    # @return [Fixnum] the status code used when signalling failure
    def status_code
      if !block_given?
        if @cached_status_code != nil
          return @cached_status_code
        end
        return @cached_status_code = @j_del.java_method(:statusCode, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling status_code()"
    end
    #  If the route specifies produces matches, e.g. produces `text/html` and `text/plain`, and the `accept` header
    #  matches one or more of these then this returns the most acceptable match.
    # @return [String] the most acceptable content type.
    def get_acceptable_content_type
      if !block_given?
        return @j_del.java_method(:getAcceptableContentType, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling get_acceptable_content_type()"
    end
    #  Add a handler that will be called just before headers are written to the response. This gives you a hook where
    #  you can write any extra headers before the response has been written when it will be too late.
    # @yield the handler
    # @return [Fixnum] the id of the handler. This can be used if you later want to remove the handler.
    def add_headers_end_handler
      if block_given?
        return @j_del.java_method(:addHeadersEndHandler, [Java::IoVertxCore::Handler.java_class]).call(Proc.new { yield })
      end
      raise ArgumentError, "Invalid arguments when calling add_headers_end_handler()"
    end
    #  Remove a headers end handler
    # @param [Fixnum] handlerID the id as returned from {@link io.vertx.ext.apex.RoutingContext#addHeadersEndHandler(io.vertx.core.Handler)}.
    # @return [true,false] true if the handler existed and was removed, false otherwise
    def remove_headers_end_handler?(handlerID=nil)
      if handlerID.class == Fixnum && !block_given?
        return @j_del.java_method(:removeHeadersEndHandler, [Java::int.java_class]).call(handlerID)
      end
      raise ArgumentError, "Invalid arguments when calling remove_headers_end_handler?(handlerID)"
    end
    #  Add a handler that will be called just before the response body has been completely written.
    #  This gives you a hook where you can write any extra data to the response before it has ended when it will be too late.
    # @yield the handler
    # @return [Fixnum] the id of the handler. This can be used if you later want to remove the handler.
    def add_body_end_handler
      if block_given?
        return @j_del.java_method(:addBodyEndHandler, [Java::IoVertxCore::Handler.java_class]).call(Proc.new { yield })
      end
      raise ArgumentError, "Invalid arguments when calling add_body_end_handler()"
    end
    #  Remove a body end handler
    # @param [Fixnum] handlerID the id as returned from {@link io.vertx.ext.apex.RoutingContext#addBodyEndHandler(io.vertx.core.Handler)}.
    # @return [true,false] true if the handler existed and was removed, false otherwise
    def remove_body_end_handler?(handlerID=nil)
      if handlerID.class == Fixnum && !block_given?
        return @j_del.java_method(:removeBodyEndHandler, [Java::int.java_class]).call(handlerID)
      end
      raise ArgumentError, "Invalid arguments when calling remove_body_end_handler?(handlerID)"
    end
    #  @return true if the context is being routed to failure handlers.
    # @return [true,false]
    def failed?
      if !block_given?
        return @j_del.java_method(:failed, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling failed?()"
    end
    #  Set the body. Used by the {::VertxApex::BodyHandler}. You will not normally call this method.
    # @param [::Vertx::Buffer] body the body
    # @return [void]
    def set_body(body=nil)
      if body.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:setBody, [Java::IoVertxCoreBuffer::Buffer.java_class]).call(body.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling set_body(body)"
    end
    #  Set the session. Used by the {::VertxApex::SessionHandler}. You will not normally call this method.
    # @param [::VertxApex::Session] session the session
    # @return [void]
    def set_session(session=nil)
      if session.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:setSession, [Java::IoVertxExtApex::Session.java_class]).call(session.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling set_session(session)"
    end
    #  Set the acceptable content type. Used by
    # @param [String] contentType the content type
    # @return [void]
    def set_acceptable_content_type(contentType=nil)
      if contentType.class == String && !block_given?
        return @j_del.java_method(:setAcceptableContentType, [Java::java.lang.String.java_class]).call(contentType)
      end
      raise ArgumentError, "Invalid arguments when calling set_acceptable_content_type(contentType)"
    end
  end
end
