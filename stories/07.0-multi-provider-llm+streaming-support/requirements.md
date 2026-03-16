# Story 07: Multi-Provider LLM Integration & Streaming Support

## Description

Extend the AI integration layer to support multiple Large Language Model (LLM) providers and introduce optional streaming responses for completion generation.

The current system is tightly coupled to a single LLM provider and synchronous completion responses. This story introduces an abstraction layer that allows the application to integrate with multiple providers, including Amazon Bedrock, while maintaining a consistent internal interface.

Additionally, streaming support will be added to enable incremental delivery of generated responses when supported by the underlying provider.

This improves architectural flexibility, cloud portability, and user experience for long-running completions.

---

# Architectural Context

The current `/analysis` workflow performs the following steps:

1. Query embedding generation
2. Vector similarity search
3. Final completion generation via an external LLM provider

The completion service currently assumes a single provider and returns the full response only after generation completes.

Introducing a provider abstraction allows the system to support multiple LLM backends without modifying core business logic.

Streaming support allows partial responses to be delivered incrementally when supported by the provider.

---

# Requirements

## A) LLM Provider Abstraction

* [ ] Introduce a provider abstraction for LLM completion operations.

A common interface must be defined, for example:

```
LLMClient
```

Responsibilities:

* generate completion responses
* optionally support streaming completions
* expose model name and usage metadata

---

## B) OpenAI Provider Implementation

* [ ] Refactor the existing OpenAI integration to implement the `LLMClient` interface.

Responsibilities:

* invoke OpenAI completion API
* return normalized response objects
* expose token usage metadata for cost tracking

Existing functionality must remain unchanged.

---

## C) Amazon Bedrock Provider Implementation

* [ ] Implement a new provider using:

```
Amazon Bedrock Runtime API
```

Responsibilities:

* invoke supported Bedrock models
* normalize response format to match the `LLMClient` interface
* extract token usage metadata when available

The system should support at least one Bedrock model, for example:

* Anthropic Claude
* Titan Text

Configuration should allow selecting the provider and model at runtime.

Example configuration:

```
ai:
  provider: bedrock
  model: claude-3-sonnet
```

---

## D) Provider Configuration

* [ ] Add configuration properties to select the active provider.

Example:

```
ai.provider=openai
```

or

```
ai.provider=bedrock
```

The system must initialize the correct `LLMClient` implementation based on configuration.

Spring dependency injection should resolve the correct provider at startup.

---

## E) Streaming Completion Support

* [ ] Extend the `LLMClient` interface to optionally support streaming responses.

Example capability:

```
streamCompletion(prompt)
```

Providers that support streaming may return tokens incrementally.

Providers that do not support streaming may fall back to synchronous completion.

---

## F) Streaming API Endpoint

* [ ] Introduce a streaming analysis endpoint.

Example:

```
GET /api/analysis/stream
```

The endpoint must:

* perform the same RAG workflow as `/analysis`
* stream completion output incrementally to the client
* use Server-Sent Events (SSE) for transport

Streaming may be demonstrated using:

```
curl -N http://localhost:8080/api/analysis/stream
```

---

## G) Cost Tracking Compatibility

* [ ] Ensure the existing cost tracking system remains functional for all providers.

Cost tracking must capture:

* model name
* input tokens
* output tokens
* estimated cost
* correlationId (when applicable)

Provider implementations must normalize usage metadata to support cost tracking.

---

# Non-Goals

This story does not include:

* embedding provider abstraction
* model fine-tuning
* provider failover or fallback logic
* distributed tracing
* UI integration for streaming

These may be introduced in future stories.

---

# Acceptance Criteria

* The system supports multiple LLM providers via a common abstraction.
* The existing OpenAI integration is refactored to use the new interface.
* Amazon Bedrock integration is implemented and configurable.
* The active provider can be selected through configuration.
* `/analysis` continues to function as before.
* `/analysis/stream` streams responses incrementally using SSE.
* Cost tracking works for both providers.
* Correlation ID propagation remains intact.
* Application builds successfully and all tests pass.

---

### Why this story is strong for your project

It upgrades your architecture from:

```
OpenAI hard-coded
```

to

```
LLM abstraction
   ├─ OpenAI
   └─ Bedrock
```

That’s **exactly the kind of design interviewers like to see**.

It also ties together the things you've already built:

* cost governance
* correlation tracking
* RAG pipeline

---

If you want, the next thing I can generate is the **`plan.md` for this story**, because the order of implementation matters quite a lot here (there’s a clean way to do it that avoids refactoring pain).
