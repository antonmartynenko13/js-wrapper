# JsWrapper

JsWrapper is a RESTful API project that provides the ability to execute Javascript code snippets using the GraalVM virtual machine. Just send some code in the body of the request and be sure that it will be executed. Or not, in which case information about the problem will also be provided.

## Requirements
No addition servlet container required. But to build and run application you need Java version not older then 1.8 and Apache Maven 3.


## Building
Run next script in root directory
```bash
mvn clean package spring-boot:repackage
```
## Deploy
Builded .jar file could be run using
```bash
java -jar target/js-wrapper-1.2.3.jar
```

## Run with Docker
After building you can easily create image and run project container with next Docker's commands
```bash
docker build . --tag jsw
docker run -it -p 8080:8080 -t jsw
```

## Usage
You can run any valid js code with traditional or arrow functions. Return value could be found in <code>resultValue</code> property of execution json object. Any execution details/logs are provided.

Read more in our [REST-API reference](https://antonmartynenko13.github.io/js-wrapper/restapidocs/)

You can also see [javadoc](https://antonmartynenko13.github.io/js-wrapper/apidocs)

And other project [details](https://antonmartynenko13.github.io/js-wrapper/)

Enjoy!

## License
[MIT](https://github.com/antonmartynenko13/js-wrapper/blob/main/LICENSE)