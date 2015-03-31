# -*- coding: utf-8 -*-
__doc__ = """
Server handlers targetting threaded servers rather than event based servers.
"""
import socket
import threading 
import traceback
from sys import exc_info
import types

from ws4py.streaming import Stream

__all__ = ['WebSocketHandler', 'EchoWebSocketHandler']

class WebSocketHandler(object):
    def __init__(self, sock, protocols, extensions):
        """
        A handler appropriate for servers. This handler
        runs the connection's read and parsing in a thread,
        meaning that incoming messages will be alerted in a different
        thread from the one that created the handler.

        @param sock: opened connection after the websocket upgrade
        @param protocols: list of protocols from the handshake
        @param extensions: list of extensions from the handshake
        """
        self.stream = Stream()
        
        self.protocols = protocols
        self.extensions = extensions

        self.sock = sock
        self.sock.settimeout(30.0)
        
        self.client_terminated = False
        self.server_terminated = False

        self._lock = threading.Lock()
        self._th = threading.Thread(target=self._receive)

    def opened(self):
        """
        Called by the server when the upgrade handshake
        has succeeeded. Starts the internal loop that
        reads bytes from the connection and hands it over
        to the stream for parsing.
        """
        self._th.start()

    def close(self, code=1000, reason=''):
        """
        Call this method to initiate the websocket connection
        closing by sending a close frame to the connected peer.

        Once this method is called, the server_terminated
        attribute is set. Calling this method several times is
        safe as the closing frame will be sent only the first
        time.

        @param code: status code describing why the connection is closed
        @param reason: a human readable message describing why the connection is closed
        """
        if not self.server_terminated:
            self.server_terminated = True
            self.write_to_connection(self.stream.close(code=code, reason=reason))
            
    def closed(self, code, reason=None):
        """
        Called by the server when the websocket connection
        is finally closed.

        @param code: status code
        @param reason: human readable message of the closing exchange
        """
        pass

    @property
    def terminated(self):
        """
        Returns True if both the client and server have been
        marked as terminated.
        """
        return self.client_terminated is True and self.server_terminated is True
    
    def write_to_connection(self, bytes):
        """
        Writes the provided bytes to the underlying connection.

        @param bytes: data tio send out
        """
        return self.sock.sendall(bytes)

    def read_from_connection(self, amount):
        """
        Reads bytes from the underlying connection.

        @param amount: quantity to read (if possible)
        """
        return self.sock.recv(amount)
        
    def close_connection(self):
        """
        Shutdowns then closes the underlying connection.
        """
        try:
            self.sock.shutdown(socket.SHUT_RDWR)
            self.sock.close()
        except:
            pass

    def ponged(self, pong):
        """
        Pong message received on the stream.

        @param pong: messaging.PongControlMessage instance
        """
        pass

    def received_message(self, m):
        """
        Message received on the stream.

        @param pong: messaging.TextMessage or messaging.BinaryMessage instance
        """
        pass

    def send(self, payload, binary=False):
        """
        Sends the given payload out.

        If payload is some bytes or a bytearray,
        then it is sent as a single message not fragmented.

        If payload is a generator, each chunk is sent as part of
        fragmented message.

        @param payload: string, bytes, bytearray or a generator
        @param binary: if set, handles the payload as a binary message
        """
        if isinstance(payload, basestring) or isinstance(payload, bytearray):
            if not binary:
                self.write_to_connection(self.stream.text_message(payload).single())
            else:
                self.write_to_connection(self.stream.binary_message(payload).single())
                
        elif type(payload) == types.GeneratorType:
            bytes = payload.next()
            first = True
            for chunk in payload:
                if not binary:
                    self.write_to_connection(self.stream.text_message(bytes).fragment(first=first))
                else:
                    self.write_to_connection(self.stream.binary_message(payload).fragment(first=first))
                bytes = chunk
                first = False
            if not binary:
                self.write_to_connection(self.stream.text_message(bytes).fragment(last=True))
            else:
                self.write_to_connection(self.stream.text_message(bytes).fragment(last=True))

    def _receive(self):
        """
        Performs the operation of reading from the underlying
        connection in order to feed the stream of bytes.

        We start with a small size of two bytes to be read
        from the connection so that we can quickly parse an
        incoming frame header. Then the stream indicates
        whatever size must be read from the connection since
        it knows the frame payload length.

        Note that we perform some automatic opererations:

        * On a closing message, we respond with a closing
          message and finally close the connection
        * We respond to pings with pong messages.
        * Whenever an error is raised by the stream parsing,
          we initiate the closing of the connection with the
          appropiate error code.
        """
        next_size = 2
        try:
            self.sock.setblocking(1)
            while not self.terminated:
                bytes = self.read_from_connection(next_size)
		if not bytes and next_size > 0:
		    break
                
                with self._lock:
                    s = self.stream
                    next_size = s.parser.send(bytes)
                    
                    if s.closing is not None:
                        if not self.server_terminated:
                            next_size = 2
                            self.close(s.closing.code, s.closing.reason)
                        else:
                            self.client_terminated = True
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
	    self.client_terminated = self.server_terminated = True
            self.close_connection()
	    if self.stream.closing:
		self.closed(self.stream.closing.code, self.stream.closing.reason)
	    else:
		self.closed(1006)


class EchoWebSocketHandler(WebSocketHandler):
    """
    Simple handler that keeps echoing whatever message
    it receives.
    """
    def received_message(self, m):
        self.send(m.data, m.is_binary)
    
