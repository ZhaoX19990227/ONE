package com.one.recognition;

import com.one.catalog.BrandAlias;
import com.one.catalog.BrandAliasRepository;
import com.one.catalog.CatalogBrand;
import com.one.catalog.CatalogBrandRepository;
import com.one.catalog.CatalogCategory;
import com.one.catalog.CatalogCategoryRepository;
import com.one.catalog.CatalogItem;
import com.one.catalog.CatalogItemRepository;
import com.one.common.BusinessException;
import com.one.common.Dimension;
import com.one.config.OneProperties;
import com.one.media.MediaAsset;
import com.one.media.MediaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class RecognitionService {
    private final RecognitionTaskRepository tasks;
    private final MediaService mediaService;
    private final FoodVisionRecognizer recognizer;
    private final CatalogCategoryRepository categories;
    private final CatalogBrandRepository brands;
    private final CatalogItemRepository items;
    private final BrandAliasRepository aliases;
    private final ObjectMapper objectMapper;
    private final OneProperties properties;

    public RecognitionService(RecognitionTaskRepository tasks, MediaService mediaService,
                              FoodVisionRecognizer recognizer, CatalogCategoryRepository categories,
                              CatalogBrandRepository brands, CatalogItemRepository items,
                              BrandAliasRepository aliases, ObjectMapper objectMapper,
                              OneProperties properties) {
        this.tasks = tasks;
        this.mediaService = mediaService;
        this.recognizer = recognizer;
        this.categories = categories;
        this.brands = brands;
        this.items = items;
        this.aliases = aliases;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public RecognitionDtos.View recognize(long userId, RecognitionDtos.StartRequest request) {
        validateDimension(request.dimension());
        MediaAsset media = mediaService.owned(userId, request.mediaAssetId());
        RecognitionTask task = tasks.save(RecognitionTask.processing(userId, media, request.dimension(),
                "QWEN:" + properties.qwen().model()));
        try {
            List<CatalogCategory> knownCategories = categories.findByDimensionAndActiveTrueOrderBySortOrderAsc(request.dimension());
            List<CatalogBrand> knownBrands = brands.findByDimensionAndActiveTrueOrderBySortOrderAsc(request.dimension());
            FoodVisionRecognizer.VisionResult result = recognizer.recognize(request.dimension(), media.getContentType(),
                    mediaService.content(media), knownCategories.stream().map(CatalogCategory::getName).toList(),
                    knownBrands.stream().map(CatalogBrand::getName).toList());
            List<RecognitionDtos.Candidate> normalized = normalize(request.dimension(), result.candidates(),
                    knownCategories, knownBrands);
            if (normalized.isEmpty()) {
                task.failed("NO_RECOGNIZABLE_ITEM");
            } else {
                task.needConfirmation(objectMapper.writeValueAsString(normalized), confidence(result.confidence()));
            }
            tasks.save(task);
            return view(task, normalized);
        } catch (BusinessException error) {
            task.failed(error.code());
            tasks.save(task);
            return view(task, List.of());
        } catch (IOException error) {
            task.failed("IMAGE_READ_FAILED");
            tasks.save(task);
            return view(task, List.of());
        } catch (Exception error) {
            task.failed("VISION_RESPONSE_INVALID");
            tasks.save(task);
            return view(task, List.of());
        }
    }

    @Transactional(readOnly = true)
    public RecognitionDtos.View get(long userId, long taskId) throws Exception {
        RecognitionTask task = ownedTask(userId, taskId);
        return view(task, readCandidates(task.getCandidatesJson()));
    }

    @Transactional
    public RecognitionDtos.View confirm(long userId, long taskId, RecognitionDtos.ConfirmRequest request) throws Exception {
        RecognitionTask task = ownedTask(userId, taskId);
        if (task.getStatus() != RecognitionStatus.NEED_CONFIRMATION && task.getStatus() != RecognitionStatus.FAILED) {
            throw new BusinessException("RECOGNITION_NOT_CONFIRMABLE", "这个识别结果不能重复确认", HttpStatus.CONFLICT);
        }
        CatalogItem item = request.itemId() == null ? null : items.findById(request.itemId())
                .filter(value -> value.isActive() && value.getDimension() == task.getDimension())
                .orElseThrow(() -> notFound("没有找到这个产品"));
        CatalogCategory category;
        CatalogBrand brand;
        if (item != null) {
            category = item.getCategory();
            brand = item.getBrand();
        } else {
            category = request.categoryId() == null ? null : categories.findById(request.categoryId())
                    .filter(value -> value.isActive() && value.getDimension() == task.getDimension())
                    .orElseThrow(() -> notFound("没有找到这个品类"));
            brand = request.brandId() == null ? null : brands.findById(request.brandId())
                    .filter(value -> value.isActive() && value.getDimension() == task.getDimension())
                    .orElseThrow(() -> notFound("没有找到这个品牌"));
            if (request.customItemName() == null || request.customItemName().isBlank()) {
                throw new BusinessException("ITEM_REQUIRED", "请选择产品或输入实际吃喝内容", HttpStatus.BAD_REQUEST);
            }
        }
        task.confirm(category, brand, item, clean(request.customBrandName()), clean(request.customItemName()));
        return view(tasks.save(task), readCandidates(task.getCandidatesJson()));
    }

    private List<RecognitionDtos.Candidate> normalize(Dimension dimension,
                                                      List<FoodVisionRecognizer.VisionCandidate> raw,
                                                      List<CatalogCategory> knownCategories,
                                                      List<CatalogBrand> knownBrands) {
        List<BrandAlias> knownAliases = aliases.findActiveByDimension(dimension);
        List<CatalogItem> knownItems = items.findByDimensionAndActiveTrue(dimension);
        return raw.stream().filter(value -> value.itemName() != null && !value.itemName().isBlank())
                .map(value -> {
                    CatalogCategory category = bestCategory(value.categoryName(), knownCategories);
                    CatalogBrand brand = bestBrand(value.brandName(), knownBrands, knownAliases);
                    CatalogItem item = bestItem(value.itemName(), brand, category, knownItems);
                    if (item != null) {
                        category = item.getCategory();
                        brand = item.getBrand();
                    }
                    return new RecognitionDtos.Candidate(
                            category == null ? null : category.getId(), category == null ? value.categoryName() : category.getName(),
                            brand == null ? null : brand.getId(), brand == null ? value.brandName() : brand.getName(),
                            brand == null ? null : brand.getLogoUrl(), item == null ? null : item.getId(),
                            item == null ? clean(value.itemName()) : item.getName(), clamp(value.confidence()),
                            value.estimatedAmountFen(), clean(value.evidence()), item != null);
                }).sorted(Comparator.comparingDouble(RecognitionDtos.Candidate::confidence).reversed())
                .limit(3).toList();
    }

    private CatalogCategory bestCategory(String name, List<CatalogCategory> values) {
        String target = normalize(name);
        if (target.isEmpty()) return null;
        return values.stream().filter(value -> close(target, normalize(value.getName()))).findFirst().orElse(null);
    }

    private CatalogBrand bestBrand(String name, List<CatalogBrand> values, List<BrandAlias> knownAliases) {
        String target = normalize(name);
        if (target.isEmpty()) return null;
        CatalogBrand direct = values.stream().filter(value -> close(target, normalize(value.getName()))
                || close(target, normalize(value.getShortName())) || close(target, normalize(value.getCode())))
                .findFirst().orElse(null);
        if (direct != null) return direct;
        return knownAliases.stream().filter(value -> close(target, normalize(value.getAliasName())))
                .map(BrandAlias::getBrand).findFirst().orElse(null);
    }

    private CatalogItem bestItem(String name, CatalogBrand brand, CatalogCategory category, List<CatalogItem> values) {
        String target = normalize(name);
        return values.stream().filter(value -> brand == null || value.getBrand() != null && value.getBrand().getId().equals(brand.getId()))
                .filter(value -> category == null || value.getCategory().getId().equals(category.getId()))
                .filter(value -> close(target, normalize(value.getName())))
                .max(Comparator.comparingInt(value -> normalize(value.getName()).length())).orElse(null);
    }

    private boolean close(String left, String right) {
        if (left.isEmpty() || right.isEmpty()) return false;
        return left.equals(right) || left.length() >= 3 && right.contains(left) || right.length() >= 3 && left.contains(right);
    }

    private String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{IsHan}a-z0-9]", "");
    }

    private RecognitionTask ownedTask(long userId, long taskId) {
        return tasks.findById(taskId).filter(value -> value.getUserId() == userId)
                .orElseThrow(() -> notFound("识别任务不存在"));
    }

    private List<RecognitionDtos.Candidate> readCandidates(String value) throws Exception {
        if (value == null || value.isBlank()) return List.of();
        return objectMapper.readerForListOf(RecognitionDtos.Candidate.class).readValue(value);
    }

    private RecognitionDtos.View view(RecognitionTask task, List<RecognitionDtos.Candidate> candidates) {
        return new RecognitionDtos.View(task.getId(), task.getMediaAsset().getId(), task.getMediaAsset().getOriginalUrl(),
                task.getDimension(), task.getStatus(), task.getProvider(),
                task.getConfidence() == null ? 0 : task.getConfidence().doubleValue(), candidates,
                task.getFailureCode(), task.getStatus() == RecognitionStatus.FAILED
                ? "没认准也没关系，照片已经保留，选个品牌或自己写下它。" : null);
    }

    private BigDecimal confidence(double value) {
        return BigDecimal.valueOf(clamp(value)).setScale(4, RoundingMode.HALF_UP);
    }

    private double clamp(double value) { return Math.max(0, Math.min(1, value)); }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.strip(); }
    private BusinessException notFound(String message) { return new BusinessException("CATALOG_NOT_FOUND", message, HttpStatus.NOT_FOUND); }

    private void validateDimension(Dimension dimension) {
        if (dimension != Dimension.MEAL && dimension != Dimension.MILK_TEA && dimension != Dimension.COFFEE) {
            throw new BusinessException("UNSUPPORTED_RECOGNITION_DIMENSION", "仅支持识别吃饭、奶茶或咖啡照片", HttpStatus.BAD_REQUEST);
        }
    }
}
