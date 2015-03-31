# -*- coding: utf-8 -*-
import os

from ws4py.framing import Frame, OPCODE_CONTINUATION, OPCODE_TEXT, \
     OPCODE_BINARY, OPCODE_CLOSE, OPCODE_PING, OPCODE_PONG

__all__ = ['TextMessage', 'BinaryMessage', 'CloseControlMessage',
           'PingControlMessage', 'PongControlMessage']

class Message(object):
    def __init__(self, opcode, data='', encoding='utf-8'):
        """
        A WebSocket message is made of an opcode defining its type
        and some bytes.

        @param opcode: message type
        @param data: message bytes
        @param encoding: how to encode the message bytes
        """
        self.opcode = opcode
        self._completed = False
        
        if isinstance(data, basestring):
            if isinstance(data, unicode):
                data = data.encode(encoding)
            # bytarrays are way faster than strings
            self.data = bytearray(data)
        elif isinstance(data, bytearray):
            self.data = data
        else:
            raise TypeError("'%s' is not a supported message data type" % type(data))
            
        self.encoding = encoding
        
    def single(self, mask=False):
        """
        Returns a frame bytes with the fin bit set and a random mask.
        """
        mask = os.urandom(4) if mask else None
        return Frame(body=self.data or '', opcode=self.opcode,
                     masking_key=mask, fin=1).build()

    def fragment(self, first=False, last=False, mask=False):
        """
        Returns a frame bytes as part of a fragmented message.

        @param first: indicates this is the first frame of the message
        @param last: indicates this is the last frame of the message,
          setting the fin bit
        """
        fin = 1 if last is True else 0
        opcode = self.opcode if first is True else OPCODE_CONTINUATION
        mask = os.urandom(4) if mask else None
        return Frame(body=self.data or '', opcode=opcode,
                     masking_key=mask, fin=fin).build()

    @property
    def completed(self):
        """
        Indicates the the message is complete, meaning
        the frame fin bit was set for its last frame.
        """
        return self._completed

    @completed.setter
    def completed(self, state):
        """
        Sets the state for this message. Usually
        set by the stream's parser.
        """
        self._completed = state
        
    def extend(self, data):
        """
        Add more bytes to the message.

        @param: bytes to add to this message.
        """
        if isinstance(data, unicode):
            data = data.encode(self.encoding)
        self.data.extend(data)
        
    def __len__(self):
        return len(self.data)

    def __str__(self):
        return str(self.data)

    def __unicode__(self):
        return unicode(self.data, self.encoding or 'utf-8')

class TextMessage(Message):
    def __init__(self, text=None):
        Message.__init__(self, OPCODE_TEXT, text)

    @property
    def is_binary(self):
        return False

    @property
    def is_text(self):
        return True

class BinaryMessage(Message):
    def __init__(self, bytes=None):
        Message.__init__(self, OPCODE_BINARY, bytes, encoding=None)

    @property
    def is_binary(self):
        return True

    @property
    def is_text(self):
        return False

class CloseControlMessage(Message):
    def __init__(self, code=1000, reason=''):
        Message.__init__(self, OPCODE_CLOSE, reason)
        self.code = code
        self.reason = reason

class PingControlMessage(Message):
    def __init__(self, data=None):
        Message.__init__(self, OPCODE_PING, data)
        
class PongControlMessage(Message):
    def __init__(self, data):
        Message.__init__(self, OPCODE_PONG, data)
