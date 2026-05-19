#!/usr/bin/env sh

MSG="GraphiQL tests server started"

mvn -am -pl vertx-web-graphql install -DskipTests
mvn -pl vertx-web-graphql exec:java -Dexec.mainClass=io.vertx.ext.web.handler.graphql.tests.GraphiQLTestsServer -Dexec.classpathScope=test > "${HOME}"/GraphiQLTestsServer.log 2>&1 &
echo $! > "${HOME}"/GraphiQLTestsServer.pid
( tail -f "${HOME}"/GraphiQLTestsServer.log & ) | grep -q "${MSG}"
echo "${MSG}"
