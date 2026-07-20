package com.one.recommendation;

import com.one.catalog.CatalogBrand;
import com.one.catalog.CatalogItem;
import com.one.catalog.CatalogItemRepository;
import com.one.common.BusinessException;
import com.one.common.Dimension;
import com.one.memory.MemorySignal;
import com.one.memory.PreferenceMemory;
import com.one.memory.PreferenceMemoryRepository;
import com.one.identity.UserAccount;
import com.one.identity.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RecommendationService {
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private final DecisionSessionRepository sessions;
    private final DecisionCandidateRepository candidates;
    private final CatalogItemRepository items;
    private final PreferenceMemoryRepository memories;
    private final UserAccountRepository users;
    private final RecommendationFeedbackRepository feedback;
    private final ObjectMapper objectMapper;
    private final RecommendationAiAdvisor aiAdvisor;

    public RecommendationService(DecisionSessionRepository sessions, DecisionCandidateRepository candidates,
                                 CatalogItemRepository items, PreferenceMemoryRepository memories,
                                 UserAccountRepository users,
                                 RecommendationFeedbackRepository feedback,
                                 ObjectMapper objectMapper, RecommendationAiAdvisor aiAdvisor) {
        this.sessions = sessions; this.candidates = candidates; this.items = items;
        this.memories = memories; this.users = users; this.feedback = feedback; this.objectMapper = objectMapper;
        this.aiAdvisor = aiAdvisor;
    }

    @Transactional
    public RecommendationDtos.View recommend(long userId, RecommendationDtos.Request request) throws Exception {
        if (!request.dimension().isRecommendable()) {
            throw new BusinessException("DIMENSION_NOT_RECOMMENDABLE", "这个维度只记录，不做推荐", HttpStatus.BAD_REQUEST);
        }
        ZonedDateTime now = ZonedDateTime.now(SHANGHAI);
        TimeSlot timeSlot = TimeSlot.at(now);
        List<CatalogItem> catalog = items.findByDimensionAndActiveTrue(request.dimension());
        Set<Long> dismissedItems = feedback.findTop100ByUserIdAndDimensionAndExpiresAtAfterOrderByCreatedAtDesc(
                        userId, request.dimension(), now.toInstant()).stream()
                .map(RecommendationFeedback::getItemId).collect(java.util.stream.Collectors.toSet());
        List<CatalogItem> fresh = catalog.stream().filter(item -> !dismissedItems.contains(item.getId())).toList();
        if (!fresh.isEmpty()) catalog = fresh;
        List<CatalogItem> affordable = catalog.stream().filter(item -> request.budgetMaxFen() == null
                || item.getDefaultPriceFen() == null || item.getDefaultPriceFen() <= request.budgetMaxFen()).toList();
        if (!affordable.isEmpty()) catalog = affordable;
        if (catalog.isEmpty()) throw new BusinessException("NO_RECOMMENDATION", "这个分类还没有可推荐内容", HttpStatus.NOT_FOUND);

        UserAccount user = users.findById(userId).orElse(null);
        boolean memoryEnabled = user != null && user.isAiEnabled();
        Set<String> preferenceTags = preferenceTags(user, request.dimension());
        List<PreferenceMemory> memoryList = memoryEnabled
                ? memories.findTop100ByUserIdAndDimensionAndActiveTrueOrderBySourceAtDesc(userId, request.dimension())
                : List.of();
        DecisionSession session = sessions.save(DecisionSession.presented(userId, request.dimension(), request.mode(),
                timeSlot, request.budgetMaxFen(), "{}"));
        int limit = request.mode() == DecisionMode.SPIN ? Math.min(8, catalog.size()) : Math.min(3, catalog.size());
        List<ScoredItem> selected = catalog.stream().map(item -> score(item, memoryList, preferenceTags, timeSlot))
                .sorted(Comparator.comparingInt(ScoredItem::score).reversed()).limit(limit).toList();
        String openingLine = openingLine(request.mode(), timeSlot);
        if (request.mode() == DecisionMode.SMART && memoryEnabled) {
            RecommendationAiAdvisor.Advice advice = aiAdvisor.advise(request.dimension(), timeSlot,
                    selected.stream().map(value -> new RecommendationAiAdvisor.Option(value.item().getId(),
                            value.item().getName(), value.item().getBrand() == null ? null : value.item().getBrand().getName(),
                            value.reason())).toList()).orElse(null);
            if (advice != null) {
                selected = applyAdvice(selected, advice);
                openingLine = advice.openingLine();
            }
        }
        session.updateContextJson(objectMapper.writeValueAsString(Map.of("openingLine", openingLine)));
        List<DecisionCandidate> saved = new ArrayList<>();
        for (int index = 0; index < selected.size(); index++) {
            ScoredItem selectedItem = selected.get(index);
            String suggestionJson = selectedItem.memory() == null || selectedItem.memory().getSuggestedValue() == null
                    ? null : objectMapper.writeValueAsString(Map.of(
                    "attribute", selectedItem.memory().getAttributeKey(),
                    "suggestedValue", selectedItem.memory().getSuggestedValue(),
                    "sourceText", selectedItem.memory().getDisplayText()));
            saved.add(DecisionCandidate.of(session, selectedItem.item(), selectedItem.score(),
                    selectedItem.reason(), suggestionJson, selectedItem.memory(), index + 1));
        }
        List<DecisionCandidate> persisted = candidates.saveAll(saved);
        if (request.mode() == DecisionMode.SPIN) session.presentWinner(weightedWinner(persisted).getId());
        return view(session, persisted);
    }

    @Transactional(readOnly = true)
    public RecommendationDtos.View get(long userId, long sessionId) {
        DecisionSession session = owned(userId, sessionId);
        return view(session, candidates.findBySessionIdOrderByPositionNoAsc(sessionId));
    }

    @Transactional
    public RecommendationDtos.View choose(long userId, long sessionId, long candidateId) {
        DecisionSession session = owned(userId, sessionId);
        candidates.findByIdAndSessionId(candidateId, sessionId)
                .orElseThrow(() -> new BusinessException("CANDIDATE_NOT_FOUND", "这个选项不在本轮推荐里", HttpStatus.NOT_FOUND));
        session.choose(candidateId);
        sessions.save(session);
        return view(session, candidates.findBySessionIdOrderByPositionNoAsc(sessionId));
    }

    @Transactional
    public RecommendationDtos.View refresh(long userId, long sessionId) throws Exception {
        DecisionSession previous = owned(userId, sessionId);
        return recommend(userId, new RecommendationDtos.Request(previous.getDimension(), previous.getMode(),
                previous.getBudgetMaxFen()));
    }

    @Transactional
    public RecommendationDtos.View dismiss(long userId, long sessionId, long candidateId,
                                            RecommendationDtos.DismissRequest request) throws Exception {
        DecisionSession session = owned(userId, sessionId);
        DecisionCandidate candidate = candidates.findByIdAndSessionId(candidateId, sessionId)
                .orElseThrow(() -> new BusinessException("CANDIDATE_NOT_FOUND", "这个选项不在本轮推荐里", HttpStatus.NOT_FOUND));
        feedback.save(RecommendationFeedback.dismissed(userId, session.getDimension(), candidate.getItem().getId(),
                sessionId, request == null ? null : request.reason()));
        return recommend(userId, new RecommendationDtos.Request(session.getDimension(), session.getMode(),
                session.getBudgetMaxFen()));
    }

    @Transactional(readOnly = true)
    public void validateChosenItem(long userId, long sessionId, Dimension dimension, Long itemId) {
        DecisionSession session = owned(userId, sessionId);
        if (session.getDimension() != dimension || session.getChosenCandidateId() == null) {
            throw new BusinessException("DECISION_MISMATCH", "推荐选择与本次记录不一致", HttpStatus.CONFLICT);
        }
        DecisionCandidate chosen = candidates.findByIdAndSessionId(session.getChosenCandidateId(), sessionId)
                .orElseThrow(() -> new BusinessException("CANDIDATE_NOT_FOUND", "推荐选项不存在", HttpStatus.NOT_FOUND));
        if (itemId == null || !chosen.getItem().getId().equals(itemId)) {
            throw new BusinessException("DECISION_ITEM_MISMATCH", "记录内容不是刚刚确认的推荐", HttpStatus.CONFLICT);
        }
    }

    @Transactional
    public void markRecorded(long userId, long sessionId, long recordId) {
        DecisionSession session = owned(userId, sessionId);
        session.record(recordId);
        sessions.save(session);
    }

    private ScoredItem score(CatalogItem item, List<PreferenceMemory> memoryList,
                             Set<String> preferenceTags, TimeSlot timeSlot) {
        PreferenceMemory memory = memoryList.stream()
                .filter(value -> value.getItemId() != null && value.getItemId().equals(item.getId()))
                .findFirst().orElseGet(() -> {
                    CatalogBrand brand = item.getBrand();
                    return brand == null ? null : memoryList.stream()
                            .filter(value -> value.getBrandId() != null && value.getBrandId().equals(brand.getId()))
                            .findFirst().orElse(null);
                });
        int score = item.getBaseWeight() + ThreadLocalRandom.current().nextInt(0, 31);
        if (item.getAttributes() != null && item.getAttributes().contains(timeSlot.name())) score += 35;
        String reason = timeReason(timeSlot);
        String matchedTag = preferenceTags.stream()
                .filter(tag -> matchesPreference(item, tag))
                .findFirst().orElse(null);
        if (matchedTag != null) {
            score += 22;
            reason = "你偏爱「" + matchedTag + "」，这次把它往前放了一点。";
        }
        if (memory != null) {
            if (memory.getSignal() == MemorySignal.REPURCHASE) {
                score += memory.getStrength();
                reason = memory.getDisplayText() + "，所以今天又把它放到你面前。";
            } else if (memory.getSignal() == MemorySignal.DISLIKE) {
                score -= memory.getStrength();
                reason = "记得" + memory.getDisplayText() + "，这次只低权重保留。";
            } else {
                score += 15;
                reason = memory.getDisplayText() + suggestionSuffix(memory);
            }
        }
        return new ScoredItem(item, score, reason, memory);
    }

    private boolean matchesPreference(CatalogItem item, String tag) {
        if (tag == null || tag.isBlank()) return false;
        return item.getAttributes() != null && item.getAttributes().contains("\"" + tag + "\"")
                || item.getName().contains(tag)
                || item.getCategory().getName().contains(tag)
                || item.getBrand() != null && item.getBrand().getName().contains(tag);
    }

    private Set<String> preferenceTags(UserAccount user, Dimension dimension) {
        if (user == null) return Set.of();
        String json = dimension == Dimension.MEAL ? user.getMealPreferences() : user.getDrinkPreferences();
        if (json == null || json.isBlank()) return Set.of();
        try {
            PreferenceTags value = objectMapper.readValue(json, PreferenceTags.class);
            return value.tags() == null ? Set.of() : new LinkedHashSet<>(value.tags());
        } catch (Exception ignored) {
            return Set.of();
        }
    }

    private String suggestionSuffix(PreferenceMemory memory) {
        return memory.getSuggestedValue() == null ? "，这次会替你留意。" : "，这次建议调成更合适的甜度。";
    }

    private String timeReason(TimeSlot slot) {
        return switch (slot) {
            case BREAKFAST -> "早上先吃得舒服一点。";
            case LUNCH -> "现在适合来一份不费脑子的满足。";
            case AFTERNOON -> "下午的这口，适合慢一点喝。";
            case DINNER -> "今晚把选择交给 ONE。";
            case LATE_NIGHT -> "夜深了，选个此刻刚好的。";
        };
    }

    private DecisionSession owned(long userId, long sessionId) {
        return sessions.findById(sessionId).filter(value -> value.getUserId() == userId)
                .orElseThrow(() -> new BusinessException("DECISION_NOT_FOUND", "这轮推荐不存在", HttpStatus.NOT_FOUND));
    }

    private DecisionCandidate weightedWinner(List<DecisionCandidate> values) {
        int total = values.stream().mapToInt(value -> Math.max(1, value.getScore())).sum();
        int cursor = ThreadLocalRandom.current().nextInt(total);
        for (DecisionCandidate value : values) {
            cursor -= Math.max(1, value.getScore());
            if (cursor < 0) return value;
        }
        return values.get(values.size() - 1);
    }

    private RecommendationDtos.View view(DecisionSession session, List<DecisionCandidate> values) {
        List<RecommendationDtos.Candidate> views = values.stream().map(value -> {
            CatalogBrand brand = value.getBrand();
            CatalogItem item = value.getItem();
            return new RecommendationDtos.Candidate(value.getId(), value.getPositionNo(), value.getCategory().getId(),
                    value.getCategory().getName(), brand == null ? null : brand.getId(),
                    brand == null ? null : brand.getName(), brand == null ? null : brand.getShortName(),
                    brand == null ? null : brand.getLogoUrl(), brand == null ? null : brand.getBrandColor(),
                    item.getId(), item.getName(), item.getImageUrl(), item.getDefaultPriceFen(),
                    value.getReasonText(), value.getSuggestionJson(), value.getId().equals(session.getChosenCandidateId()));
        }).toList();
        return new RecommendationDtos.View(session.getId(), session.getDimension(), session.getMode(),
                session.getTimeSlot(), session.getStatus(), session.getWinnerCandidateId(), session.getChosenCandidateId(),
                openingLine(session), views);
    }

    private List<ScoredItem> applyAdvice(List<ScoredItem> selected, RecommendationAiAdvisor.Advice advice) {
        Map<Long, ScoredItem> byItemId = selected.stream().collect(java.util.stream.Collectors.toMap(
                value -> value.item().getId(), value -> value));
        List<ScoredItem> result = new ArrayList<>();
        for (RecommendationAiAdvisor.RankedItem ranked : advice.items()) {
            ScoredItem value = byItemId.remove(ranked.itemId());
            if (value != null) result.add(new ScoredItem(value.item(), value.score(), ranked.reason(), value.memory()));
        }
        selected.stream().filter(value -> byItemId.containsKey(value.item().getId())).forEach(result::add);
        return result;
    }

    private String openingLine(DecisionSession session) {
        try {
            String value = objectMapper.readTree(session.getContextJson()).path("openingLine").stringValue();
            if (value != null && !value.isBlank()) return value;
        } catch (Exception ignored) {
            // 兼容历史会话和异常上下文，回退到确定性文案。
        }
        return openingLine(session.getMode(), session.getTimeSlot());
    }

    private String openingLine(DecisionMode mode, TimeSlot slot) {
        if (mode == DecisionMode.SPIN) return "别纠结，让圆盘替你偏心一次。";
        return switch (slot) {
            case BREAKFAST -> "早安，先照顾今天的第一口。";
            case LUNCH -> "午间到站，按你的记忆挑了三样。";
            case AFTERNOON -> "下午适合给自己一点轻松。";
            case DINNER -> "今晚想吃什么，ONE 已经有点懂你了。";
            case LATE_NIGHT -> "夜宵可以有，但让这一口更合适。";
        };
    }

    private record ScoredItem(CatalogItem item, int score, String reason, PreferenceMemory memory) {}
    private record PreferenceTags(List<String> tags) {}
}
