# Hotel Microservices

A Java/Spring Boot microservices project demonstrating service
discovery, centralized configuration, API Gateway routing, OAuth 2.0
resource-server security, inter-service communication, and resilience
patterns.

## Overview

The application is composed of six independently runnable modules:

  ------------------------------------------------------------------------
  Module                            Port Responsibility   Data store
  ---------------- --------------------- ---------------- ----------------
  UserService                     `8081` User CRUD and    MySQL
                                         aggregation of   
                                         each user's      
                                         ratings and      
                                         associated hotel 
                                         details          

  HotelService                    `8082` Hotel CRUD       MySQL
                                         operations       

  RatingService                   `8083` Create and       MongoDB
                                         retrieve ratings 
                                         by user or hotel 

  Gateway                         `8084` Authenticated    ---
                                         entry point and  
                                         load-balanced    
                                         routing          

  ConfigServer                    `8085` Loads shared     Git repository
                                         configuration    
                                         from a           
                                         Git-backed       
                                         repository       

  Service Registry                `8761` Eureka service   ---
                                         registration and 
                                         discovery        
  ------------------------------------------------------------------------

The MySQL services use the `microservices` database. RatingService uses
MongoDB's `microservices` database.

## Architecture

``` text
                         Client
                           |
                           v
                    API Gateway :8084
                    /       |       \
                   v        v        v
            UserService HotelService RatingService
               :8081       :8082       :8083
                   \        |         /
                    \       |        /
                     Eureka Registry
                         :8761

                 ConfigServer :8085
                         |
                         v
              Git-backed configuration
```

-   Services register with Eureka and discover downstream services by
    service name.
-   Spring Cloud Gateway uses load-balanced `lb://...` destinations.
-   ConfigServer loads shared settings from the configured Git
    repository.
-   UserService uses OpenFeign to retrieve ratings and hotel details,
    then aggregates those details into user responses.
-   UserService forwards the incoming `Authorization: Bearer ...` header
    on its OpenFeign requests when the incoming request has a bearer
    token.

## Technology Stack

-   Java 25
-   Spring Boot 4.1.1
-   Spring Cloud 2025.1.3
-   Spring Cloud Gateway (WebFlux)
-   Spring Cloud Config
-   Netflix Eureka
-   Spring Cloud OpenFeign
-   Spring Security OAuth 2.0 Resource Server / JWT
-   Resilience4j
-   Spring Data JPA / Hibernate
-   MySQL
-   Spring Data MongoDB / MongoDB
-   Spring Boot Actuator
-   Maven

> Use the Java and framework versions configured in the module POM
> files. Confirm Spring Boot and Spring Cloud compatibility before
> upgrading versions.

## Prerequisites

Install and configure:

-   JDK 25
-   Maven, or the Maven Wrapper included in a module
-   MySQL Server
-   MongoDB Server
-   Git
-   An IDE such as Spring Tool Suite or IntelliJ IDEA (optional)

## Database Setup

### MySQL

Create the database used by UserService and HotelService:

``` sql
CREATE DATABASE microservices;
```

The service configuration defaults to:

``` yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/microservices}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

Set `DB_PASSWORD` in your environment (and `DB_USERNAME` or `DB_URL` if
needed). Never commit real database credentials.

### MongoDB

Start MongoDB locally. RatingService uses the `microservices` database
with the following connection details:

``` yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017
      database: microservices
```

MongoDB can create the database and collections when data is first
written.

## Centralized Configuration

The services import ConfigServer at:

``` text
http://localhost:8085
```

The local import currently used by the modules is:

``` yaml
spring:
  config:
    import: "configserver:http://localhost:8085"
