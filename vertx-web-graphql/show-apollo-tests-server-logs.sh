#!/usr/bin/env sh

cat "${HOME}"/ApolloTestsServer.log 2>/dev/null || echo "No logs"
