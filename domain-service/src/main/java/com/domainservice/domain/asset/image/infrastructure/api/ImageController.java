package com.domainservice.domain.asset.image.infrastructure.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.domainservice.domain.asset.image.domain.entity.Image;
import com.domainservice.domain.asset.image.domain.service.AssetService;
import com.domainservice.domain.asset.image.infrastructure.dto.ImageUrlResponse;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/images")
public class ImageController {

	private final AssetService<Image> assetService;

	@PostMapping
	public ImageUrlResponse uploadImage(@RequestPart("file") MultipartFile profileImage) {
		Image image = assetService.uploadUserProfile(profileImage);
		return new ImageUrlResponse(image.getS3Url());
	}
}
