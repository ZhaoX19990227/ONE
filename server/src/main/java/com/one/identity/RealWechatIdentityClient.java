package com.one.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.one.common.BusinessException;
import com.one.config.OneProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(prefix = "one.wechat", name = "mock-enabled", havingValue = "false")
public class RealWechatIdentityClient implements WechatIdentityClient {

    private final RestClient restClient;
    private final OneProperties properties;

    public RealWechatIdentityClient(RestClient.Builder builder, OneProperties properties) {
        this.restClient = builder.baseUrl("https://api.weixin.qq.com").build();
        this.properties = properties;
    }

    @Override
    public WechatSession exchangeCode(String code) {
        OneProperties.Wechat wechat = properties.wechat();
        if (!StringUtils.hasText(wechat.appId()) || !StringUtils.hasText(wechat.appSecret())) {
            throw new IllegalStateException("Production WeChat credentials are not configured");
        }

        Code2SessionResponse response = restClient.get()
                .uri(uri -> uri.path("/sns/jscode2session")
                        .queryParam("appid", wechat.appId())
                        .queryParam("secret", wechat.appSecret())
                        .queryParam("js_code", code)
                        .queryParam("grant_type", "authorization_code")
                        .build())
                .retrieve()
                .body(Code2SessionResponse.class);

        if (response == null || !StringUtils.hasText(response.openId()) || response.errorCode() != null) {
            throw new BusinessException("WECHAT_LOGIN_FAILED", "微信登录凭证无效", HttpStatus.UNAUTHORIZED);
        }
        return new WechatSession(response.openId(), response.unionId());
    }

    private record Code2SessionResponse(
            @JsonProperty("openid") String openId,
            @JsonProperty("unionid") String unionId,
            @JsonProperty("errcode") Integer errorCode,
            @JsonProperty("errmsg") String errorMessage
    ) {
    }
}
