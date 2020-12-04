#!/usr/bin/env sh

pkill -9 -F "${HOME}"/ApolloTestsServer.pid 2>/dev/null
rm -f "${HOME}"/ApolloTestsServer.pid
rm -f "${HOME}"/ApolloTestsServer.log
