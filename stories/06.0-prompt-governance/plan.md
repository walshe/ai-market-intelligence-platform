# Plan: Story 06.0 — Prompt Governance

## 1. Goal
Centralize and manage LLM prompts to improve maintainability, observability, and safety. Move prompts out of hard-coded service logic into a structured configuration repository.

## 2. Infrastructure Changes
- **Spring Boot Configuration**:
    - Define a custom `@ConfigurationProperties` class to hold prompt definitions.
    - Update `application.yml` (or a dedicated `prompts.yml`) with the initial prompt registry.
    - Configure the `PromptService` bean to load these properties.

## 3. Implementation Steps

### Phase 1: Prompt Registry & Structure
- Define `PromptDefinition` record to store system prompt, user template, and versioning info (optional for MVP).
- Create `PromptProperties` class annotated with `@ConfigurationProperties(prefix = "application.prompts")`.
- Populate initial prompts in `application.yml`:
    - `analysis.system.prompt`
    - `analysis.user.template`

### Phase 2: Prompt Retrieval Service
- Implement `PromptService` to:
    - Retrieve a `PromptDefinition` by its key.
    - Handle placeholder substitution (e.g., `{query}`, `{context}`) using a simple string replacement or a template engine.
    - Provide a `render(String key, Map<String, Object> variables)` method.

### Phase 3: Integration with Analysis Workflow
- Refactor `AnalysisService` (or equivalent) to inject `PromptService`.
- Replace hard-coded strings with calls to `promptService.render("analysis.user.template", variables)`.
- Ensure the system instruction is also retrieved via `PromptService`.

### Phase 4: Logging & Observability
- Add logging to `PromptService` or the calling service to record the prompt key used (but not the full text).
- Include `correlationId` in the logs to tie prompts to specific requests.

### Phase 5: Verification & Refinement
- Create unit tests for `PromptService` covering:
    - Successful retrieval and substitution.
    - Handling of missing keys (e.g., throw a custom exception).
    - Handling of missing variables in templates.
- Update integration tests for the analysis workflow to verify that behavior remains consistent.

## 4. Risks & Mitigations
- **Configuration Errors**: Typographical errors in prompt keys or placeholder names could break LLM calls. Mitigation: Use `@Validated` on `PromptProperties` and add startup checks if possible.
- **Complexity**: Over-engineering the template engine. Mitigation: Stick to simple `{variable}` substitution for the MVP as per requirements.
