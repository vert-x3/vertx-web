# Hello World Apex Verticle example

The same as the simplest hello world example but this time the hello world server is in a verticle.

## Run it in your IDE

Right click the HelloWorld class and run as main class.

Now point your browser at http://localhost:8080

## Build and run from command line

Build it into a fat, executable jar with:

    mvn install

Run it with

    java -jar target/apex-example-verticle-3.0.0-SNAPSHOT-fat.jar

Now point your browser at http://localhost:8080

As it's a verticle you can use any of the command line options for deploying verticles, e.g. to increase the number
of instances:

java -jar target/apex-example-verticle-3.0.0-SNAPSHOT-fat.jar -instances 8

This makes it easy to scale your services across available cores without having to write the code to manually
instantiate more servers yourself.



