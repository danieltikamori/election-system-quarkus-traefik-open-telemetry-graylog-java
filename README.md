# Project to study the use of OpenTelemetry with Quarkus and CI/CD

This project aims to study the use of OpenTelemetry with Quarkus and CI/CD with option to use native image.

## Prerequisites

- [OpenTelemetry](https://github.com/open-telemetry/opentelemetry-java-instrumentation)
- [Quarkus](https://quarkus.io)
- [CI/CD](https://github.com/quarkusio/quarkus-continuous-integration)
- [MongoDB](https://www.mongodb.com)
- [Traefik](https://traefik.io)
- [OpenSearch](https://opensearch.org)
- [Jaeger](https://www.jaegertracing.io)
- [Docker](https://www.docker.com)

## Installation

We will use the [Docker Compose](https://docs.docker.com/compose/) to manage the containers.

### SDKMAN! and Java
For Java, we will use the [SDKMAN!](https://sdkman.io) to manage the Java version.

Install sdkman, then run:

```bash
sdk list java
sdk install java 17.0.6-tem
sdk use java 17.0.6-tem
java --version
```
openjdk 17.0.6 2023-01-17
OpenJDK Runtime Environment Temurin-17.0.6+10 (build 17.0.6+10)
OpenJDK 64-Bit Server VM Temurin-17.0.6+10 (build 17.0.6+10, mixed mode, sharing)

```bash
env | grep JAVA_HOME
```
JAVA_HOME=/Users/<user>/.sdkman/candidates/java/17.0.6-tem

Another option is to use the GraalVM (22).

```bash
sdk list java
sdk install java 22.3.r17-grl
sdk use java 22.3.r17-grl
java --version
```
openjdk 17.0.5 2022-10-18
OpenJDK Runtime Environment GraalVM CE 22.3.0 (build 17.0.5+8-jvmci-22.3-b08)
OpenJDK 64-Bit Server VM GraalVM CE 22.3.0 (build 17.0.5+8-jvmci-22.3-b08, mixed mode, sharing)

```bash
env | grep JAVA_HOME
JAVA_HOME=/Users/<user>/.sdkman/candidates/java/22.3.r17-grl
export GRAALVM_HOME=/home/${current_user}/path/to/graalvm
env | grep GRAALVM_HOME
GRAALVM_HOME=/Users/<user>/.sdkman/candidates/java/22.3.r17-grl
```

See:
https://graalvm.github.io/native-build-tools/0.9.6/graalvm-setup.html


### Native Image option

```bash
sdk use java 22.3.r17-grl
```
The native-image is usually already installed. The following command will install it if not.

```bash
gu install native-image
```

### Quarkus

```bash
sdk list quarkus
sdk install quarkus 2.16.4.Final
which quarkus
/Users/<user>/.sdkman/candidates/quarkus/current/bin/quarkus
quarkus --version
2.16.4.Final
```

## Setup

### Docker Compose

https://github.com/thpoiani/lab-quarkus/blob/main/docker-compose.yml
https://github.com/thpoiani/lab-quarkus/blob/main/common.yml

```bash
docker compose up -d reverse-proxy
docker compose up -d jaeger
docker compose up -d mongodb opensearch
docker compose up -d graylog
curl -H "Content-Type: application/json" \
-H "Authorization: Basic YWRtaW46YWRtaW4=" \
-H "X-Requested-By: curl" \
-X POST -v -d '{"title":"udp
input","configuration":{"recv_buffer_size":262144,"bind_address":"0.0.0.0","port":12201,"de
compress_size_limit":8388608},"type":"org.graylog2.inputs.gelf.udp.GELFUDPInput","global":t
rue}' http://logging.private.dio.localhost/api/system/inputs
docker compose up -d caching database
```

### Quarkus

#### Setup

Create a project in your IDE, preferably IntelliJ or ECLIPSE.

In the IDE terminal (project directory), run the following command to create the applications:

```bash
quarkus create app com.example:election-management \
--extension='resteasy-reactive,logging-gelf,opentelemetry,smallrye-context-propagation,smallrye-health' \
--no-code
```

```bash
quarkus create app com.example:voting-app \
--extension='resteasy-reactive,logging-gelf,opentelemetry,smallrye-context-propagation,smallrye-health' \
--no-code
```

```bash
quarkus create app com.example:result-app \
--extension='resteasy-reactive,logging-gelf,opentelemetry,smallrye-context-propagation,smallrye-health' \
--no-code
```
Go to the <application>/src/main/resources directory, open `application.properties` file and add the following lines:

For election-management/src/main/resources/application.properties:

```
# Application name will be used to differentiate at Logging
quarkus.application.name=election-management
# Graceful shutdown. Shutdown after finishing executing tasks.
quarkus.shutdown.timeout=5S

# There's %dev, %test and %prod profiles
#Logging
%prod.quarkus.log.handler.gelf.enabled=true
%prod.quarkus.log.handler.gelf.additional-field."app".value=${quarkus.application.name}
%prod.quarkus.log.handler.gelf.include-full-mdc=true
%prod.quarkus.log.console.format=%d{HH:mm:ss} %-5p traceId=%X{traceId}, parentId=%X{parentId}, spanId=%X{spanId}, sampled=%X{sampled} [%c{2.}] (%t) %s%e%n

# Open telemetry
%prod.quarkus.opentelemetry.enabled=true
%dev.quarkus.opentelemetry.enabled=false
```

For result-app/src/main/resources/application.properties:

```
quarkus.application.name=result-app
quarkus.shutdown.timeout=5S

# LOGGING
%prod.quarkus.log.handler.gelf.enabled=true
%prod.quarkus.log.handler.gelf.additional-field."app".value=${quarkus.application.name}
%prod.quarkus.log.handler.gelf.include-full-mdc=true
%prod.quarkus.log.console.format=%d{HH:mm:ss} %-5p traceId=%X{traceId}, parentId=%X{parentId}, spanId=%X{spanId}, sampled=%X{sampled} [%c{2.}] (%t) %s%e%n

# OPENTELEMETRY
%prod.quarkus.opentelemetry.enabled=true
%dev.quarkus.opentelemetry.enabled=false
```

For voting-app/src/main/resources/application.properties:

```
quarkus.application.name=voting-app
quarkus.shutdown.timeout=5S

# LOGGING
%prod.quarkus.log.handler.gelf.enabled=true
%prod.quarkus.log.handler.gelf.additional-field."app".value=${quarkus.application.name}
%prod.quarkus.log.handler.gelf.include-full-mdc=true
%prod.quarkus.log.console.format=%d{HH:mm:ss} %-5p traceId=%X{traceId}, parentId=%X{parentId}, spanId=%X{spanId}, sampled=%X{sampled} [%c{2.}] (%t) %s%e%n

# OPENTELEMETRY
%prod.quarkus.opentelemetry.enabled=true
%dev.quarkus.opentelemetry.enabled=false
```
**Important**: If the directories are not recognized as modules, add the following lines to `pom.xml`:

```xml
<modules>
    <module>election-management</module>
    <module>voting-app</module>
    <module>result-app</module>
</modules>
```

OR (see: https://stackoverflow.com/questions/33531334/convert-directories-with-java-files-to-java-modules-in-intellij):

File > New > Project From Existing Sources...

Select the directory.

OR:

- Find the pom.xml in the directory.

- Right-click the pom.xml.

- Then you can see the popup windows. Select the last item "add as maven project".

Then, the maven build tool can automatically discern the directory as a module and import the specified jar dependencies.

## Build

### JVM Build

One way:

```bash
./mvnw package
# or ./mvnw clean package
docker build -f src/main/docker/Dockerfile.jvm -t tkmr/election-management .
```

Or (recommended) run:

```bash
./cicd-build.sh <application-name>
# ./cicd-build.sh election-management
```

Testing:

```bash
TAG=1.0.0 docker compose up -d election-management
```

### Native Image Build

```bash
./mvnw package -Pnative
docker build -f src/main/docker/Dockerfile.native -t tkmr/election-management .
```

## Deployment

Progressive Deployment
Blue Green Deployment

Run:

```bash
./cicd-blue-green-deployment.sh
```

## Architecture

See: https://www.thoughtworks.com/insights/blog/architecture/demystify-software-architecture-patterns

In this project we will use the Onion architecture.

We will create 3 directories in each application: api, domain, infrastructure.

Run:

```bash
mkdir -p {election-management,result-app,voting-app}/src/main/java/{api,domain,infrastructure}
```

### Testing and developing:

```bash
cd election-management
quarkus dev
```

5005 is the port for debug. Using Intellij, go to Run menu, Attach to Process and select the process with port 5005.
Other IDEs may have the same functionality.

Live coding is activated, as you code, it will rebuild and redeploy.

At the terminal, `r` will resume testing. `h` show more options.

Go to the browser and visit http://localhost:8080/q/dev.
Examine the options. Endpoints are available too.

To quit, `q` or [Ctrl + C].

Not recommended as there are many drawbacks, but possible:
To execute directly with the IDE, create Main class at the infrastructure directory and execute it.

### References:

https://quarkus.io/guides/maven-tooling#dev-mode
https://quarkus.io/guides/dev-services
https://quarkus.io/guides/lifecycle#the-main-method
https://quarkus.io/guides/config#configuring-quarkus

## Development

**Domain model**: Java Record.

**Testing**:
Test Driven Development
Mocking

**Service Layer**:
Dependency injection

**Repository** pattern

**Query Object**
Builder pattern

### References:

Domain Model
https://martinfowler.com/eaaCatalog/domainModel.html
https://docs.oracle.com/en/java/javase/17/language/records.html
Testing
https://quarkus.io/guides/getting-started-testing
https://quarkus.io/guides/continuous-testing
https://martinfowler.com/bliki/TestDrivenDevelopment.html
https://www.thoughtworks.com/insights/blog/test-driven-development-best-thing-has-happened-software-design
https://www.thoughtworks.com/insights/topic/testing
Service Layer
https://martinfowler.com/eaaCatalog/serviceLayer.html
https://www.martinfowler.com/articles/injection.html
https://quarkus.io/guides/cdi-reference
Repository
https://martinfowler.com/eaaCatalog/repository.html
https://martinfowler.com/eaaCatalog/queryObject.html
https://martinfowler.com/dslCatalog/constructionBuilder.html

First run in the terminal:

```bash
cd election-management
quarkus dev
```

Then, at election-management/src/main/java/domain/, create a Record class Candidate.

**Java records** are a special kind of class introduced in **Java 14**. They are designed for simplicity, aiming to encapsulate data without the clutter of boilerplate code. [With their concise syntax, records allow us to create **immutable data holders** effortlessly](https://reflectoring.io/beginner-friendly-guide-to-java-records/)[1](https://reflectoring.io/beginner-friendly-guide-to-java-records/).

Here are some key points about Java records:

1. **Purpose**: Records serve as a transparent carrier for immutable data.
2. **Introduction**: They were introduced as a **preview feature** in **Java 14** (JEP 359). [After a second preview in **Java 15** (JEP 384), they became final in **Java 16** (JEP 395)](https://www.infoq.com/jp/articles/exploring-java-records/)[2](https://www.infoq.com/jp/articles/exploring-java-records/).
3. **Features**:
    - Records add a special class called **“record”** for holding immutable data.
    - They automatically implement accessors and methods inherited from `Object`.
    - You can optionally provide custom constructors for validation and normalization.
    - Records are not suitable for objects with changing field values.
    - They do not aim to completely eliminate the “boilerplate problem.”
    - Unlike JavaBeans, they don’t follow the annotation-driven approach; instead, they adhere to traditional Java practices.
4. **Syntax**:
    - A record definition looks like this:

      Java

        ```java
        record Point(int x, int y) {}
        ```

      AI で生成されたコード。ご確認のうえ、慎重にご使用ください。。

    - It is equivalent to a class with similar functionality:

      Java

        ```java
        class Point {
            private final int x;
            private final int y;
            
            Point(int x, int y) {
                this.x = x;
                this.y = y;
            }
            
            public int x() {
                return x;
            }
            
            public int y() {
                return y;
            }
            
            // Other auto-generated methods (equals, hashCode, toString)
        }
        ```

      AI で生成されたコード。ご確認のうえ、慎重にご使用ください。。

5. **Record Components**:
    - A record consists of a **header** (listing its components) and an optional **body**.
    - Components are the record’s fields.
    - You can have zero or more components.
    - Component names cannot match Object class method names.
6. **Auto-Generated Members**:
    - The compiler automatically generates:
        - Private final fields corresponding to each component.
        - Public accessors (e.g., `x()` instead of `getX()`).
        - Standard constructor (canonical constructor).
        - `equals()`, `hashCode()`, and `toString()` implementations.
7. **Differences from Regular Classes**:
    - Records have limitations:
        - No `extends` clause (implicitly inherit from `java.lang.Record`).
        - Implicitly `final` and cannot be `abstract`.
        - Cannot explicitly declare non-static fields.
        - No instance initializers (`{...}`).
        - Explicitly written members must match types.
    - Records achieve immutability by design.
    - They can have compact constructors.
8. **Commonalities with Regular Classes**:
    - Can be declared at top level or nested.
    - Support generics.
    - Can implement interfaces.
    - Can include regular constructors, non-native methods, static initializers, and static fields.

In summary, Java records simplify data modeling and provide a language-level syntax for common programming patterns. [They’re a powerful addition to the Java language](https://qiita.com/ReiTsukikazu/items/6dc3ec9ea9646c472db0)

### Testing
Then create a class CandidateService in the same directory.

Now let's write some tests.

At src, create a new directory named test. At CandidateService class, right-click the lamp and select Create Test or click the class name and Generate Test.
It will generate automatically the test class.


