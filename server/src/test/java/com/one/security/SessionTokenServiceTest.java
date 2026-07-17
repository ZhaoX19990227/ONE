package com.one.security;

import com.one.config.OneProperties;
import com.one.identity.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class SessionTokenServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-16T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void shouldIssueAndVerifyToken() {
        SessionTokenService service = service(Duration.ofHours(1));

        String token = service.issue(42L, UserRole.MEMBER);

        assertThat(service.verify(token)).contains(new OnePrincipal(42L, UserRole.MEMBER));
    }

    @Test
    void shouldRejectTamperedToken() {
        SessionTokenService service = service(Duration.ofHours(1));
        String token = service.issue(42L, UserRole.MEMBER);

        assertThat(service.verify(token + "x")).isEmpty();
    }

    private SessionTokenService service(Duration ttl) {
        OneProperties properties = new OneProperties(
                new OneProperties.Security("test-secret-with-at-least-32-characters", ttl),
                new OneProperties.Admin("admin", "password"),
                new OneProperties.Wechat("", "", true), true);
        return new SessionTokenService(properties, CLOCK);
    }
}
