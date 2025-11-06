package com.common.asset.image.application;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.common.asset.image.domain.entity.Image;
import com.common.asset.image.domain.service.AssetService;
import com.common.asset.image.infrastructure.ProfileImageUploadClient;
import com.common.asset.image.infrastructure.dto.ImageUrlResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ProfileImageLoader implements ProfileImageUploadClient {

	private final AssetService<Image> assetService;

	@Override
	public ImageUrlResponse uploadImage(MultipartFile file) {
		Image upload = assetService.uploadUserProfile(file);
		return new ImageUrlResponse(upload.getS3Url());
	}
}
