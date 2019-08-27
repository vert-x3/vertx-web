@echo off

setlocal enableextensions

rd /s /q build
rd /s /q ..\src\main\resources\io\vertx\ext\web\handler\graphiql
md ..\src\main\resources\io\vertx\ext\web\handler\graphiql
call npm install
call npm run build
node copy-to-resources.js

endlocal
