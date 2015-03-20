#!/usr/bin/env python

# Http quirks
# ===========
#
# During the work on SockJS few interesting aspects of Http were
# identified. Following tests try to trigger that. If the tests end
# with success - you can be more confident that your web server will
# survive clients that are violating some aspects of http.
#
# This tests aren't really a part of SockJS test suite, it's more
# about verification http quirks.
import unittest2 as unittest
import uuid
import urlparse
import httplib_fork as httplib
import os

test_top_url = os.environ.get('SOCKJS_URL', 'http://localhost:8081')
base_url = test_top_url + '/echo'

def POST_empty(url):
    u = urlparse.urlparse(url)
    if u.scheme == 'http':
        conn = httplib.HTTPConnection(u.netloc)
    elif u.scheme == 'https':
        conn = httplib.HTTPSConnection(u.netloc)
    else:
        assert False, "Unsupported scheme " + u.scheme
    path = u.path + ('?' + u.query if u.query else '')
    conn.request('POST', path)
    res = conn.getresponse()
    headers = dict( (k.lower(), v) for k, v in res.getheaders() )
    body = res.read()
    conn.close()
    return res.status, body, headers

class HttpQuirks(unittest.TestCase):
    def test_emptyContentLengthForPost(self):
        # Doing POST without Content-Length shouldn't break the
        # server (it does break misultin)
        trans_url = base_url + '/000/' + str(uuid.uuid4())
        status, body, _ = POST_empty(trans_url + '/xhr')
        self.assertEqual(body, 'o\n')
        self.assertEqual(status, 200)


if __name__ == '__main__':
    unittest.main()
