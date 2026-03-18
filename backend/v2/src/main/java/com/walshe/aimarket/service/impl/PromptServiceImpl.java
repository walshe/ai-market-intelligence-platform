package com.walshe.aimarket.service.impl;

import com.walshe.aimarket.config.PromptProperties;
import com.walshe.aimarket.service.PromptService;
import com.walshe.aimarket.service.dto.PromptDefinition;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link PromptService} for retrieving and rendering prompts.
 */
@Service
public class PromptServiceImpl implements PromptService {

    private static final Logger LOG = LoggerFactory.getLogger(PromptServiceImpl.class);

    private final PromptProperties promptProperties;

    public PromptServiceImpl(PromptProperties promptProperties) {
        this.promptProperties = promptProperties;
    }

    @Override
    public PromptDefinition getPrompt(String key) {
        if (promptProperties.getDefinitions() == null) {
             throw new IllegalArgumentException("Prompt definitions are not loaded. Check application configuration.");
        }
        if (!promptProperties.getDefinitions().containsKey(key)) {
            LOG.error("Available prompt keys: {}", promptProperties.getDefinitions().keySet());
            throw new IllegalArgumentException("Prompt definition not found for key: " + key);
        }
        return promptProperties.getDefinitions().get(key);
    }

    @Override
    public String renderPrompt(String key, Map<String, String> variables) {
        LOG.info("Rendering prompt with key: {}", key);
        PromptDefinition definition = getPrompt(key);
        String template = definition.userTemplate();

        String rendered = template;
        // Include systemPrompt as a possible variable if present in the definition
        if (definition.systemPrompt() != null) {
            rendered = rendered.replace("{systemPrompt}", definition.systemPrompt());
        }

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            rendered = rendered.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return rendered;
    }
}
