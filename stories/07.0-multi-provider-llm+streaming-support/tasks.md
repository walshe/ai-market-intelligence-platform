# Tasks — Story 07: Multi-Provider LLM Integration, Streaming & Provider Benchmarking

---

# Design Rules (Critical Before Implementation)

To avoid the most common architecture mistakes in Spring AI projects, enforce the following separations:

### 1. Separate Completion vs Embeddings

Do **not** mix embeddings and completions in the same interface.

Correct design:

```
LLMClient        → text generation
EmbeddingClient  → embedding generation
```

This prevents painful refactors later when using:

* OpenAI embeddings
* Bedrock completions
* local embedding models

---

### 2. Normalize Usage Metrics Across Providers

Every provider must produce the same normalized metadata:

```
provider
model
inputTokens
outputTokens
latencyMs
```

This enables **provider benchmarking** and **cost comparison**, which will make the project look much more production-grade.

---

# Phase 1 — Introduce LLM Completion Abstraction

## Goal

Decouple the application from a specific completion provider.

## Tasks

* [ ] Create package:

```
ai.llm
```

* [ ] Create interface:

```
LLMClient
```

Responsibilities:

* synchronous completion
* streaming completion
* usage metadata reporting

Methods:

```
CompletionResponse complete(String prompt, String correlationId)
Flux<String> streamCompletion(String prompt, String correlationId)
```

---

* [ ] Create DTO:

```
CompletionResponse
```

Fields:

```
String generatedText
Integer inputTokens
Integer outputTokens
String modelName
String provider
Long latencyMs
```

Latency will later support provider benchmarking.

---

* [ ] Ensure DTO is **provider-neutral**.

* [ ] Build project and confirm compilation.

---

# Phase 2 — Introduce Embedding Abstraction

## Goal

Prevent future refactoring by separating embedding logic.

## Tasks

* [ ] Create package:

```
ai.embedding
```

* [ ] Create interface:

```
EmbeddingClient
```

Method:

```
List<Float> generateEmbedding(String text)
```

---

* [ ] Create implementation:

```
OpenAIEmbeddingClient implements EmbeddingClient
```

* [ ] Move current embedding generation logic into this class.

* [ ] Update services to use `EmbeddingClient`:

Affected areas:

* `IngestionService`
* query embedding generation in `AnalysisService`

---

* [ ] Verify ingestion still works:

Expected behavior:

* document chunks created
* embeddings stored in pgvector
* ingestion remains idempotent

---

# Phase 3 — Refactor OpenAI Completion Integration

## Goal

Move OpenAI completion behind the abstraction.

## Tasks

* [ ] Create class:

```
OpenAIClient implements LLMClient
```

* [ ] Move existing OpenAI completion code into this class.

* [ ] Measure request latency.

Example:

```
startTime = System.currentTimeMillis()
call API
latency = endTime - startTime
```

* [ ] Populate `CompletionResponse`:

```
generatedText
inputTokens
outputTokens
modelName
provider="openai"
latencyMs
```

---

* [ ] Implement streaming completion.

If Spring AI streaming unavailable:

fallback:

```
Flux.just(fullResponse)
```

---

* [ ] Update `AnalysisService` to depend on:

```
LLMClient
```

instead of OpenAI-specific classes.

---

* [ ] Verify `/analysis` endpoint works unchanged.

---

# Phase 4 — Provider Configuration

## Goal

Allow switching providers via configuration.

## Tasks

* [ ] Add configuration properties:

```
ai.provider=openai
ai.model=gpt-4o-mini
```

* [ ] Create configuration class:

```
LLMProviderConfiguration
```

Responsibilities:

* read provider config
* register correct `LLMClient` bean

---

* [ ] Verify OpenAI loads when:

```
ai.provider=openai
```

* [ ] Start application and confirm `/analysis` works.

---

# Phase 5 — Implement Amazon Bedrock Provider

## Goal

Add Bedrock support.

## Tasks

* [ ] Add dependency:

```
software.amazon.awssdk:bedrockruntime
```

---

* [ ] Create class:

```
BedrockClient implements LLMClient
```

