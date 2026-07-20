package com.one.catalog;

import com.one.common.BusinessException;
import com.one.config.OneProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import com.one.record.LifeRecordRepository;

@Service
public class CatalogAdminService {
    private final CatalogCustomEntryRepository entries;
    private final CatalogItemRepository items;
    private final BrandAliasRepository aliases;
    private final LifeRecordRepository records;
    private final String adminSecret;

    public CatalogAdminService(CatalogCustomEntryRepository entries, CatalogItemRepository items,
                               BrandAliasRepository aliases, LifeRecordRepository records, OneProperties properties) {
        this.entries = entries; this.items = items; this.aliases = aliases; this.records = records;
        this.adminSecret = properties.adminSecret();
    }

    @Transactional(readOnly = true)
    public CatalogAdminDtos.PendingList pending(String secret) {
        authorize(secret);
        List<CatalogAdminDtos.Pending> values = entries.findTop100ByStatusOrderByCreatedAtAsc("PENDING").stream()
                .map(value -> new CatalogAdminDtos.Pending(value.getId(), value.getDimension(), value.getCategoryId(),
                        value.getBrandName(), value.getItemName())).toList();
        return new CatalogAdminDtos.PendingList(values);
    }

    @Transactional
    public CatalogAdminDtos.NormalizeResult normalize(String secret, long sourceId, long targetItemId) {
        authorize(secret);
        CatalogCustomEntry source = entries.findById(sourceId).filter(value -> "PENDING".equals(value.getStatus()))
                .orElseThrow(() -> new BusinessException("CUSTOM_ENTRY_NOT_FOUND", "待归一内容不存在", HttpStatus.NOT_FOUND));
        CatalogItem target = items.findById(targetItemId).filter(CatalogItem::isActive)
                .orElseThrow(() -> new BusinessException("CATALOG_ITEM_NOT_FOUND", "目标产品不存在", HttpStatus.NOT_FOUND));
        if (source.getDimension() != target.getDimension()) {
            throw new BusinessException("NORMALIZE_DIMENSION_MISMATCH", "来源与目标维度不一致", HttpStatus.CONFLICT);
        }
        List<CatalogCustomEntry> same = entries.findByDimensionAndBrandNameAndItemNameAndStatus(
                source.getDimension(), source.getBrandName(), source.getItemName(), "PENDING");
        same.forEach(value -> value.normalize(target));
        String targetBrandName = target.getBrand() == null ? null : target.getBrand().getName();
        int historicalRecords = records.normalizeCustomRecords(source.getDimension(), source.getBrandName(),
                source.getItemName(), target.getCategory(), target.getBrand(), target, target.getName(), targetBrandName);
        boolean aliasCreated = false;
        CatalogBrand targetBrand = target.getBrand();
        if (targetBrand != null && source.getBrandName() != null && !source.getBrandName().equals(targetBrand.getName())
                && !aliases.existsByBrandIdAndAliasName(targetBrand.getId(), source.getBrandName())) {
            aliases.save(BrandAlias.of(targetBrand, source.getBrandName())); aliasCreated = true;
        }
        return new CatalogAdminDtos.NormalizeResult(sourceId, targetItemId, same.size(), historicalRecords, aliasCreated);
    }

    @Transactional
    public void ignore(String secret, long sourceId) {
        authorize(secret);
        CatalogCustomEntry source = entries.findById(sourceId).filter(value -> "PENDING".equals(value.getStatus()))
                .orElseThrow(() -> new BusinessException("CUSTOM_ENTRY_NOT_FOUND", "待处理内容不存在", HttpStatus.NOT_FOUND));
        source.ignore();
    }

    private void authorize(String supplied) {
        if (adminSecret == null || adminSecret.isBlank()) {
            throw new BusinessException("ADMIN_DISABLED", "管理接口未启用", HttpStatus.SERVICE_UNAVAILABLE);
        }
        byte[] expected = adminSecret.getBytes(StandardCharsets.UTF_8);
        byte[] actual = supplied == null ? new byte[0] : supplied.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new BusinessException("ADMIN_UNAUTHORIZED", "管理密钥无效", HttpStatus.FORBIDDEN);
        }
    }
}
