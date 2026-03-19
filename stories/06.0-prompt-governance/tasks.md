# Tasks: Story 06.0 — Prompt Governance

## 1. Prompt Registry & Structure (Phase 1)

- [x] Create `PromptProperties` in `com.walshe.aimarket.config`.
    - Use `@ConfigurationProperties(prefix = "application.prompts")`.
    - Define a `Map<String, PromptDefinition>` to hold prompt configurations.
- [x] Create `PromptDefinition` record in `com.walshe.aimarket.service.dto`.
    - Fields: `systemPrompt`, `userTemplate`.
- [x] Add initial prompts to `backend/v2/src/main/resources/config/application.yml`.
    - Key: `analysis.system.prompt` -> "You are a financial analysis assistant."
    - Key: `analysis.user.template` -> "Given the following context: {context}\n\nAnswer the question: {query}"

## 2. Prompt Retrieval Service Implementation (Phase 2)

- [x] Create `PromptService` interface and its implementation `PromptServiceImpl`.
- [x] Implement `getPrompt(String key)` to retrieve a `PromptDefinition` from the properties map.
- [x] Implement `renderUserTemplate(String key, Map<String, String> variables)` method.
    - Logic: Use simple `{placeholder}` replacement using `String.replace()` or `MessageFormat`.
- [x] Add basic error handling for missing prompt keys or variables.

## 3. Integration with Analysis Workflow (Phase 3)

- [x] Identify the service class(es) currently using hard-coded strings (e.g., `AnalysisServiceImpl`).
- [x] Inject `PromptService` using constructor injection.
- [x] Refactor the completion call logic to:
    - Retrieve system prompt via `promptService.getPrompt("analysis.system.prompt")`.
    - Render user prompt template via `promptService.renderUserTemplate("analysis.user.template", variables)`.
- [x] Remove hard-coded prompt constants or literals from the code.

## 4. Logging & Observability (Phase 4)

- [x] Add logging in `PromptServiceImpl` or the calling service to record the prompt key used.
    - Example: `logger.info("Using prompt key: {} for request: {}", key, correlationId);`
- [x] Verify that the full prompt text is **not** being logged as per Requirement F.

## 5. Testing & Verification (Phase 5)

- [x] Create unit tests for `PromptServiceImpl`:
    - `testShouldRetrieveExistingPromptByKey`
    - `testShouldPerformVariableSubstitution`
    - `testShouldThrowExceptionForMissingPromptKey`
- [x] Update `AnalysisServiceImplIntTest` to verify integration:
    - Ensure the LLM client still receives expected content.
- [x] Verify application boots successfully with the new configuration.
