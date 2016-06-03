#!/usr/bin/env python
"""
[**SockJS-protocol**](https://github.com/sockjs/sockjs-protocol) is an
effort to define a protocol between in-browser
[SockJS-client](https://github.com/sockjs/sockjs-client) and its
server-side counterparts, like
[SockJS-node](https://github.com/sockjs/sockjs-client). This should
help others to write alternative server implementations.


This protocol definition is also a runnable test suite, do run it
against your server implementation. Supporting all the tests doesn't
guarantee that SockJS client will work flawlessly, end-to-end tests
using real browsers are always required.
"""
import os
import time
import re
import unittest2 as unittest
from utils_02 import GET, GET_async, POST, POST_async, OPTIONS
from utils_02 import WebSocket8Client
import uuid


# Base URL
# ========

"""
The SockJS server provides one or more SockJS services. The services
are usually exposed with a simple url prefixes, like:
`http://localhost:8000/echo` or
`http://localhost:8000/broadcast`. We'll call this kind of url a
`base_url`. There is nothing wrong with base url being more complex,
like `http://localhost:8000/a/b/c/d/echo`. Base url should
never end with a slash.

Base url is the url that needs to be supplied to the SockJS client.

All paths under base url are controlled by SockJS server and are
defined by SockJS protocol.

SockJS protocol can be using either http or https.

To run this tests server pointed by `base_url` needs to support
following services:

 - `echo` - responds with identical data as received
 - `disabled_websocket_echo` - identical to `echo`, but with websockets disabled
 - `close` - server immediately closes the session

This tests should not be run more often than once in five seconds -
many tests operate on the same (named) sessions and they need to have
enough time to timeout.
"""
test_top_url = os.environ.get('SOCKJS_URL', 'http://localhost:8081')
base_url = test_top_url + '/echo'
close_base_url = test_top_url + '/close'
wsoff_base_url = test_top_url + '/disabled_websocket_echo'


# Static URLs
# ===========

class Test(unittest.TestCase):
    # We are going to test several `404/not found` pages. We don't
    # define a body or a content type.
    def verify404(self, r, cookie=False):
        self.assertEqual(r.status, 404)
        if cookie is False:
            self.verify_no_cookie(r)
        elif cookie is True:
            self.verify_cookie(r)

    # In some cases `405/method not allowed` is more appropriate.
    def verify405(self, r):
        self.assertEqual(r.status, 405)
        self.assertFalse(r['content-type'])
        self.assertFalse(r.body)
        self.verify_no_cookie(r)

    # Multiple transport protocols need to support OPTIONS method. All
    # responses to OPTIONS requests must be cacheable and contain
    # appropriate headers.
    def verify_options(self, url, allowed_methods):
        for origin in [None, 'test']:
            h = {}
            if origin:
                h['Origin'] = origin
            r = OPTIONS(url, headers=h)
            self.assertEqual(r.status, 204)
            self.assertTrue(re.search('public', r['Cache-Control']))
            self.assertTrue(re.search('max-age=[1-9][0-9]{6}', r['Cache-Control']),
                            "max-age must be large, one year (31536000) is best")
            self.assertTrue(r['Expires'])
            self.assertTrue(int(r['access-control-max-age']) > 1000000)
            self.assertEqual(r['Allow'], allowed_methods)
            self.assertFalse(r.body)
            self.verify_cors(r, origin)
            self.verify_cookie(r)

    # All transports except WebSockets need sticky session support
    # from the load balancer. Some load balancers enable that only
    # when they see `JSESSIONID` cookie. For all the session urls we
    # must set this cookie.
    def verify_cookie(self, r):
        self.assertEqual(r['Set-Cookie'].split(';')[0].strip(),
                         'JSESSIONID=dummy')
        self.assertEqual(r['Set-Cookie'].split(';')[1].lower().strip(),
                         'path=/')

    def verify_no_cookie(self, r):
        self.assertFalse(r['Set-Cookie'])

    # Most of the XHR/Ajax based transports do work CORS if proper
    # headers are set.
    def verify_cors(self, r, origin=None):
        self.assertEqual(r['access-control-allow-origin'], origin or '*')
        # In order to get cookies (`JSESSIONID` mostly) flying, we
        # need to set `allow-credentials` header to true.
        self.assertEqual(r['access-control-allow-credentials'], 'true')

    # Sometimes, due to transports limitations we need to request
    # private data using GET method. In such case it's very important
    # to disallow any caching.
    def verify_not_cached(self, r, origin=None):
        self.assertEqual(r['Cache-Control'],
                         'no-store, no-cache, must-revalidate, max-age=0')
        self.assertFalse(r['Expires'])
        self.assertFalse(r['ETag'])
        self.assertFalse(r['Last-Modified'])


