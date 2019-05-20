#!/usr/bin/env python
"""
[**SockJS-protocol**](https://github.com/sockjs/sockjs-protocol) is an
effort to define a protocol between in-browser
[SockJS-client](https://github.com/sockjs/sockjs-client) and its
server-side counterparts, like
[SockJS-node](https://github.com/sockjs/sockjs-node). This should
help others to write alternative server implementations.


This protocol definition is also a runnable test suite, do run it
against your server implementation. Supporting all the tests doesn't
guarantee that SockJS client will work flawlessly, end-to-end tests
using real browsers are always required.
"""
import os
import random
import time
import json
import re
import unittest2 as unittest
from utils import GET, GET_async, POST, POST_async, OPTIONS, old_POST_async
from utils import WebSocket8Client
from utils import RawHttpConnection
import uuid


# Base URL
# ========

"""
The SockJS server provides one or more SockJS services. The services
are usually exposed with a simple url prefix, like:
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
 - `cookie_needed_echo` - identical to `echo`, but with JSESSIONID cookies sent
 - `close` - server immediately closes the session

This tests should not be run more often than once in five seconds -
many tests operate on the same (named) sessions and they need to have
enough time to timeout.
"""
test_top_url = os.environ.get('SOCKJS_URL', 'http://localhost:8081')
base_url = test_top_url + '/echo'
close_base_url = test_top_url + '/close'
wsoff_base_url = test_top_url + '/disabled_websocket_echo'
cookie_base_url = test_top_url + '/cookie_needed_echo'


# Static URLs
# ===========

class Test(unittest.TestCase):
    # We are going to test several `404/not found` pages. We don't
    # define a body or a content type.
    def verify404(self, r):
        self.assertEqual(r.status, 404)

    # In some cases `405/method not allowed` is more appropriate.
    def verify405(self, r):
        self.assertEqual(r.status, 405)
        self.assertFalse(r['content-type'])
        self.assertTrue(r['allow'])
        self.assertFalse(r.body)

    # Compare the 'content-type' header ignoring spaces
    def verify_content_type(self, r, content_type):
        self.assertEqual(r['content-type'].replace(' ', ''), content_type)

    # Multiple transport protocols need to support OPTIONS method. All
    # responses to OPTIONS requests must be cacheable and contain
    # appropriate headers.
    def verify_options(self, url, allowed_methods):
        for origin in ['test', 'null']:
            h = {'Access-Control-Request-Method': allowed_methods, 'Origin': origin}
            r = OPTIONS(url, headers=h)
            # A 200 'OK' or a 204 'No Content' should both be acceptable as responses for a CORS request.
            self.assertTrue(r.status == 204 or r.status == 200)
            self.assertTrue(re.search('public', r['Cache-Control']))
            self.assertTrue(re.search('max-age=[1-9][0-9]{6}', r['Cache-Control']),
                            "max-age must be large, one year (31536000) is best")
            self.assertTrue(r['Expires'])
            self.assertTrue(int(r['access-control-max-age']) > 1000000)
            # A server may respond to a preflight request with HTTP methods in addition to method specified in the 'Access-Control-Request-Method' header
            for header in allowed_methods.split(','):
                self.assertTrue(header.strip() in r['Access-Control-Allow-Methods'], 'Access-Control-Allow-Methods did not contain :' + header)
            self.assertFalse(r.body)
            self.verify_cors(r, origin)

    def verify_no_cookie(self, r):
        self.assertFalse(r['Set-Cookie'])

    # Most of the XHR/Ajax based transports do work CORS if proper
    # headers are set.
    def verify_cors(self, r, origin=None):
        if origin:
            self.assertEqual(r['access-control-allow-origin'], origin)
            # In order to get cookies (`JSESSIONID` mostly) flying, we
            # need to set `allow-credentials` header to true.
            self.assertEqual(r['access-control-allow-credentials'], 'true')
        else:
            self.assertEqual(r['access-control-allow-origin'], '*')
            self.assertFalse(r['access-control-allow-credentials'])

    # Sometimes, due to transports limitations we need to request
    # private data using GET method. In such case it's very important
    # to disallow any caching.
    def verify_not_cached(self, r, origin=None):
        self.assertEqual(r['Cache-Control'],
                         'no-store, no-cache, no-transform, must-revalidate, max-age=0')
        self.assertFalse(r['Expires'])
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
            self.verify_content_type(r, 'text/plain;charset=UTF-8')
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
  <script src="(?P<sockjs_url>[^"]*)"></script>
  <script>
    document.domain = document.domain;
    SockJS.bootstrap_iframe\(\);
  </script>
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
        self.verify_content_type(r, 'text/html;charset=UTF-8')
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

