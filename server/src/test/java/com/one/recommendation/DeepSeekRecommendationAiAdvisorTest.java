package com.one.recommendation;

import com.one.config.OneProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DeepSeekRecommendationAiAdvisorTest {
    @TempDir Path tempDirectory;

    @Test
    void shouldKeepOnlyKnownUniqueCandidates() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        DeepSeekRecommendationAiAdvisor advisor = new DeepSeekRecommendationAiAdvisor(
                RestClient.create(), properties(), objectMapper, new SimpleMeterRegistry());
        String content = objectMapper.writeValueAsString(Map.of(
                "openingLine", "今晚把选择缩成刚好的三个。",
                "items", List.of(
                        Map.of("itemId", 2, "reason", "下午反馈过偏甜，这次记得选少少甜。"),
                        Map.of("itemId", 999, "reason", "非法新增候选"),
                        Map.of("itemId", 2, "reason", "重复候选"))));
        String response = objectMapper.writeValueAsString(Map.of("choices", List.of(
                Map.of("message", Map.of("content", content)))));

        RecommendationAiAdvisor.Advice result = advisor.parse(response, List.of(
                new RecommendationAiAdvisor.Option(1, "牛肉面", null, "午间适合"),
                new RecommendationAiAdvisor.Option(2, "伯牙绝弦", "霸王茶姬", "建议少甜"))).orElseThrow();

        assertThat(result.openingLine()).isEqualTo("今晚把选择缩成刚好的三个。");
        assertThat(result.items()).containsExactly(
                new RecommendationAiAdvisor.RankedItem(2, "下午反馈过偏甜，这次记得选少少甜。"));
    }

    private OneProperties properties() {
        return new OneProperties("test-secret-with-at-least-32-characters", Duration.ofHours(1), true,
                "demo", "", "", "", new OneProperties.Qwen(false, "", "", "", "qwen3.6-flash", Duration.ofSeconds(30)),
                new OneProperties.DeepSeek(false, "", "https://api.deepseek.com", "deepseek-v4-flash", Duration.ofSeconds(6)),
                new OneProperties.Storage("local", tempDirectory, "https://one.test/api/media/public", 10_485_760,
                        new OneProperties.Oss("", "", "", "", "one/media")),
                new OneProperties.ContentSafety(false, true, "", "baselineCheck", "", ""));
    }
}
