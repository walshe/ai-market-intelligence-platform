# AI Market Intelligence Platform

A modular Spring Boot backend demonstrating production-style integration of Large Language Models (LLMs) into a financial decision-support system using Retrieval-Augmented Generation (RAG), pgvector, cost governance, and JWT authentication.

---

## 🚀 Core Features

- **Retrieval-Augmented Generation (RAG)**: Orchestrates query embedding, similarity search in pgvector, and LLM-based analysis.
- **Cost Governance**: Automated tracking of token usage and estimated USD costs for both embedding and completion calls.
- **Semantic Search**: Powered by PostgreSQL and the `pgvector` extension for efficient cosine similarity search.
- **Secure by Default**: JWT-based authentication for all non-public endpoints.
- **Modular Architecture**: Clean separation of concerns between ingestion, retrieval, and analysis layers.

---

## 🏗️ Architecture Overview

```text
                ┌────────────────────────────┐
                │        REST Layer          │
                │  (JWT Authenticated APIs)  │
                └──────────────┬─────────────┘
                               │
                ┌──────────────┴─────────────┐
                │        Service Layer        │
                │                             │
                │  IngestionService           │
                │  ChunkingService            │
                │  EmbeddingService           │
                │  RetrievalService           │
                │  PromptBuilderService       │
                │  LlmClient (interface)      │
                │  OpenAiLlmClient            │
                │  CostTrackingService        │
                └──────────────┬─────────────┘
                               │
                ┌──────────────┴─────────────┐
                │      Persistence Layer      │
                │                             │
                │  document                   │
                │  document_chunk             │
                │  (pgvector cosine index)    │
                └──────────────┬─────────────┘
                               │
                ┌──────────────┴─────────────┐
                │     PostgreSQL + pgvector  │
                └────────────────────────────┘
```

For more details, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3 (JHipster base)
- **Database**: PostgreSQL 15 + pgvector
- **AI/LLM**: OpenAI API (Embeddings & Chat Completions)
- **Authentication**: Spring Security + JWT
- **Migration**: Liquibase
- **Environment**: Docker, mise

---

## ⚙️ Development Setup

### 1. Prerequisites