# Greeting url: `/`
# ----------------
class BaseUrlGreeting(Test):
    # The most important part of the url scheme, is without doubt, the
    # top url. Make sure the greeting is valid.
    def test_greeting(self):
        for url in [base_url, base_url + '/']:
            r = GET(url)
            self.assertEqual(r.status, 200)
            self.assertEqual(r['content-type'], 'text/plain; charset=UTF-8')
            self.assertEqual(r.body, 'Welcome to SockJS!\n')
            self.verify_no_cookie(r)

    # Other simple requests should return 404.
    def test_notFound(self):
        for suffix in ['/a', '/a.html', '//', '///', '/a/a', '/a/a/', '/a',
                       '/a/']:
            self.verify404(GET(base_url + suffix))


# IFrame page: `/iframe*.html`
# ----------------------------
class IframePage(Test):
    """
    Some transports don't support cross domain communication
    (CORS). In order to support them we need to do a cross-domain
    trick: on remote (server) domain we serve an simple html page,
    that loads back SockJS client javascript and is able to
    communicate with the server within the same domain.
    """
    iframe_body = re.compile('''
^<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="X-UA-Compatible" content="IE=edge" />
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <script>
    document.domain = document.domain;
    _sockjs_onload = function\(\){SockJS.bootstrap_iframe\(\);};
  </script>
  <script src="(?P<sockjs_url>[^"]*)"></script>
</head>
<body>
  <h2>Don't panic!</h2>
  <p>This is a SockJS hidden iframe. It's used for cross domain magic.</p>
</body>
</html>$
'''.strip())

    # SockJS server must provide this html page.
    def test_simpleUrl(self):
        self.verify(base_url + '/iframe.html')

    # To properly utilize caching, the same content must be served
    # for request which try to version the iframe. The server may want
    # to give slightly different answer for every SockJS client
    # revision.
    def test_versionedUrl(self):
        for suffix in ['/iframe-a.html', '/iframe-.html', '/iframe-0.1.2.html',
                       '/iframe-0.1.2abc-dirty.2144.html']:
            self.verify(base_url + suffix)

    # In some circumstances (`devel` set to true) client library
    # wants to skip caching altogether. That is achieved by
    # supplying a random query string.
    def test_queriedUrl(self):
        for suffix in ['/iframe-a.html?t=1234', '/iframe-0.1.2.html?t=123414',
                       '/iframe-0.1.2abc-dirty.2144.html?t=qweqweq123']:
            self.verify(base_url + suffix)

    # Malformed urls must give 404 answer.
    def test_invalidUrl(self):
        for suffix in ['/iframe.htm', '/iframe', '/IFRAME.HTML', '/IFRAME',
                       '/iframe.HTML', '/iframe.xml', '/iframe-/.html']:
            r = GET(base_url + suffix)
            self.verify404(r)

    # The '/iframe.html' page and its variants must give `200/ok` and be
    # served with 'text/html' content type.
    def verify(self, url):
        r = GET(url)
        self.assertEqual(r.status, 200)
        self.assertEqual(r['content-type'], 'text/html; charset=UTF-8')
        # The iframe page must be strongly cacheable, supply
        # Cache-Control, Expires and Etag headers and avoid
        # Last-Modified header.
        self.assertTrue(re.search('public', r['Cache-Control']))
        self.assertTrue(re.search('max-age=[1-9][0-9]{6}', r['Cache-Control']),
                        "max-age must be large, one year (31536000) is best")
        self.assertTrue(r['Expires'])
        self.assertTrue(r['ETag'])
        self.assertFalse(r['last-modified'])

        # Body must be exactly as specified, with the exception of
        # `sockjs_url`, which should be configurable.
        match = self.iframe_body.match(r.body.strip())
        self.assertTrue(match)
        # `Sockjs_url` must be a valid url and should utilize caching.
        sockjs_url = match.group('sockjs_url')
        self.assertTrue(sockjs_url.startswith('/') or
                        sockjs_url.startswith('http'))
        self.verify_no_cookie(r)
        return r


    # The iframe page must be strongly cacheable. ETag headers must
    # not change too often. Server must support 'if-none-match'
    # requests.
    def test_cacheability(self):
        r1 = GET(base_url + '/iframe.html')
        r2 = GET(base_url + '/iframe.html')
        self.assertEqual(r1['etag'], r2['etag'])
        self.assertTrue(r1['etag']) # Let's make sure ETag isn't None.

        r = GET(base_url + '/iframe.html', headers={'If-None-Match': r1['etag']})
        self.assertEqual(r.status, 304)
        self.assertFalse(r['content-type'])
        self.assertFalse(r.body)

