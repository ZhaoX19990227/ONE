package com.one.media;

import com.aliyun.green20220302.Client;
import com.aliyun.green20220302.models.ImageModerationRequest;
import com.aliyun.teaopenapi.models.Config;
import com.one.config.OneProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "one.content-safety.enabled", havingValue = "true")
public class AliyunContentSafetyGateway implements ContentSafetyGateway {
    private final Client client; private final String service; private final ObjectMapper objectMapper;
    public AliyunContentSafetyGateway(OneProperties properties, ObjectMapper objectMapper) throws Exception {
        OneProperties.ContentSafety value = properties.contentSafety();
        require(value.accessKeyId(), "ALIBABA_CLOUD_ACCESS_KEY_ID");
        require(value.accessKeySecret(), "ALIBABA_CLOUD_ACCESS_KEY_SECRET");
        require(value.endpoint(), "CONTENT_SAFETY_ENDPOINT"); require(value.service(), "CONTENT_SAFETY_SERVICE");
        Config config = new Config().setAccessKeyId(value.accessKeyId()).setAccessKeySecret(value.accessKeySecret()).setEndpoint(value.endpoint());
        this.client = new Client(config); this.service = value.service(); this.objectMapper = objectMapper;
    }
    @Override
    public Review review(String imageUrl) throws Exception {
        String parameters = objectMapper.writeValueAsString(Map.of("imageUrl", imageUrl, "dataId", java.util.UUID.randomUUID().toString()));
        var response = client.imageModeration(new ImageModerationRequest().setService(service).setServiceParameters(parameters));
        if (response.getBody() == null || response.getBody().getCode() == null || response.getBody().getCode() != 200) {
            throw new IllegalStateException("Aliyun content safety returned a non-success response");
        }
        var data = response.getBody().getData();
        if (data == null || data.getResult() == null) return new Review(true, "nonLabel");
        String risky = data.getResult().stream().map(value -> value.getLabel()).filter(value -> value != null)
                .filter(value -> !"nonLabel".equalsIgnoreCase(value) && !"normal".equalsIgnoreCase(value)).findFirst().orElse(null);
        return new Review(risky == null, risky == null ? "nonLabel" : risky);
    }
    private static void require(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalStateException(name + " is required when content safety is enabled");
    }
}
