package com.one.catalog;

import com.one.common.Dimension;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/catalog")
@Transactional(readOnly = true)
public class CatalogController {
    private final CatalogCategoryRepository categories;
    private final CatalogBrandRepository brands;
    private final CatalogItemRepository items;

    public CatalogController(CatalogCategoryRepository categories, CatalogBrandRepository brands,
                             CatalogItemRepository items) {
        this.categories = categories;
        this.brands = brands;
        this.items = items;
    }

    @GetMapping("/categories")
    public List<CategoryView> categories(@RequestParam Dimension dimension) {
        return categories.findByDimensionAndActiveTrueOrderBySortOrderAsc(dimension).stream()
                .map(CategoryView::from).toList();
    }

    @GetMapping("/brands")
    public List<BrandView> brands(@RequestParam Dimension dimension) {
        return brands.findByDimensionAndActiveTrueOrderBySortOrderAsc(dimension).stream()
                .map(BrandView::from).toList();
    }

    @GetMapping("/items")
    public List<ItemView> items(@RequestParam Dimension dimension,
                                @RequestParam(required = false) Long categoryId,
                                @RequestParam(required = false) Long brandId) {
        List<CatalogItem> result = brandId != null
                ? items.findByDimensionAndBrandIdAndActiveTrue(dimension, brandId)
                : categoryId != null
                ? items.findByDimensionAndCategoryIdAndActiveTrue(dimension, categoryId)
                : items.findByDimensionAndActiveTrue(dimension);
        return result.stream().map(ItemView::from).toList();
    }

    public record CategoryView(long id, Dimension dimension, String code, String name, String icon, String color) {
        static CategoryView from(CatalogCategory value) { return new CategoryView(value.getId(), value.getDimension(), value.getCode(), value.getName(), value.getIcon(), value.getColor()); }
    }
    public record BrandView(long id, Dimension dimension, String code, String name, String shortName, String logoUrl, String color) {
        static BrandView from(CatalogBrand value) { return new BrandView(value.getId(), value.getDimension(), value.getCode(), value.getName(), value.getShortName(), value.getLogoUrl(), value.getBrandColor()); }
    }
    public record ItemView(long id, Dimension dimension, long categoryId, Long brandId, String brandName,
                           String name, String imageUrl, Integer defaultPriceFen, String attributes) {
        static ItemView from(CatalogItem value) {
            CatalogBrand brand = value.getBrand();
            return new ItemView(value.getId(), value.getDimension(), value.getCategory().getId(),
                    brand == null ? null : brand.getId(), brand == null ? null : brand.getName(), value.getName(),
                    value.getImageUrl(), value.getDefaultPriceFen(), value.getAttributes());
        }
    }
}
