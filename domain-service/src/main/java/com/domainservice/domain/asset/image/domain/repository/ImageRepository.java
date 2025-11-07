package com.domainservice.domain.asset.image.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.domainservice.domain.asset.image.domain.entity.Image;

public interface ImageRepository extends JpaRepository<Image, String> {
	Image save(Image image);

	Optional<Image> findById(String id);

	Optional<Image> findByS3Key(String s3Key);

	void deleteByS3Key(String s3Key);

	void deleteById(String id);
}
