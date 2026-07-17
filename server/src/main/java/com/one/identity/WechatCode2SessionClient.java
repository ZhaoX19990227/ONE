package com.one.identity;

import com.one.common.BusinessException;
import com.one.config.OneProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WechatCode2SessionClient {
    private final RestClient client = RestClient.create();
    private final OneProperties properties;

    public WechatCode2SessionClient(OneProperties properties) { this.properties = properties; }

    public String resolveOpenId(String code) {
        if (blank(properties.wechatAppId()) || blank(properties.wechatAppSecret())) {
            throw new BusinessException("WECHAT_NOT_CONFIGURED", "微信登录尚未配置", HttpStatus.SERVICE_UNAVAILABLE);
        }
        try {
            Code2SessionResponse response = client.get().uri(builder -> builder
                    .scheme("https").host("api.weixin.qq.com").path("/sns/jscode2session")
                    .queryParam("appid", properties.wechatAppId())
                    .queryParam("secret", properties.wechatAppSecret())
                    .queryParam("js_code", code)
                    .queryParam("grant_type", "authorization_code").build()).retrieve()
                    .body(Code2SessionResponse.class);
            if (response == null || blank(response.openid()) || response.errcode() != null && response.errcode() != 0) {
                throw new BusinessException("WECHAT_LOGIN_FAILED", "微信登录凭证已失效，请重试", HttpStatus.UNAUTHORIZED);
            }
            return response.openid();
        } catch (BusinessException error) {
            throw error;
        } catch (Exception error) {
            throw new BusinessException("WECHAT_UNAVAILABLE", "微信登录暂时不可用，请稍后再试", HttpStatus.BAD_GATEWAY);
        }
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
    private record Code2SessionResponse(String openid, String session_key, String unionid,
                                        Integer errcode, String errmsg) {}
}
