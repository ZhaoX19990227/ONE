package com.one.security;

import com.one.config.OneProperties;
import com.one.identity.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
public class SessionTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final byte[] secret;
    private final OneProperties properties;
    private final Clock clock;

    @Autowired
    public SessionTokenService(OneProperties properties) {
        this(properties, Clock.systemUTC());
    }

    SessionTokenService(OneProperties properties, Clock clock) {
        if (properties.security().tokenSecret() == null
                || properties.security().tokenSecret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("ONE_TOKEN_SECRET must contain at least 32 bytes");
        }
        this.secret = properties.security().tokenSecret().getBytes(StandardCharsets.UTF_8);
        this.properties = properties;
        this.clock = clock;
    }

    public String issue(long userId, UserRole role) {
        long expiresAt = Instant.now(clock).plus(properties.security().tokenTtl()).getEpochSecond();
        String payload = userId + ":" + role.name() + ":" + expiresAt;
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + sign(encodedPayload);
    }

    public Optional<OnePrincipal> verify(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 2 || !MessageDigest.isEqual(
                    sign(parts[0]).getBytes(StandardCharsets.US_ASCII),
                    parts[1].getBytes(StandardCharsets.US_ASCII))) {
                return Optional.empty();
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String[] claims = payload.split(":", -1);
            if (claims.length != 3 || Long.parseLong(claims[2]) <= Instant.now(clock).getEpochSecond()) {
                return Optional.empty();
            }
            return Optional.of(new OnePrincipal(Long.parseLong(claims[0]), UserRole.valueOf(claims[1])));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot sign session token", exception);
        }
    }
}
