package com.one.integration.ai;

import com.one.activity.ActivityMode;
import com.one.activity.ActivityType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RuleBasedActivityDraftAssistant implements ActivityDraftAssistant {

    private static final Pattern CAPACITY = Pattern.compile("(?:缺|要|共|限)([一二三四五六七八九十\\d]+)(?:个|位|人)");
    private static final Pattern DEPOSIT = Pattern.compile("(?:押金|鸽子金)[^\\d]{0,4}(\\d{1,3})");

    @Override
    public DraftSuggestion suggest(String naturalLanguage) {
        ActivityType type = detectType(naturalLanguage);
        ActivityMode mode = containsAny(naturalLanguage, "线上", "开黑", "游戏", "区服")
                ? ActivityMode.ONLINE : ActivityMode.OFFLINE;
        int capacity = detectCapacity(naturalLanguage, type);
        int depositFen = detectDeposit(naturalLanguage);
        List<String> tags = detectTags(naturalLanguage);
        Map<String, Object> attributes = detectAttributes(naturalLanguage, type);
        List<String> confirmations = new ArrayList<>(List.of("startAt", "endAt"));
        if (mode == ActivityMode.OFFLINE) {
            confirmations.addAll(List.of("cityCode", "district", "address"));
        }
        return new DraftSuggestion(type, mode, buildTitle(naturalLanguage, type), naturalLanguage.strip(),
                capacity, depositFen, tags, attributes, confirmations, "RULE_FALLBACK");
    }

    private ActivityType detectType(String text) {
        if (containsAny(text, "羽毛球", "打球")) return ActivityType.BADMINTON;
        if (containsAny(text, "桌游", "阿瓦隆", "璀璨宝石")) return ActivityType.BOARD_GAME;
        if (containsAny(text, "开黑", "游戏", "段位", "王者", "英雄联盟")) return ActivityType.GAMING;
        if (containsAny(text, "散步", "citywalk", "Citywalk", "逛街")) return ActivityType.CITY_WALK;
        if (containsAny(text, "咖啡", "聊天")) return ActivityType.COFFEE;
        return ActivityType.OTHER;
    }

    private int detectCapacity(String text, ActivityType type) {
        Matcher matcher = CAPACITY.matcher(text);
        if (matcher.find()) {
            return Math.max(2, Math.min(20, parseChineseNumber(matcher.group(1))));
        }
        return type == ActivityType.GAMING ? 5 : 6;
    }

    private int detectDeposit(String text) {
        Matcher matcher = DEPOSIT.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) * 100 : 0;
    }

    private List<String> detectTags(String text) {
        List<String> tags = new ArrayList<>();
        if (containsAny(text, "新手", "第一次")) tags.add("新手友好");
        if (containsAny(text, "不卷", "轻松", "娱乐")) tags.add("轻松局");
        if (containsAny(text, "开麦", "语音")) tags.add("可开麦");
        if (containsAny(text, "社恐", "人少", "小局")) tags.add("小范围");
        return tags.isEmpty() ? List.of("等待同频") : List.copyOf(tags);
    }

    private Map<String, Object> detectAttributes(String text, ActivityType type) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        if (type == ActivityType.GAMING) {
            attributes.put("mic", containsAny(text, "开麦", "语音") ? "可以开麦" : "需要确认");
            attributes.put("style", containsAny(text, "不卷", "娱乐") ? "轻松娱乐" : "需要确认");
        } else if (type == ActivityType.BADMINTON) {
            attributes.put("level", containsAny(text, "新手", "初级") ? "新手-初级" : "需要确认");
            attributes.put("intensity", containsAny(text, "不卷", "轻松") ? "轻松" : "需要确认");
        }
        return Map.copyOf(attributes);
    }

    private String buildTitle(String text, ActivityType type) {
        String clean = text.strip().replaceAll("[，。！？]+$", "");
        if (clean.length() <= 32) return clean;
        return switch (type) {
            case BADMINTON -> "一起打场不赶时间的羽毛球";
            case BOARD_GAME -> "来一场轻松的桌游小局";
            case GAMING -> "今晚开黑，等一个同频队友";
            case CITY_WALK -> "一起在城市里慢慢走";
            case COFFEE -> "找个舒服的地方喝杯咖啡";
            default -> clean.substring(0, 32);
        };
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    private int parseChineseNumber(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            int tenIndex = value.indexOf('十');
            if (tenIndex >= 0) {
                int tens = tenIndex == 0 ? 1 : chineseDigit(value.charAt(tenIndex - 1));
                int ones = tenIndex == value.length() - 1 ? 0 : chineseDigit(value.charAt(tenIndex + 1));
                return tens > 0 && ones >= 0 ? tens * 10 + ones : 6;
            }
            int digit = value.length() == 1 ? chineseDigit(value.charAt(0)) : -1;
            return digit > 0 ? digit : 6;
        }
    }

    private int chineseDigit(char value) {
        return "一二三四五六七八九".indexOf(value) + 1;
    }
}
