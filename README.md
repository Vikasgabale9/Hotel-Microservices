# Hotel Microservices

A Spring Boot microservices project demonstrating service discovery,
centralized configuration, API Gateway routing, inter-service
communication, and resilience patterns.

## Overview

The system is split into independent services:

-   **UserService** --- manages user data using MySQL.
-   **HotelService** --- manages hotel data using MySQL.
-   **RatingService** --- manages rating data using MongoDB.
-   **API Gateway** --- provides a single entry point and routes
    requests to backend services.
-   **Service Registry** --- Eureka server for service registration and
    discovery.
-   **Config Server** --- loads shared configuration from a
    GitHub-backed configuration repository.

## Architecture

``` text
                         Client
                           |
                           v
                    API Gateway :8084
                     /      |      \
                    v       v       v
             UserService HotelService RatingService
                :8081       :8082       :8083
                    \         |          /
                     \        |         /
                      v       v        v
                     Eureka Service Registry
                              :8761

                 Config Server :8085
                        |
                        v
               GitHub Config Repository
```

Services register with Eureka and can discover one another by service
name. The API Gateway uses load-balanced service URIs (`lb://...`) to
route requests. The Config Server centralizes shared settings such as
Eureka configuration.

## Technology Stack

  Technology                       Purpose
  -------------------------------- ------------------------------------
  Java 25                          Runtime and language
  Spring Boot 4.1.1                Application framework
  Spring Cloud 2025.1.3            Microservices integrations
  Spring Cloud Netflix Eureka      Service registration and discovery
  Spring Cloud Gateway (WebFlux)   API routing
  Spring Cloud Config              Centralized configuration
  Spring Cloud OpenFeign           Declarative HTTP clients
  Spring Data JPA / Hibernate      Relational persistence
  MySQL                            User and hotel data
  Spring Data MongoDB              Rating data
  Resilience4j                     Circuit breaker and retry patterns
  Spring Boot Actuator             Health and monitoring endpoints
  Maven                            Build and dependency management

> Verify that the Spring Boot and Spring Cloud versions used in each
> module are compatible. This README reflects the versions currently
> described for this project.

## Service Ports

  Component                     Port Database
  ------------------------- -------- -----------------------------
  UserService                 `8081` MySQL --- `microservices`
  HotelService                `8082` MySQL --- `microservices`
  RatingService               `8083` MongoDB --- `microservices`
  API Gateway                 `8084` ---
  Config Server               `8085` GitHub-backed configuration
  Eureka Service Registry     `8761` ---

## Prerequisites

Install and configure:

-   JDK 25
-   Maven (or use the included Maven Wrapper)
-   MySQL Server
-   MongoDB Server
-   Git
-   An IDE such as IntelliJ IDEA or Spring Tool Suite (optional)

Make sure the configured database servers are running before starting
their respective services.

## Database Setup

### MySQL

Create the database used by UserService and HotelService:

``` sql
CREATE DATABASE microservices;
```

Configure the credentials in the relevant service configuration.
Example:

``` yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/microservices
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

Set `DB_PASSWORD` in your environment, or use your local configuration.
Do not commit real credentials to Git.

### MongoDB

Ensure MongoDB is running locally and the `microservices` database is
available. RatingService has used this configuration:

``` yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017
      database: microservices
```

MongoDB can create the database and collections when data is first
written, depending on the operation and configuration.

## Configuration Server

The services use the Config Server at:

``` text
http://localhost:8085
```

A service can import remote configuration with:

``` yaml
spring:
  config:
    import: configserver:http://localhost:8085
```

Use `optional:configserver:http://localhost:8085` only if the service
should be allowed to start when the Config Server is unavailable.
Without `optional:`, the application can fail during startup if it
cannot load its remote configuration.

The project keeps shared Eureka settings in the GitHub configuration
repository to avoid duplicating them in each service's local
`application.yml`. Ensure the repository contains configuration files
matching the service application names, and that the Config Server
points to the correct repository.

## API Gateway Routes

The configured Gateway routes are:

  Route ID          Path pattern       Destination
  ----------------- ------------------ ----------------------
  `USERSERVICE`     `/api/users/**`    `lb://USERSERVICE`
  `HOTELSERVICE`    `/api/hotels/**`   `lb://HOTELSERVICE`
  `RATINGSERVICE`   `/ratings/**`      `lb://RATINGSERVICE`

Use consistent path casing. If a backend controller uses `/Api/users/**`
while the Gateway predicate uses `/api/users/**`, align the paths or
configure an appropriate route rewrite.

## Known UserService Endpoints

The following endpoints are based on the current UserController
implementation:

  Method     Path                    Purpose
  ---------- ----------------------- -----------------------
  `POST`     `/Api/users/create`     Create a user
  `GET`      `/Api/users/getAll`     Retrieve all users
  `GET`      `/Api/users/{userId}`   Retrieve a user by ID
  `PUT`      `/Api/users/{userId}`   Update a user
  `DELETE`   `/Api/users/{userId}`   Delete a user

