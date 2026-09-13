# AI Decisions & Overrides Log

This log tracks architectural decisions, corrections, and manual overrides made during development.

- Removed deprecated `@Temporal` annotation from `FeedEntryNote` entity to satisfy strict `-Werror` compiler requirements.
## Log Entries

1. **PowerShell flag quoting** — Maven `-D` arguments are quoted in Windows
   commands, for example `"-Dmaven.compiler.release=21"`, so PowerShell passes
   them to the Maven wrapper as single arguments.
2. **Spotless CRLF fix** — Java formatting is applied through
   `.\mvnw.cmd spotless:apply -pl commafeed-server`; this normalizes edited
   source files to the repository's LF convention instead of retaining
   Windows CRLF line endings.
3. **Deprecated `@Temporal` removal** — The `FeedEntryNote.created` mapping was
   kept as a `Date` timestamp without the deprecated annotation so the Java 21
   compiler can run with `-Werror`.
4. **`serialVersionUID` addition** — `LlmServiceException` explicitly declares
   `serialVersionUID` to satisfy the compiler's serializable-class warning under
   the project's warnings-as-errors policy.
5. **Graceful LLM failure handling** — Provider configuration, transport, HTTP,
   timeout, and malformed-response failures are converted to safe service
   exceptions. The REST layer returns `503` when the provider is unavailable
   and `502` for upstream failures without exposing stack traces or secrets.
6. **Switch from Groq to Gemini 3.5 Flash** — The Level 2 alternative-generation
   integration was changed to Google Gemini 3.5 Flash so the implementation uses
   the selected provider and its `contents`/`parts` request contract.
7. **Fix Java HTTP URL concatenation** — The Gemini request builds its URI with
   `URI.create(API_URL + apiKey)` after keeping the `?key=` suffix in the base
   URL, avoiding malformed endpoint strings caused by separated concatenation.
8. **Test keyword notifications through a live feed subscription** — Notification
   behavior was validated in the refresh pipeline by subscribing to a live test
   feed and waiting for newly fetched entries, rather than testing only isolated
   matching helpers.
