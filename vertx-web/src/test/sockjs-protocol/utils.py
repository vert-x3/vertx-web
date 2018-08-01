import urlparse
import httplib_fork as httplib
from ws4py.client.threadedclient import WebSocketClient
import Queue
import socket
import re

class HttpResponse:
    def __init__(self, method, url,
                 headers={}, body=None, async=False, load=True):
        headers = headers.copy()
        u = urlparse.urlparse(url)
        kwargs = {'timeout': 1.0}
        if u.scheme == 'http':
            conn = httplib.HTTPConnection(u.netloc, **kwargs)
        elif u.scheme == 'https':
            conn = httplib.HTTPSConnection(u.netloc, **kwargs)
        else:
            assert False, "Unsupported scheme " + u.scheme
        assert u.fragment == ''
        path = u.path + ('?' + u.query if u.query else '')
        self.conn = conn
        if not body:
            if method is 'POST':
                # The spec says: "Applications SHOULD use this field
                # to indicate the transfer-length of the message-body,
                # unless this is prohibited by the rules in section
                # 4.4."
                # http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.13
                # While httplib sets it only if there is body.
                headers['Content-Length'] = 0
            conn.request(method, path, headers=headers)
        else:
            if isinstance(body, unicode):
                body = body.encode('utf-8')
            conn.request(method, path, headers=headers, body=body)

        if load:
            if not async:
                self._load()
            else:
                self._async_load()

    def _get_status(self):
        return self.res.status
    status = property(_get_status)

    def __getitem__(self, key):
        return self.headers.get(key.lower())

    def _load(self):
        # That works for Content-Length responses.
        self.res = self.conn.getresponse()
        self.headers = dict( (k.lower(), v) for k, v in self.res.getheaders() )
        self.body = self.res.read()
        self.close()

    def close(self):
        if self.conn:
            self.conn.close()
            self.conn = None

    def _async_load(self):
        # That works for Transfer-Encoding: Chunked
        self.res = self.conn.getresponse()
        self.headers = dict( (k.lower(), v) for k, v in self.res.getheaders() )

    def read(self):
        data =  self.res.read(10240)
        if data:
            return data
        else:
            self.close()
            return None

def old_POST_async(url, **kwargs):
    return HttpResponse('POST', url, async=True, **kwargs)


class WebSocket8Client(object):
    class ConnectionClosedException(Exception): pass

    def __init__(self, url):
        queue = Queue.Queue()
        self.queue = queue
        class IntWebSocketClient(WebSocketClient):
            def received_message(self, m):
                queue.put(unicode(str(m), 'utf-8'))
            def closed(self, code, reason):
                queue.put((code, reason))
            # def read_from_connection(self, amount):
            #     r = super(IntWebSocketClient, self).read_from_connection(amount)
            #     if self.stream.closing:
            #         queue.put((self.stream.closing.code, self.stream.closing.reason[2:]))
            #     elif not r:
            #         queue.put((1000, ""))
            #     return r
        self.client = IntWebSocketClient(url)
        self.client.connect()

    def close(self):
        if self.client:
            self.client.running = False
            self.client.close()
            self.client._th.join()
            self.client = None

    def send(self, data):
        self.client.send(data)

    def recv(self):
        try:
            r = self.queue.get(timeout=1.0)
            if isinstance(r, tuple):
                ce = self.ConnectionClosedException()
                (ce.code, ce.reason) = r
                raise ce
            return r
        except:
            self.close()
            raise

def recvline(s):
    b = []
    c = None
    while c != '\n':
        c = s.recv(1)
        b.append( c )
    return ''.join(b)


class CaseInsensitiveDict(object):
    def __init__(self, *args, **kwargs):
        self.lower = {}
        self.d = dict(*args, **kwargs)
        for k in self.d:
            self[k] = self.d[k]

    def __getitem__(self, key, *args, **kwargs):
        pkey = self.lower.setdefault(key.lower(), key)
        return self.d.__getitem__(pkey, *args, **kwargs)

    def __setitem__(self, key, *args, **kwargs):
        pkey = self.lower.setdefault(key.lower(), key)
        return self.d.__setitem__(pkey, *args, **kwargs)

    def items(self):
        for k in self.lower.values():
            yield (k, self[k])

    def __repr__(self): return repr(self.d)
    def __str__(self): return str(self.d)

    def get(self, key, *args, **kwargs):
        pkey = self.lower.setdefault(key.lower(), key)
        return self.d.get(pkey, *args, **kwargs)

    def __contains__(self, key):
        pkey = self.lower.setdefault(key.lower(), key)
        return pkey in self.d

class Response(object):
    def __repr__(self):
        return '<Response HTTP/%s %s %r %r>' % (
            self.http, self.status, self.description, self.headers)

    def __str__(self): return repr(self)

    def __getitem__(self, key):
        return self.headers.get(key)

    def get(self, key, default):
        return self.headers.get(key, default)


