# -*- coding: utf-8 -*-
import os
from urlparse import urlsplit
import socket
import threading
import traceback
from sys import exc_info

from ws4py.client import WebSocketBaseClient
from ws4py.exc import HandshakeError

__all__ = ['WebSocketClient']

class WebSocketClient(WebSocketBaseClient):
    def __init__(self, url, sock=None, protocols=None, version='8'):
        WebSocketBaseClient.__init__(self, url, protocols=protocols, version=version)
        if not sock:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM, 0)
        sock.settimeout(3)
        self.sock = sock

        self.running = True

        self._lock = threading.Lock()
        self._th = threading.Thread(target=self._receive)

    def write_to_connection(self, bytes):
        return self.sock.sendall(bytes)

    def read_from_connection(self, amount):
        return self.sock.recv(amount)
        
    def close_connection(self):
        try:
            self.sock.shutdown(socket.SHUT_RDWR)
            self.sock.close()
        except:
            pass
        
    def connect(self):
        parts = urlsplit(self.url)
        host, port = parts.netloc, 80
        if ':' in host:
            host, port = parts.netloc.split(':')
        self.sock.connect((host, int(port)))
        
        self.write_to_connection(self.handshake_request)

        response = ''
        while True:
            bytes = self.sock.recv(128)
            if not bytes:
                break
            response += bytes
            if '\r\n\r\n' in response:
                break

        if not response:
            self.close_connection()
            raise HandshakeError("Invalid response")

        headers, _, body = response.partition('\r\n\r\n')
        response_line, _, headers = headers.partition('\r\n')

        self.__buffer = body

        try:
            self.process_response_line(response_line)
            protocols, extensions = self.process_handshake_header(headers)
        except HandshakeError:
            self.close_connection()
            raise
        
        self._th.start()
        self.opened(protocols, extensions)

    def _receive(self):
        next_size = 2
        try:
            self.sock.setblocking(1)
            while self.running:
                if self.__buffer:
                    bytes, self.__buffer = self.__buffer[:next_size], self.__buffer[next_size:]
                else:
                    bytes = self.read_from_connection(next_size)

                with self._lock:
                    s = self.stream
                    next_size = s.parser.send(bytes)

                    if s.closing is not None:
                        if not self.client_terminated:
                            next_size = 2
                            self.close()
                        else:
                            self.server_terminated = True
                            self.running = False
                            break

                    elif s.errors:
                        errors = s.errors[:]
                        for error in s.errors:
                            self.close(error.code, error.reason)
                            s.errors.remove(error)
			break
                            
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
                    
        except:
            print "".join(traceback.format_exception(*exc_info()))
        finally:
            self.close_connection()
	    if self.stream.closing:
		self.closed(self.stream.closing.code, self.stream.closing.reason)
	    else:
		self.closed(1006)

if __name__ == '__main__':
    import time
    
    class MyClient(WebSocketClient):
        def opened(self, protocols, extensions):
            WebSocketClient.opened(self, protocols, extensions)
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

    try:
        ws = MyClient('http://localhost:9000/', protocols=['http-only', 'chat'])
        ws.connect()
    except KeyboardInterrupt:
        ws.close()
