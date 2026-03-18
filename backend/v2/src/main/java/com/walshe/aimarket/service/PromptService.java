package com.walshe.aimarket.service;

import com.walshe.aimarket.service.dto.PromptDefinition;
import java.util.Map;

/**
 * Service for managing and retrieving prompts.
 */
public interface PromptService {

    /**
     * Retrieves a prompt definition by its key.
     *
     * @param key the prompt key
     * @return the prompt definition
     */
    PromptDefinition getPrompt(String key);

    /**
     * Renders a prompt by substituting placeholders in the user template.
     *
     * @param key       the prompt key
     * @param variables the variables for substitution
     * @return the rendered prompt string
     */
    String renderPrompt(String key, Map<String, String> variables);
}
