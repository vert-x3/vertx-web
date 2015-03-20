events = require('events')
common = require('./common')

exports.GenericClient = class GenericClient extends events.EventEmitter
    constructor: (url) ->
        @id = ('' + Math.random()).slice(2, 16)
        @url = url + '/0/' + @id
        @buffer = []
        @is_opened = false
        @is_closed = false
        @sending = false
        @_kick_recv()

    _got_message: (msg) ->
        [type, payload] = [msg.slice(0,1), msg.slice(1)]
        switch type
            when 'o'
                if not @is_opened
                    @is_opened = true
                    @emit('open')
                else
                    @is_closed = true
                    @emit('close', 1000, 'Connection prematurely closed!')
            when 'h' then null
            when 'a'
                for m in JSON.parse(payload)
                    @emit('message', m)
            when 'c'
                @is_closed = true
                [status, reason] = JSON.parse(payload)
                @emit('close', status, reason)
            else
                throw Error('unknown type ' + type)

    send: (msg) ->
        @buffer.push(msg)
        if not @sending and not @is_closed
            @_kick_send()

    close: () ->
        @is_closed = true

exports.XhrPollingClient = class XhrPollingClient extends GenericClient
    _kick_send: () ->
        @sending = true
        r = common.POST(@url + '/xhr_send',
                 {'Content-Type': 'application/xml'},
                 JSON.stringify(@buffer))
        @buffer = []
        r.on 'end', () =>
            @sending = false
            if not @is_closed and @buffer.length > 0
                @_kick_send()

    _kick_recv: () ->
        r = common.POST(@url + '/xhr')
        r.on 'end', (body) =>
            @_got_message(body)
            if not @is_closed
                @_kick_recv()
