package com.one.integration.safety;

public interface ContentSafetyGateway {
    SafetyResult check(String content);

    record SafetyResult(boolean safe, String reasonCode) {
        public static SafetyResult allowed() {
            return new SafetyResult(true, null);
        }
    }
}
