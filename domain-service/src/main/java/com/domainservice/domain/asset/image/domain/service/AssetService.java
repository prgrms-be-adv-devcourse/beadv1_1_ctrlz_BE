package com.domainservice.domain.asset.image.domain.service;

import org.springframework.web.multipart.MultipartFile;

import com.domainservice.domain.asset.image.domain.entity.Image;

public interface AssetService<T> {

	T uploadUserProfile(MultipartFile file);
	T getImage(String id);
	Image updateProfileImage(MultipartFile profileImage, String imageId);
	void delete(String id);
}
