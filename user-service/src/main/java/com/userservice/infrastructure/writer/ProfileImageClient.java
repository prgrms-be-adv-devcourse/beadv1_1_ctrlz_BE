package com.userservice.infrastructure.writer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.userservice.infrastructure.writer.dto.ImageResponse;

@FeignClient(name = "profile-image-service", url = "localhost:8081")
public interface ProfileImageClient {

	@PostMapping(value = "/api/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ImageResponse uploadImage(@RequestPart("file") MultipartFile profileImage);
}
