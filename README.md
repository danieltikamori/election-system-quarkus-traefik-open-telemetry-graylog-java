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
-X POST -v -d '{"title":"udp\ninput","configuration":{"recv_buffer_size":262144,"bind_address":"0.0.0.0","port":12201,"decompress_size_limit":8388608},"type":"org.graylog2.inputs.gelf.udp.GELFUDPInput","global":true}' http://logging.private.tkmr.localhost/api/system/inputs
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
Note: make sure the .sh files are executable. `chmod u+x cicd-build.sh`

```bash
./cicd-build.sh <application-name>
# ./cicd-build.sh election-management
```

If an error related to git occur, it is probably about local settings.
Run to verify local configuration:

```bash
git config --local -l
```

Then run:

```bash
git config user.name <versioningcontrolapp-username>
git config user.email <your-email>
git config user.signingkey <your signingkey> (optional)
git config --local -l
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

Verify the Docker image to retrieve the image name and tag:

```bash
docker images
```

Run:

```bash
./cicd-blue-green-deployment.sh appdirectory 1.0.0
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


### Election Management - service

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

#### Testing
Then create a class CandidateService in the same directory.

Now let's write some tests.

At src, create a new directory named test. At CandidateService class, right-click the lamp and select Create Test or click the class name and Generate Test.
It will generate automatically the test class.

At the terminal (with Quarkus running), type `r` to resume testing.

Fill the files.

#### Repository

Create an interface CandidateRepository in the domain directory. It will serve as a database abstraction.
The implementation will be in the infrastructure directory.

At the infrastructure, create a new package named repository. At the newly created package, create a class SQLCandidateRepository in it.
Implement the interface CandidateRepository and use the SQLCandidateRepository as the implementation.

Mock the repository to verify if it works. Add Mockito in the pom.xml.

Fill the files.

Add Instancio in the pom.xml. Instancio populates data.

Fill the files.

At the CandidateServiceTest, create a break point at `candidateService.save(candidate);` and go to Run menu and Attach to Process.
Type `r` to resume testing. Verify if the Instancio created a candidate sample.

Remove the break point. Restart the execution.

**IMPORTANT NOTE**

As I was developing, there's a possibility the of Main class don't be created. So verify and create Main class in the application infrastructure.

#### Build for testing purposes

Certify the .sh files are executable. Make sure other containers are running (the ones started in the Setup section).
If note running, run these commands:

```bash
docker compose up -d reverse-proxy
docker compose up -d jaeger
docker compose up -d mongodb opensearch
docker compose up -d graylog
curl -H "Content-Type: application/json" \
-H "Authorization: Basic YWRtaW46YWRtaW4=" \
-H "X-Requested-By: curl" \
-X POST -v -d '{"title":"udp\ninput","configuration":{"recv_buffer_size":262144,"bind_address":"0.0.0.0","port":12201,"decompress_size_limit":8388608},"type":"org.graylog2.inputs.gelf.udp.GELFUDPInput","global":true}' http://logging.private.tkmr.localhost/api/system/inputs
docker compose up -d caching database
```

Build the application. Run:

```bash
cd election-management
./cicd-build.sh election-management
docker images
./cicd-blue-green-deployment.sh election-management <tag>
```

Open the browser, type `localhost:8080/dashboard/`.
Should open the dashboard. Then open `logging.private.tkmr.localhost`.
Enter admin admin to login.


### Election Management - Repository

**Migration**
- Flyway 
- Testcontainers

**Data Mapper**
- Hibernate ORM

At the terminal ./election-management, run:

```bash
quarkus extension add 'quarkus-flyway' 'quarkus-jdbc-mariadb'
mkdir -p src/main/resources/db/migration
```
The mkdir command will create a directory to store the versioning files of the database (structure).

At pom.xml add the following dependency:

```xml
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-mysql</artifactId>
    </dependency>
```

Go to the migration directory and create a file named `V1__CreateTableCandidates.sql`
Fill the file.

Go to application.properties files and add the following:

```properties
# FLYWAY
quarkus.flyway.migrate-at-start=true

# TESTCONTAINERS
quarkus.datasource.devservices.image-name=mariadb:10.11.2
```

Open the terminal, at /election-management, run `quarkus dev`.
Verify if the database is running and connected.

Then run:
```bash
docker exec -it <database-container-id> mysql -uquarkus -pquarkus quarkus
```

**Important note**
The command above worked with MariaDB 10.11.2. Tried with a more recent version and don worked at all, mysql executable not found in $PATH.

Now that connected to MariaDB, run:

```shell
show tables;
```

Then one after another:

```shell
select * from flyway_schema_history;
select * from candidates;
```

Go to docker-compose.yml and at election-management, below `image:`, add:

```yaml
 environment:
      - QUARKUS_DATASOURCE_USERNAME=election-management-user
      - QUARKUS_DATASOURCE_PASSWORD=election-management-password
      - QUARKUS_DATASOURCE_JDBC_URL=jdbc:mariadb://database:3306/election-management
```

