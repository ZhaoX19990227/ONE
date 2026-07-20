package com.one.recommendation;

import com.one.common.Dimension;
import com.one.config.OneProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
class DeepSeekRecommendationAiAdvisor implements RecommendationAiAdvisor {
    private static final int MAX_OPENING_LENGTH = 40;
    private static final int MAX_REASON_LENGTH = 60;

    private final RestClient client;
    private final OneProperties.DeepSeek properties;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    DeepSeekRecommendationAiAdvisor(@Qualifier("deepSeekRestClient") RestClient client,
                                    OneProperties oneProperties, ObjectMapper objectMapper,
                                    MeterRegistry meterRegistry) {
        this.client = client;
        this.properties = oneProperties.deepseek();
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Optional<Advice> advise(Dimension dimension, TimeSlot timeSlot, List<Option> options) {
        if (!properties.enabled() || isBlank(properties.apiKey()) || options.isEmpty()) return Optional.empty();
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", properties.model());
            body.put("temperature", 0.3);
            body.put("max_tokens", 380);
            body.put("thinking", Map.of("type", "disabled"));
            body.put("response_format", Map.of("type", "json_object"));
            body.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt()),
                    Map.of("role", "user", "content", objectMapper.writeValueAsString(Map.of(
                            "dimension", dimension.name(), "timeSlot", timeSlot.name(), "options", options)))));
            String response = client.post().uri(endpoint())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(String.class);
            Optional<Advice> advice = parse(response, options);
            meterRegistry.counter("one_recommendation_ai_total", "result",
                    advice.isPresent() ? "success" : "invalid").increment();
            return advice;
        } catch (Exception ignored) {
            meterRegistry.counter("one_recommendation_ai_total", "result", "fallback").increment();
            return Optional.empty();
        }
    }

    Optional<Advice> parse(String response, List<Option> options) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        String content = root.path("choices").path(0).path("message").path("content").stringValue();
        if (isBlank(content)) return Optional.empty();
        JsonNode result = objectMapper.readTree(stripCodeFence(content));
        String openingLine = clean(result.path("openingLine").stringValue(), MAX_OPENING_LENGTH);
        Map<Long, Option> allowed = options.stream().collect(java.util.stream.Collectors.toMap(Option::itemId, value -> value));
        Set<Long> seen = new HashSet<>();
        List<RankedItem> ranked = new ArrayList<>();
        for (JsonNode item : result.path("items")) {
            long itemId = item.path("itemId").asLong(-1);
            if (!allowed.containsKey(itemId) || !seen.add(itemId)) continue;
            String reason = clean(item.path("reason").stringValue(), MAX_REASON_LENGTH);
            ranked.add(new RankedItem(itemId, isBlank(reason) ? allowed.get(itemId).localReason() : reason));
        }
        if (isBlank(openingLine) || ranked.isEmpty()) return Optional.empty();
        return Optional.of(new Advice(openingLine, ranked));
    }

    private String systemPrompt() {
        return """
                你是 ONE 的轻量吃喝推荐编辑。只根据给出的候选、时间段和匿名口味理由排序，不能新增候选，不能猜测用户身份或健康信息。
                语气克制、有趣、不说教。openingLine 最多 40 个中文字符；每条 reason 最多 60 个中文字符，并保留有价值的历史口味提示。
                返回严格 JSON：{"openingLine":"一句动态回应","items":[{"itemId":数字,"reason":"短理由"}]}。
                """;
    }

    private String endpoint() {
        String base = isBlank(properties.baseUrl()) ? "https://api.deepseek.com" : properties.baseUrl().strip();
        base = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        return base.endsWith("/chat/completions") ? base : base + "/chat/completions";
    }

    private String clean(String value, int maxLength) {
        if (value == null) return null;
        String cleaned = value.strip().replaceAll("[\\r\\n]+", " ");
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength);
    }

    private String stripCodeFence(String value) {
        String stripped = value.strip();
        if (!stripped.startsWith("```")) return stripped;
        int firstLine = stripped.indexOf('\n');
        int lastFence = stripped.lastIndexOf("```");
        return firstLine >= 0 && lastFence > firstLine ? stripped.substring(firstLine + 1, lastFence).strip() : stripped;
    }

    private boolean isBlank(String value) { return value == null || value.isBlank(); }
}
