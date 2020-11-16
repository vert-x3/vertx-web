#!/usr/bin/env sh

MSG="SockJS writeHandler tests server started"

mvn test-compile exec:java -Dexec.mainClass=io.vertx.ext.web.handler.sockjs.SockJSWriteHandlerTestServer -Dexec.classpathScope=test > "${HOME}"/SockJSWriteHandlerTestServer.log 2>&1 &
echo $! > "${HOME}"/SockJSWriteHandlerTestServer.pid
( tail -f "${HOME}"/SockJSWriteHandlerTestServer.log & ) | grep -q "${MSG}"
echo "${MSG}"
