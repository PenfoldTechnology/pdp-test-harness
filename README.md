# PDP Certificate Authority (C&A) Stub

This is a standalone Java Spring Boot application that simulates the behavior of the real Certificate Authority (C&A) system for Pension Dashboard Provider (PDP) testing and development.

## Overview

The C&A stub implements the OAuth2/UMA 2.0 specification required by the PDP ecosystem, providing essential endpoints for token management, resource registration, and permission handling.

### Key Features

- **OAuth2/UMA 2.0 Compliance**: Full implementation of token, introspection, permission, and resource registration endpoints
- **Embedded H2 Database**: Persistent storage for resources, tokens, and permissions
- **OpenAPI Integration**: Auto-generated REST controllers from OpenAPI specifications
- **Docker Support**: Containerized deployment with Docker Compose integration
- **HTTP/HTTPS Support**: Configurable secure and insecure endpoints
- **mTLS Ready**: Mutual TLS support for production-like scenarios

## Architecture

### Technology Stack
- **Framework**: [Spring Boot](https://spring.io/projects/spring-boot) 3.x
- **Java Version**: Java 21 (LTS)
- **Build Tool**: Gradle 8.x
- **Database**: H2 (embedded, file-based with TCP server)
- **API Specification**: OpenAPI 3.0 with code generation

### Core Endpoints

The C&A stub implements the following PDP Technical Specification (v1.1) endpoints:

| Endpoint | Purpose | Description |
|----------|---------|-------------|
| **`POST /token`** | Token issuance | Issues access tokens (RPTs) for various OAuth2 grant types |
| **`POST /introspect`** | Token validation | Validates tokens and returns metadata |
| **`POST /perm`** | Permission tickets | Issues UMA permission tickets for resource access |
| **`POST /rreguri`** | Resource registration | Manages registered resource URIs |
| **`GET /jwks`** | Public keys | Provides JWT verification keys |

### Database Schema

The embedded H2 database maintains:
- **Registered Resources**: Pension data endpoints and metadata
- **Users**: Test user accounts and credentials  
- **Permissions**: Access control and scope definitions
- **Tokens**: Issued RPTs and their lifecycle state

## Quick Start

### Using Docker Compose (Recommended)

From the project root directory:

```bash
# Start the C&A stub
docker-compose up ca-stub

# Run in background
docker-compose up -d ca-stub

# View logs
docker-compose logs -f ca-stub

# Stop the service
docker-compose down
```

### Manual Setup

#### Prerequisites
- **Java 21**: `java -version` should show version 21
- **Gradle**: Version 8.x (or use included wrapper)
- **Docker**: For containerized deployment (optional)

#### Build and Run

```bash
# Navigate to C&A stub directory
cd external/cas-stub

# Build the application
./gradlew build

# Run the application
./gradlew bootRun

# Alternative: Build JAR and run
./gradlew bootJar
java -jar build/libs/cas-stub-*.jar
```

## Available Services

Once running, the C&A stub exposes the following services:

| Service | URL | Purpose |
|---------|-----|---------|
| **OAuth2/UMA API** | http://localhost:8081 | Main API endpoints |
| **Secure API** | https://localhost:8443 | HTTPS/mTLS endpoints |
| **H2 Web Console** | http://localhost:8081/data | Database management UI |
| **Health Check** | http://localhost:8081/health | Service health status |
| **OpenAPI Docs** | http://localhost:8081/swagger-ui.html | Interactive API documentation |
| **Home Page** | http://localhost:8081/ | Service information |

### Database Access

**H2 Web Console**: http://localhost:8081/data
- **JDBC URL**: `jdbc:h2:file:./db/cas`
- **Username**: `sa`
- **Password**: (leave empty)

**Direct JDBC Connection**:
```
URL: jdbc:h2:file:./db/cas
Driver: org.h2.Driver
User: sa
Password: (empty)
```

## Configuration

### Environment Variables

The C&A stub can be configured via environment variables (used in docker-compose.yml):

```bash
# Port Configuration
PORT=8443                    # HTTPS/secure port
UNSECURE_PORT=8081          # HTTP/unsecure port

# Database Configuration  
DB=jdbc:h2:file:./db/cas;AUTO_SERVER=TRUE

# Security Configuration
ENABLE_SSL=false            # Enable/disable HTTPS
ENABLE_MTLS=want           # mTLS configuration (want/need/none)

# Logging
LOG_LEVEL=INFO             # Logging level (DEBUG/INFO/WARN/ERROR)
```

### Application Properties

Key configuration in `src/main/resources/application.yaml`:

```yaml
server:
  port: 8081                # Default HTTP port
  ssl:
    enabled: false         # SSL configuration

spring:
  datasource:
    url: jdbc:h2:file:./db/cas;AUTO_SERVER=TRUE
    username: sa
    password:
    driver-class-name: org.h2.Driver
  
  h2:
    console:
      enabled: true        # Enable H2 web console
      path: /data          # Console URL path
```

## Docker Deployment

### Using Docker Compose (Recommended)

The project includes a `docker-compose.yml` configuration for easy deployment:

```yaml
services:
  ca-stub:
    build:
      context: ./external/cas-stub
      dockerfile: Dockerfile
    ports:
      - "8443:8443"  # HTTPS port
      - "8081:8081"  # HTTP port
    environment:
      - PORT=8443
      - UNSECURE_PORT=8081
      - DB=jdbc:h2:file:./db/cas;AUTO_SERVER=TRUE
      - ENABLE_SSL=false
      - ENABLE_MTLS=want
    volumes:
      - ./external/cas-stub/db:/cas/db
      - ./external/cas-stub/mTLS:/cas/mTLS
```

### Manual Docker Build

```bash
# Build Docker image
docker build . -t pdp-cas-stub:latest --build-arg API_PORT=8081

# Run container
docker run -p 8081:8081 -p 8443:8443 \
  -v ./casTestLogs:/cas/logs \
  -v ./db:/cas/db \
  pdp-cas-stub:latest
```

## Testing & Verification

### Health Check

Verify the C&A stub is running:

```bash
# Basic health check
curl http://localhost:8081/health

# Expected response
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}
```

### API Testing

Test the OAuth2/UMA endpoints:

```bash
# Test token endpoint
curl -X POST http://localhost:8081/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=test&client_secret=test"

# Test introspection endpoint  
curl -X POST http://localhost:8081/introspect \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "token=your_token_here"
```

### Database Verification

Access the H2 console to verify database setup:

1. Navigate to http://localhost:8081/data
2. Use connection details:
   - **JDBC URL**: `jdbc:h2:file:./db/cas`
   - **User**: `sa`
   - **Password**: (empty)
3. Check tables: `REGISTERED_RESOURCE`, `USER`, `SCOPE`

## Troubleshooting

### Common Issues

**Port already in use:**
```bash
# Check what's using port 8081
lsof -i :8081

# Kill process if needed
kill -9 <PID>
```

**Java version issues:**
```bash
# Check Java version
java -version

# Should show Java 21
# Install Java 21 if needed:
# - macOS: brew install openjdk@21
# - Ubuntu: apt install openjdk-21-jdk
```

**Database connection issues:**
```bash
# Verify database files exist
ls -la db/cas.*

# Check database file permissions
ls -la db/
```

**Build failures:**
```bash
# Clean build
./gradlew clean build

# Build with debug info
./gradlew build --info --stacktrace
```

### Logging

**Enable debug logging:**
```bash
# Set environment variable
export LOG_LEVEL=DEBUG

# Or modify application.yaml
logging:
  level:
    uk.org.ca.stub.simulator: DEBUG
```

**View application logs:**
```bash
# Docker Compose logs
docker-compose logs -f ca-stub

# Container logs  
docker logs -f <container_id>

# Local file logs
tail -f casTestLogs/server.log
```

## Development Guide

### Starting the service

```bash
# Development mode with hot reload
./gradlew bootRun

# Build and run JAR
./gradlew bootJar
java -jar build/libs/cas-stub-*.jar

# Run tests
./gradlew test
```

### Code structure

```
└── uk.org.ca.stub.simulator
    ├── configuration
    │   └── dbinitializer
    ├── entity
    ├── filter
    ├── interceptor
    ├── pojo
    │   └── entity
    ├── repository
    ├── rest
    │   ├── api
    │   ├── controller
    │   ├── exception
    │   └── model
    ├── service
    └── utils
```

The package `uk.org.ca.stub.simulator.rest.*` contains the API files. +
Package `controller` contains the implementation of the interfaces within `api`. +
The content of the `api` and `model` packages are generated using a generator from the Original OpenAPI specification, 
but `controller` is manually created, and it will need to be modified if the interface / the API spec change.

Each controller will rely on a service to implement the stub logic.

Persistence entities are modeled using JPA and the repositories implement Spring `JpaRepository` for interact with the
database.

### Code generation

The API interfaces code is generated using the Gradle Open API tool.
The task `openApiGenerate` configured in the [build.gradle](build.gradle) file will build under the [generated](generated) dir 
the files based on the contents of the spec configured.

The `api` and `model` packages in [uk/org/ca/stub/simulator/rest](generated/src/main/java/uk/org/ca/stub/simulator/rest/) need to be refactored to be part of the actual
project withing the `uk.org.ca.stub.simulator.rest` package

Links:

* [Gradle plugin documentation](https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-gradle-plugin)
* [Spring generator documentation](https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/spring.md)

### Container Build

The [Dockerfile](Dockerfile) in the project will use a container to build the project as a container.

To build the project, run the command:

```shell
docker build . -t pdp-stub-cas:latest --build-arg API_PORT=8081
```

### Running the Container

For running it, binding the local directory in the operator's file system `casTestLogs` with the container logs 
directory; the application will use it to keep the log files (adjust the port if needed):

```shell
docker run -p 8081:8081 -v ./casTestLogs:/cas/logs pdp-stub-cas:latest
```

The http logs will be also printed in the console.

If you want to use different values for DB location and credentials, you can run it with:
