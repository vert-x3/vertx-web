#!/usr/bin/env sh

MSG="GraphQLWS tests server started"

mvn -am -pl vertx-web-graphql install -DskipTests
mvn -pl vertx-web-graphql exec:java -Dexec.mainClass=io.vertx.ext.web.handler.graphql.GraphQLWSTestsServer -Dexec.classpathScope=test > "${HOME}"/GraphQLWSTestsServer.log 2>&1 &
echo $! > "${HOME}"/GraphQLWSTestsServer.pid
( tail -f "${HOME}"/GraphQLWSTestsServer.log & ) | grep -q "${MSG}"
echo "${MSG}"