```

Because the import is not marked `optional:`, a service may fail to
start if ConfigServer is unavailable or cannot retrieve the required
configuration.

The ConfigServer must point to the intended Git-backed configuration
repository. Make sure the repository contains configuration files
matching the services' `spring.application.name` values. Review the
external configuration repository for environment-specific values,
including the Auth0 issuer and audience configuration; do not put
secrets in this README.

Configuration repository referenced during development:
[Hotel-Microservices-Configuration](https://github.com/Vikasgabale9/Hotel-Microservices-Configuration)

## Security

The Gateway, UserService, HotelService, and RatingService each configure
Spring Security as an OAuth 2.0 resource server:

-   CSRF is disabled for these stateless API configurations.
-   Form login and HTTP Basic authentication are disabled.
-   `/actuator/health` is permitted without authentication.
-   Other requests require a valid bearer token.
-   JWT validation settings, including the expected issuer and any
    audience validation, must be supplied and verified in the effective
    configuration used by each service.

Example request header:

``` http
Authorization: Bearer <access-token>
```

UserService's `InterServiceAuthConfig` forwards the incoming bearer
token on OpenFeign requests when the incoming request includes an
`Authorization` header beginning with `Bearer`. This allows downstream
resource servers to validate the same token.

**Important:** This repository's Java security configuration enables JWT
resource-server support, but the effective issuer/audience values may be
supplied by ConfigServer. Verify those values in the configuration
repository and test a valid token, an expired token, and a token with
the wrong issuer/audience before relying on the setup.

## API Gateway Routes

Gateway listens on port `8084` and declares these routes:

  Route ID          Path predicate     Destination
  ----------------- ------------------ ----------------------
  `USERSERVICE`     `/api/users/**`    `lb://USERSERVICE`
  `HOTELSERVICE`    `/api/hotels/**`   `lb://HOTELSERVICE`
  `RATINGSERVICE`   `/ratings/**`      `lb://RATINGSERVICE`

Use the path capitalization shown below. The UserController mapping is
`/api/users`, so `/api/users/...` is the expected path.

## API Reference

All business API requests require a valid bearer token unless the
security configuration is deliberately changed. Examples below use the
Gateway address; replace `<access-token>` with a valid token.

### UserService

Base path: `/api/users`

  -----------------------------------------------------------------------
  Method                  Path                    Purpose
  ----------------------- ----------------------- -----------------------
  `POST`                  `/api/users/create`     Create a user

  `GET`                   `/api/users/getAll`     Retrieve users with
                                                  their ratings and hotel
                                                  details

  `GET`                   `/api/users/{userId}`   Retrieve a user with
                                                  ratings and hotel
                                                  details

  `PUT`                   `/api/users/{userId}`   Update a user

  `DELETE`                `/api/users/{userId}`   Delete a user
  -----------------------------------------------------------------------

Example:

``` bash
curl -H "Authorization: Bearer <access-token>" \
  http://localhost:8084/api/users/getAll
```

Create a user:

``` bash
curl -X POST http://localhost:8084/api/users/create \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Example User","email":"example@example.com","about":"Sample profile"}'
```

The create endpoint returns an `ApiResponse` wrapper. Update and delete
endpoints also use the service's `ApiResponse` format.

### HotelService

Base path: `/api/hotels`

  Method     Path                 Purpose
  ---------- -------------------- ------------------------
  `POST`     `/api/hotels`        Create a hotel
  `GET`      `/api/hotels`        Retrieve all hotels
  `GET`      `/api/hotels/{id}`   Retrieve a hotel by ID
  `PUT`      `/api/hotels/{id}`   Update a hotel
  `DELETE`   `/api/hotels/{id}`   Delete a hotel

Example:

``` bash
curl -H "Authorization: Bearer <access-token>" \
  http://localhost:8084/api/hotels
```

### RatingService

Base path: `/ratings`

  Method   Path                         Purpose
  -------- ---------------------------- ------------------------------
  `POST`   `/ratings`                   Create a rating
  `GET`    `/ratings`                   Retrieve all ratings
  `GET`    `/ratings/user/{userId}`     Retrieve ratings for a user
  `GET`    `/ratings/hotel/{hotelId}`   Retrieve ratings for a hotel

Example:

``` bash
curl -H "Authorization: Bearer <access-token>" \
  http://localhost:8084/ratings/user/USR001
```

Use the request body fields defined by the `Hotel` and `Rating` model
classes when creating or updating records.

## Resilience4j

UserService configures two circuit-breaker instances:

-   `ratingHotelBreaker` --- used by `UserController.getAllUsers()` and
    `UserServiceImpl.getAllUsers()`.
-   `userByIdBreaker` --- used by `UserController.getUser()` and
    `UserServiceImpl.getUser()`.

The UserController fallback methods return a `503 Service Unavailable`
response. The service-layer fallback methods log the failure and throw
an `IllegalStateException`.

The UserService YAML also defines Retry and RateLimiter instances. In
the inspected Java source, corresponding `@Retry` and `@RateLimiter`
annotations were not found. Therefore, those YAML entries should be
treated as configuration definitions, not as proof that Retry or
RateLimiter is currently applied to methods.

The circuit breakers are useful around the user aggregation flow because
it calls downstream HotelService and RatingService endpoints. Test the
fallback behavior by making a downstream service unavailable and then
calling the affected UserService endpoint.

## Actuator and Health Checks

The project includes Actuator health endpoints. UserService exposes
`health`; HotelService and RatingService expose `health` and `info` in
their local configuration. The Gateway security configuration permits
`/actuator/health`.

Check endpoints locally:

``` text
http://localhost:8081/actuator/health
http://localhost:8082/actuator/health
http://localhost:8083/actuator/health
```

Only expose detailed health information in trusted development
environments. Do not publicly expose detailed Actuator data in
production without an appropriate security policy.

Eureka dashboard:

``` text
http://localhost:8761
```

## Running the Project

Start the dependencies and services in this order:

1.  Start MySQL and MongoDB.
2.  Start the Service Registry on port `8761`.
3.  Start ConfigServer on port `8085` and verify it can read the Git
    configuration repository.
4.  Start HotelService on port `8082`.
5.  Start RatingService on port `8083`.
6.  Start UserService on port `8081`.
7.  Start Gateway on port `8084`.

From a module directory on Windows, build and run with the Maven
Wrapper:

``` powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

If the wrapper is not present in that module, use an installed Maven
version:

``` bash
mvn clean package
mvn spring-boot:run
```

Run each module's main application class from your IDE if preferred.

## Verification Checklist

-   [ ] MySQL is running and the `microservices` database exists.
-   [ ] MongoDB is running on `localhost:27017`.
-   [ ] Eureka dashboard opens at `http://localhost:8761`.
-   [ ] ConfigServer starts on port `8085` and reads the expected Git
    repository.
-   [ ] All three business services register with Eureka.
-   [ ] Gateway starts on port `8084`.
-   [ ] A valid access token succeeds on protected endpoints.
-   [ ] Missing, expired, or invalid tokens are rejected.
-   [ ] Auth0 issuer and audience settings are correct in effective
    configuration.
-   [ ] Gateway routes match controller paths and return expected
    responses.
-   [ ] UserService can aggregate rating and hotel data through
    OpenFeign.
-   [ ] Circuit-breaker fallback behavior is tested when downstream
    calls fail.
-   [ ] Actuator endpoints expose only the intended information.

## Troubleshooting

### Service does not register with Eureka

-   Confirm Eureka is running at `http://localhost:8761`.
-   Check each service's effective configuration and
    `spring.application.name`.
-   Inspect application logs for connection and registration errors.

### ConfigServer connection fails

-   Confirm ConfigServer is running on port `8085`.
-   Verify the Git repository URL, access permissions, and configuration
    filenames.
-   Confirm the service's `spring.config.import` value is correct.

### Gateway returns 404

-   Confirm the requested path matches a Gateway predicate.
-   Check controller mappings and path capitalization.
-   Confirm the target service is registered with Eureka.

### API returns 401 Unauthorized

-   Include `Authorization: Bearer <access-token>`.
-   Verify token expiration and signature.
-   Check the configured issuer and audience against the token and Auth0
    API settings.

### API returns 503 or aggregation fails

-   Check that HotelService and RatingService are running and
    registered.
-   Call downstream endpoints directly with a valid token.
-   Inspect UserService logs for OpenFeign errors and circuit-breaker
    fallback messages.

### Database connection fails

-   Confirm the database server is running.
-   Check the URL, database name, username, password, and port.
-   Keep secrets out of source control.

### Circuit-breaker health information is missing

-   Confirm Actuator is included and the health endpoint is exposed.
-   Confirm `registerHealthIndicator: true` is configured for the
    relevant instance.
-   Ensure the instance name matches the `@CircuitBreaker` annotation.

## Security and Production Notes

-   Never commit database passwords, access tokens, Auth0 secrets, or
    other credentials.
-   Use environment variables or a dedicated secrets manager for
    sensitive configuration.
-   Do not expose detailed Actuator health information publicly.
-   Configure and verify JWT issuer/audience validation for every
    resource server.
-   Use appropriate timeouts and resilience policies for remote calls.
-   Add integration tests for authentication, Gateway routing, database
    access, and downstream failures before production deployment.

## Future Improvements

-   Apply Retry and RateLimiter annotations to selected methods only
    after defining appropriate behavior and testing their interaction
    with circuit breakers.
-   Add request validation and consistent error responses across
    services.
-   Expand automated integration and security tests.
-   Add centralized logging and distributed tracing.
-   Add Docker Compose for local infrastructure.
-   Document any additional endpoints when they are added to the
    controllers.

## License

No license file was confirmed in the supplied project snapshot. Add a
`LICENSE` file and update this section if you intend to distribute the
project publicly.
