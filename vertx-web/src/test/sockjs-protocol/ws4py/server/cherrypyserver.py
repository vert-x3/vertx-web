# -*- coding: utf-8 -*-
__doc__ = """
WebSocket within CherryPy is a tricky bit since CherryPy is
a threaded server which would choke quickly if each thread
of the server were kept attached to a long living connection
that WebSocket expects.

In order to work around this constraint, we take some advantage
of some internals of CherryPy as well as the introspection
Python provides.

Basically, whene the WebSocket upgrade is performed, we take over
the socket and let CherryPy take back the thread that was
associated with the upgrade request.

These operations require a bit of work at various levels of
the CherryPy framework but this module takes care of them
and from your application's perspective, this is abstracted.

Here are the various utilities provided by this module:

 * WebSocketTool: The tool is in charge to perform the
                  HTTP upgrade and detach the socket from
                  CherryPy. It runs at various hook points of the
                  request's processing. Enable that tool at
                  any path you wish to handle as a WebSocket
                  handler.
                  
 * WebSocketPlugin: The plugin tracks the web socket handler
                    instanciated. It also cleans out websocket handler
                    which connection have been closed down.
             
Simple usage example:

    import cherrypy
    from ws4py.server.cherrypyserver import WebSocketPlugin, WebSocketTool, WebSocketHandler
    from ws4py.server.handler.threadedhandler import EchoWebSocketHandler
    
    cherrypy.config.update({'server.socket_port': 9000})
    WebSocketPlugin(cherrypy.engine).subscribe()
    cherrypy.tools.websocket = WebSocketTool()

    class Root(object):
        @cherrypy.expose
        def index(self):
            return 'some HTML with a websocket javascript connection'

        @cherrypy.expose
        def ws(self):
            pass
        
    cherrypy.quickstart(Root(), '/', config={'/ws': {'tools.websocket.on': True,
                                                     'tools.websocket.handler_cls': EchoWebSocketHandler}})


Note that you can set the handler class on per-path basis,
meaning you could also dynamically change the class based
on other envrionmental settings (is the user authenticated for ex).

The current implementation of the handler is based on a thread that will
constantly read bytes from the socket and feed the stream instance with them
until an error or a close condition arise. This might be a bit
suboptimal and one could implement the handler in a different fashion
using a poll based socket handling (select, poll, tornado, gevent, etc.)
"""
import base64
from hashlib import sha1
import inspect
import socket

import cherrypy
from cherrypy import Tool
from cherrypy.process import plugins
from cherrypy.wsgiserver import HTTPConnection, HTTPRequest

from ws4py import WS_KEY
from ws4py.exc import HandshakeError
from ws4py.server.handler.threadedhandler import WebSocketHandler

__all__ = ['WebSocketTool', 'WebSocketPlugin']

