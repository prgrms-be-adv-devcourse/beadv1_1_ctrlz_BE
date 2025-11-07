package com.domainservice.domain.asset.image.domain.service;

import org.springframework.web.multipart.MultipartFile;

public interface AssetService<T> {

	T uploadUserProfile(MultipartFile file);
	T getImage(String id);
}
