package com.walshe.aimarket.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JPA AttributeConverter to persist List<Double> into PostgreSQL pgvector column using its text literal representation.
 * Example persisted value: [0.1, 0.2, 0.3]
 */
@Converter(autoApply = false)
public class DoubleListToVectorStringConverter implements AttributeConverter<List<Double>, String> {

    @Override
    public String convertToDatabaseColumn(List<Double> attribute) {
        if (attribute == null) {
            return null;
        }
        // pgvector accepts text input in square brackets with comma-separated numbers
        return attribute.stream()
            .map(d -> {
                if (d == null) return "0"; // defensive: avoid nulls in vector
                // Use Java's toString to preserve precision; pgvector parses standard decimal notation
                return Double.toString(d);
            })
            .collect(Collectors.joining(", ", "[", "]"));
    }

    @Override
    public List<Double> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        String trimmed = dbData.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        if (trimmed.isBlank()) {
            return List.of();
        }
        String[] parts = trimmed.split(",");
        return java.util.Arrays.stream(parts)
            .map(String::trim)
            .map(Double::valueOf)
            .collect(Collectors.toList());
    }
}