- [mise](https://mise.jfm.me/) (recommended for managing tool versions)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- An OpenAI API Key

### 2. Install Toolchain

```bash
mise install
```

### 3. Environment Variables

Create or update your `.env` file in the root directory:

```bash
OPENAI_API_KEY=your_openai_api_key_here
```

### 4. Start Infrastructure

Start the pgvector-enabled PostgreSQL database:

```bash
docker-compose up -d
```

### 5. Run the Application

Navigate to the backend directory and start the Spring Boot application:

```bash
cd backend/v2
./mvnw
```

The application will be available at `http://localhost:8080`.

---

## 📖 Usage Guide

### Authentication

1. **Get a Token**: Send a POST request to `/api/authenticate` with your credentials.
2. **Authorize**: Use the returned `id_token` in the `Authorization: Bearer <token>` header for subsequent requests.

### Document Ingestion

To perform RAG, you first need to ingest documents:

```bash
POST /api/documents
{
  "title": "Q4 Financial Report",
  "content": "Full text of the report..."
}
```

This triggers:
- Text chunking (sentence-aware).
- Embedding generation for each chunk.
- Persistence in `document_chunk` table with vector data.

### RAG Analysis

Run semantic queries against your ingested documents:

```bash
POST /api/v1/analysis
{
  "query": "How was the growth in Q4?",
  "topK": 5
}
```

### Cost Governance

View aggregated cost metrics for your AI operations:

```bash
GET /api/v1/metrics/cost
```

> **Note**: Cost tracking is currently implemented at the **platform level**. Usage is aggregated by model and call type, but not yet attributed to individual users. Individual user billing or quotas would require an enhancement to the `CostLog` schema and `CostTrackingService`.

### Interactive Documentation (Swagger UI)

Explore all API endpoints interactively:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🧪 Testing

Run unit and integration tests using Maven:

```bash
./mvnw test
```

Integration tests use [Testcontainers](https://www.testcontainers.org/) to spin up a real PostgreSQL + pgvector instance.

---

## 📜 Guidelines

This project follows strict architectural and coding guidelines. See [docs/AI_EXECUTION_PROTOCOL.md](docs/AI_EXECUTION_PROTOCOL.md) for details on:
- Constructor injection.
- Package-private visibility.
- Service layer separation.
- Cost tracking requirements.


## Example Usage

Ingest document:

    curl -X 'POST' \
      'http://localhost:8080/api/documents' \
      -H 'accept: */*' \
      -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTc3NjAwNjg2NCwiYXV0aCI6IlJPTEVfQURNSU4gUk9MRV9VU0VSIiwiaWF0IjoxNzczNDE0ODY0LCJ1c2VySWQiOjF9.Hz217Gz_9VLpK82Gnu5IxTT3eX7V_JjEVQY_wJdeOR4VQaEvT86tjFUxZbg-TkbZex6yCjFHF0OngAQ5iv1EpQ' \
      -H 'Content-Type: application/json' \
      -d "{\"title\": \"Wall Street's Newest Bitcoin Treasury Bull: B. Riley Launches Upside Coverage on Strategy and Strive\", \"content\": \"B. Riley has initiated Buy ratings on two Nasdaq-listed bitcoin accumulation companies: MicroStrategy (MSTR) with a \$175 price target (current \$138.95) and Strive (ASST) with a \$12 target (current \$8.51). With bitcoin trading near \$70,000, B. Riley sees compressed valuations as a buying opportunity rather than structural deterioration. MicroStrategy holds 738,731 bitcoins, making it the largest corporate bitcoin holder by a wide margin, and trades at 1.2x net asset value versus a 3.4x peak reached in 2024. The firm highlights MSTR's diversified digital credit platform spanning six securities and capital structure flexibility including perpetual preferred stock and convertible notes. For Strive, analyst Fedor Shabalin points to a dual-engine model combining a bitcoin treasury with an operating asset management business, minimal near-term convertible debt maturities offering payment certainty, and a current share price reflecting a valuation discount relative to underlying business strength.\", \"createdAt\": \"2026-03-10T14:02:00.000Z\"}"
  

Analysis query:

    curl -X 'POST' \
      'http://localhost:8080/api/v1/analysis' \
      -H 'accept: */*' \
      -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTc3NjAwNjczMSwiYXV0aCI6IlJPTEVfQURNSU4gUk9MRV9VU0VSIiwiaWF0IjoxNzczNDE0NzMxLCJ1c2VySWQiOjF9.H_f7v7CerEmuzILn5oLGGZxEqTfgb9izXW22Yt1opEnE911Nl3dtFG267xDwdojp7Cn8pHBIHmCqJl68-AkeSA' \
      -H 'Content-Type: application/json' \
      -d '{
      "query": "Whats the new on Strategy today re Bitcoin?",
      "topK": 2
    }'


response:

        {
          "summary": "B. Riley has initiated a Buy rating on MicroStrategy (MSTR) with a price target of $175, highlighting its large bitcoin holdings and diversified digital credit platform. MSTR currently trades at $138.95, with bitcoin priced near $70,000, prompting B. Riley to see this as a buying opportunity despite compressed valuations.",
          "riskFactors": [
            "Volatility in bitcoin prices",
            "Regulatory changes affecting cryptocurrency",
            "Market competition in asset management"
          ],
          "confidenceScore": 0.8,
          "modelUsed": "gpt-4o-mini-2024-07-18",
          "tokensUsed": 458
        }



Cost tracking:

    curl -X 'GET' \
      'http://localhost:8080/api/v1/metrics/cost' \
      -H 'accept: */*' \
      -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTc3NjAwNjczMSwiYXV0aCI6IlJPTEVfQURNSU4gUk9MRV9VU0VSIiwiaWF0IjoxNzczNDE0NzMxLCJ1c2VySWQiOjF9.H_f7v7CerEmuzILn5oLGGZxEqTfgb9izXW22Yt1opEnE911Nl3dtFG267xDwdojp7Cn8pHBIHmCqJl68-AkeSA'



    {
      "totalUsd": 0.00000418,
      "byModel": {
        "text-embedding-3-small": 0.00000418
      },
      "byCallType": {
        "EMBEDDING": 0.00000418
      }
    }


checking kafka topic for cost messages:

general
    
    ./kafka-console-consumer --bootstrap-server localhost:9092 --topic ai-cost-logs --from-beginning

app consumer

    ./kafka-console-consumer --bootstrap-server localhost:9092 --topic ai-cost-logs --from-beginning