Responsibilities:

* invoke Bedrock Runtime API
* normalize response
* extract usage metadata

---

* [ ] Support at least one model:

Recommended:

```
anthropic.claude-3-sonnet
```

---

* [ ] Populate `CompletionResponse`:

```
generatedText
inputTokens
outputTokens
modelName
provider="bedrock"
latencyMs
```

---

* [ ] Integrate with `CostTrackingService`.

---

* [ ] Add configuration support:

```
ai.provider=bedrock
ai.model=anthropic.claude-3-sonnet
```

---

* [ ] Verify provider switching works.

Run:

```
/analysis
```

with both providers.

---

# Phase 6 — Streaming Completion Support

## Goal

Allow incremental responses.

## Tasks

* [ ] Confirm `LLMClient` streaming method exists.

* [ ] Implement OpenAI streaming support.

* [ ] Implement Bedrock streaming if supported.

If not supported:

```
Flux.just(fullResponse)
```

fallback.

---

* [ ] Ensure correlation ID propagates through streaming calls.

---

# Phase 7 — Streaming Analysis Endpoint

## Goal

Expose streaming RAG responses.

## Tasks

* [ ] Add endpoint in `AnalysisResource`:

```
GET /api/analysis/stream
```

---

* [ ] Configure response type:

```
text/event-stream
```

---

* [ ] Implement Server-Sent Events.

Options:

```
Flux<ServerSentEvent<String>>
```

or

```
SseEmitter
```

---

* [ ] Reuse RAG pipeline:

1. generate query embedding
2. vector similarity search
3. construct prompt
4. stream completion tokens

---

* [ ] Emit tokens as SSE events:

```
data: token
data: token
data: token
```

---

* [ ] Verify streaming:

```
curl -N http://localhost:8080/api/analysis/stream
```

---

# Phase 8 — Cost Tracking Compatibility

## Goal

Ensure cost governance continues to work.

## Tasks

* [ ] Ensure `CompletionResponse` contains usage metadata.

* [ ] Update `CostTrackingService` to accept provider field.

* [ ] Verify cost logs created for:

* OpenAI completions

* Bedrock completions

---

* [ ] Ensure correlation ID propagates.

---

* [ ] For streaming:

record cost **after completion finishes**.

---

# Phase 9 — Provider Benchmarking (Small Production-Grade Enhancement)

## Goal

Allow comparing provider performance.

## Tasks

* [ ] Add fields to `CostLog`:

```
provider
model
latencyMs
```

---

* [ ] Ensure every completion records latency.

---

* [ ] Extend metrics endpoint:

```
GET /api/v1/metrics/cost
```

Add optional aggregation fields:

Example outputs:

```
cost by provider
average latency
tokens per request
```

---

This enables answering interview questions like:

> "Which model is fastest for your workload?"

---

# Phase 10 — Validation and Testing

## Functional Testing

* [ ] Run `/analysis` with OpenAI.
* [ ] Switch provider to Bedrock.
* [ ] Run `/analysis` again.

---

## Streaming Testing

Test using:

```
curl -N http://localhost:8080/api/analysis/stream
```

Confirm tokens arrive incrementally.

---

## Observability Testing

* [ ] Confirm correlation IDs propagate.
* [ ] Confirm `CostLog` entries created.

---

## System Validation

* [ ] Application boots successfully.
* [ ] Existing ingestion pipeline still works.
* [ ] Vector search unaffected.
* [ ] All tests pass.

---

# Final Verification Checklist

* [ ] Completion abstraction implemented
* [ ] Embedding abstraction implemented
* [ ] OpenAI provider refactored
* [ ] Bedrock provider implemented
* [ ] Providers selectable via configuration
* [ ] Streaming completions supported
* [ ] `/analysis/stream` endpoint working
* [ ] Cost tracking supports all providers
* [ ] Provider benchmarking metrics available
* [ ] Correlation IDs propagate correctly
* [ ] Application builds successfully

---

This final version does **three things that make the project look significantly more senior**:

1. **Multi-provider AI architecture**
2. **Streaming responses**
3. **Provider benchmarking (latency + cost)**
