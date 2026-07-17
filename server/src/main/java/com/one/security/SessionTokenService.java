package com.one.security;

import com.one.config.OneProperties;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class SessionTokenService {
    private final byte[] secret;
    private final long ttlSeconds;
    private final ObjectMapper objectMapper;

    public SessionTokenService(OneProperties properties, ObjectMapper objectMapper) {
        this.secret = properties.tokenSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) throw new IllegalStateException("ONE_TOKEN_SECRET must contain at least 32 bytes");
        this.ttlSeconds = properties.tokenTtl().toSeconds();
        this.objectMapper = objectMapper;
    }

    public String issue(long userId) {
        try {
            String payload = encode(objectMapper.writeValueAsBytes(
                    new TokenPayload(userId, Instant.now().plusSeconds(ttlSeconds).getEpochSecond())));
            return payload + "." + encode(sign(payload));
        } catch (Exception error) {
            throw new IllegalStateException("Unable to issue token", error);
        }
    }

    public OnePrincipal verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2 || !java.security.MessageDigest.isEqual(sign(parts[0]), decode(parts[1]))) return null;
            TokenPayload payload = objectMapper.readValue(decode(parts[0]), TokenPayload.class);
            return payload.expiresAt() >= Instant.now().getEpochSecond() ? new OnePrincipal(payload.userId()) : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private byte[] sign(String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
    }

    private String encode(byte[] value) { return Base64.getUrlEncoder().withoutPadding().encodeToString(value); }
    private byte[] decode(String value) { return Base64.getUrlDecoder().decode(value); }
    private record TokenPayload(long userId, long expiresAt) {}
}
