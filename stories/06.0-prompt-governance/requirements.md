# Story 06.0: Prompt Governance

## Description

Introduce a prompt governance mechanism to centralize and manage prompts used for LLM interactions.

Prompts should not be hard-coded directly within service logic. Instead, they should be managed through a structured prompt repository to improve maintainability, observability, and future extensibility.

This mechanism will support versioning, auditing, and safe evolution of prompts without requiring extensive code modifications.

---

# Architectural Context

The system currently performs LLM completions for the `/analysis` workflow using prompts that define:

- the system instruction
- the expected output structure
- the context format for retrieved documents

Without governance, prompts risk becoming:

- duplicated across services
- difficult to modify safely
- inconsistent across environments

Centralizing prompts provides a single source of truth for prompt definitions and ensures consistent usage across the AI pipeline.

---

# Requirements

## A) Prompt Registry

- [ ] Implement a centralized prompt registry.

The registry must:

- store prompts using a unique prompt key
- allow prompts to be retrieved by key
- support multiple prompt definitions if additional workflows are introduced later

Example prompt keys:

- `analysis.system.prompt`
- `analysis.user.template`

---

## B) Prompt Structure

Prompts must support the following components:

- system prompt
- user prompt template
- placeholders for runtime variables

Example placeholders:

- `{query}`
- `{context}`

Prompt templates should support simple variable substitution.

Example:

```

System Prompt:
You are a financial analysis assistant.

User Prompt Template:
Given the following context:

{context}

Answer the question:

{query}

```

---

## C) Prompt Management

Prompts must be stored in a centralized configuration source.

Acceptable MVP options:

- application configuration files
- structured YAML or JSON configuration

Database-backed prompt storage is **out of scope for this story**.

---

## D) Prompt Retrieval Service

- [ ] Implement `PromptService`.

Responsibilities:

- retrieve prompt definitions by key
- perform placeholder substitution
- return fully constructed prompt objects ready for LLM invocation

The service must be reusable across multiple AI workflows.

---

## E) Integration with LLM Calls

- [ ] Update the analysis workflow to obtain prompts via `PromptService`.

The analysis service must:

- request the prompt using its key
- supply runtime variables such as `{query}` and `{context}`
- construct the final prompt for the completion model

Direct prompt strings in service classes must be removed.

---

## F) Logging & Observability

- [ ] Log the prompt key used during each LLM invocation.

Example log:

```

analysis request correlationId=abc123 prompt=analysis.system.prompt

```

The full prompt text should **not** be logged to avoid excessive log volume or potential data leakage.

---

# Non-Goals

This story does not include:

- prompt A/B testing
- prompt versioning in a database
- prompt experimentation frameworks
- prompt editing UI
- prompt safety filters or guardrails

These may be introduced in future stories.

---

# Acceptance Criteria

- Prompts are no longer hard-coded within service logic.
- Prompts are retrieved through `PromptService`.
- Prompt templates support placeholder substitution.
- Analysis workflow uses prompts from the centralized registry.
- Prompt keys are logged for observability.
- System behavior remains unchanged aside from prompt management improvements.
- Application boots successfully and all tests pass.
```