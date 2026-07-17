package com.one.config;

import com.one.activity.Activity;
import com.one.activity.ActivityMode;
import com.one.activity.ActivityRepository;
import com.one.activity.ActivityType;
import com.one.identity.UserAccount;
import com.one.identity.UserAccountRepository;
import com.one.identity.UserRole;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

@Component
@ConditionalOnProperty(prefix = "one", name = "demo-data-enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {

    private final UserAccountRepository userRepository;
    private final ActivityRepository activityRepository;

    public DemoDataInitializer(UserAccountRepository userRepository, ActivityRepository activityRepository) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (activityRepository.count() > 0) {
            return;
        }
        UserAccount host = userRepository.findByOpenId(mockOpenId("demo"))
                .orElseGet(() -> userRepository.save(UserAccount.create(mockOpenId("demo"), "林间来信")));
        host.updateProfile("林间来信", null, "310100", "上海", "认真生活，也认真玩。",
                "[\"羽毛球\",\"桌游\",\"Citywalk\"]");

        UserAccount admin = userRepository.findByOpenId(mockOpenId("admin"))
                .orElseGet(() -> userRepository.save(UserAccount.create(mockOpenId("admin"), "ONE 运营")));
        admin.grantRole(UserRole.ADMIN);

        Instant now = Instant.now();
        activityRepository.save(Activity.create(new Activity.ActivityDraft(
                host.getId(), ActivityType.BADMINTON, ActivityMode.OFFLINE,
                "周四晚｜不卷的羽毛球", "下班后来打一场轻松的双打。新手友好，不计分也不催球，结束后可以一起喝点东西。",
                null, "310100", "上海", "静安区", "静安体育中心 3 号场",
                new BigDecimal("31.2304160"), new BigDecimal("121.4737010"),
                now.plus(Duration.ofDays(2)), now.plus(Duration.ofDays(2)).plus(Duration.ofHours(2)),
                now.plus(Duration.ofDays(1)), 6, 3800, 2000, false,
                "[\"新手友好\",\"轻松局\",\"地铁可达\"]",
                "{\"level\":\"新手-初级\",\"equipment\":\"可借球拍\",\"intensity\":\"轻松\"}")));

        activityRepository.save(Activity.create(new Activity.ActivityDraft(
                host.getId(), ActivityType.BOARD_GAME, ActivityMode.OFFLINE,
                "周末微醺桌游夜", "六人小局，玩璀璨宝石和阿瓦隆。会讲规则，第一次来完全没关系。",
                null, "310100", "上海", "徐汇区", "衡山路附近，报名后可见门牌",
                null, null, now.plus(Duration.ofDays(4)), now.plus(Duration.ofDays(4)).plus(Duration.ofHours(4)),
                now.plus(Duration.ofDays(3)), 6, 5200, 0, true,
                "[\"有人教学\",\"社恐友好\",\"六人小局\"]",
                "{\"difficulty\":\"轻中度\",\"games\":[\"璀璨宝石\",\"阿瓦隆\"]}")));
    }

    private String mockOpenId(String code) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(code.getBytes(StandardCharsets.UTF_8));
            return "mock_" + HexFormat.of().formatHex(digest, 0, 12);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot create demo OpenID", exception);
        }
    }
}
