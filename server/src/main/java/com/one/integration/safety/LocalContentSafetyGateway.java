package com.one.integration.safety;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocalContentSafetyGateway implements ContentSafetyGateway {

    private static final List<String> BLOCKED_MARKERS = List.of(
            "裸聊", "赌博", "代充", "返利", "私下转账", "毒品"
    );

    @Override
    public SafetyResult check(String content) {
        String normalized = content == null ? "" : content.replaceAll("\\s+", "");
        return BLOCKED_MARKERS.stream().anyMatch(normalized::contains)
                ? new SafetyResult(false, "LOCAL_HIGH_RISK_MARKER")
                : SafetyResult.allowed();
    }
}