# Chunking test: `/chunking_test`
# -------------------------------
#
# Warning: this functionality is going to be removed.
class ChunkingTest(Test):
    # This feature is used in order to check if the client and
    # intermediate proxies support http chunking.
    #
    # The chunking test requires the server to send six http chunks
    # containing a `h` byte delayed by varying timeouts.
    #
    # First, the server must send a 'h' frame.
    #
    # Then, the server must send 2048 bytes of `%20` character
    # (space), as a prelude, followed by a 'h' character.
    #
    # That should be followed by a series of `h` frames with following
    # delays between them:
    #
    #  * 5 ms
    #  * 25 ms
    #  * 125 ms
    #  * 625 ms
    #  * 3125 ms
    #
    # At that point the server should close the request. The client
    # will break the connection as soon as it detects that chunking is
    # indeed working.
    def test_basic(self):
        t0 = time.time()
        r = POST_async(base_url + '/chunking_test')
        self.assertEqual(r.status, 200)
        self.assertEqual(r['content-type'],
                         'application/javascript; charset=UTF-8')
        self.verify_no_cookie(r)
        self.verify_cors(r)

        # In first chunk the server must send a 'h' frame:
        self.assertEqual(r.read(), 'h\n')
        # As second chunk the server must send 2KiB prelude.
        self.assertEqual(r.read(), ' ' * 2048 + 'h\n')
        # Later the server must send a `h` byte.
        self.assertEqual(r.read(), 'h\n')
        # In third chunk the server must send a `h` byte.
        self.assertEqual(r.read(), 'h\n')

        # At least 30 ms must have passed since the request.
        t1 = time.time()
        self.assertGreater((t1-t0) * 1000., 30.)
        r.close()

    # Chunking test must support CORS.
    def test_options(self):
        self.verify_options(base_url + '/chunking_test', 'OPTIONS, POST')


# Session URLs
# ============

# Top session URL: `/<server>/<session>`
# --------------------------------------
#
# The session between the client and the server is always initialized
# by the client. The client chooses `server_id`, which should be a
# three digit number: 000 to 999. It can be supplied by user or
# randomly generated. The main reason for this parameter is to make it
# easier to configure load balancer - and enable sticky sessions based
# on first part of the url.
#
# Second parameter `session_id` must be a random string, unique for
# every session.
#
# It is undefined what happens when two clients share the same
# `session_id`. It is a client responsibility to choose identifier
# with enough entropy.
#
# Neither server nor client API's can expose `session_id` to the
# application. This field must be protected from the app.
class SessionURLs(Test):

    # The server must accept any value in `server` and `session` fields.
    def test_anyValue(self):
        self.verify('/a/a')
        for session_part in ['/_/_', '/1/1', '/abcdefgh_i-j%20/abcdefg_i-j%20']:
            self.verify(session_part)

    # To test session URLs we're going to use `xhr-polling` transport
    # facilitites.
    def verify(self, session_part):
        r = POST(base_url + session_part + '/xhr')
        self.assertEqual(r.body, 'o\n')
        self.assertEqual(r.status, 200)

    # But not an empty string, anything containing dots or paths with
    # less or more parts.
    def test_invalidPaths(self):
        for suffix in ['//', '/a./a', '/a/a.', '/./.' ,'/', '///']:
            self.verify404(GET(base_url + suffix + '/xhr'))
            self.verify404(POST(base_url + suffix + '/xhr'))

    # A session is identified by only `session_id`. `server_id` is a
    # parameter for load balancer and must be ignored by the server.
    def test_ignoringServerId(self):
        ''' See Protocol.test_simpleSession for explanation. '''
        session_id = str(uuid.uuid4())
        r = POST(base_url + '/000/' + session_id + '/xhr')
        self.assertEqual(r.body, 'o\n')
        self.assertEqual(r.status, 200)

        payload = '["a"]'
        r = POST(base_url + '/000/' + session_id + '/xhr_send', body=payload)
        self.assertFalse(r.body)
        self.assertEqual(r.status, 204)

        r = POST(base_url + '/999/' + session_id + '/xhr')
        self.assertEqual(r.body, 'a["a"]\n')
        self.assertEqual(r.status, 200)

