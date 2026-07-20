package com.one.record;

import com.one.catalog.CatalogBrand;
import com.one.catalog.CatalogBrandRepository;
import com.one.catalog.CatalogCategory;
import com.one.catalog.CatalogCategoryRepository;
import com.one.catalog.CatalogCustomEntry;
import com.one.catalog.CatalogCustomEntryRepository;
import com.one.catalog.CatalogItem;
import com.one.catalog.CatalogItemRepository;
import com.one.common.BusinessException;
import com.one.common.Dimension;
import com.one.memory.MemoryService;
import com.one.memory.PreferenceMemory;
import com.one.recommendation.RecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class RecordService {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private final LifeRecordRepository records;
    private final MealRecordDetailRepository meals;
    private final DrinkRecordDetailRepository drinks;
    private final PrivateHabitRecordRepository habits;
    private final CatalogCategoryRepository categories;
    private final CatalogBrandRepository brands;
    private final CatalogItemRepository items;
    private final CatalogCustomEntryRepository customEntries;
    private final MemoryService memoryService;
    private final RecommendationService recommendationService;
    private final ObjectMapper objectMapper;

    public RecordService(LifeRecordRepository records, MealRecordDetailRepository meals,
                         DrinkRecordDetailRepository drinks, PrivateHabitRecordRepository habits,
                         CatalogCategoryRepository categories, CatalogBrandRepository brands,
                         CatalogItemRepository items, CatalogCustomEntryRepository customEntries,
                         MemoryService memoryService, RecommendationService recommendationService,
                         ObjectMapper objectMapper) {
        this.records = records; this.meals = meals; this.drinks = drinks; this.habits = habits;
        this.categories = categories; this.brands = brands; this.items = items;
        this.customEntries = customEntries; this.memoryService = memoryService;
        this.recommendationService = recommendationService; this.objectMapper = objectMapper;
    }

    @Transactional
    public RecordDtos.RecordView createMeal(long userId, RecordDtos.MealRequest request) throws Exception {
        validateDecision(userId, request.decisionSessionId(), Dimension.MEAL, request.itemId(), request.source());
        Selection selection = resolve(Dimension.MEAL, request.categoryId(), request.brandId(), request.itemId(),
                request.customBrandName(), request.customItemName(), userId);
        LifeRecord record = records.save(LifeRecord.confirmed(userId, Dimension.MEAL, request.occurredAt(),
                request.decisionSessionId(), selection.category(), selection.brand(), selection.item(),
                selection.customBrandName(), selection.title(), request.thumbnailUrl(), money(request.money()), request.rating(), request.note(),
                request.source() == null ? RecordSource.MANUAL : request.source()));
        MealRecordDetail detail = meals.save(MealRecordDetail.create(record.getId(), request.diningMode(),
                request.hungerLevel(), request.tasteFeedback(), request.satiety(), request.repurchaseIntent()));
        markDecisionRecorded(userId, request.decisionSessionId(), record.getId());
        List<PreferenceMemory> memories = memoryService.rememberMeal(record, detail);
        return view(record, detail.getTasteFeedback(), memories, null, null);
    }

    @Transactional
    public RecordDtos.RecordView createDrink(long userId, RecordDtos.DrinkRequest request) throws Exception {
        if (request.dimension() != Dimension.MILK_TEA && request.dimension() != Dimension.COFFEE) {
            throw new BusinessException("INVALID_DRINK_DIMENSION", "饮品只能选择奶茶或咖啡", HttpStatus.BAD_REQUEST);
        }
        validateDecision(userId, request.decisionSessionId(), request.dimension(), request.itemId(), request.source());
        Selection selection = resolve(request.dimension(), request.categoryId(), request.brandId(), request.itemId(),
                request.customBrandName(), request.customItemName(), userId);
        LifeRecord record = records.save(LifeRecord.confirmed(userId, request.dimension(), request.occurredAt(),
                request.decisionSessionId(), selection.category(), selection.brand(), selection.item(),
                selection.customBrandName(), selection.title(), request.thumbnailUrl(), money(request.money()), request.rating(), request.note(),
                request.source() == null ? RecordSource.MANUAL : request.source()));
        DrinkRecordDetail detail = drinks.save(DrinkRecordDetail.create(record.getId(), request.sugarLevel(),
                request.iceLevel(), request.cupSize(), objectMapper.writeValueAsString(request.toppings()),
                request.tasteFeedback(), request.repurchaseIntent()));
        markDecisionRecorded(userId, request.decisionSessionId(), record.getId());
        List<PreferenceMemory> memories = memoryService.rememberDrink(record, detail);
        return view(record, detail.getTasteFeedback(), memories, null, null);
    }

    @Transactional
    public RecordDtos.RecordView createDeer(long userId, RecordDtos.DeerRequest request) {
        LocalDate date = request.occurredAt().atZone(ZONE).toLocalDate();
        Instant from = date.atStartOfDay(ZONE).toInstant();
        Instant to = date.plusDays(1).atStartOfDay(ZONE).toInstant();
        int ordinal = Math.toIntExact(records.countByUserIdAndRecordTypeAndRecordStatusAndOccurredAtBetween(
                userId, Dimension.PRIVATE_HABIT, RecordStatus.CONFIRMED, from, to) + 1);
        LifeRecord record = records.save(LifeRecord.confirmed(userId, Dimension.PRIVATE_HABIT,
                request.occurredAt(), null, null, null, null, null, "鹿一下", null, null, null,
                null, RecordSource.MANUAL));
        PrivateHabitRecord detail = habits.save(PrivateHabitRecord.create(record.getId(), ordinal,
                request.bodyFeeling() == null ? BodyFeeling.NOT_RECORDED : request.bodyFeeling()));
        return view(record, null, List.of(), ordinal, deerMessage(ordinal, detail.getBodyFeeling()));
    }

    @Transactional(readOnly = true)
    public List<RecordDtos.RecordView> recordsForDay(long userId, LocalDate date) {
        Instant from = date.atStartOfDay(ZONE).toInstant();
        Instant to = date.plusDays(1).atStartOfDay(ZONE).toInstant();
        return records.findByUserIdAndRecordStatusAndOccurredAtBetweenOrderByOccurredAtAsc(
                        userId, RecordStatus.CONFIRMED, from, to).stream()
                .map(this::detailedView).toList();
    }

    @Transactional
    public void delete(long userId, long recordId) {
        LifeRecord record = records.findByIdAndUserIdAndRecordStatus(recordId, userId, RecordStatus.CONFIRMED)
                .orElseThrow(() -> new BusinessException("RECORD_NOT_FOUND", "这条记录已经不存在", HttpStatus.NOT_FOUND));
        record.delete();
        memoryService.forgetByRecord(userId, recordId);
    }

    private RecordDtos.RecordView detailedView(LifeRecord record) {
        return switch (record.getRecordType()) {
            case MEAL -> view(record, meals.findById(record.getId()).map(MealRecordDetail::getTasteFeedback).orElse(null),
                    List.of(), null, null);
            case MILK_TEA, COFFEE -> view(record,
                    drinks.findById(record.getId()).map(DrinkRecordDetail::getTasteFeedback).orElse(null),
                    List.of(), null, null);
            case PRIVATE_HABIT -> habits.findById(record.getId())
                    .map(detail -> view(record, null, List.of(), detail.getOrdinalOfDay(),
                            deerMessage(detail.getOrdinalOfDay(), detail.getBodyFeeling())))
                    .orElseGet(() -> view(record, null, List.of(), null, null));
        };
    }

    private Selection resolve(Dimension dimension, Long categoryId, Long brandId, Long itemId,
                              String customBrand, String customItem, long userId) {
        if (itemId != null) {
            CatalogItem item = items.findById(itemId).filter(value -> value.getDimension() == dimension && value.isActive())
                    .orElseThrow(() -> new BusinessException("CATALOG_ITEM_NOT_FOUND", "没有找到这个产品", HttpStatus.NOT_FOUND));
            return new Selection(item.getCategory(), item.getBrand(), item, null, item.getName());
        }
        if (customItem == null || customItem.isBlank()) {
            throw new BusinessException("ITEM_REQUIRED", "请选择识别结果或输入实际吃喝内容", HttpStatus.BAD_REQUEST);
        }
        CatalogCategory category = categoryId == null ? null : categories.findById(categoryId)
                .filter(value -> value.getDimension() == dimension && value.isActive()).orElseThrow(() ->
                        new BusinessException("CATEGORY_NOT_FOUND", "品类不存在", HttpStatus.NOT_FOUND));
        CatalogBrand brand = brandId == null ? null : brands.findById(brandId)
                .filter(value -> value.getDimension() == dimension && value.isActive()).orElseThrow(() ->
                        new BusinessException("BRAND_NOT_FOUND", "品牌不存在", HttpStatus.NOT_FOUND));
        String itemName = customItem.strip();
        String brandName = clean(customBrand);
        CatalogCustomEntry normalized = customEntries
                .findFirstByDimensionAndBrandNameAndItemNameAndStatusAndNormalizedItemIdNotNullOrderByUpdatedAtDesc(
                        dimension, brandName, itemName, "NORMALIZED").orElse(null);
        if (normalized != null) {
            CatalogItem item = items.findById(normalized.getNormalizedItemId())
                    .filter(value -> value.getDimension() == dimension && value.isActive()).orElse(null);
            if (item != null) return new Selection(item.getCategory(), item.getBrand(), item, null, item.getName());
        }
        customEntries.save(CatalogCustomEntry.pending(userId, dimension, categoryId,
                brandName, itemName));
        return new Selection(category, brand, null, brand == null ? brandName : null, itemName);
    }

    private LifeRecord.MoneyInput money(RecordDtos.MoneyRequest money) {
        return money == null ? null : new LifeRecord.MoneyInput(
                money.originalAmountFen(), money.discountAmountFen(), money.actualAmountFen());
    }

    private void validateDecision(long userId, Long sessionId, Dimension dimension, Long itemId, RecordSource source) {
        if (sessionId == null) {
            if (source == RecordSource.RECOMMENDATION) {
                throw new BusinessException("DECISION_REQUIRED", "推荐记录缺少本轮推荐信息", HttpStatus.BAD_REQUEST);
            }
            return;
        }
        recommendationService.validateChosenItem(userId, sessionId, dimension, itemId);
    }

    private void markDecisionRecorded(long userId, Long sessionId, long recordId) {
        if (sessionId != null) recommendationService.markRecorded(userId, sessionId, recordId);
    }

    private RecordDtos.RecordView view(LifeRecord record, TasteFeedback feedback,
                                       List<PreferenceMemory> memories, Integer ordinal, String deerMessage) {
        CatalogBrand brand = record.getBrand();
        CatalogCategory category = record.getCategory();
        CatalogItem item = record.getItem();
        return new RecordDtos.RecordView(record.getId(), record.getRecordType(), record.getOccurredAt(),
                record.getTitle(), category == null ? null : category.getId(), brand == null ? null : brand.getId(),
                record.getBrandNameSnapshot(), brand == null ? null : brand.getShortName(),
                brand == null ? null : brand.getBrandColor(), brand == null ? null : brand.getLogoUrl(),
                item == null ? null : item.getId(), record.getThumbnailUrl(), record.getActualAmountFen(),
                record.getRating(), record.getSource(), record.getNote(), feedback == null ? null : feedback.name(),
                memories.stream().map(PreferenceMemory::getDisplayText).toList(), ordinal, deerMessage);
    }

    private String deerMessage(int ordinal, BodyFeeling feeling) {
        if (feeling == BodyFeeling.UNCOMFORTABLE) return "今天先让森林安静一下，照顾好身体感受。";
        return switch (ordinal) {
            case 1 -> "今日第一只小鹿路过。";
            case 2 -> "今天遇见了两只小鹿。";
            default -> "鹿群今天有点热闹，身体感觉还好吗？";
        };
    }

    private String clean(String value) { return value == null || value.isBlank() ? null : value.strip(); }

    private record Selection(CatalogCategory category, CatalogBrand brand, CatalogItem item,
                             String customBrandName, String title) {}
}