# Info test: `/info`
# ------------------
#
# Warning: this is a replacement of `/chunking_test` functionality
# from SockJS 0.1.
class InfoTest(Test):
    # This url is called before the client starts the session. It's
    # used to check server capabilities (websocket support, cookies
    # requiremet) and to get the value of "origin" setting (currently
    # not used).
    #
    # But more importantly, the call to this url is used to measure
    # the roundtrip time between the client and the server. So, please,
    # do respond to this url in a timely fashion.
    def test_basic(self):
        r = GET(base_url + '/info', headers={'Origin': 'test'})
        self.assertEqual(r.status, 200)
        self.verify_content_type(r, 'application/json;charset=UTF-8')
        self.verify_no_cookie(r)
        self.verify_not_cached(r)
        self.verify_cors(r, 'test')

        data = json.loads(r.body)
        # Are websockets enabled on the server?
        self.assertEqual(data['websocket'], True)
        # Do transports need to support cookies (ie: for load
        # balancing purposes.
        self.assertTrue(data['cookie_needed'] in  [True, False])
        # List of allowed origins. Currently ignored.
        self.assertEqual(data['origins'], ['*:*'])
        # Source of entropy for random number generator.
        self.assertTrue(type(data['entropy']) in [int, long])

    # As browsers don't have a good entropy source, the server must
    # help with tht. Info url must supply a good, unpredictable random
    # number from the range <0; 2^32-1> to feed the browser.
    def test_entropy(self):
        r1 = GET(base_url + '/info')
        data1 = json.loads(r1.body)
        r2 = GET(base_url + '/info')
        data2 = json.loads(r2.body)
        self.assertTrue(type(data1['entropy']) in [int, long])
        self.assertTrue(type(data2['entropy']) in [int, long])
        self.assertNotEqual(data1['entropy'], data2['entropy'])

    # Info url must support CORS.
    def test_options(self):
        self.verify_options(base_url + '/info', 'OPTIONS, GET')

    # SockJS client may be hosted from file:// url. In practice that
    # means the 'Origin' headers sent by the browser will have a value
    # of a string "null". Unfortunately, just echoing back "null"
    # won't work - browser will understand that as a rejection. We
    # must respond with star "*" origin in such case.
    def test_options_null_origin(self):
            url = base_url + '/info'
            r = OPTIONS(url, headers={'Origin': 'null', 'Access-Control-Request-Method': 'POST'})
            self.assertTrue(r.status == 204 or r.status == 200)
            self.assertFalse(r.body)
            self.assertEqual(r['access-control-allow-origin'], 'null')

    # The 'disabled_websocket_echo' service should have websockets
    # disabled.
    def test_disabled_websocket(self):
        r = GET(wsoff_base_url + '/info')
        self.assertEqual(r.status, 200)
        data = json.loads(r.body)
        self.assertEqual(data['websocket'], False)


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
        # add some randomness, so that test could be rerun immediately.
        r = '%s' % random.randint(0, 1024)
        self.verify('/a/a' + r)
        for session_part in ['/_/_' + r, '/1/' + r, '/abcdefgh_i-j%20/abcdefg_i-j%20'+ r]:
            self.verify(session_part)

    # To test session URLs we're going to use `xhr-polling` transport
    # facilitites.
    def verify(self, session_part):
        r = POST(base_url + session_part + '/xhr')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'o\n')

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
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'o\n')

        payload = '["a"]'
        r = POST(base_url + '/000/' + session_id + '/xhr_send', body=payload)
        self.assertEqual(r.status, 204)
        self.assertFalse(r.body)

        r = POST(base_url + '/999/' + session_id + '/xhr')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'a["a"]\n')

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
# * `c` - Close frame. This frame is sent to the browser every time
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
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'o\n')

        # After a session was established the server needs to accept
        # requests for sending messages.
        "Xhr-polling accepts messages as a list of JSON-encoded strings."
        payload = '["a"]'
        r = POST(trans_url + '/xhr_send', body=payload)
        self.assertEqual(r.status, 204)
        self.assertFalse(r.body)

        '''We're using an echo service - we'll receive our message
        back. The message is encoded as an array 'a'.'''
        r = POST(trans_url + '/xhr')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'a["a"]\n')

        # Sending messages to not existing sessions is invalid.
        payload = '["a"]'
        r = POST(base_url + '/000/bad_session/xhr_send', body=payload)
        self.verify404(r)

        # The session must time out after 5 seconds of not having a
        # receiving connection. The server must send a heartbeat frame
        # every 25 seconds. The heartbeat frame contains a single `h`
        # character. This delay may be configurable.
        pass
        # The server must not allow two receiving connections to wait
        # on a single session. In such case the server must send a
        # close frame to the new connection.
        r1 = old_POST_async(trans_url + '/xhr', load=False)
        time.sleep(0.25)
        r2 = POST(trans_url + '/xhr')

        self.assertEqual(r2.body, 'c[2010,"Another connection still open"]\n')
        self.assertEqual(r2.status, 200)

        r1.close()

    # The server may terminate the connection, passing error code and
    # message.
    def test_closeSession(self):
        trans_url = close_base_url + '/000/' + str(uuid.uuid4())
        r = POST(trans_url + '/xhr')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'o\n')

        r = POST(trans_url + '/xhr')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'c[3000,"Go away!"]\n')

        # Until the timeout occurs, the server must constantly serve
        # the close message.

        r = POST(trans_url + '/xhr')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'c[3000,"Go away!"]\n')


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
    # Normal requests to websocket should not succeed.
    def test_httpMethod(self):
        r = GET(base_url + '/0/0/websocket')
        self.assertEqual(r.status, 400)

    # Some proxies and load balancers can rewrite 'Connection' header,
    # in such case we must refuse connection.
    def test_invalidConnectionHeader(self):
        r = GET(base_url + '/0/0/websocket', headers={'Upgrade': 'WebSocket',
                                                      'Connection': 'close'})
        self.assertEqual(r.status, 400)
        self.assertTrue('Not a valid websocket request', r.body)

    # WebSocket should only accept GET
    def test_invalidMethod(self):
        for h in [{'Upgrade': 'WebSocket', 'Connection': 'Upgrade'},
                  {}]:
            r = POST(base_url + '/0/0/websocket', headers=h)
            self.verify405(r)


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

        # The connection should be closed after the close frame.
        with self.assertRaises(websocket.ConnectionClosedException):
            if ws.recv() is None:
                raise websocket.ConnectionClosedException
        ws.close()

    # Empty frames must be ignored by the server side.
    def test_empty_frame(self):
        ws_url = 'ws:' + base_url.split(':',1)[1] + \
                 '/000/' + str(uuid.uuid4()) + '/websocket'
        ws = websocket.create_connection(ws_url)
        self.assertEqual(ws.recv(), u'o')
        # Server must ignore empty messages.
        ws.send(u'')
        # Server must also ignore frames with no messages.
        ws.send(u'[]')
        ws.send(u'["a"]')
        self.assertEqual(ws.recv(), u'a["a"]')
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

        ws1.send(u'["a"]')
        self.assertEqual(ws1.recv(), u'a["a"]')

        ws2.send(u'["b"]')
        self.assertEqual(ws2.recv(), u'a["b"]')

        ws1.close()
        ws2.close()

        # It is correct to reuse the same `session_id` after closing a
        # previous connection.
        ws1 = websocket.create_connection(ws_url)
        self.assertEqual(ws1.recv(), u'o')
        ws1.send(u'["a"]')
        self.assertEqual(ws1.recv(), u'a["a"]')
        ws1.close()

    # # WARNING: This functionality is not supported by Netty/Vert.x
    # #
    # # Verify WebSocket headers sanity. Due to HAProxy design the
    # # websocket server must support writing response headers *before*
    # # receiving -76 nonce. In other words, the websocket code must
    # # work like that:
    # #
    # # * Receive request headers.
    # # * Write response headers.
    # # * Receive request nonce.
    # # * Write response nonce.
    # def test_haproxy(self):
    #     url = base_url.split(':',1)[1] + \
    #              '/000/' + str(uuid.uuid4()) + '/websocket'
    #     ws_url = 'ws:' + url
    #     http_url = 'http:' + url
    #     origin = '/'.join(http_url.split('/')[:3])
    #
    #     c = RawHttpConnection(http_url)
    #     r = c.request('GET', http_url, http='1.1', headers={
    #             'Connection':'Upgrade',
    #             'Upgrade':'WebSocket',
    #             'Origin': origin,
    #             'Sec-WebSocket-Key1': '4 @1  46546xW%0l 1 5',
    #             'Sec-WebSocket-Key2': '12998 5 Y3 1  .P00'
    #             })
    #     # First check response headers
    #     self.assertEqual(r.status, 101)
    #     self.assertEqual(r.headers['connection'].lower(), 'upgrade')
    #     self.assertEqual(r.headers['upgrade'].lower(), 'websocket')
    #     self.assertEqual(r.headers['sec-websocket-location'], ws_url)
    #     self.assertEqual(r.headers['sec-websocket-origin'], origin)
    #     self.assertFalse('Content-Length' in r.headers)
    #     # Later send token
    #     c.send('aaaaaaaa')
    #     self.assertEqual(c.read()[:16],
    #                      '\xca4\x00\xd8\xa5\x08G\x97,\xd5qZ\xba\xbfC{')

    # When user sends broken data - broken JSON for example, the
    # server must abruptly terminate the ws connection.
    def test_broken_json(self):
        ws_url = 'ws:' + base_url.split(':',1)[1] + \
                 '/000/' + str(uuid.uuid4()) + '/websocket'
        ws = websocket.create_connection(ws_url)
        self.assertEqual(ws.recv(), u'o')
        ws.send(u'["a')
        with self.assertRaises(websocket.ConnectionClosedException):
            if ws.recv() is None:
                raise websocket.ConnectionClosedException
        ws.close()


