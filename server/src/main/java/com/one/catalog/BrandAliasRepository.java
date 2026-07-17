package com.one.catalog;

import com.one.common.Dimension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BrandAliasRepository extends JpaRepository<BrandAlias, Long> {
    @Query("select a from BrandAlias a join fetch a.brand b where b.dimension = :dimension and b.active = true")
    List<BrandAlias> findActiveByDimension(Dimension dimension);
}
