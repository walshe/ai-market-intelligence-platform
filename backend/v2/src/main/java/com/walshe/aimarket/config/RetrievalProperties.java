package com.walshe.aimarket.service;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Properties for retrieval defaults.
 */
@ConfigurationProperties(prefix = "application.retrieval")
@Validated
public class RetrievalProperties {

    @Min(1)
    private int defaultTopK = 5;

    public int getDefaultTopK() {
        return defaultTopK;
    }

    public void setDefaultTopK(int defaultTopK) {
        this.defaultTopK = defaultTopK;
    }
}
