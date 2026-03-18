package com.walshe.aimarket.config;

import com.walshe.aimarket.service.dto.PromptDefinition;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for prompts.
 */
@ConfigurationProperties(prefix = "application.prompts")
public class PromptProperties {

    private Map<String, PromptDefinition> definitions;

    public Map<String, PromptDefinition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(Map<String, PromptDefinition> definitions) {
        this.definitions = definitions;
    }
}
