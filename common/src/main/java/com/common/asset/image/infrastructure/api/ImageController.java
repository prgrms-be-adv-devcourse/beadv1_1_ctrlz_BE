package com.common.asset.image.infrastructure.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.common.asset.image.application.AssetService;
import com.common.asset.image.domain.entity.Image;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/images")
public class ImageController {

	private final AssetService<Image> assetService;

	@PostMapping
	public ResponseEntity<Image> uploadImage(@RequestParam("file") MultipartFile profileImage) {
		Image upload = assetService.upload(profileImage);
		return ResponseEntity.ok(upload);
	}

	@GetMapping("/{id}")
	public ResponseEntity<String> getAllImages(@PathVariable String id) {
		Image image = assetService.getImage(id);
		return ResponseEntity.ok(image.getS3Url());
	}
}
