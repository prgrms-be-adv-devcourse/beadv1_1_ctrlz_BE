package com.domainservice.domain.asset.image.infrastructure;

import org.springframework.web.multipart.MultipartFile;

import com.domainservice.domain.asset.image.infrastructure.dto.ImageUrlResponse;

public interface ProfileImageUploadClient {
	ImageUrlResponse uploadImage(MultipartFile file);
}
