# CommaFeed AI Assistant Instructions

## Project Context
- CommaFeed is a self-hosted RSS reader in Java (module: commafeed-server).
- NEVER touch or edit anything in commafeed-client (React frontend). Focus exclusively on commafeed-server.

## Architectural Layering Rules
Strictly follow the existing 4-layer architecture of the project:
1. JPA Entity: Put in com.commafeed.backend.model. Follow existing JPA annotations.
2. DAO: Put in com.commafeed.backend.dao. MUST extend GenericDAO<T> matching existing DAO implementations.
3. Service: Put in com.commafeed.backend.service. Contains business logic and transactional operations.
4. REST Resource: Put in com.commafeed.frontend.rest.resources. Use JAX-RS annotations. Reuse existing DTOs, response structures, and exception mapping.

## Guidelines & Constraints
- Do not introduce unnecessary dependencies.
- Follow existing code formatting, naming conventions, and logging patterns.
- Never hardcode secrets or API keys. Read from environment variables.
- Write clean, maintainable code with proper HTTP status codes.

## Workspace Conventions
- Target Java 21 for server compilation and tests unless a task explicitly requires another release.
- Use the Maven wrapper on Windows (`.\mvnw.cmd`) rather than relying on a globally installed Maven executable.
- Run Spotless with `.\mvnw.cmd spotless:apply -pl commafeed-server`; Java source formatting uses LF line endings.
- Keep changes scoped to `commafeed-server` and project-level documentation/configuration unless the task explicitly says otherwise.
