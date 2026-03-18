package com.walshe.aimarket.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.walshe.aimarket.config.PromptProperties;
import com.walshe.aimarket.service.dto.PromptDefinition;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PromptServiceImplTest {

    private PromptProperties promptProperties;
    private PromptServiceImpl promptService;

    @BeforeEach
    void setUp() {
        promptProperties = new PromptProperties();
        Map<String, PromptDefinition> definitions = new HashMap<>();
        definitions.put("test.prompt", new PromptDefinition("System instruction", "Context: {context}, Query: {query}"));
        definitions.put("system.prompt", new PromptDefinition("Instruction: {systemPrompt}", "User: {query}"));
        promptProperties.setDefinitions(definitions);
        promptService = new PromptServiceImpl(promptProperties);
    }

    @Test
    void shouldRetrieveExistingPromptByKey() {
        PromptDefinition definition = promptService.getPrompt("test.prompt");
        assertThat(definition).isNotNull();
        assertThat(definition.systemPrompt()).isEqualTo("System instruction");
    }

    @Test
    void shouldThrowExceptionForMissingPromptKey() {
        assertThatThrownBy(() -> promptService.getPrompt("non.existent"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Prompt definition not found for key: non.existent");
    }

    @Test
    void shouldPerformVariableSubstitution() {
        Map<String, String> variables = Map.of(
            "context", "The context info",
            "query", "The user query"
        );

        String rendered = promptService.renderPrompt("test.prompt", variables);

        assertThat(rendered).isEqualTo("Context: The context info, Query: The user query");
    }

    @Test
    void shouldSubstituteSystemPromptIfPlaceholderPresent() {
        Map<String, PromptDefinition> definitions = new HashMap<>();
        definitions.put("system.test", new PromptDefinition("Base instruction", "Instruction: {systemPrompt}, User: {query}"));
        promptProperties.setDefinitions(definitions);

        Map<String, String> variables = Map.of(
            "query", "Hello"
        );

        String rendered = promptService.renderPrompt("system.test", variables);

        assertThat(rendered).isEqualTo("Instruction: Base instruction, User: Hello");
    }
}
