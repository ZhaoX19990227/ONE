package com.one.media;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    Optional<MediaAsset> findByStorageKeyAndStatus(String storageKey, MediaStatus status);
}