class RawHttpConnection(object):
    def __init__(self, url):
        u = urlparse.urlparse(url)
        self.s = socket.create_connection((u.hostname, u.port), timeout=1)

    def request(self, method, url, headers={}, body=None, timeout=1, http="1.1"):
        headers = CaseInsensitiveDict(headers)
        if method == 'POST':
            body = (body or '').encode('utf-8')
        u = urlparse.urlparse(url)
        headers['Host'] = u.hostname + ':' + str(u.port) if u.port else u.hostname
        if body is not None:
            headers['Content-Length'] = str(len(body))

        rel_url = url[ url.find(u.path): ]

        req = ["%s %s HTTP/%s" % (method, rel_url, http)]
        for k, v in headers.items():
            req.append( "%s: %s" % (k, v) )
        req.append('')
        req.append('')
        self.send('\r\n'.join(req))

        if body:
            self.send(body)

        head = recvline(self.s)
        r = re.match(r'HTTP/(?P<version>\S+) (?P<status>\S+) (?P<description>.*)', head)

        resp = Response()
        resp.http = r.group('version')
        resp.status = int(r.group('status'))
        resp.description = r.group('description').rstrip('\r\n')

        resp.headers = CaseInsensitiveDict()
        while True:
            header = recvline(self.s)
            if header in ['\n', '\r\n']:
                break
            k, _, v = header.partition(':')
            resp.headers[k] = v.lstrip().rstrip('\r\n')

        return resp

    def read(self, size=None):
        if size is None:
            # A single packet by default
            return self.s.recv(999999)
        data = []
        while size > 0:
            c = self.s.recv(size)
            if not c:
                raise Exception('Socket closed!')
            size -= len(c)
            data.append( c )
        return ''.join(data)

    def read_till_eof(self):
        data = []
        while True:
            c = self.s.recv(999999)
            if not c:
                break
            data.append( c )
        return ''.join(data)

    def closed(self):
        # To check if socket is being closed, we need to recv and see
        # if the response is empty. If it is not - we're in trouble -
        # abort.
        t = self.s.settimeout(0.1)
        r = self.s.recv(1) == ''
        if not r:
            raise Exception('Socket not closed!')
        self.s.settimeout(t)
        return r

    def read_chunk(self):
        line = recvline(self.s).rstrip('\r\n')
        bytes = int(line, 16) + 2 # Additional \r\n
        return self.read(bytes)[:-2]

    def send(self, data):
        self.s.sendall(data)

    def close(self):
        self.s.close()


def SynchronousHttpRequest(method, url, **kwargs):
    c = RawHttpConnection(url)
    r = c.request(method, url, **kwargs)
    if r.get('Transfer-Encoding', '').lower() == 'chunked':
        chunks = []
        while True:
            chunk = c.read_chunk()
            if len(chunk) == 0:
                break
            chunks.append( chunk )
        r.body = ''.join(chunks)
    elif r.get('Content-Length', ''):
        cl = int(r['Content-Length'])
        r.body = c.read(cl)
    elif 'close' in [k.strip() for k in r.get('Connection', '').lower().split(',')]:
        r.body = c.read_till_eof()
    else:
        # Whitelist statuses that may not need a response
        if r.status in [101, 304, 204] or (r.status == 200 and method == 'OPTIONS'):
            r.body = ''
        else:
            raise Exception(str(r.status) + ' '+str(r.headers) + " No Transfer-Encoding:chunked nor Content-Length nor Connection:Close!")
    c.close()
    return r

def GET(url, **kwargs):
    return SynchronousHttpRequest('GET', url, **kwargs)

def POST(url, **kwargs):
    return SynchronousHttpRequest('POST', url, **kwargs)

def OPTIONS(url, **kwargs):
    return SynchronousHttpRequest('OPTIONS', url, **kwargs)

def AsynchronousHttpRequest(method, url, **kwargs):
    c = RawHttpConnection(url)
    r = c.request(method, url, **kwargs)
    if r.get('Transfer-Encoding', '').lower() == 'chunked':
        def read():
            return c.read_chunk()
        r.read = read
    elif r.get('Content-Length', ''):
        cl = int(r['Content-Length'])
        def read():
            return c.read(cl)
        r.read = read
    elif ('close' in [k.strip() for k in r.get('Connection', '').lower().split(',')]
          or r.status == 101):
        def read():
            return c.read()
        r.read = read
    else:
        raise Exception(str(r.status) + ' '+str(r.headers) + " No Transfer-Encoding:chunked nor Content-Length nor Connection:Close!")
    def close():
        c.close()
    r.close = close
    return r

def GET_async(url, **kwargs):
    return AsynchronousHttpRequest('GET', url, **kwargs)

def POST_async(url, **kwargs):
    return AsynchronousHttpRequest('POST', url, **kwargs)