class WebSocketTool(Tool):
    def __init__(self):
        Tool.__init__(self, 'before_request_body', self.upgrade)

    def _setup(self):
        conf = self._merged_args()
        hooks = cherrypy.serving.request.hooks
        p = conf.pop("priority", getattr(self.callable, "priority",
                                         self._priority))
        hooks.attach(self._point, self.callable, priority=p, **conf)
        hooks.attach('before_finalize', self.complete,
                     priority=p)
        hooks.attach('on_end_resource', self.cleanup_headers,
                     priority=70)
        hooks.attach('on_end_request', self.start_handler,
                     priority=70)

    def upgrade(self, protocols=None, extensions=None, version=8, handler_cls=WebSocketHandler):
        """
        Performs the upgrade of the connection to the WebSocket
        protocol.

        The provided protocols may be a list of WebSocket
        protocols supported by the instance of the tool.

        When no list is provided and no protocol is either
        during the upgrade, then the protocol parameter is
        not taken into account. On the other hand,
        if the protocol from the handshake isn't part
        of the provided list, the upgrade fails immediatly.
        """
        request = cherrypy.serving.request
        request.process_request_body = False
        
        ws_protocols = None
        ws_location = None
        ws_version = version
        ws_key = None
        ws_extensions = []
        
        if request.method != 'GET':
            raise HandshakeError('Method is not GET')

        for key, expected_value in [('Upgrade', 'websocket'),
                                     ('Connection', 'Upgrade')]:
            actual_value = request.headers.get(key)
            if not actual_value:
                raise HandshakeError('Header %s is not defined' % key)
            if expected_value and expected_value not in actual_value:
                raise HandshakeError('Illegal value for header %s: %s' %
                                     (key, actual_value))
            
        key = request.headers.get('Sec-WebSocket-Key')
        if key:
            ws_key = base64.b64decode(key)
            if len(ws_key) != 16:
                raise HandshakeError("WebSocket key's length is invalid")
        
        version = request.headers.get('Sec-WebSocket-Version')
        if version:
            if version != str(ws_version):
                raise HandshakeError('Unsupported WebSocket version')
        else:
            raise HandshakeError('WebSocket version required')
        
        protocols = protocols or []
        subprotocols = request.headers.get('Sec-WebSocket-Protocol')
        if subprotocols:
            ws_protocols = []
            for s in subprotocols.split(','):
                s = s.strip()
                if s in protocols:
                    ws_protocols.append(s)

        exts = extensions or []
        extensions = request.headers.get('Sec-WebSocket-Extensions')
        if extensions:
            for ext in extensions.split(','):
                ext = ext.strip()
                if ext in exts:
                    ws_extensions.append(ext)
        
        location = []
        include_port = False
        if request.scheme == "https":
            location.append("wss://")
            include_port = request.local.port != 443
        else:
            location.append("ws://")
            include_port = request.local.port != 80
        location.append('localhost')
        if include_port:
            location.append(":%d" % request.local.port)
        location.append(request.path_info)
        if request.query_string != "":
            location.append("?%s" % request.query_string)
        ws_location = ''.join(location)

        
        response = cherrypy.serving.response
        response.stream = True
        response.status = '101 Switching Protocols'
        response.headers['Content-Type'] = 'text/plain'
        response.headers['Upgrade'] = 'websocket'
        response.headers['Connection'] = 'Upgrade'
        response.headers['Sec-WebSocket-Version'] = str(ws_version)
        response.headers['Sec-WebSocket-Accept'] = base64.b64encode(sha1(key + WS_KEY).digest())
        if ws_protocols:
            response.headers['Sec-WebSocket-Protocol'] = ', '.join(ws_protocols)
        if ws_extensions:
            response.headers['Sec-WebSocket-Extensions'] = ','.join(ws_extensions)

	addr = (request.remote.ip, request.remote.port)
        ws_conn = request.rfile.rfile._sock
        request.ws_handler = handler_cls(ws_conn, ws_protocols, ws_extensions)
	# Start tracking the handler 
        cherrypy.engine.publish('handle-websocket', request.ws_handler, addr)
        
    def complete(self):
        """
	Sets some internal flags of CherryPy so that it
	doesn't close the socket down.
	"""
        self._set_internal_flags()

    def cleanup_headers(self):
        """
	Some clients aren't that smart when it comes to
	headers lookup.
	"""
        response = cherrypy.response
        headers = response.header_list[:]
        for (k, v) in headers:
            if k.startswith('Sec-Web'):
                response.header_list.remove((k, v))
                response.header_list.append((k.replace('Sec-Websocket', 'Sec-WebSocket'), v))

    def start_handler(self):
        """
	Runs at the end of the request processing by calling
	the opened method of the handler. 
	"""
        request = cherrypy.request
        request.ws_handler.opened()
        request.ws_handler = None
	# By doing this we detach the socket from
	# the CherryPy stack avoiding memory leaks
	request.rfile.rfile._sock = None
        
    def _set_internal_flags(self):
        """
        CherryPy has two internal flags that we are interested in
        to enable WebSocket within the server. They can't be set via
        a public API and considering I'd want to make this extension
        as compatible as possible whilst refraining in exposing more
        than should be within CherryPy, I prefer performing a bit
        of introspection to set those flags. Even by Python standards
        such introspection isn't the cleanest but it works well
        enough in this case.

        This also means that we do that only on WebSocket
        connections rather than globally and therefore we do not
        harm the rest of the HTTP server.
        """
        current = inspect.currentframe()
        while True:
            if not current:
                break
            _locals = current.f_locals
            if 'self' in _locals:
               if type(_locals['self']) == HTTPRequest:
                   _locals['self'].close_connection = True
               if type(_locals['self']) == HTTPConnection:
                   _locals['self'].linger = True
                   # HTTPConnection is more inner than
                   # HTTPRequest so we can leave once
                   # we're done here
                   return
            _locals = None
            current = current.f_back

