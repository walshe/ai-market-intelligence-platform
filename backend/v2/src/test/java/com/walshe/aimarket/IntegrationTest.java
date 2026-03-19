package com.walshe.aimarket;

import com.walshe.aimarket.config.AsyncSyncConfiguration;
import com.walshe.aimarket.config.EmbeddedSQL;
import com.walshe.aimarket.config.JacksonConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { AiMarketIntelligenceApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class }, properties = {
    "application.prompts.definitions.[analysis.system.prompt].systemPrompt=You are a financial analysis assistant. Use only the provided context to answer. If the context is insufficient, say you don't know. Be concise and objective.",
    "application.prompts.definitions.[analysis.system.prompt].userTemplate=[SYSTEM]\\n{systemPrompt}\\n\\n[CONTEXT]\\n{context}\\n\\n[USER QUERY]\\n{query}\\n\\n[OUTPUT FORMAT]\\nReturn a strictly valid JSON object with the following fields: summary (string), riskFactors (array of strings), confidenceScore (number between 0 and 1), modelUsed (string), tokensUsed (integer). Do not include markdown fences or extra commentary.",
    "application.prompts.definitions.[analysis.streaming.prompt].systemPrompt=You are a financial analysis assistant. Use only the provided context to answer. If the context is insufficient, say you don't know. Be concise and objective.",
    "application.prompts.definitions.[analysis.streaming.prompt].userTemplate=[SYSTEM]\\n{systemPrompt}\\n\\n[CONTEXT]\\n{context}\\n\\n[USER QUERY]\\n{query}\\n\\n[INSTRUCTION]\\nProvide a concise financial analysis answering the query using the context. Return plain text only. Do not return JSON."
})
@EmbeddedSQL
public @interface IntegrationTest {
}
