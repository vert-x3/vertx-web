# -*- coding: utf-8 -*-
import socket
from urlparse import urlsplit

from tornado import iostream, escape

from ws4py.client import WebSocketBaseClient
from ws4py.exc import HandshakeError

__all__ = ['TornadoWebSocketClient']

class TornadoWebSocketClient(WebSocketBaseClient):
    def __init__(self, url, protocols=None, version='8'):
        WebSocketBaseClient.__init__(self, url, protocols=protocols, version=version)
        self.io = iostream.IOStream(socket.socket(socket.AF_INET, socket.SOCK_STREAM, 0))

    def connect(self):
        parts = urlsplit(self.url)
        host, port = parts.netloc, 80
        if ':' in host:
            host, port = parts.netloc.split(':')
        self.io.connect((host, int(port)), self.__send_handshake)

    def __send_handshake(self):
        self.io.set_close_callback(self.__connection_closed)
        self.io.write(escape.utf8(self.handshake_request),
                      self.__handshake_sent)
    
    def __connection_closed(self, *args, **kwargs):
        self.server_terminated = True
        self.closed(1006, 'Connection closed during handshake')
    
    def __handshake_sent(self):
        self.io.read_until("\r\n\r\n", self.__handshake_completed)

    def __handshake_completed(self, data):
        self.io.set_close_callback(None)
        try:
            response_line, _, headers = data.partition('\r\n')
            self.process_response_line(response_line)
            protocols, extensions = self.process_handshake_header(headers)
        except HandshakeError:
            self.io.close()
            raise
        
        self.opened(protocols, extensions)
        self.io.read_bytes(1, self.__fetch_more)

    def __fetch_more(self, bytes):
        s = self.stream
        try:
            next_size = s.parser.send(bytes)
        except:
            self.close_connection()
            self.closed(1006)
            return
                
        if s.closing is not None:
            if not self.client_terminated:
                next_size = 2
                self.close()
            else:
                self.server_terminated = True
                self.close_connection()
                self.closed(s.closing.code, s.closing.reason)
                return
            
        elif s.errors:
            errors = s.errors[:]
            for error in s.errors:
                self.close(error.code, error.reason)
                s.errors.remove(error)
                
        elif s.has_message:
            self.received_message(s.message)
            s.message.data = None
            s.message = None

        for ping in s.pings:
            self.write_to_connection(s.pong(str(ping.data)))
        s.pings = []

        for pong in s.pongs:
            self.ponged(pong)
        s.pongs = []
    
        self.io.read_bytes(next_size, self.__fetch_more)
     
    def write_to_connection(self, bytes):
        self.io.write(bytes)

    def read_from_connection(self, amount):
        self.io.read_bytes(amount, self.__fetch_more)
        
    def close_connection(self):
        self.io.close()

if __name__ == '__main__':
    from tornado import ioloop

    class MyClient(TornadoWebSocketClient):
        def opened(self, protocols, extensions):
            def data_provider():
                for i in range(1, 200, 25):
                    yield "#" * i

            self.send(data_provider())
            
            for i in range(0, 200, 25):
                self.send("*" * i)

        def received_message(self, m):
            print m, len(str(m))
            if len(str(m)) == 175:
                self.close()

        def closed(self, code, reason):
            ioloop.IOLoop.instance().stop()
                
    ws = MyClient('http://localhost:9000/', protocols=['http-only', 'chat'])
    ws.connect()
        
    ioloop.IOLoop.instance().start()