# Protocol and framing
# --------------------
#
# SockJS tries to stay API-compatible with WebSockets, but not on the
# network layer. For technical reasons SockJS must introduce custom
# framing and simple custom protocol.
#
# ### Framing accepted by the client
#
# SockJS client accepts following frames:
#
# * `o` - Open frame. Every time a new session is established, the
#   server must immediately send the open frame. This is required, as
#   some protocols (mostly polling) can't distinguish between a
#   properly established connection and a broken one - we must
#   convince the client that it is indeed a valid url and it can be
#   expecting further messages in the future on that url.
#
# * `h` - Heartbeat frame. Most loadbalancers have arbitrary timeouts
#   on connections. In order to keep connections from breaking, the
#   server must send a heartbeat frame every now and then. The typical
#   delay is 25 seconds and should be configurable.
#
# * `a` - Array of json-encoded messages. For example: `a["message"]`.
#
# * `c` - Close frame. This frame is send to the browser every time
#   the client asks for data on closed connection. This may happen
#   multiple times. Close frame contains a code and a string explaining
#   a reason of closure, like: `c[3000,"Go away!"]`.
#
# ### Framing accepted by the server
#
# SockJS server does not have any framing defined. All incoming data
# is treated as incoming messages, either single json-encoded messages
# or an array of json-encoded messages, depending on transport.
#
# ### Tests
#
# To explain the protocol we'll use `xhr-polling` transport
# facilities.
class Protocol(Test):
    # When server receives a request with unknown `session_id` it must
    # recognize that as request for a new session. When server opens a
    # new sesion it must immediately send an frame containing a letter
    # `o`.
    def test_simpleSession(self):
        trans_url = base_url + '/000/' + str(uuid.uuid4())
        r = POST(trans_url + '/xhr')
        "New line is a frame delimiter specific for xhr-polling"
        self.assertEqual(r.body, 'o\n')
        self.assertEqual(r.status, 200)

        # After a session was established the server needs to accept
        # requests for sending messages.
        "Xhr-polling accepts messages as a list of JSON-encoded strings."
        payload = '["a"]'
        r = POST(trans_url + '/xhr_send', body=payload)
        self.assertFalse(r.body)
        self.assertEqual(r.status, 204)

        '''We're using an echo service - we'll receive our message
        back. The message is encoded as an array 'a'.'''
        r = POST(trans_url + '/xhr')
        self.assertEqual(r.body, 'a["a"]\n')
        self.assertEqual(r.status, 200)

        # Sending messages to not existing sessions is invalid.
        payload = '["a"]'
        r = POST(base_url + '/000/bad_session/xhr_send', body=payload)
        self.verify404(r, cookie=True)

        # The session must time out after 5 seconds of not having a
        # receiving connection. The server must send a heartbeat frame
        # every 25 seconds. The heartbeat frame contains a single `h`
        # character. This delays may be configurable.
        pass
        # The server must not allow two receiving connections to wait
        # on a single session. In such case the server must send a
        # close frame to the new connection.
        r1 = POST_async(trans_url + '/xhr', load=False)
        r2 = POST(trans_url + '/xhr')
        r1.close()
        self.assertEqual(r2.body, 'c[2010,"Another connection still open"]\n')
        self.assertEqual(r2.status, 200)

    # The server may terminate the connection, passing error code and
    # message.
    def test_closeSession(self):
        trans_url = close_base_url + '/000/' + str(uuid.uuid4())
        r = POST(trans_url + '/xhr')
        self.assertEqual(r.body, 'o\n')

        r = POST(trans_url + '/xhr')
        self.assertEqual(r.body, 'c[3000,"Go away!"]\n')
        self.assertEqual(r.status, 200)

        # Until the timeout occurs, the server must constantly serve
        # the close message.
        r = POST(trans_url + '/xhr')
        self.assertEqual(r.body, 'c[3000,"Go away!"]\n')
        self.assertEqual(r.status, 200)


# WebSocket protocols: `/*/*/websocket`
# -------------------------------------
import websocket

# The most important feature of SockJS is to support native WebSocket
# protocol. A decent SockJS server should support at least the
# following variants:
#
#   - hixie-75 (Chrome 4, Safari 5.0.0)
#   - hixie-76/hybi-00 (Chrome 6, Safari 5.0.1)
#   - hybi-07 (Firefox 6)
#   - hybi-10 (Firefox 7, Chrome 14)
#
class WebsocketHttpErrors(Test):
    # User should be able to disable websocket transport
    # altogether. This is useful when load balancer doesn't
    # support websocket protocol and we need to be able to reject
    # the transport immediately. This is achieved by returning 404
    # response on websocket transport url. This particular 404 page
    # must be small (less than 1KiB).
    def test_disabledTransport(self):
        r = GET(wsoff_base_url + '/0/0/websocket')
        self.verify404(r)
        if r.body:
            self.assertLess(len(r.body), 1025)

    # Normal requests to websocket should not succeed.
    def test_httpMethod(self):
        r = GET(base_url + '/0/0/websocket')
        self.assertEqual(r.status, 400)
        self.assertEqual(r.body, 'Can "Upgrade" only to "WebSocket".')

    # Server should be able to reject connections if origin is
    # invalid.
    def test_verifyOrigin(self):
        '''
        r = GET(base_url + '/0/0/websocket', {'Upgrade': 'WebSocket',
                                              'Origin': 'VeryWrongOrigin'})
        self.assertEqual(r.status, 400)
        self.assertEqual(r.body, 'Unverified origin.')
        '''
        pass

    # Some proxies and load balancers can rewrite 'Connection' header,
    # in such case we must refuse connection.
    def test_invalidConnectionHeader(self):
        r = GET(base_url + '/0/0/websocket', headers={'Upgrade': 'WebSocket',
                                                      'Connection': 'close'})
        self.assertEqual(r.status, 400)
        self.assertEqual(r.body, '"Connection" must be "Upgrade".')

    # WebSocket should only accept GET
    def test_invalidMethod(self):
        for h in [{'Upgrade': 'WebSocket', 'Connection': 'Upgrade'},
                  {}]:
            r = POST(base_url + '/0/0/websocket', headers=h)
            self.assertEqual(r.status, 405)
            self.assertFalse(r.body)


