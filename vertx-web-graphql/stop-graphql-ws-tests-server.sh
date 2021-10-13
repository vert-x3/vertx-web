#!/usr/bin/env sh

pkill -9 -F "${HOME}"/GraphQLWSTestsServer.pid 2>/dev/null
rm -f "${HOME}"/GraphQLWSTestsServer.pid
rm -f "${HOME}"/GraphQLWSTestsServer.log