Also at database, replace existing environment: with:

```yaml
    environment:
      - MARIADB_USER=election-management-user
      - MARIADB_PASSWORD=election-management-password
      - MARIADB_DATABASE=election-management
      - MARIADB_ROOT_PASSWORD=root
```

To update the database container, run:

```bash
docker compose stop database
docker compose rm database
docker compose up -d database
```

#### Adding Hibernate

Open the terminal, at /election-management, run:

```bash
quarkus extension add 'quarkus-hibernate-orm'
```

Update the application.properties adding:

```properties
# HIBERNATE
quarkus.datasource.db-kind=mariadb
quarkus.hibernate-orm.database.generation=none
%dev.quarkus.hibernate-orm.log.sql=true
%test.quarkus.hibernate-orm.log.sql=true
%dev.quarkus.hibernate-orm.log.bind-parameters=true
%test.quarkus.hibernate-orm.log.bind-parameters=true
```

Create tests for SQLCandidateRepository class and CandidateRepository interface.

For the implementation, the test is basically database connection check.

For the interface, all the logic will be inside interface test. It is possible to test MySQL, Redis, etc.

#### entities

At infrastructure/repositories/, create a package called `entities`.
Inside it create a Candidate class.

Update the Candidate and SQLCandidateRepository classes.

Write the tests for SQLCandidateRepositoryTest and CandidateRepositoryTest.

Run `quarkus dev` and test typing `r`.

After successful test, make sure the database, etc. containers are running and then run the `cicd-build.sh` and then `cicd-blue-green-deployment.sh`.

Open the browser and open http://logging.private.tkmr.localhost search.

### Election management - API layer

- JSON Rest Services
- Data Transfer Object (DTO)
- Integration Test

Domain layer have the business rules and some  external communication interfaces.
Infrastructure layer have what is specific for external communication and database configuration.
API layer do the communication between both. Gateway between Domain and Infrastructure.

At API, create a new `CandidateApi` class. Update the file and create the test.

#### DTO

DTO stands for Data Transfer Object. It is an object that carries data between processes.
DTOs are often used to encapsulate the data that needs to be transferred over a network or between different layers of an application.
They typically do not contain any business logic but instead focus on data exchange.

Some best practices for working with Data Transfer Objects (DTOs) include:

- Keep DTOs simple and focused on data transfer only, avoid adding business logic to them.
- Use DTOs to transfer only the necessary data between layers or components of an application.
- Consider using mapping libraries to easily convert between DTOs and domain objects.
- Name DTOs clearly to indicate their purpose and the data they represent.
- Avoid using nested DTO structures if possible, as they can lead to complexity and performance issues.
- Validate DTO data at the entry point of the application to ensure consistency and integrity.

These best practices can help maintain a clean and efficient data transfer process within an application.

At api, create a new `dto` package. Inside dto, create 2 packages, one `in` and another `out`.
At `ìn` package, create CreateCandidate and UpdateCandidate records.
At `out` package, create Candidate record.

Update the tests.

Now we can think about REST service.

AT infrastructure, create resources package and inside it CandidateResource class.

Generate test for CandidateResource class.

At pom.xml, add a testing Rest assured, Resteasy, Reactive Jackson, and OpenAPI dependencies:


```xml
    <dependency>
      <groupId>io.rest-assured</groupId>
      <artifactId>rest-assured</artifactId>
      <scope>test</scope>
    </dependency>

    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-resteasy-reactive</artifactId>
    </dependency>
    
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
    </dependency>

    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-smallrye-openapi</artifactId>
    </dependency>
```

After updating the files, stop traefik and election-management container if still running.
Run quarkus dev at election-management directory. Open the browser at http://localhost:8080/q/dev.
Verify that there's new options like Smallrye OpenAPI. 
Open http://localhost:8080/q/openapi.json to see the API specification.
Open Swagger UI. You can test the application here.

Post something like:

```json
{
  "givenName": "Daniel",
  "familyName": "Tikamori",
  "email": "tes@test.com",
  "jobTitle": "Solutions Architect" 
}
```

Then at terminal, run:

```bash
docker exec -it {mariadb-container-id} mysql -uquarkus -pquarkus quarkus
```

Then:

`select * from candidates;` to show the candidates table.

Now we can create an integration test.

**Unit testing:**

Test a code snippet. A method, for example.

**Integration testing:**

In the case of Quarkus, it will leave an application running at 8080. Start another instance in another port and send a request to the application.
It verifies if the communication between different microservices are working.

At test/.../resources, create a `CandidateResourceIT` testing class.

A class that ends with IT isn't executed by JUnit. It is executed by FailSafe, specified at pom.xml:

`<artifactId>maven-failsafe-plugin</artifactId>`