# Support WebSocket Hixie-76 protocol
class WebsocketHixie76(Test):
    def test_transport(self):
        ws_url = 'ws:' + base_url.split(':',1)[1] + \
                 '/000/' + str(uuid.uuid4()) + '/websocket'
        ws = websocket.create_connection(ws_url)
        self.assertEqual(ws.recv(), u'o')
        ws.send(u'["a"]')
        self.assertEqual(ws.recv(), u'a["a"]')
        ws.close()

    def test_close(self):
        ws_url = 'ws:' + close_base_url.split(':',1)[1] + \
                 '/000/' + str(uuid.uuid4()) + '/websocket'
        ws = websocket.create_connection(ws_url)
        self.assertEqual(ws.recv(), u'o')
        self.assertEqual(ws.recv(), u'c[3000,"Go away!"]')
        ws.close()

    # Empty frames must be ignored by the server side.
    def test_empty_frame(self):
        ws_url = 'ws:' + base_url.split(':',1)[1] + \
                 '/000/' + str(uuid.uuid4()) + '/websocket'
        ws = websocket.create_connection(ws_url)
        self.assertEqual(ws.recv(), u'o')
        # Server must ignore empty messages.
        ws.send(u'')
        ws.send(u'"a"')
        self.assertEqual(ws.recv(), u'a["a"]')
        ''' TODO: should ws connection be automatically closed after
        sending a close frame?'''
        ws.close()

    # For WebSockets, as opposed to other transports, it is valid to
    # reuse `session_id`. The lifetime of SockJS WebSocket session is
    # defined by a lifetime of underlying WebSocket connection. It is
    # correct to have two separate sessions sharing the same
    # `session_id` at the same time.
    def test_reuseSessionId(self):
        on_close = lambda(ws): self.assertFalse(True)

        ws_url = 'ws:' + base_url.split(':',1)[1] + \
                 '/000/' + str(uuid.uuid4()) + '/websocket'
        ws1 = websocket.create_connection(ws_url, on_close=on_close)
        self.assertEqual(ws1.recv(), u'o')

        ws2 = websocket.create_connection(ws_url, on_close=on_close)
        self.assertEqual(ws2.recv(), u'o')

        ws1.send(u'"a"')
        self.assertEqual(ws1.recv(), u'a["a"]')

        ws2.send(u'"b"')
        self.assertEqual(ws2.recv(), u'a["b"]')

        ws1.close()
        ws2.close()

        # It is correct to reuse the same `session_id` after closing a
        # previous connection.
        ws1 = websocket.create_connection(ws_url)
        self.assertEqual(ws1.recv(), u'o')
        ws1.send(u'"a"')
        self.assertEqual(ws1.recv(), u'a["a"]')
        ws1.close()

    # Verify WebSocket headers sanity. Due to HAProxy design the
    # websocket server must support writing response headers *before*
    # receiving -76 nonce. In other words, the websocket code must
    # work like that:
    #
    # * Receive request headers.
    # * Write response headers.
    # * Receive request nonce.
    # * Write response nonce.
    def test_headersSanity(self):
        url = base_url.split(':',1)[1] + \
                 '/000/' + str(uuid.uuid4()) + '/websocket'
        ws_url = 'ws:' + url
        http_url = 'http:' + url
        origin = '/'.join(http_url.split('/')[:3])
        h = {'Upgrade': 'WebSocket',
             'Connection': 'Upgrade',
             'Origin': origin,
             'Sec-WebSocket-Key1': '4 @1  46546xW%0l 1 5',
             'Sec-WebSocket-Key2': '12998 5 Y3 1  .P00'
            }

        r = GET_async(http_url, headers=h)
        self.assertEqual(r.status, 101)
        self.assertEqual(r['sec-websocket-location'], ws_url)
        self.assertEqual(r['connection'].lower(), 'upgrade')
        self.assertEqual(r['upgrade'].lower(), 'websocket')
        self.assertEqual(r['sec-websocket-origin'], origin)
        self.assertFalse(r['content-length'])
        r.close()

    # When user sends broken data - broken JSON for example, the
    # server must terminate the ws connection.
    def test_broken_json(self):
        ws_url = 'ws:' + base_url.split(':',1)[1] + \
                 '/000/' + str(uuid.uuid4()) + '/websocket'
        ws = websocket.create_connection(ws_url)
        self.assertEqual(ws.recv(), u'o')
        ws.send(u'"a')
        with self.assertRaises(websocket.ConnectionClosedException):
            # Raises on error, returns None on valid closure.
            if ws.recv() is None:
                raise websocket.ConnectionClosedException()


