# AI Market Intelligence Platform

A modular Spring Boot backend demonstrating production-style integration of Large Language Models (LLMs) into a financial decision-support system using Retrieval-Augmented Generation (RAG), pgvector, cost governance, and JWT authentication.

---

## Architecture Overview

TODO: Insert architecture diagram (ASCII or image)

---

## Tech Stack

- Spring Boot (JHipster)
- PostgreSQL + pgvector
- OpenAI API
- Docker
- JWT Authentication
- Liquibase

---

## Development Setup

This project uses `mise` to manage tool versions.

The project uses a pgvector-enabled PostgreSQL image to support semantic vector search.

### Install toolchain

```bash
mise install



Start PostgreSQL
docker-compose up -d

Run backend
cd backend/java
./mvnw


Swagger UI available at:

http://localhost:8080/swagger-ui/index.html