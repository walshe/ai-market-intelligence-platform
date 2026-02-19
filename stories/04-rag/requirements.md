# Story 04: Retrieval & RAG

## Description
Develop the core Retrieval-Augmented Generation (RAG) capabilities, allowing users to query the platform for contextual financial analysis.

## Requirements
- [ ] Implement `RetrievalService` to fetch the top-k most relevant document chunks based on query similarity.
- [ ] Implement `PromptBuilderService` to construct the prompt using retrieved context and user query.
- [ ] Define the `LlmClient` interface and implement `OpenAiLlmClient`.
- [ ] Implement the `POST /api/v1/analysis` endpoint.
- [ ] Ensure the response follows the structured format defined in `PRD.md`.
- [ ] Verify the end-to-end RAG flow from query to structured response.

## Acceptance Criteria
- `POST /api/v1/analysis` returns a 200 OK with a valid JSON response.
- The response includes a summary, risk factors, confidence score, and token usage.
- Retrieval successfully finds chunks with high cosine similarity to the query.
- Integration tests cover the full RAG cycle.
