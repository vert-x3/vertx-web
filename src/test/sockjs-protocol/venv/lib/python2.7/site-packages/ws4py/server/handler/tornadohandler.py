# -*- coding: utf-8 -*-
import tornado.ioloop
import tornado.web
from tornado.websocket import WebSocketHandler

class EchoWebSocket(WebSocketHandler):
    def on_message(self, message):
        binary = self.ws_connection._frame_opcode == 0x2
        self.ws_connection.write_message(message, binary)
            