# The server must support Hybi-10 protocol
class WebsocketHybi10(Test):
    def test_transport(self):
        trans_url = base_url.replace('http', 'ws') + '/000/' + str(uuid.uuid4()) + '/websocket'
        ws = WebSocket8Client(trans_url)

        self.assertEqual(ws.recv(), 'o')
        # Server must ignore empty messages.
        ws.send(u'')
        ws.send(u'["a"]')
        self.assertEqual(ws.recv(), 'a["a"]')
        ws.close()

    def test_close(self):
        trans_url = close_base_url.replace('http', 'ws') + '/000/' + str(uuid.uuid4()) + '/websocket'
        ws = WebSocket8Client(trans_url)
        self.assertEqual(ws.recv(), u'o')
        self.assertEqual(ws.recv(), u'c[3000,"Go away!"]')
        with self.assertRaises(ws.ConnectionClosedException):
            ws.recv()
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
    # server must abruptly terminate the ws connection.
    def test_broken_json(self):
        ws_url = 'ws:' + base_url.split(':',1)[1] + \
                 '/000/' + str(uuid.uuid4()) + '/websocket'
        ws = WebSocket8Client(ws_url)
        self.assertEqual(ws.recv(), u'o')
        ws.send(u'["a')
        with self.assertRaises(ws.ConnectionClosedException):
            ws.recv()
        ws.close()

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
        r = POST(url + '/xhr', headers={'Origin': 'test'})
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'o\n')
        self.verify_content_type(r, 'application/javascript;charset=UTF-8')
        self.verify_cors(r, 'test')
        # iOS 6 caches POSTs. Make sure we send no-cache header.
        self.verify_not_cached(r)

        # Xhr transports receive json-encoded array of messages.
        r = POST(url + '/xhr_send', body='["x"]', headers={'Origin': 'test'})
        self.assertEqual(r.status, 204)
        self.assertFalse(r.body)
        # The content type of `xhr_send` must be set to `text/plain`,
        # even though the response code is `204`. This is due to
        # Firefox/Firebug behaviour - it assumes that the content type
        # is xml and shouts about it.
        self.verify_content_type(r, 'text/plain;charset=UTF-8')
        self.verify_cors(r, 'test')
        # iOS 6 caches POSTs. Make sure we send no-cache header.
        self.verify_not_cached(r)

        r = POST(url + '/xhr')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'a["x"]\n')

    # Publishing messages to a non-existing session must result in
    # a 404 error.
    def test_invalid_session(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = POST(url + '/xhr_send', body='["x"]')
        self.verify404(r)

    # The server must behave when invalid json data is sent or when no
    # json data is sent at all.
    def test_invalid_json(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = POST(url + '/xhr')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'o\n')

        r = POST(url + '/xhr_send', body='["x')
        self.assertEqual(r.status, 500)
        self.assertTrue("Broken JSON encoding." in r.body)

        r = POST(url + '/xhr_send', body='')
        self.assertEqual(r.status, 500)
        self.assertTrue("Payload expected." in r.body)

        r = POST(url + '/xhr_send', body='["a"]')
        self.assertFalse(r.body)
        self.assertEqual(r.status, 204)

        r = POST(url + '/xhr')
        self.assertEqual(r.body, 'a["a"]\n')
        self.assertEqual(r.status, 200)

    # The server must accept messages sent with different content
    # types.
    def test_content_types(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = POST(url + '/xhr')
        self.assertEqual(r.body, 'o\n')

        ctypes = ['text/plain', 'T', 'application/json', 'application/xml', '',
                  'application/json; charset=utf-8', 'text/xml; charset=utf-8',
                  'text/xml']
        for ct in ctypes:
            r = POST(url + '/xhr_send', body='["a"]', headers={'Content-Type': ct})
            self.assertEqual(r.status, 204)
            self.assertFalse(r.body)

        r = POST(url + '/xhr')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'a[' + (',').join(['"a"']*len(ctypes)) +']\n')

    # When client sends a CORS request with
    # 'Access-Control-Request-Headers' header set, the server must
    # echo back this header as 'Access-Control-Allow-Headers'. This is
    # required in order to get CORS working. Browser will be unhappy
    # otherwise.
    def test_request_headers_cors(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = OPTIONS(url + '/xhr',
                headers={'Origin': 'test', 'Access-Control-Request-Method': 'POST', 'Access-Control-Request-Headers': 'a, b, c'})
        self.assertTrue(r.status == 204 or r.status == 200)
        self.verify_cors(r, 'test')
        self.assertEqual(r['Access-Control-Allow-Headers'], 'a, b, c')

        url = base_url + '/000/' + str(uuid.uuid4())
        r = OPTIONS(url + '/xhr',
                headers={'Origin': 'test', 'Access-Control-Request-Method': 'POST', 'Access-Control-Request-Headers': ''})
        self.assertTrue(r.status == 204 or r.status == 200)
        self.verify_cors(r, 'test')
        self.assertFalse(r['Access-Control-Allow-Headers'])

        url = base_url + '/000/' + str(uuid.uuid4())
        r = OPTIONS(url + '/xhr',
                headers={'Origin': 'test', 'Access-Control-Request-Method': 'POST'})
        self.assertTrue(r.status == 204 or r.status == 200)
        self.verify_cors(r, 'test')
        self.assertFalse(r['Access-Control-Allow-Headers'])

    # The client must be able to send frames containint no messages to
    # the server.  This is used as a heartbeat mechanism - client may
    # voluntairly send frames with no messages once in a while.
    def test_sending_empty_frame(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = POST(url + '/xhr')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'o\n')

        # Sending empty frames with no data must allowed.
        r = POST(url + '/xhr_send', body='[]')
        self.assertEqual(r.status, 204)

        r = POST(url + '/xhr_send', body='["a"]')
        self.assertEqual(r.status, 204)

        r = POST(url + '/xhr')
        self.assertEqual(r.body, 'a["a"]\n')
        self.assertEqual(r.status, 200)


# XhrStreaming: `/*/*/xhr_streaming`
# ----------------------------------
class XhrStreaming(Test):
    def test_options(self):
        self.verify_options(base_url + '/abc/abc/xhr_streaming',
                            'OPTIONS, POST')

    def test_transport(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = POST_async(url + '/xhr_streaming', headers={'Origin': 'test'})
        self.assertEqual(r.status, 200)
        self.verify_content_type(r, 'application/javascript;charset=UTF-8')
        self.verify_cors(r, 'test')
        # iOS 6 caches POSTs. Make sure we send no-cache header.
        self.verify_not_cached(r)

        # The transport must first send 2KiB of `h` bytes as prelude.
        self.assertEqual(r.read(), 'h' *  2048 + '\n')

        self.assertEqual(r.read(), 'o\n')

        r1 = POST(url + '/xhr_send', body='["x"]')
        self.assertEqual(r1.status, 204)
        self.assertFalse(r1.body)

        self.assertEqual(r.read(), 'a["x"]\n')
        r.close()

    def test_response_limit(self):
        # Single streaming request will buffer all data until
        # closed. In order to remove (garbage collect) old messages
        # from the browser memory we should close the connection every
        # now and then. By default we should close a streaming request
        # every 128KiB messages was send. The test server should have
        # this limit decreased to 4096B.
        url = base_url + '/000/' + str(uuid.uuid4())
        r = POST_async(url + '/xhr_streaming')
        self.assertEqual(r.status, 200)
        self.assertTrue(r.read()) # prelude
        self.assertEqual(r.read(), 'o\n')

        # Test server should gc streaming session after 4096 bytes
        # were sent (including framing).
        msg = '"' + ('x' * 128) + '"'
        for i in range(31):
            r1 = POST(url + '/xhr_send', body='[' + msg + ']')
            self.assertEqual(r1.status, 204)
            self.assertEqual(r.read(), 'a[' + msg + ']\n')

        # The connection should be closed after enough data was
        # delivered.
        self.assertFalse(r.read())


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
        self.verify_content_type(r, 'text/event-stream')
        # As EventSource is requested using GET we must be very
        # careful not to allow it being cached.
        self.verify_not_cached(r)

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

    def test_response_limit(self):
        # Single streaming request should be closed after enough data
        # was delivered (by default 128KiB, but 4KiB for test server).
        # Although EventSource transport is better, and in theory may
        # not need this mechanism, there are some bugs in the browsers
        # that actually prevent the automatic GC. See:
        #  * https://bugs.webkit.org/show_bug.cgi?id=61863
        #  * http://code.google.com/p/chromium/issues/detail?id=68160
        url = base_url + '/000/' + str(uuid.uuid4())
        r = GET_async(url + '/eventsource')
        self.assertEqual(r.status, 200)
        self.assertTrue(r.read()) # prelude
        self.assertEqual(r.read(), 'data: o\r\n\r\n')

        # Test server should gc streaming session after 4096 bytes
        # were sent (including framing).
        msg = '"' + ('x' * 4096) + '"'
        r1 = POST(url + '/xhr_send', body='[' + msg + ']')
        self.assertEqual(r1.status, 204)
        self.assertEqual(r.read(), 'data: a[' + msg + ']\r\n\r\n')

        # The connection should be closed after enough data was
        # delivered.
        self.assertFalse(r.read())


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
        self.verify_content_type(r, 'text/html;charset=UTF-8')
        # As HtmlFile is requested using GET we must be very careful
        # not to allow it being cached.
        self.verify_not_cached(r)

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
        self.assertTrue('"callback" parameter required' in r.body)

    # Supplying invalid characters to callback parameter is invalid
    # and must result in a 500 errors. Invalid characters are any
    # matching the following regexp: `[^a-zA-Z0-9-_.]`
    def test_invalid_callback(self):
        for callback in ['%20', '*', 'abc(', 'abc%28']:
            r = GET(base_url + '/a/a/htmlfile?c=' + callback)
            self.assertEqual(r.status, 500)
            self.assertTrue('invalid "callback" parameter' in r.body)

    def test_response_limit(self):
        # Single streaming request should be closed after enough data
        # was delivered (by default 128KiB, but 4KiB for test server).
        url = base_url + '/000/' + str(uuid.uuid4())
        r = GET_async(url + '/htmlfile?c=callback')
        self.assertEqual(r.status, 200)
        self.assertTrue(r.read()) # prelude
        self.assertEqual(r.read(),
                         '<script>\np("o");\n</script>\r\n')

        # Test server should gc streaming session after 4096 bytes
        # were sent (including framing).
        msg = ('x' * 4096)
        r1 = POST(url + '/xhr_send', body='["' + msg + '"]')
        self.assertEqual(r1.status, 204)
        self.assertEqual(r.read(),
                         '<script>\np("a[\\"' + msg + '\\"]");\n</script>\r\n')

        # The connection should be closed after enough data was
        # delivered.
        self.assertFalse(r.read())

# JsonpPolling: `/*/*/jsonp`, `/*/*/jsonp_send`
# ---------------------------------------------
class JsonPolling(Test):
    def test_transport(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = GET(url + '/jsonp?c=%63allback')
        self.assertEqual(r.status, 200)
        self.verify_content_type(r, 'application/javascript;charset=UTF-8')
        # As JsonPolling is requested using GET we must be very
        # careful not to allow it being cached.
        self.verify_not_cached(r)

        self.assertEqual(r.body, '/**/callback("o");\r\n')

        r = POST(url + '/jsonp_send', body='d=%5B%22x%22%5D',
                 headers={'Content-Type': 'application/x-www-form-urlencoded'})
        # Konqueror does weird things on 204. As a workaround we need
        # to respond with something - let it be the string `ok`.
        self.assertEqual(r.body, 'ok')
        self.assertEqual(r.status, 200)
        self.verify_content_type(r, 'text/plain;charset=UTF-8')
        # iOS 6 caches POSTs. Make sure we send no-cache header.
        self.verify_not_cached(r)

        r = GET(url + '/jsonp?c=%63allback')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, '/**/callback("a[\\"x\\"]");\r\n')


    def test_no_callback(self):
        r = GET(base_url + '/a/a/jsonp')
        self.assertEqual(r.status, 500)
        self.assertTrue('"callback" parameter required' in r.body)

    # Supplying invalid characters to callback parameter is invalid
    # and must result in a 500 errors. Invalid characters are any
    # matching the following regexp: `[^a-zA-Z0-9-_.]`
    def test_invalid_callback(self):
        for callback in ['%20', '*', 'abc(', 'abc%28']:
            r = GET(base_url + '/a/a/jsonp?c=' + callback)
            self.assertEqual(r.status, 500)
            self.assertTrue('invalid "callback" parameter' in r.body)

    # The server must behave when invalid json data is sent or when no
    # json data is sent at all.
    def test_invalid_json(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = GET(url + '/jsonp?c=x')
        self.assertEqual(r.body, '/**/x("o");\r\n')

        r = POST(url + '/jsonp_send', body='d=%5B%22x',
                 headers={'Content-Type': 'application/x-www-form-urlencoded'})
        self.assertEqual(r.status, 500)
        self.assertTrue("Broken JSON encoding." in r.body)

        for data in ['', 'd=', 'p=p']:
            r = POST(url + '/jsonp_send', body=data,
                     headers={'Content-Type': 'application/x-www-form-urlencoded'})
            self.assertEqual(r.status, 500)
            self.assertTrue("Payload expected." in r.body)

        r = POST(url + '/jsonp_send', body='d=%5B%22b%22%5D',
                 headers={'Content-Type': 'application/x-www-form-urlencoded'})
        self.assertEqual(r.body, 'ok')

        r = GET(url + '/jsonp?c=x')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, '/**/x("a[\\"b\\"]");\r\n')

    # The server must accept messages sent with different content
    # types.
    def test_content_types(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = GET(url + '/jsonp?c=x')
        self.assertEqual(r.body, '/**/x("o");\r\n')

        r = POST(url + '/jsonp_send', body='d=%5B%22abc%22%5D',
                 headers={'Content-Type': 'application/x-www-form-urlencoded'})
        self.assertEqual(r.body, 'ok')
        r = POST(url + '/jsonp_send', body='["%61bc"]',
                 headers={'Content-Type': 'text/plain'})
        self.assertEqual(r.body, 'ok')

        r = GET(url + '/jsonp?c=x')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, '/**/x("a[\\"abc\\",\\"%61bc\\"]");\r\n')

    def test_close(self):
        url = close_base_url + '/000/' + str(uuid.uuid4())
        r = GET(url + '/jsonp?c=x')
        self.assertEqual(r.body, '/**/x("o");\r\n')

        r = GET(url + '/jsonp?c=x')
        self.assertEqual(r.body, '/**/x("c[3000,\\"Go away!\\"]");\r\n')

        r = GET(url + '/jsonp?c=x')
        self.assertEqual(r.body, '/**/x("c[3000,\\"Go away!\\"]");\r\n')

    def test_sending_empty_frame(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r = GET(url + '/jsonp?c=x')
        self.assertEqual(r.body, '/**/x("o");\r\n')

        # Sending frames containing no messages must be allowed.
        r = POST(url + '/jsonp_send', body='d=%5B%5D',
                 headers={'Content-Type': 'application/x-www-form-urlencoded'})
        self.assertEqual(r.body, 'ok')

        r = POST(url + '/jsonp_send', body='d=%5B%22x%22%5D',
                 headers={'Content-Type': 'application/x-www-form-urlencoded'})
        self.assertEqual(r.body, 'ok')

        r = GET(url + '/jsonp?c=x')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, '/**/x("a[\\"x\\"]");\r\n')


# JSESSIONID cookie
# -----------------
#
# All transports except WebSockets need sticky session support from
# the load balancer. Some load balancers enable that only when they
# see `JSESSIONID` cookie. User of a sockjs server must be able to
# opt-in for this functionality - and set this cookie for all the
# session urls.
#
# Detailed explanation of this functionality is available [in this
# thread on SockJS mailing
# list](https://groups.google.com/group/sockjs/msg/ef0c508bb774a9ac).
#
class JsessionidCookie(Test):
    # Verify if info has cookie_needed set.
    def test_basic(self):
        r = GET(cookie_base_url + '/info')
        self.assertEqual(r.status, 200)
        self.verify_no_cookie(r)

        data = json.loads(r.body)
        self.assertEqual(data['cookie_needed'], True)

    # Helper to check cookie validity.
    def verify_cookie(self, r):
        self.assertEqual(r['Set-Cookie'].split(';')[0].strip(),
                         'JSESSIONID=dummy')
        self.assertEqual(r['Set-Cookie'].split(';')[1].lower().strip(),
                         'path=/')

    # JSESSIONID cookie must be set by default
    def test_xhr(self):
        # polling url must set cookies
        url = cookie_base_url + '/000/' + str(uuid.uuid4())
        r = POST(url + '/xhr')
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'o\n')
        self.verify_cookie(r)

        # Cookie must be echoed back if it's already set.
        url = cookie_base_url + '/000/' + str(uuid.uuid4())
        r = POST(url + '/xhr', headers={'Cookie': 'JSESSIONID=abcdef'})
        self.assertEqual(r.status, 200)
        self.assertEqual(r.body, 'o\n')
        self.assertEqual(r['Set-Cookie'].split(';')[0].strip(),
                         'JSESSIONID=abcdef')
        self.assertEqual(r['Set-Cookie'].split(';')[1].lower().strip(),
                         'path=/')

    def test_xhr_streaming(self):
        url = cookie_base_url + '/000/' + str(uuid.uuid4())
        r = POST_async(url + '/xhr_streaming')
        self.assertEqual(r.status, 200)
        self.verify_cookie(r)

    def test_eventsource(self):
        url = cookie_base_url + '/000/' + str(uuid.uuid4())
        r = GET_async(url + '/eventsource')
        self.assertEqual(r.status, 200)
        self.verify_cookie(r)

    def test_htmlfile(self):
        url = cookie_base_url + '/000/' + str(uuid.uuid4())
        r = GET_async(url + '/htmlfile?c=%63allback')
        self.assertEqual(r.status, 200)
        self.verify_cookie(r)

    def test_jsonp(self):
        url = cookie_base_url + '/000/' + str(uuid.uuid4())
        r = GET(url + '/jsonp?c=%63allback')
        self.assertEqual(r.status, 200)
        self.verify_cookie(r)

        self.assertEqual(r.body, '/**/callback("o");\r\n')

        r = POST(url + '/jsonp_send', body='d=%5B%22x%22%5D',
                 headers={'Content-Type': 'application/x-www-form-urlencoded'})
        self.assertEqual(r.body, 'ok')
        self.assertEqual(r.status, 200)
        self.verify_cookie(r)


# Raw WebSocket url: `/websocket`
# -------------------------------
#
# SockJS protocol defines a bit of higher level framing. This is okay
# when the browser uses SockJS-client to establish the connection, but
# it's not really appropriate when the connection is being established
# from another program. Although SockJS focuses on server-browser
# communication, it should be straightforward to connect to SockJS
# from the command line or using any programming language.
#
# In order to make writing command-line clients easier, we define this
# `/websocket` entry point. This entry point is special and doesn't
# use any additional custom framing, no open frame, no
# heartbeats. Only raw WebSocket protocol.
class RawWebsocket(Test):
    def test_transport(self):
        ws = WebSocket8Client(base_url.replace('http', 'ws') + '/websocket')
        ws.send(u'Hello world!\uffff')
        self.assertEqual(ws.recv(), u'Hello world!\uffff')
        ws.close()

    def test_close(self):
        ws = WebSocket8Client(close_base_url.replace('http', 'ws') + '/websocket')
        with self.assertRaises(ws.ConnectionClosedException) as ce:
            ws.recv()
        self.assertEqual(ce.exception.reason, "Go away!")
        ws.close()



# JSON Unicode Encoding
# =====================
#
# SockJS takes the responsibility of encoding Unicode strings for the
# user.  The idea is that SockJS should properly deliver any valid
# string from the browser to the server and back. This is actually
# quite hard, as browsers do some magical character
# translations. Additionally there are some valid characters from
# JavaScript point of view that are not valid Unicode, called
# surrogates (JavaScript uses UCS-2, which is not really Unicode).
#
# Dealing with unicode surrogates (0xD800-0xDFFF) is quite special. If
# possible we should make sure that server does escape decode
# them. This makes sense for SockJS servers that support UCS-2
# (SockJS-node), but can't really work for servers supporting unicode
# properly (Python).
#
# The browser must escape quite a list of chars, this is due to
# browser mangling outgoing chars on transports like XHR.
escapable_by_client = re.compile(u"[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u2000-\u20ff\ufeff\ufff0-\uffff\x00-\x1f\ufffe\uffff\u0300-\u0333\u033d-\u0346\u034a-\u034c\u0350-\u0352\u0357-\u0358\u035c-\u0362\u0374\u037e\u0387\u0591-\u05af\u05c4\u0610-\u0617\u0653-\u0654\u0657-\u065b\u065d-\u065e\u06df-\u06e2\u06eb-\u06ec\u0730\u0732-\u0733\u0735-\u0736\u073a\u073d\u073f-\u0741\u0743\u0745\u0747\u07eb-\u07f1\u0951\u0958-\u095f\u09dc-\u09dd\u09df\u0a33\u0a36\u0a59-\u0a5b\u0a5e\u0b5c-\u0b5d\u0e38-\u0e39\u0f43\u0f4d\u0f52\u0f57\u0f5c\u0f69\u0f72-\u0f76\u0f78\u0f80-\u0f83\u0f93\u0f9d\u0fa2\u0fa7\u0fac\u0fb9\u1939-\u193a\u1a17\u1b6b\u1cda-\u1cdb\u1dc0-\u1dcf\u1dfc\u1dfe\u1f71\u1f73\u1f75\u1f77\u1f79\u1f7b\u1f7d\u1fbb\u1fbe\u1fc9\u1fcb\u1fd3\u1fdb\u1fe3\u1feb\u1fee-\u1fef\u1ff9\u1ffb\u1ffd\u2000-\u2001\u20d0-\u20d1\u20d4-\u20d7\u20e7-\u20e9\u2126\u212a-\u212b\u2329-\u232a\u2adc\u302b-\u302c\uaab2-\uaab3\uf900-\ufa0d\ufa10\ufa12\ufa15-\ufa1e\ufa20\ufa22\ufa25-\ufa26\ufa2a-\ufa2d\ufa30-\ufa6d\ufa70-\ufad9\ufb1d\ufb1f\ufb2a-\ufb36\ufb38-\ufb3c\ufb3e\ufb40-\ufb41\ufb43-\ufb44\ufb46-\ufb4e]")
#
# The server is able to send much more chars verbatim. But, it can't
# send Unicode surrogates over Websockets, also various \u2xxxx chars
# get mangled. Additionally, if the server is capable of handling
# UCS-2 (ie: 16 bit character size), it should be able to deal with
# Unicode surrogates 0xD800-0xDFFF:
# http://en.wikipedia.org/wiki/Mapping_of_Unicode_characters#Surrogates
escapable_by_server = re.compile(u"[\x00-\x1f\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufff0-\uffff]")

client_killer_string_esc = '"' + ''.join([
        r'\u%04x' % (i) for i in range(65536)
            if escapable_by_client.match(unichr(i))]) + '"'
server_killer_string_esc = '"' + ''.join([
        r'\u%04x'% (i) for i in range(255, 65536)
            if escapable_by_server.match(unichr(i))]) + '"'

class JSONEncoding(Test):
    def test_xhr_server_encodes(self):
        # Make sure that server encodes at least all the characters
        # it's supposed to encode.
        trans_url = base_url + '/000/' + str(uuid.uuid4())
        r = POST(trans_url + '/xhr')
        self.assertEqual(r.body, 'o\n')
        self.assertEqual(r.status, 200)

        payload = '["' + json.loads(server_killer_string_esc) + '"]'
        r = POST(trans_url + '/xhr_send', body=payload)
        self.assertEqual(r.status, 204)

        r = POST(trans_url + '/xhr')
        self.assertEqual(r.status, 200)
        # skip framing, quotes and parenthesis
        recv = r.body.strip()[2:-1]

        # Received string is indeed what we sent previously, aka - escaped.
        self.assertEqual(recv, server_killer_string_esc)

    def test_xhr_server_decodes(self):
        # Make sure that server decodes the chars we're customly
        # encoding.
        trans_url = base_url + '/000/' + str(uuid.uuid4())
        r = POST(trans_url + '/xhr')
        self.assertEqual(r.body, 'o\n')
        self.assertEqual(r.status, 200)

        payload = '[' + client_killer_string_esc + ']' # Sending escaped
        r = POST(trans_url + '/xhr_send', body=payload)
        self.assertEqual(r.status, 204)

        r = POST(trans_url + '/xhr')
        self.assertEqual(r.status, 200)
        # skip framing, quotes and parenthesis
        recv = r.body.strip()[2:-1]

        # Received string is indeed what we sent previously. We don't
        # really need to know what exactly got escaped and what not.
        a = json.loads(recv)
        b = json.loads(client_killer_string_esc)
        self.assertEqual(a, b)


# Handling close
# ==============
#
# Dealing with session closure is quite complicated part of the
# protocol. The exact details here don't matter that much to the
# client side, but it's good to have a common behaviour on the server
# side.
#
# This is less about defining the protocol and more about sanity
# checking implementations.
class HandlingClose(Test):
    # When server is closing session, it should unlink current
    # request. That means, if a new request appears, it should receive
    # an application close message rather than "Another connection
    # still open" message.
    def test_close_frame(self):
        url = close_base_url + '/000/' + str(uuid.uuid4())
        r1 = POST_async(url + '/xhr_streaming')

        r1.read() # prelude
        self.assertEqual(r1.read(), 'o\n')
        self.assertEqual(r1.read(), 'c[3000,"Go away!"]\n')

        r2 = POST_async(url + '/xhr_streaming')
        r2.read() # prelude
        self.assertEqual(r2.read(), 'c[3000,"Go away!"]\n')

        # HTTP streaming requests should be automatically closed after
        # close.
        self.assertFalse(r1.read())
        self.assertFalse(r2.read())

    def test_close_request(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r1 = POST_async(url + '/xhr_streaming')

        r1.read() # prelude
        self.assertEqual(r1.read(), 'o\n')

        r2 = POST_async(url + '/xhr_streaming')
        r2.read() # prelude
        self.assertEqual(r2.read(), 'c[2010,"Another connection still open"]\n')

        # HTTP streaming requests should be automatically closed after
        # getting the close frame.
        self.assertFalse(r2.read())

    # When a polling request is closed by a network error - not by
    # server, the session should be automatically closed. When there
    # is a network error - we're in an undefined state. Some messages
    # may have been lost, there is not much we can do about it.
    def test_abort_xhr_streaming(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r1 = POST_async(url + '/xhr_streaming')
        r1.read() # prelude
        self.assertEqual(r1.read(), 'o\n')

        # Can't do second polling request now.
        r2 = POST_async(url + '/xhr_streaming')
        r2.read() # prelude
        self.assertEqual(r2.read(), 'c[2010,"Another connection still open"]\n')
        self.assertFalse(r2.read())

        r1.close()
        time.sleep(0.25)

        # Polling request now, after we aborted previous one, should
        # trigger a connection closure. Implementations may close
        # the session and forget the state related. Alternatively
        # they may return a 1002 close message.
        r3 = POST_async(url + '/xhr_streaming')
        r3.read() # prelude
        self.assertTrue(r3.read() in ['o\n', 'c[1002,"Connection interrupted"]\n'])
        r3.close()

    # The same for polling transports
    def test_abort_xhr_polling(self):
        url = base_url + '/000/' + str(uuid.uuid4())
        r1 = POST(url + '/xhr')
        self.assertEqual(r1.body, 'o\n')

        r1 = old_POST_async(url + '/xhr', load=False)
        time.sleep(0.25)

        # Can't do second polling request now.
        r2 = POST(url + '/xhr')
        self.assertEqual(r2.body, 'c[2010,"Another connection still open"]\n')

        r1.close()
        time.sleep(0.25)

        # Polling request now, after we aborted previous one, should
        # trigger a connection closure. Implementations may close
        # the session and forget the state related. Alternatively
        # they may return a 1002 close message.
        r3 = POST(url + '/xhr')
        self.assertTrue(r3.body in ['o\n', 'c[1002,"Connection interrupted"]\n'])

# Http 1.0 and 1.1 chunking
# =========================
#
# There seem to be a lot of confusion about http/1.0 and http/1.1
# content-length and transfer-encoding:chunking headers. Although
# following tests don't really test anything sockjs specific, it's
# good to make sure that the server is behaving about this.
#
# It is not the intention of this test to verify all possible urls -
# merely to check the sanity of http server implementation.  It is
# assumed that the implementator is able to apply presented behaviour
# to other urls served by the sockjs server.
class Http10(Test):
    # We're going to test a greeting url. No dynamic content, just the
    # simplest possible response.
    def test_synchronous(self):
        c = RawHttpConnection(base_url)
        # In theory 'connection:Keep-Alive' isn't a valid http/1.0
        # header, but in this header may in practice be issued by a
        # http/1.0 client:
        # http://www.freesoft.org/CIE/RFC/2068/248.htm
        r = c.request('GET', base_url, http='1.0',
                      headers={'Connection':'Keep-Alive'})
        self.assertEqual(r.status, 200)
        # In practice the exact http version on the response doesn't
        # really matter. Many serves always respond 1.1.
        self.assertTrue(r.http in ['1.0', '1.1'])
        # Transfer-encoding is not allowed in http/1.0.
        self.assertFalse(r.headers.get('transfer-encoding'))

        # There are two ways to give valid response. Use
        # Content-Length (and maybe connection:Keep-Alive) or
        # Connection: close.
        if not r.headers.get('content-length'):
            self.assertEqual(r.headers['connection'].lower(), 'close')
            self.assertEqual(c.read(), 'Welcome to SockJS!\n')
            self.assertTrue(c.closed())
        else:
            self.assertEqual(int(r.headers['content-length']), 19)
            self.assertEqual(c.read(19), 'Welcome to SockJS!\n')
            connection = r.headers.get('connection', '').lower()
            if connection in ['close', '']:
                # Connection-close behaviour is default in http 1.0
                self.assertTrue(c.closed())
            else:
                self.assertEqual(connection, 'keep-alive')
                # We should be able to issue another request on the same connection
                r = c.request('GET', base_url, http='1.0',
                              headers={'Connection':'Keep-Alive'})
                self.assertEqual(r.status, 200)

    def test_streaming(self):
        url = close_base_url + '/000/' + str(uuid.uuid4())
        c = RawHttpConnection(url)
        # In theory 'connection:Keep-Alive' isn't a valid http/1.0
        # header, but in this header may in practice be issued by a
        # http/1.0 client:
        # http://www.freesoft.org/CIE/RFC/2068/248.htm
        r = c.request('POST', url + '/xhr_streaming', http='1.0',
                      headers={'Connection':'Keep-Alive'})
        self.assertEqual(r.status, 200)
        # Transfer-encoding is not allowed in http/1.0.
        self.assertFalse(r.headers.get('transfer-encoding'))
        # Content-length is not allowed - we don't know it yet.
        self.assertFalse(r.headers.get('content-length'))

        # `Connection` should be not set or be `close`. On the other
        # hand, if it is set to `Keep-Alive`, it won't really hurt, as
        # we are confident that neither `Content-Length` nor
        # `Transfer-Encoding` are set.

        # This is a the same logic as HandlingClose.test_close_frame
        self.assertEqual(c.read(2048+1)[0], 'h') # prelude
        self.assertEqual(c.read(2), 'o\n')
        self.assertEqual(c.read(19), 'c[3000,"Go away!"]\n')
        self.assertTrue(c.closed())


class Http11(Test):
    def test_synchronous(self):
        c = RawHttpConnection(base_url)
        r = c.request('GET', base_url, http='1.1',
                      headers={'Connection':'Keep-Alive'})
        # Keepalive is default in http 1.1
        self.assertTrue(r.http, '1.1')
        self.assertTrue(r.headers.get('connection', '').lower() in ['keep-alive', ''],
                         "Your server doesn't support connection:Keep-Alive")
        # Server should use 'Content-Length' or 'Transfer-Encoding'
        if r.headers.get('content-length'):
            self.assertEqual(int(r.headers['content-length']), 19)
            self.assertEqual(c.read(19), 'Welcome to SockJS!\n')
            self.assertFalse(r.headers.get('transfer-encoding'))
        else:
            self.assertEqual(r.headers['transfer-encoding'].lower(), 'chunked')
            self.assertEqual(c.read_chunk(), 'Welcome to SockJS!\n')
            self.assertEqual(c.read_chunk(), '')
        # We should be able to issue another request on the same connection
        r = c.request('GET', base_url, http='1.1',
                      headers={'Connection':'Keep-Alive'})
        self.assertEqual(r.status, 200)

    def test_streaming(self):
        url = close_base_url + '/000/' + str(uuid.uuid4())
        c = RawHttpConnection(url)
        r = c.request('POST', url + '/xhr_streaming', http='1.1',
                      headers={'Connection':'Keep-Alive'})
        self.assertEqual(r.status, 200)
        # Transfer-encoding is required in http/1.1.
        self.assertTrue(r.headers['transfer-encoding'].lower(), 'chunked')
        # Content-length is not allowed.
        self.assertFalse(r.headers.get('content-length'))
        # Connection header can be anything, so don't bother verifying it.

        # This is a the same logic as HandlingClose.test_close_frame
        self.assertEqual(c.read_chunk()[0], 'h') # prelude
        self.assertEqual(c.read_chunk(), 'o\n')
        self.assertEqual(c.read_chunk(), 'c[3000,"Go away!"]\n')
        self.assertEqual(c.read_chunk(), '')


# Footnote
# ========

# Make this script runnable.
if __name__ == '__main__':
    unittest.main()
