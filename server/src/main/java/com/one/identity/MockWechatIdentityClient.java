package com.one.identity;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
@ConditionalOnProperty(prefix = "one.wechat", name = "mock-enabled", havingValue = "true")
public class MockWechatIdentityClient implements WechatIdentityClient {

    @Override
    public WechatSession exchangeCode(String code) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(code.getBytes(StandardCharsets.UTF_8));
            return new WechatSession("mock_" + HexFormat.of().formatHex(digest, 0, 12), null);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