# The server must support Hybi-10 protocol
class WebsocketHybi10(Test):
    def test_transport(self):
        trans_url = base_url + '/000/' + str(uuid.uuid4()) + '/websocket'
        ws = WebSocket8Client(trans_url)

        self.assertEqual(ws.recv(), 'o')
        # Server must ignore empty messages.
        ws.send(u'')
        ws.send(u'"a"')
        self.assertEqual(ws.recv(), 'a["a"]')
        ''' TODO: should ws connection be automatically closed after
        sending a close frame?'''
        ws.close()

    def test_close(self):
        trans_url = close_base_url + '/000/' + str(uuid.uuid4()) + '/websocket'
        ws = WebSocket8Client(trans_url)
        self.assertEqual(ws.recv(), u'o')
        self.assertEqual(ws.recv(), u'c[3000,"Go away!"]')
        ws.close()

    # Verify WebSocket headers sanity. Server must support both
    # Hybi-07 and Hybi-10.
    def test_headersSanity(self):
        for version in ['7', '8', '13']:
            url = base_url.split(':',1)[1] + \
                '/000/' + str(uuid.uuid4()) + '/websocket'
            ws_url = 'ws:' + url
            http_url = 'http:' + url
            origin = '/'.join(http_url.split('/')[:3])
            h = {'Upgrade': 'websocket',
                 'Connection': 'Upgrade',
                 'Sec-WebSocket-Version': version,
                 'Sec-WebSocket-Origin': 'http://asd',
                 'Sec-WebSocket-Key': 'x3JJHMbDL1EzLkh9GBhXDw==',
                 }

            r = GET_async(http_url, headers=h)
            self.assertEqual(r.status, 101)
            self.assertEqual(r['sec-websocket-accept'], 'HSmrc0sMlYUkAGmm5OPpG2HaGWk=')
            self.assertEqual(r['connection'].lower(), 'upgrade')
            self.assertEqual(r['upgrade'].lower(), 'websocket')
            self.assertFalse(r['content-length'])
            r.close()

    # When user sends broken data - broken JSON for example, the
    # server must terminate the ws connection.
    def test_broken_json(self):
        ws_url = 'ws:' + base_url.split(':',1)[1] + \
                 '/000/' + str(uuid.uuid4()) + '/websocket'
        ws = WebSocket8Client(ws_url)
        self.assertEqual(ws.recv(), u'o')
        ws.send(u'"a')
        self.assertRaises(WebSocket8Client.ConnectionClosedException, ws.recv)

    # As a fun part, Firefox 6.0.2 supports Websockets protocol '7'. But,
    # it doesn't send a normal 'Connection: Upgrade' header. Instead it
    # sends: 'Connection: keep-alive, Upgrade'. Brilliant.
    def test_firefox_602_connection_header(self):
        url = base_url.split(':',1)[1] + \
            '/000/' + str(uuid.uuid4()) + '/websocket'
        ws_url = 'ws:' + url
        http_url = 'http:' + url
        origin = '/'.join(http_url.split('/')[:3])
        h = {'Upgrade': 'websocket',
             'Connection': 'keep-alive, Upgrade',
             'Sec-WebSocket-Version': '7',
             'Sec-WebSocket-Origin': 'http://asd',
             'Sec-WebSocket-Key': 'x3JJHMbDL1EzLkh9GBhXDw==',
             }
        r = GET_async(http_url, headers=h)
        self.assertEqual(r.status, 101)


