# Story 05: Cost Governance

## Description
Implement mechanisms to track LLM usage costs and expose metrics for governance and observability.

## Requirements
- [ ] Create the `CostLog` entity to store token usage and estimated costs.
- [ ] Implement `CostTrackingService` to calculate and persist cost records for each LLM call.
- [ ] Capture input tokens, output tokens, model used, and estimated USD cost.
- [ ] Implement `GET /api/v1/metrics/cost` endpoint to expose cost data.
- [ ] Ensure all LLM calls are intercepted or wrapped to ensure tracking is consistent.

## Acceptance Criteria
- Every `/analysis` request results in a new `CostLog` entry.
- `GET /api/v1/metrics/cost` returns aggregated or detailed cost information.
- Costs are calculated correctly based on the model's pricing configuration.
- Service is resilient to tracking failures (tracking should not block the main response if possible, or should be transactional).
