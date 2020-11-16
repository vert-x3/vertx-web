#!/usr/bin/env sh

(pkill -9 -F "${HOME}"/SockJSWriteHandlerTestServer.pid 2>/dev/null) || (rm -f "${HOME}"/SockJSWriteHandlerTestServer.pid && rm -f "${HOME}"/SockJSWriteHandlerTestServer.log)