class WebSocketPlugin(plugins.SimplePlugin):
    def __init__(self, bus):
        plugins.SimplePlugin.__init__(self, bus)
        self.handlers = []

    def start(self):
        cherrypy.log("Starting WebSocket processing")
        self.bus.subscribe('handle-websocket', self.handle)
        self.bus.subscribe('websocket-broadcast', self.broadcast)
        self.bus.subscribe('main', self.cleanup)
        
    def stop(self):
        cherrypy.log("Terminating WebSocket processing")
        self.bus.unsubscribe('main', self.cleanup)
        self.bus.unsubscribe('handle-websocket', self.handle)
        self.bus.unsubscribe('websocket-broadcast', self.broadcast)
	self.cleanup()

    def handle(self, ws_handler, peer_addr):
        """
	Tracks the provided handler.

	@param ws_handler: websocket handler instance
	@param peer_addr: remote peer address for tracing purpose
	"""
        cherrypy.log("Managing WebSocket connection from %s:%d" % (peer_addr[0], peer_addr[1]))
        self.handlers.append((ws_handler, peer_addr))

    def cleanup(self):
        """
	Performs a bit of cleanup on tracked handlers
	by closing connection of terminated streams then
	removing them from the tracked list.
	"""
        handlers = self.handlers[:]
        for peer in handlers:
	    handler, addr = peer
            if handler.terminated:
		cherrypy.log("Removing WebSocket connection from peer: %s:%d" % (addr[0], addr[1]))
                handler.close_connection()
                handler._th.join()
                self.handlers.remove(peer)

    def broadcast(self, message, binary=False):
        """
        Broadcasts a message to all connected clients known to
        the server.

        @param message: a message suitable to pass to the send() method
          of the connected handler.
        @param binary: whether or not the message is a binary one
        """
        handlers = self.handlers[:]
        for peer in handlers:
            try:
                handler, addr = peer
                handler.send(message, binary)
            except:
                cherrypy.log(traceback=True)
            
if __name__ == '__main__':
    import random
    from ws4py.server.handler.threadedhandler import EchoWebSocketHandler
    
    cherrypy.config.update({'server.socket_host': '127.0.0.1',
                            'server.socket_port': 9000})
    WebSocketPlugin(cherrypy.engine).subscribe()
    cherrypy.tools.websocket = WebSocketTool()
    
    class Root(object):
        @cherrypy.expose
        @cherrypy.tools.websocket(on=False)
        def ws(self):
            return """<html>
        <head>
          <script type='application/javascript' src='https://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js'> </script>
          <script type='application/javascript'>
            $(document).ready(function() {
              var ws = new WebSocket('ws://192.168.0.10:8888/');
              ws.onmessage = function (evt) {
                 $('#chat').val($('#chat').val() + evt.data + '\\n');                  
              };
              ws.onopen = function() {
                 ws.send("Hello there");
              };
              $('#chatform').submit(function() {
                 ws.send('%(username)s: ' + $('#message').val());
                 $('#message').val("");
                 return false;
              });
            });
          </script>
        </head>
        <body>
        <form action='/echo' id='chatform' method='get'>
          <textarea id='chat' cols='35' rows='10'></textarea>
          <br />
          <label for='message'>%(username)s: </label><input type='text' id='message' />
          <input type='submit' value='Send' />
          </form>
        </body>
        </html>
        """ % {'username': "User%d" % random.randint(0, 100)}

        @cherrypy.expose
        def index(self):
            cherrypy.log("Handler created: %s" % repr(cherrypy.request.ws_handler))
        
    cherrypy.quickstart(Root(), '/', config={'/': {'tools.websocket.on': True,
						   'tools.websocket.handler_cls': EchoWebSocketHandler}})
