package com.walshe.aimarket.service.impl;

import com.walshe.aimarket.service.ChunkingService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic, sentence-aware chunking implementation.
 * - Splits on sentence punctuation [. ! ? ; :]
 * - Enforces a maximum chunk length (characters)
 * - Preserves original order
 */
@Service
class SimpleChunkingService implements ChunkingService {

    // Keep default package-private visibility for Spring component per guidelines
    static final int DEFAULT_MAX_CHUNK_LEN = 800; // conservative, can be tuned

    private final int maxChunkLength;

    SimpleChunkingService() {
        this(DEFAULT_MAX_CHUNK_LEN);
    }

    SimpleChunkingService(int maxChunkLength) {
        this.maxChunkLength = Math.max(1, maxChunkLength);
    }

    @Override
    public List<String> chunk(String content) {
        List<String> result = new ArrayList<>();
        if (content == null) {
            return result;
        }
        String normalized = content.replaceAll("\r\n?", "\n"); // normalize newlines
        // Split by sentence boundaries while keeping delimiters
        List<String> sentences = splitIntoSentences(normalized);

        StringBuilder current = new StringBuilder();
        for (String sentence : sentences) {
            if (sentence.isBlank()) continue;
            if (sentence.length() > maxChunkLength) {
                // hard-wrap long sentence deterministically
                flushBuffer(result, current);
                splitLongSentence(sentence, result);
                continue;
            }
            if (current.length() == 0) {
                current.append(sentence.trim());
            } else if (current.length() + 1 + sentence.length() <= maxChunkLength) {
                current.append(' ').append(sentence.trim());
            } else {
                flushBuffer(result, current);
                current.append(sentence.trim());
            }
        }
        flushBuffer(result, current);
        return result;
    }

    private static void flushBuffer(List<String> result, StringBuilder current) {
        if (current.length() > 0) {
            result.add(current.toString());
            current.setLength(0);
        }
    }

    private void splitLongSentence(String sentence, List<String> out) {
        String s = sentence.trim();
        int start = 0;
        while (start < s.length()) {
            int end = Math.min(s.length(), start + maxChunkLength);
            // avoid cutting inside a multi-space region if possible
            int cut = end;
            if (end < s.length()) {
                int lastSpace = s.lastIndexOf(' ', end - 1);
                if (lastSpace >= start + Math.max(1, maxChunkLength / 2)) {
                    cut = lastSpace;
                }
            }
            out.add(s.substring(start, cut));
            start = cut;
            while (start < s.length() && s.charAt(start) == ' ') start++; // skip spaces
        }
    }

    private static List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            sb.append(c);
            if (isSentenceBoundary(c)) {
                // include following closing quotes/brackets deterministically
                int j = i + 1;
                while (j < text.length() && isClosingPunctuation(text.charAt(j))) {
                    sb.append(text.charAt(j));
                    i = j;
                    j++;
                }
                sentences.add(trimSoft(sb.toString()));
                sb.setLength(0);
            }
        }
        if (sb.length() > 0) {
            sentences.add(trimSoft(sb.toString()));
        }
        return sentences;
    }

    private static boolean isSentenceBoundary(char c) {
        return c == '.' || c == '!' || c == '?' || c == ';' || c == ':';
    }

    private static boolean isClosingPunctuation(char c) {
        return c == '"' || c == '\'' || c == ')' || c == ']' || c == '}';
    }

    private static String trimSoft(String s) {
        // collapse internal runs of whitespace deterministically
        return s.trim().replaceAll("\s+", " ");
    }
}
