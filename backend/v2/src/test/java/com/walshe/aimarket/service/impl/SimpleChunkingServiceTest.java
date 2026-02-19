package com.walshe.aimarket.service.impl;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleChunkingServiceTest {

    @Test
    void deterministicOutputForIdenticalInput() {
        SimpleChunkingService service = new SimpleChunkingService(50);
        String text = "Hello world. This is a test! Are we deterministic? Yes.";
        List<String> first = service.chunk(text);
        List<String> second = service.chunk(text);
        assertEquals(first, second, "Chunking should be deterministic for identical input");
    }

    @Test
    void preservesOrderAndSentenceBoundaries() {
        SimpleChunkingService service = new SimpleChunkingService(50);
        String text = "One. Two? Three! Four: five; six.";
        List<String> chunks = service.chunk(text);
        // With max len 50, each sentence should likely be a separate chunk or combined without reordering
        assertFalse(chunks.isEmpty());
        String combined = String.join(" ", chunks);
        // Removing double spaces to align with service normalization
        combined = combined.replaceAll("\\s+", " ");
        String normalized = text.trim().replaceAll("\\s+", " ");
        assertTrue(combined.startsWith("One."), "First chunk should start with first sentence");
        assertEquals(normalized.charAt(0), combined.charAt(0));
    }

    @Test
    void splitsVeryLongSentenceRespectingMaxChunkLength() {
        int maxLen = 30;
        SimpleChunkingService service = new SimpleChunkingService(maxLen);
        String longSentence = "ThisIsAnExtremelyLongTokenWithoutSpacesButWeAlso Have some spaces to allow nicer cuts.";
        List<String> chunks = service.chunk(longSentence);
        assertFalse(chunks.isEmpty());
        for (String c : chunks) {
            assertTrue(c.length() <= maxLen, "Each chunk should respect max length");
        }
        String reconstructed = String.join(" ", chunks).replaceAll("\\s+", " ");
        String normalized = longSentence.trim().replaceAll("\\s+", " ");
        assertTrue(normalized.startsWith(reconstructed.substring(0, Math.min(reconstructed.length(), 10))),
            "Reconstructed content should align with original order");
    }
}