# XhrPolling: `/*/*/xhr`, `/*/*/xhr_send`
# ---------------------------------------
#
# The server must support xhr-polling.
class XhrPolling(Test):
    # The transport must support CORS requests, and answer correctly
    # to OPTIONS requests.
    def test_options(self):
        for suffix in ['/xhr', '/xhr_send']:
            self.verify_options(base_url + '/abc/abc' + suffix,
                                'OPTIONS, POST')

    # Test the transport itself.
    def test_transport(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = POST(url + '/xhr')
        self.assertEqual(r.body, 'o\n')
        self.assertEqual(r.status, 200)
        self.assertEqual(r['content-type'],
                         'application/javascript; charset=UTF-8')
        self.verify_cookie(r)
        self.verify_cors(r)

        # Xhr transports receive json-encoded array of messages.
        r = POST(url + '/xhr_send', body='["x"]')
        self.assertFalse(r.body)
        self.assertEqual(r.status, 204)
        # The content type of `xhr_send` must be set to `text/plain`,
        # even though the response code is `204`. This is due to
        # Firefox/Firebug behaviour - it assumes that the content type
        # is xml and shouts about it.
        self.assertEqual(r['content-type'], 'text/plain')
        self.verify_cookie(r)
        self.verify_cors(r)

        r = POST(url + '/xhr')
        self.assertEqual(r.body, 'a["x"]\n')
        self.assertEqual(r.status, 200)

    # Publishing messages to a non-existing session must result in
    # a 404 error.
    def test_invalid_session(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = POST(url + '/xhr_send', body='["x"]')
        self.verify404(r, cookie=None)

    # The server must behave when invalid json data is send or when no
    # json data is sent at all.
    def test_invalid_json(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = POST(url + '/xhr')
        self.assertEqual(r.body, 'o\n')

        r = POST(url + '/xhr_send', body='["x')
        self.assertEqual(r.body.strip(), "Broken JSON encoding.")
        self.assertEqual(r.status, 500)

        r = POST(url + '/xhr_send', body='')
        self.assertEqual(r.body.strip(), "Payload expected.")
        self.assertEqual(r.status, 500)

        r = POST(url + '/xhr_send', body='["a"]')
        self.assertFalse(r.body)
        self.assertEqual(r.status, 204)

        r = POST(url + '/xhr')
        self.assertEqual(r.body, 'a["a"]\n')
        self.assertEqual(r.status, 200)

    # The server must accept messages send with different content
    # types.
    def test_content_types(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = POST(url + '/xhr')
        self.assertEqual(r.body, 'o\n')

        ctypes = ['text/plain', 'T', 'application/json', 'application/xml', '']
        for ct in ctypes:
            r = POST(url + '/xhr_send', body='["a"]', headers={'Content-Type': ct})
            self.assertFalse(r.body)
            self.assertEqual(r.status, 204)

        r = POST(url + '/xhr')
        self.assertEqual(r.body, 'a["a","a","a","a","a"]\n')
        self.assertEqual(r.status, 200)

    # JSESSIONID cookie must be set by default.
    def test_jsessionid(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = POST(url + '/xhr')
        self.assertEqual(r.body, 'o\n')
        self.verify_cookie(r)

        # And must be echoed back if it's already set.
        url = base_url + '/000/' + str(uuid.uuid4())
        r = POST(url + '/xhr', headers={'Cookie': 'JSESSIONID=abcdef'})
        self.assertEqual(r.body, 'o\n')
        self.assertEqual(r['Set-Cookie'].split(';')[0].strip(),
                         'JSESSIONID=abcdef')
        self.assertEqual(r['Set-Cookie'].split(';')[1].lower().strip(),
                         'path=/')


# XhrStreaming: `/*/*/xhr_streaming`
# ----------------------------------
class XhrStreaming(Test):
    def test_options(self):
        self.verify_options(base_url + '/abc/abc/xhr_streaming',
                            'OPTIONS, POST')

    def test_transport(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = POST_async(url + '/xhr_streaming')
        self.assertEqual(r.status, 200)
        self.assertEqual(r['Content-Type'],
                         'application/javascript; charset=UTF-8')
        self.verify_cookie(r)
        self.verify_cors(r)

        # The transport must first send 2KiB of `h` bytes as prelude.
        self.assertEqual(r.read(), 'h' *  2048 + '\n')

        self.assertEqual(r.read(), 'o\n')

        r1 = POST(url + '/xhr_send', body='["x"]')
        self.assertFalse(r1.body)
        self.assertEqual(r1.status, 204)

        self.assertEqual(r.read(), 'a["x"]\n')
        r.close()


# EventSource: `/*/*/eventsource`
# -------------------------------
#
# For details of this protocol framing read the spec:
#
# * [http://dev.w3.org/html5/eventsource/](http://dev.w3.org/html5/eventsource/)
#
# Beware leading spaces.
class EventSource(Test):
    def test_transport(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = GET_async(url + '/eventsource')
        self.assertEqual(r.status, 200)
        self.assertEqual(r['Content-Type'],
                         'text/event-stream; charset=UTF-8')
        # As EventSource is requested using GET we must be very
        # carefull not to allow it being cached.
        self.verify_not_cached(r)
        self.verify_cookie(r)

        # The transport must first send a new line prelude, due to a
        # bug in Opera.
        self.assertEqual(r.read(), '\r\n')

        self.assertEqual(r.read(), 'data: o\r\n\r\n')

        r1 = POST(url + '/xhr_send', body='["x"]')
        self.assertFalse(r1.body)
        self.assertEqual(r1.status, 204)

        self.assertEqual(r.read(), 'data: a["x"]\r\n\r\n')

        # This protocol doesn't allow binary data and we need to
        # specially treat leading space, new lines and things like
        # \x00. But, now the protocol json-encodes everything, so
        # there is no way to trigger this case.
        r1 = POST(url + '/xhr_send', body=r'["  \u0000\n\r "]')
        self.assertFalse(r1.body)
        self.assertEqual(r1.status, 204)

        self.assertEqual(r.read(),
                         'data: a["  \\u0000\\n\\r "]\r\n\r\n')

        r.close()


# HtmlFile: `/*/*/htmlfile`
# -------------------------
#
# Htmlfile transport is based on research done by Michael Carter. It
# requires a famous `document.domain` trick. Read on:
#
# * [http://stackoverflow.com/questions/1481251/what-does-document-domain-document-domain-do](http://stackoverflow.com/questions/1481251/what-does-document-domain-document-domain-do)
# * [http://cometdaily.com/2007/11/18/ie-activexhtmlfile-transport-part-ii/](http://cometdaily.com/2007/11/18/ie-activexhtmlfile-transport-part-ii/)
#
class HtmlFile(Test):
    head = r'''
<!doctype html>
<html><head>
  <meta http-equiv="X-UA-Compatible" content="IE=edge" />
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head><body><h2>Don't panic!</h2>
  <script>
    document.domain = document.domain;
    var c = parent.%s;
    c.start();
    function p(d) {c.message(d);};
    window.onload = function() {c.stop();};
  </script>
'''.strip()

    def test_transport(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = GET_async(url + '/htmlfile?c=%63allback')
        self.assertEqual(r.status, 200)
        self.assertEqual(r['Content-Type'],
                         'text/html; charset=UTF-8')
        # As HtmlFile is requested using GET we must be very careful
        # not to allow it being cached.
        self.verify_not_cached(r)
        self.verify_cookie(r)

        d = r.read()
        self.assertEqual(d.strip(), self.head % ('callback',))
        self.assertGreater(len(d), 1024)
        self.assertEqual(r.read(),
                         '<script>\np("o");\n</script>\r\n')

        r1 = POST(url + '/xhr_send', body='["x"]')
        self.assertFalse(r1.body)
        self.assertEqual(r1.status, 204)

        self.assertEqual(r.read(),
                         '<script>\np("a[\\"x\\"]");\n</script>\r\n')
        r.close()

    def test_no_callback(self):
        r = GET(base_url + '/a/a/htmlfile')
        self.assertEqual(r.status, 500)
        self.assertEqual(r.body.strip(), '"callback" parameter required')

# JsonpPolling: `/*/*/jsonp`, `/*/*/jsonp_send`
# ---------------------------------------------
class JsonPolling(Test):
    def test_transport(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = GET(url + '/jsonp?c=%63allback')
        self.assertEqual(r.status, 200)
        self.assertEqual(r['Content-Type'],
                         'application/javascript; charset=UTF-8')
        # As JsonPolling is requested using GET we must be very
        # carefull not to allow it being cached.
        self.verify_not_cached(r)
        self.verify_cookie(r)

        self.assertEqual(r.body, 'callback("o");\r\n')

        r = POST(url + '/jsonp_send', body='d=%5B%22x%22%5D',
                 headers={'Content-Type': 'application/x-www-form-urlencoded'})
        # Konqueror does weird things on 204. As a workaround we need
        # to respond with something - let it be the string `ok`.
        self.assertEqual(r.body, 'ok')
        self.assertEqual(r.status, 200)
        self.assertFalse(r['Content-Type'])
        self.verify_cookie(r)

        r = GET(url + '/jsonp?c=%63allback')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'callback("a[\\"x\\"]");\r\n')


    def test_no_callback(self):
        r = GET(base_url + '/a/a/jsonp')
        self.assertEqual(r.status, 500)
        self.assertEqual(r.body.strip(), '"callback" parameter required')

    # The server must behave when invalid json data is send or when no
    # json data is sent at all.
    def test_invalid_json(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = GET(url + '/jsonp?c=x')
        self.assertEqual(r.body, 'x("o");\r\n')

        r = POST(url + '/jsonp_send', body='d=%5B%22x',
                 headers={'Content-Type': 'application/x-www-form-urlencoded'})
        self.assertEqual(r.body.strip(), "Broken JSON encoding.")
        self.assertEqual(r.status, 500)

        for data in ['', 'd=', 'p=p']:
            r = POST(url + '/jsonp_send', body=data,
                     headers={'Content-Type': 'application/x-www-form-urlencoded'})
            self.assertEqual(r.body.strip(), "Payload expected.")
            self.assertEqual(r.status, 500)

        r = POST(url + '/jsonp_send', body='d=%5B%22b%22%5D',
                 headers={'Content-Type': 'application/x-www-form-urlencoded'})
        self.assertEqual(r.body, 'ok')

        r = GET(url + '/jsonp?c=x')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'x("a[\\"b\\"]");\r\n')

    # The server must accept messages sent with different content
    # types.
    def test_content_types(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = GET(url + '/jsonp?c=x')
        self.assertEqual(r.body, 'x("o");\r\n')

        r = POST(url + '/jsonp_send', body='d=%5B%22abc%22%5D',
                 headers={'Content-Type': 'application/x-www-form-urlencoded'})
        self.assertEqual(r.body, 'ok')
        r = POST(url + '/jsonp_send', body='["%61bc"]',
                 headers={'Content-Type': 'text/plain'})
        self.assertEqual(r.body, 'ok')

        r = GET(url + '/jsonp?c=x')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'x("a[\\"abc\\",\\"%61bc\\"]");\r\n')


# Protocol Quirks
# ===============
#
# Over the time there were various implementation quirks
# found. Following tests go through the quirks and verify that the
# server behaves itself.
#
# This is less about defining the protocol and more about sanity checking
# implementations.
class ProtocolQuirks(Test):
    def test_closeSession_another_connection(self):
        # When server is closing session, it should unlink current
        # request. That means, if a new request appears, it should
        # receive an application close message rather than "Another
        # connection still open" message.
        url = close_base_url + '/000/' + str(uuid.uuid4())
        r1 = POST_async(url + '/xhr_streaming')

        r1.read() # prelude
        self.assertEqual(r1.read(), 'o\n')
        self.assertEqual(r1.read(), 'c[3000,"Go away!"]\n')

        r2 = POST_async(url + '/xhr_streaming')
        r2.read() # prelude
        self.assertEqual(r2.read(), 'c[3000,"Go away!"]\n')

        ''' TODO: should request be automatically closed after close?
        self.assertEqual(r1.read(), None)
        self.assertEqual(r2.read(), None)
        '''

# Footnote
# ========

# Make this script runnable.
if __name__ == '__main__':
    unittest.main()
