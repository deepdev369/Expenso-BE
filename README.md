# Expenso Backend

Expenso is a modern, offline-first personal finance application. This repository contains the **Java Backend**, which acts as the central synchronization hub for mobile clients and orchestrates complex asynchronous flows with the Python AI Microservice.

## 🏗 Architecture

The backend is built using **Hexagonal Architecture (Ports and Adapters)**. This cleanly separates our core business logic from framework-specific implementation details.

### Core Stack
*   **Java 21**
*   **Spring Boot 3.2.x**
*   **Spring Data JPA / Hibernate 6.x**
*   **PostgreSQL**
*   **Spring Security (OAuth2 Login & JWT)**

### Key Technical Features
1.  **Offline-First Mobile Sync:** Traditional REST CRUD operations have been replaced with high-performance `/sync` bulk upsert endpoints. This allows the Android application to resolve conflicts gracefully and push hundreds of offline edits in a single network call using client-generated UUIDs (`clientReferenceId`).
2.  **Stateful AI Webhooks:** Tight integration with a Fast-API Python Microservice. Audio and image processing requests return instantly (`202 Accepted`) to prevent mobile network timeouts, while the AI extraction runs asynchronously and calls back via Webhooks to create "Stateful Drafts" in the Postgres database.
3.  **Enterprise Tenant Isolation:** Implements Multi-Tenancy using Aspect-Oriented Programming (AOP) and Hibernate `@Filter`. All database reads and writes are automatically scoped to the authenticated user's ID at the database engine level, making cross-tenant data leaks mathematically impossible.
4.  **Global Soft Deletes:** Soft deletion is natively configured across all entities using Hibernate `@SQLRestriction`, ensuring deleted records are automatically ignored by all queries globally.

---

## 🚀 Getting Started

### Prerequisites
*   JDK 21
*   Maven
*   PostgreSQL running locally or via Docker
*   The Expenso Python AI Microservice (running locally on port `8000`)

### Configuration
1.  Ensure you have a PostgreSQL database named `expenso`.
2.  Update your `application.properties` with your database credentials and OAuth2 keys:
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/expenso
    spring.datasource.username=postgres
    spring.datasource.password=postgres
    
    spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_ID
    spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_SECRET
    
    ai.service.internal-token=valid-internal-token
    ```

### Running Locally
You can boot the application using the Maven wrapper:

```bash
./mvnw spring-boot:run
```
The application will be accessible at `http://localhost:8080`.

### Building for Production
To package the application into a standalone JAR:

```bash
./mvnw clean package
```

Run the packaged JAR with the production profile:

```bash
java -Dspring.profiles.active=production -jar ./target/expenso-0.0.1-SNAPSHOT.jar
```

## 📚 Further Reading
* [Bootify.io Hexagonal Architecture Tips](https://bootify.io/next-steps/)
* [Spring Boot Reference Guide](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
* [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/jpa.html)
