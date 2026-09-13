# Level 2 Architectural Plan

## Goal

Add an authenticated endpoint that generates an alternative version of a feed
entry using a configurable large-language-model (LLM) provider. The design must
keep provider-specific code isolated, avoid exposing credentials, and return
different HTTP statuses for an unknown entry and an LLM failure.

## DTOs

Add request/response DTOs under the existing frontend model/request conventions:

- `GenerateAlternativeRequest` if the operation later needs options such as
  tone, language, or length. The initial endpoint can derive its input from the
  persisted entry and require no request body.
- `AlternativeTextResponse` containing the generated text and, optionally,
  provider/model metadata that is safe to expose.
- Keep DTOs independent of JPA entities. Do not serialize `User`, `FeedEntry`,
  or provider client objects directly.

## LLM service layer

Create a backend service boundary that separates application logic from the
provider implementation:

1. Define an `LlmService` interface with an operation that accepts the feed
   entry content and returns generated alternative text.
2. Implement a provider adapter for the selected deployment:
   - Groq or Gemini for a hosted API, using an environment-provided API key.
   - Ollama for a local deployment, using a configurable base URL and no
     committed secret.
3. Read provider selection, model, endpoint, and credentials from environment
   variables or the existing configuration system. Never hardcode API keys,
   place secrets in source control, or log request credentials/prompts that may
   contain private data.
4. Translate provider timeouts, rate limits, invalid responses, and transport
   failures into a dedicated service exception. Keep retries bounded and avoid
   silently returning placeholder text.
5. Unit-test the orchestration with a mocked `LlmService`; provider adapters
   should have focused tests for response parsing and failure translation.

## REST endpoint

Add a secured JAX-RS resource in the existing REST resource package:

```text
POST /entry/{id}/generate-alternative
```

The resource should:

- Extract the authenticated user using `AuthenticationContext`.
- Load the entry through the service/DAO layer and enforce the same ownership
  and subscription rules used by existing entry resources.
- Build the provider-neutral input from the entry title/content.
- Return `AlternativeTextResponse` as JSON.
- Keep transaction boundaries at the resource/service boundary according to
  existing project conventions; the outbound LLM call must not hold a database
  transaction longer than necessary.

## HTTP status mapping

Use distinct, stable responses:

| Condition | Status | Meaning |
| --- | ---: | --- |
| Entry does not exist or is not visible to the authenticated user | `404 Not Found` | The requested entry cannot be generated. |
| LLM provider timeout, rate limit, transport failure, or invalid provider response | `502 Bad Gateway` | The application could not obtain a valid response from the upstream LLM. |
| LLM feature is not configured or no provider is available | `503 Service Unavailable` | The capability is temporarily unavailable on this instance. |
| Invalid request parameters | `400 Bad Request` | The request cannot be processed as submitted. |

Use the project's existing exception mapping conventions so error bodies remain
consistent. Do not collapse missing entries and upstream LLM failures into the
same generic success-shaped response.

## Delivery sequence

1. Add DTOs and service/provider interfaces.
2. Add configuration-backed provider selection and one adapter.
3. Add service tests for entry lookup, authorization, successful generation, and
   each error classification.
4. Add the secured REST endpoint and resource-level tests.
5. Run Spotless and the targeted server tests with the Java 21 Maven-wrapper
   command before committing.