Flyway have a property called skipITs, that usually is set to `true`. So in order to run integration tests, it must be set as `false`.
There's a command to do it without changing the code:

At terminal, election-management directory, run:

```bash
./mvnw verify -DskipITs=false -Dquarkus.log.handler.gelf.enabled=false -Dquarkus.opentelemetry.enabled=false -Dquarkus.datasource.jdbc.driver=org.mariadb.jdbc.Driver
```

This command will run integration tests of the application in prod profile, but connect to the test container.

#### Changing the opentelemetry driver

As we can send HTTP requests and want to use Jaeger to trace the database, we will add opentelemetry driver at application.properties:

```properties
%prod.quarkus.datasource.jdbc.driver=io.opentelemetry.instrumentation.jdbc.OpenTelemetryDriver
```

Also, add one opentelemetry dependency at pom.xml:

```xml
    <dependency>
      <groupId>io.opentelemetry.instrumentation</groupId>
      <artifactId>opentelemetry-jdbc</artifactId>
    </dependency>
```

As we changed the driver, we will be modifying also the docker-compose.yml.

Modify the following line:

`      - QUARKUS_DATASOURCE_JDBC_URL=jdbc:mariadb://database:3306/election-management`

To:

`      - QUARKUS_DATASOURCE_JDBC_URL=jdbc:otel:mariadb://database:3306/election-management`

#### Build to test the new telemetry driver

Run the `cicd-build.sh election-management` and then the `cicd-blue-green-deployment.sh election-management {tag}`.

Open `http://localhost:8080/dashboard#/http/routers` to see the mapping.

Then open `http://localhost:8080/dashboard#/http/routers/election-management@docker`.

Swagger usually is accessible only on dev env, but we can change to be visible at prod.

Also, can test through Postman.

GET `http://vote.tkmr.localhost/api/candidates`

If we get errors like 404, re-run Traefik container or the election-management container.
Sometimes may not work, so proceed to POST method.

POST Body JSON:

```json
{
   "givenName": "Daniel",
   "familyName": "Tikamori",
   "email": "tes@test.com",
   "jobTitle": "Solutions Architect"
}
```

GET `http://vote.tkmr.localhost/api/candidates`

Verify if worked.

#### Tracing with Jaeger

Open `http://telemetry.private.tkmr.localhost/search` admin admin.

Select election-management at Service field and press Find Traces button.

Click on any result items (preferably the ones with higher spans number).
Explore clicking the bars, etc.

Jaeger is useful to verify execution times and if there's a costly method, it can be traced adding a specific annotation.

### Events

- Redis
- Event Driven - Redis Pub/Sub

Create V2 of migration:

At resources/db.migration, create a `V2__CreateTableElections.sql`.
May add SEED with https://mockaroo.com to populate the database.

Keep running `quarkus dev` at election-management.

At domain, create a `Election` record.
Also at domain, create `ElectionService` class.
To persist election data, create `ElectionRepository` interface.

From now on, we will be focusing on development. May create tests if you will.

At infrastructure/repositories, create a `SQLElectionRepository` class that implements the `ElectionRepository` interface.
At infrastructure/repositories, create a `RedisElectionRepository` class that implements the `ElectionRepository` interface.
When persist data in the database, it will also persist at Redis caching database using the same interface.

Now we need election entity. At infrastructure/repositories/entities, create an `Election` class entity.
Also, inside the same package, create `ElectionCandidate` and then `ElectionCandidateId` class.

Now we need import Redis client.

We can add the dependency directly into the pom.xml or run at the election-management:

```bash
quarkus extension add 'quarkus-redis-client'
```

Update the `RedisElectionRepository` class.

At api, create `ElectionApi` class.

At infrastructure/resources/, create `ElectionResource` class.

Now it is time to test. Stop other containers that are using 8080 port. Run through `quarkus dev`.

Test using Swagger UI. If you use the seed, using GET method, it should show data.
Test POST method too.

Test election creation at Election resource. It will work only if there's at least a candidate.

Take a look at the test database, using `docker exec -it {mariadb-container-id} mysql -uquarkus -pquarkus quarkus` command.

Run:

```sql
select * from elections;
select * from election_candidate;
```

Now connect to Redis, run:

```bash
docker ps | grep redis
```

Copy the test Redis container id.

```bash
docker exec -it {redis-container-id} redis-cli
```
Key referring the election:
```bash
keys *
```

We can see everything in the sorted set using 0 and -1. WITHSCORES allow to see the votes.

```bash
ZRANGE {key-starting-without-""} 0 -1 WITHSCORES
```

Subscribe to elections channel:

```bash
SUBSCRIBE elections
```

To simulate a scenario with many applications listening the same channel, open new terminals and connect to Redis:

```bash
docker exec -it {redis-container-id} redis-cli

SUBSCRIBE elections
```

Back to Swagger, publish (POST) another election. A message should be sent to the terminals listening the channel.

See: https://developertoarchitect.com/lessons/lesson137.html