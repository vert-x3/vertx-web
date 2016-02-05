events = require('events')
http = require('http')
urlparse = require('url').parse

exports.HttpRequest = class HttpRequest extends events.EventEmitter
    constructor: (method, url, user_headers, body) ->
        u = urlparse(url)
        headers = {}
        extend(headers, user_headers)
        headers['Content-Length'] = (body or '').length
        options =
          host: u.hostname
          port: Number(u.port) or (if u.protocol is 'http:' then 80 else 443)
          path: u.pathname + (if u.query then '?' + u.query else '')
          method: method
          headers: headers
          agent: false

        @chunks = []
        @req = http.request options, (@res) =>
            @req.socket.setTimeout(60000)
            @status = @res.statusCode
            @headers = @res.headers
            @emit('start')
            @res.on 'data', (chunk) =>
                chunk = chunk.toString('utf-8')
                @chunks.push(chunk)
                @emit('chunk', chunk)
            @res.on 'end', =>
                @data = @chunks.join('')
                @emit('end', @data)
            @res.on 'close', =>
                console.log('close')

        @req.on 'error', (e) =>
            console.log('error!',e)
            process.exit(1)
        if (body or '').length > 0
            @req.end(body, 'utf-8')
        else
            @req.end()

    _on_response: (@res) =>

exports.GET = (url, headers, body) ->
    new HttpRequest('GET', url, headers, body)

exports.POST = (url, headers, body) ->
    new HttpRequest('POST', url, headers, body)

exports.extend = extend = (dst, src) ->
    for k of src
        if src.hasOwnProperty(k)
            dst[k] = src[k]
    return dst

exports.StdDev = class StdDev
    constructor: ->
        @sum = 0.0
        @sum_sq = 0.0
        @count = 0

    add: (v) ->
        @sum += v
        @sum_sq += v*v
        @count += 1

    avg: () ->
        if @count is 0
            return null
        return @sum / @count

    dev: () ->
        if @count is 0
            return null
        avg = @avg()
        variance = (@sum_sq / @count) - (avg * avg)
        return Math.sqrt(variance)

exports.now = ->
    (new Date()).getTime()
