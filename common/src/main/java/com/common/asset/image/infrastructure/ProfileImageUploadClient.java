package com.common.asset.image.infrastructure;

import org.springframework.web.multipart.MultipartFile;

import com.common.asset.image.infrastructure.dto.ImageUrlResponse;

public interface ProfileImageUploadClient {
	ImageUrlResponse uploadImage(MultipartFile file);
}
