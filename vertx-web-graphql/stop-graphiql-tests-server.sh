#!/usr/bin/env sh

pkill -9 -F "${HOME}"/GraphiQLTestsServer.pid 2>/dev/null
rm -f "${HOME}"/GraphiQLTestsServer.pid
rm -f "${HOME}"/GraphiQLTestsServer.log
