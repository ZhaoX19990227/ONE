package com.one.identity;

public interface WechatIdentityClient {
    WechatSession exchangeCode(String code);

    record WechatSession(String openId, String unionId) {
    }
}
