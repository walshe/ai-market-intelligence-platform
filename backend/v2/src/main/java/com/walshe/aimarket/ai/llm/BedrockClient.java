package com.walshe.aimarket.ai.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walshe.aimarket.config.LlmProperties;
import com.walshe.aimarket.service.CostTrackingService;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamResponseHandler;
import software.amazon.awssdk.services.bedrockruntime.model.ResponseStream;

/**
 * Amazon Bedrock implementation of {@link LLMCompletionClient}.
 */
public class BedrockClient implements LLMCompletionClient {

    private static final Logger LOG = LoggerFactory.getLogger(BedrockClient.class);
    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final BedrockRuntimeAsyncClient bedrockRuntimeAsyncClient;
    private final LlmProperties properties;
    private final CostTrackingService costTrackingService;
    private final ObjectMapper objectMapper;

    public BedrockClient(LlmProperties properties, CostTrackingService costTrackingService, ObjectMapper objectMapper) {
        this.properties = properties;
        this.costTrackingService = costTrackingService;
        this.objectMapper = objectMapper;

        var region = Region.of(properties.bedrock().region());
        var credentialsProvider = (properties.bedrock().accessKey() != null && !properties.bedrock().accessKey().isBlank() &&
            properties.bedrock().secretKey() != null && !properties.bedrock().secretKey().isBlank())
            ? StaticCredentialsProvider.create(AwsBasicCredentials.create(properties.bedrock().accessKey(), properties.bedrock().secretKey()))
            : DefaultCredentialsProvider.create();

        this.bedrockRuntimeClient = BedrockRuntimeClient.builder()
            .region(region)
            .credentialsProvider(credentialsProvider)
            .build();

        this.bedrockRuntimeAsyncClient = BedrockRuntimeAsyncClient.builder()
            .region(region)
            .credentialsProvider(credentialsProvider)
            .build();
    }

    @Override
    public CompletionResponse complete(String prompt, String correlationId) {
        long startTime = System.currentTimeMillis();
        String modelId = properties.bedrock().modelName();

        try {
            // Construct payload for Anthropic Claude 3 (Messages API format)
            ClaudeRequest request = new ClaudeRequest(
                "bedrock-2023-05-31",
                1024,
                new Message[]{ new Message("user", prompt) }
            );

            String body = objectMapper.writeValueAsString(request);

            InvokeModelRequest invokeRequest = InvokeModelRequest.builder()
                .modelId(modelId)
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromUtf8String(body))
                .build();

            InvokeModelResponse response = bedrockRuntimeClient.invokeModel(invokeRequest);
            long latencyMs = System.currentTimeMillis() - startTime;

            ClaudeResponse claudeResponse = objectMapper.readValue(
                response.body().asString(StandardCharsets.UTF_8),
                ClaudeResponse.class
            );

            String content = extractContent(claudeResponse);
            int inputTokens = claudeResponse.usage() != null ? claudeResponse.usage().input_tokens() : 0;
            int outputTokens = claudeResponse.usage() != null ? claudeResponse.usage().output_tokens() : 0;

            costTrackingService.logCompletionUsage(
                modelId,
                inputTokens,
                outputTokens,
                correlationId,
                "bedrock",
                latencyMs
            );

            return new CompletionResponse(
                content,
                inputTokens,
                outputTokens,
                modelId,
                "bedrock",
                latencyMs
            );

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to process JSON for Bedrock request/_response", e);
        } catch (Exception e) {
            LOG.error("Error calling Bedrock for model {}: {}", modelId, e.getMessage());
            throw new RuntimeException("Error calling Bedrock API", e);
        }
    }

    private String extractContent(ClaudeResponse response) {
        if (response.content() != null && response.content().length > 0) {
            return response.content()[0].text();
        }
        return "";
    }

    @Override
    public Flux<String> streamCompletion(String prompt, String correlationId) {
        String modelId = properties.bedrock().modelName();

        try {
            ClaudeRequest request = new ClaudeRequest(
                "bedrock-2023-05-31",
                1024,
                new Message[]{ new Message("user", prompt) }
            );

            String body = objectMapper.writeValueAsString(request);

            InvokeModelWithResponseStreamRequest invokeRequest = InvokeModelWithResponseStreamRequest.builder()
                .modelId(modelId)
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromUtf8String(body))
                .build();

            long startTime = System.currentTimeMillis();
            return Flux.create(sink -> {
                bedrockRuntimeAsyncClient.invokeModelWithResponseStream(invokeRequest,
                    InvokeModelWithResponseStreamResponseHandler.builder()
                        .onEventStream(publisher -> publisher.subscribe(new org.reactivestreams.Subscriber<>() {
                            @Override
                            public void onSubscribe(org.reactivestreams.Subscription s) {
                                s.request(Long.MAX_VALUE);
                            }

                            @Override
                            public void onNext(ResponseStream event) {
                                event.accept(InvokeModelWithResponseStreamResponseHandler.Visitor.builder()
                                    .onChunk(chunk -> {
                                        try {
                                            String chunkJson = chunk.bytes().asString(StandardCharsets.UTF_8);
                                            StreamResponse streamResponse = objectMapper.readValue(chunkJson, StreamResponse.class);
                                            if ("content_block_delta".equals(streamResponse.type()) && streamResponse.delta() != null) {
                                                sink.next(streamResponse.delta().text());
                                            } else if ("message_stop".equals(streamResponse.type()) && streamResponse.amazon_bedrock_invocation_metrics() != null) {
                                                long latencyMs = System.currentTimeMillis() - startTime;
                                                AmazonBedrockInvocationMetrics metrics = streamResponse.amazon_bedrock_invocation_metrics();
                                                costTrackingService.logCompletionUsage(
                                                    modelId,
                                                    metrics.inputTokenCount(),
                                                    metrics.outputTokenCount(),
                                                    correlationId,
                                                    "bedrock",
                                                    latencyMs
                                                );
                                            }
                                        } catch (Exception e) {
                                            LOG.error("Error parsing Bedrock stream chunk", e);
                                        }
                                    })
                                    .build());
                            }

                            @Override
                            public void onError(Throwable t) {
                                sink.error(t);
                            }

                            @Override
                            public void onComplete() {
                                sink.complete();
                            }
                        }))
                        .build()
                );
            });

        } catch (Exception e) {
            LOG.error("Error starting Bedrock stream for model {}: {}", modelId, e.getMessage());
            return Flux.error(e);
        }
    }

    // Claude 3 Request/Response DTOs
    private record ClaudeRequest(
        String anthropic_version,
        int max_tokens,
        Message[] messages
    ) {}

    private record Message(String role, String content) {}

    private record ClaudeResponse(
        String id,
        String type,
        String role,
        Content[] content,
        String model,
        String stop_reason,
        String stop_sequence,
        Usage usage
    ) {}

    private record Content(String type, String text) {}

    private record Usage(int input_tokens, int output_tokens) {}

    private record StreamResponse(String type, Delta delta, Usage usage, AmazonBedrockInvocationMetrics amazon_bedrock_invocation_metrics) {}

    private record AmazonBedrockInvocationMetrics(int inputTokenCount, int outputTokenCount) {}

    private record Delta(String type, String text) {}
}