Example request to get all users directly:

``` http
GET http://localhost:8081/Api/users/getAll
```

Example request through the Gateway, subject to the configured route
path:

``` http
GET http://localhost:8084/api/users/getAll
```

The Gateway and controller path casing must match. HotelService and
RatingService endpoint paths should be documented here after confirming
their controller mappings.

## Resilience: Circuit Breaker and Retry

UserService includes Resilience4j integration for learning and testing
resilience patterns.

-   **Circuit breaker**: tracks failures and can temporarily stop calls
    when the configured failure threshold is reached.
-   **Retry**: repeats a failed operation up to the configured maximum
    number of attempts.
-   **Actuator**: exposes health information.

Example configuration shape:

``` yaml
management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: always
  health:
    circuitbreakers:
      enabled: true

resilience4j:
  circuitbreaker:
    instances:
      ratingHotelBreaker:
        registerHealthIndicator: true
        failureRateThreshold: 50
        minimumNumberOfCalls: 5
        waitDurationInOpenState: 6s
        slidingWindowSize: 10
        slidingWindowType: COUNT_BASED

  retry:
    instances:
      ratingHotelBreaker:
        max-attempts: 3
        wait-duration: 5s
```

The corresponding Java methods must use matching `@CircuitBreaker`
and/or `@Retry` instance names. YAML alone does not apply these patterns
to a method.

Check health at:

``` text
http://localhost:8081/actuator/health
```

**Design note:** Put CircuitBreaker and Retry around calls that can fail
independently, especially remote Feign calls. Applying them to a
controller that only accesses its own database does not protect a
separate downstream microservice. Retry only transient failures; broad
retries can waste time or repeat operations unnecessarily.

## Running the Project

Start the services in this order so that dependencies are available:

1.  Start MySQL and MongoDB.
2.  Start the Eureka Service Registry on port `8761`.
3.  Start the Config Server on port `8085`.
4.  Start HotelService on port `8082`.
5.  Start RatingService on port `8083`.
6.  Start UserService on port `8081`.
7.  Start API Gateway on port `8084`.

The services may start in a different order if they are configured to
tolerate dependencies being temporarily unavailable, but this order
makes initial troubleshooting easier.

From a module directory, use the Maven Wrapper on Windows:

``` powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

Or open each module in your IDE and run its `*Application` main class.

## Verification Checklist

-   [ ] MySQL is reachable and the `microservices` database exists.
-   [ ] MongoDB is running on port `27017`.
-   [ ] Eureka dashboard opens at `http://localhost:8761`.
-   [ ] Config Server starts on port `8085` and can read the GitHub
    config repository.
-   [ ] All services register with Eureka.
-   [ ] API Gateway starts on port `8084`.
-   [ ] UserService endpoints respond when called directly.
-   [ ] Gateway paths match backend controller mappings.
-   [ ] Actuator health is reachable at `/actuator/health`.
-   [ ] CircuitBreaker and Retry names match the annotations in Java
    code.

## Troubleshooting

### Service does not register with Eureka

-   Confirm Eureka is running at `http://localhost:8761`.
-   Check the service's effective configuration from Config Server.
-   Verify `spring.application.name` matches the intended service
    configuration.
-   Check application logs for connection or configuration errors.

### Config Server connection fails

-   Confirm Config Server is running on port `8085`.
-   Verify the Git repository URL and configuration filenames.
-   Decide whether `spring.config.import` should be mandatory or use the
    `optional:` prefix.

### Gateway returns 404

-   Confirm the requested path matches the Gateway predicate.
-   Check capitalization differences between Gateway routes and
    controller mappings.
-   Confirm the target service is registered with Eureka.

### Database connection fails

-   Confirm the database server is running.
-   Check the URL, database name, credentials, and port.
-   Keep credentials out of version control.

### CircuitBreaker health information is missing

-   Confirm Actuator is included.
-   Confirm the health endpoint is exposed.
-   Set `registerHealthIndicator: true` for the relevant CircuitBreaker
    instance.
-   Confirm the configured instance name matches the annotation name.

## Security Notes

-   Do not commit database passwords, API keys, tokens, or other
    secrets.
-   Prefer environment variables or a secure secrets-management
    solution.
-   Avoid exposing detailed Actuator health information publicly in
    production.
-   Add authentication and authorization before exposing sensitive
    endpoints.

## Current Scope and Future Improvements

Potential improvements include:

-   Add authentication and role-based authorization.
-   Add request validation and consistent error responses.
-   Add integration tests for each service and Gateway route.
-   Add centralized logging and distributed tracing.
-   Add Docker Compose for local infrastructure.
-   Document confirmed HotelService and RatingService API endpoints.
-   Add production-ready timeout, retry, and circuit-breaker policies
    for Feign calls.

## License

Add the license used by this repository before publishing or
distributing the project.
