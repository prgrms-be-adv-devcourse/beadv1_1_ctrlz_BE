package com.domainservice.domain.asset.image.application;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.domainservice.domain.asset.image.domain.entity.Image;
import com.domainservice.domain.asset.image.domain.service.AssetService;
import com.domainservice.domain.asset.image.infrastructure.ProfileImageUploadClient;
import com.domainservice.domain.asset.image.infrastructure.dto.ImageUrlResponse;

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
