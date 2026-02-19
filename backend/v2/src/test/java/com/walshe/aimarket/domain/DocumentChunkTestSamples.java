package com.walshe.aimarket.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DocumentChunkTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static DocumentChunk getDocumentChunkSample1() {
        return new DocumentChunk().id(1L).chunkIndex(1).embeddingModel("embeddingModel1");
    }

    public static DocumentChunk getDocumentChunkSample2() {
        return new DocumentChunk().id(2L).chunkIndex(2).embeddingModel("embeddingModel2");
    }

    public static DocumentChunk getDocumentChunkRandomSampleGenerator() {
        return new DocumentChunk()
            .id(longCount.incrementAndGet())
            .chunkIndex(intCount.incrementAndGet())
            .embeddingModel(UUID.randomUUID().toString());
    }
}
