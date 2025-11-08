package com.user.infrastructure.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.user.infrastructure.feign.dto.ImageResponse;

@FeignClient(name = "profile-image-service", url = "localhost:8081")
public interface ProfileImageClient {

	@PostMapping(value = "/api/images/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ImageResponse updateProfileImage(@RequestPart("file") MultipartFile profileImage, @PathVariable("id") String id);
}
