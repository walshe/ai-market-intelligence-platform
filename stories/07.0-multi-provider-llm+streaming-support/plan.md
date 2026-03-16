# Story 07: Multi-Provider LLM Integration & Streaming Support

## Implementation Plan

---

# Overview

This story introduces two major architectural improvements:

1. **LLM Provider Abstraction**
2. **Streaming Response Support**

The work will be implemented incrementally to avoid breaking the existing `/analysis` workflow.

The recommended implementation order is:

1. Introduce the LLM abstraction layer.
2. Refactor the existing OpenAI integration to use the abstraction.
3. Add Amazon Bedrock support as an additional provider.
4. Extend the abstraction to support streaming responses.
5. Implement a streaming API endpoint using Server-Sent Events (SSE).
6. Ensure compatibility with existing cost tracking and correlation logging.

This phased approach ensures the application remains functional at every stage.

---

# Phase 1 — Introduce LLM Abstraction Layer

## Goal

Decouple the application from a specific LLM provider by introducing a provider interface.

## Actions

Create a new interface:

```
LLMClient
```

Responsibilities:

* Generate completion responses
* Optionally support streaming
* Provide usage metadata (tokens, model)

Example responsibilities:

```
CompletionResponse complete(prompt, correlationId)
Flux<String> streamCompletion(prompt, correlationId)
```

Where:

* `CompletionResponse` contains final text output and token usage.
* Streaming returns incremental tokens when supported.

## Expected Result

The application can depend on `LLMClient` instead of a provider-specific implementation.

No functional changes should occur yet.

---

# Phase 2 — Refactor Existing OpenAI Integration

## Goal

Adapt the current OpenAI completion implementation to the new abstraction.

## Actions

1. Create:

```
OpenAIClient implements LLMClient
```

2. Move existing OpenAI logic into this class.

3. Ensure the implementation returns a normalized response object containing:

* generated text
* input tokens
* output tokens
* model name

4. Update the analysis workflow to depend on:

```
LLMClient
```

instead of a concrete OpenAI class.

## Expected Result

The system behaves exactly as before but is now provider-agnostic.

---

# Phase 3 — Introduce Provider Configuration

## Goal

Allow the active provider to be selected via configuration.

## Actions

Add configuration properties:

```
ai.provider=openai
ai.model=gpt-4o-mini
```

Create a configuration component that selects the correct provider implementation at startup.

For example:

```
LLMClient llmClient
```

resolved based on the configured provider.

Spring dependency injection should handle provider selection.

## Expected Result

The system continues using OpenAI but is now configurable.

---

# Phase 4 — Implement Amazon Bedrock Provider

## Goal

Add Amazon Bedrock as a supported LLM provider.

## Actions

Create:

```
BedrockClient implements LLMClient
```

Responsibilities:

* invoke the Bedrock Runtime API
* normalize responses into the shared `CompletionResponse` format
* extract token usage metadata when available

Initial model support may include:

* Claude 3 Sonnet
* Titan Text

Configuration example:

```
ai.provider=bedrock
ai.model=anthropic.claude-3-sonnet
```

Ensure cost tracking integrates with Bedrock responses.

## Expected Result

The system can switch between OpenAI and Bedrock using configuration only.

---

# Phase 5 — Extend Abstraction for Streaming

## Goal

Enable streaming responses for supported providers.

## Actions

Extend the `LLMClient` interface with a streaming method.

Example capability:

```
streamCompletion(prompt, correlationId)
```

Return type should support incremental delivery, such as:

```
Flux<String>
```

Providers that do not support streaming may fall back to synchronous responses.

Streaming should not break existing synchronous functionality.

## Expected Result

The application can generate responses incrementally.

---

# Phase 6 — Implement Streaming Analysis Endpoint

## Goal

Expose streaming responses via a dedicated endpoint.

## Endpoint

```
GET /api/analysis/stream
```

Transport mechanism:

```
text/event-stream
```

using **Server-Sent Events (SSE)**.

## Workflow

The streaming endpoint performs the same RAG workflow:

1. Generate query embedding
2. Perform vector search
3. Construct prompt
4. Stream completion tokens

Streaming should begin immediately after the LLM response starts.

## Testing

Streaming can be demonstrated using:

```
curl -N http://localhost:8080/api/analysis/stream
```

Expected output:

```
data: The
data: market
data: conditions
data: suggest
```

---

# Phase 7 — Ensure Cost Tracking Compatibility

## Goal

Maintain compatibility with the existing cost governance system.

## Actions

Ensure all providers supply normalized usage metadata including:

* model name
* input tokens
* output tokens

The `CostTrackingService` must continue generating `CostLog` entries.

Correlation IDs must propagate through:

* completion calls
* streaming completions when possible

Streaming may record cost after the completion finishes.

## Expected Result

Cost tracking works consistently across all providers.

---

# Phase 8 — Validation and Testing

## Verification Steps

1. Run `/analysis` using OpenAI.
2. Switch provider configuration to Bedrock.
3. Run `/analysis` again and confirm successful completion.
4. Test `/analysis/stream` endpoint.
5. Verify streaming tokens arrive incrementally.
6. Confirm cost logs are generated correctly.
7. Verify correlation IDs remain attached to requests.

## Expected Result

* Both providers operate correctly.
* Streaming works when supported.
* Cost tracking remains accurate.
* Observability features continue to function.

---

# Final Architecture Outcome

After this story the AI layer will resemble:

```
AnalysisService
      |
      v
   LLMClient
     /   \
    /     \
OpenAI   Bedrock
```

Streaming capability will be available for supported providers without modifying the core RAG workflow.

This architecture enables future additions such as:

* local models
* additional cloud providers
* provider failover
* model experimentation
