#!/usr/bin/env coffee
require("coffee-script")

client = require('./client')
common = require('./common')



url = 'http://localhost:8080/echo'
count = 500
hz = 1
seconds = 20

console.log(' [*] Connecting to ' + url +
            ' (count:' + count +
            ', hz:' + hz + ', seconds:' + seconds + ')')

connected_counter = 0
connected = ->
    connected_counter += 1
    if connected_counter is count
        console.log(' [*] All connected. Starting')
        for conn in conns
            conn()

closed_counter = 0
closed = ->
    closed_counter += 1
    if closed_counter is count
        console.log(' [*] Done. avg=', stats.avg(),
                    ' dev=', stats.dev(),
                    ' (' + stats.count + ' data points)')


stats = new common.StdDev()

conns = for i in [0...count]
    do (i) ->
        c = seconds * hz
        conn = new client.XhrPollingClient(url)
        conn.on('open', connected)
        conn.on 'message', (t0) ->
            c -= 1
            delay = common.now() - Number(t0)
            stats.add(delay)
            if c > 0
                go = -> conn.send('' + common.now())
                setTimeout(go, 1000/hz)
            else
                closed()
                conn.close()
        conn.on 'close', (_, reason) ->
            closed()
            console.log('ERROR', reason)
        return ->
            conn.send('' + common.now())

