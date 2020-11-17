#!/usr/bin/env sh

MSG="Apollo tests server started"

mvn -am -pl vertx-web-graphql install -DskipTests
mvn -pl vertx-web-graphql exec:java -Dexec.mainClass=io.vertx.ext.web.handler.graphql.ApolloTestsServer -Dexec.classpathScope=test > "${HOME}"/ApolloTestsServer.log 2>&1 &
echo $! > "${HOME}"/ApolloTestsServer.pid
( tail -f "${HOME}"/ApolloTestsServer.log & ) | grep -q "${MSG}"
echo "${MSG}"
