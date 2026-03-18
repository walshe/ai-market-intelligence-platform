package com.walshe.aimarket.service.dto;

import java.io.Serializable;

/**
 * A DTO for a prompt definition.
 *
 * @param systemPrompt   The system instruction for the LLM.
 * @param userTemplate   The user prompt template with placeholders like {query} and {context}.
 */
public record PromptDefinition(String systemPrompt, String userTemplate) implements Serializable {
}
