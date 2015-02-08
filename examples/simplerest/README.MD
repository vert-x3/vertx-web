# Simple REST Micro-Service Apex Example

A simple REST/HTTP micro-service example.

The REST service manages a product directory.

There are three possible operations on the REST API:

* List all products: GET /products
* Add a product: PUT /products/<productID>
* Get product details: GET /products/<productID>

## Run it in your IDE

Right click the SimpleREST class and run as main class.

Now point your browser at http://localhost:8080/products/

## Build and run from command line

Build it into a fat, executable jar with:

    mvn install

Run it with

    java -jar target/apex-example-simplerest-3.0.0-SNAPSHOT-fat.jar

Now point your browser at http://localhost:8080/products/

