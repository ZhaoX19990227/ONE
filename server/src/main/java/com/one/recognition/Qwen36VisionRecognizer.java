package com.one.recognition;

import com.one.common.BusinessException;
import com.one.common.Dimension;
import com.one.config.OneProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class Qwen36VisionRecognizer implements FoodVisionRecognizer {
    private final RestClient client;
    private final OneProperties.Qwen properties;
    private final ObjectMapper objectMapper;

    public Qwen36VisionRecognizer(@Qualifier("qwenRestClient") RestClient client,
                                  OneProperties oneProperties, ObjectMapper objectMapper) {
        this.client = client;
        this.properties = oneProperties.qwen();
        this.objectMapper = objectMapper;
    }

    @Override
    public VisionResult recognize(Dimension dimension, String contentType, byte[] image,
                                  List<String> knownCategories, List<String> knownBrands) {
        validateConfiguration();
        String imageUrl = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(image);
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", properties.model());
        request.put("temperature", 0.1);
        request.put("response_format", Map.of("type", "json_object"));
        request.put("messages", List.of(Map.of("role", "user", "content", List.of(
                Map.of("type", "image_url", "image_url", Map.of("url", imageUrl)),
                Map.of("type", "text", "text", prompt(dimension, knownCategories, knownBrands))))));
        try {
            String response = client.post().uri(endpoint())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON).body(request).retrieve().body(String.class);
            return parse(response);
        } catch (BusinessException error) {
            throw error;
        } catch (Exception error) {
            throw new BusinessException("QWEN_UNAVAILABLE", "照片识别暂时走神了，请手动选择或稍后再试", HttpStatus.BAD_GATEWAY);
        }
    }

    private VisionResult parse(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        String content = root.path("choices").path(0).path("message").path("content").stringValue();
        if (content == null || content.isBlank()) throw new IllegalStateException("Empty Qwen response");
        JsonNode result = objectMapper.readTree(stripCodeFence(content));
        JsonNode candidatesNode = result.path("candidates");
        List<VisionCandidate> candidates = objectMapper.readerForListOf(VisionCandidate.class)
                .readValue(candidatesNode);
        double confidence = result.path("confidence").asDouble(
                candidates.stream().mapToDouble(VisionCandidate::confidence).max().orElse(0));
        return new VisionResult(candidates.stream().limit(3).toList(), clamp(confidence));
    }

    private String prompt(Dimension dimension, List<String> categories, List<String> brands) {
        String type = switch (dimension) {
            case MEAL -> "正餐、小吃或餐饮外卖";
            case MILK_TEA -> "奶茶、果茶或茶饮";
            case COFFEE -> "咖啡饮品";
            default -> throw new IllegalArgumentException("Unsupported recognition dimension");
        };
        return """
                你是 ONE 小程序的吃喝照片识别器。识别图片中的%s，只陈述图片能支持的信息。
                已有品类：%s
                已有品牌：%s
                品牌不确定时返回 null，不得猜测；优先使用已有目录中的准确中文名称。
                返回严格 JSON，不要 Markdown：
                {"confidence":0.0,"candidates":[{"categoryName":"品类或null","brandName":"品牌或null","itemName":"实际食物/饮品名称","confidence":0.0,"estimatedAmountFen":null,"evidence":"短证据"}]}
                candidates 最多 3 个，confidence 范围 0 到 1。金额无法从图片确定，通常返回 null。
                """.formatted(type, String.join("、", categories), String.join("、", brands));
    }

    private String endpoint() {
        String base = properties.baseUrl();
        if (base == null || base.isBlank()) {
            base = "https://" + properties.workspaceId() + ".cn-beijing.maas.aliyuncs.com/compatible-mode/v1";
        }
        base = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        return base.endsWith("/chat/completions") ? base : base + "/chat/completions";
    }

    private void validateConfiguration() {
        if (!properties.enabled() || properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new BusinessException("VISION_NOT_CONFIGURED", "照片识别尚未配置，请先手动记录", HttpStatus.SERVICE_UNAVAILABLE);
        }
        if ((properties.baseUrl() == null || properties.baseUrl().isBlank())
                && (properties.workspaceId() == null || properties.workspaceId().isBlank())) {
            throw new BusinessException("VISION_NOT_CONFIGURED", "照片识别工作空间尚未配置", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private String stripCodeFence(String value) {
        String stripped = value.strip();
        if (!stripped.startsWith("```")) return stripped;
        int firstLine = stripped.indexOf('\n');
        int lastFence = stripped.lastIndexOf("```");
        return firstLine >= 0 && lastFence > firstLine ? stripped.substring(firstLine + 1, lastFence).strip() : stripped;
    }

    private double clamp(double value) { return Math.max(0, Math.min(1, value)); }
}
