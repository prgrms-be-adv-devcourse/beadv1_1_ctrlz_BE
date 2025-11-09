package com.domainservice.domain.asset.image.application;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ImageCompressor {

	public File compressToWebp( MultipartFile originalFile) {
		String originalFilename = originalFile.getOriginalFilename();
		try {
			return ImmutableImage.loader()
				.fromBytes(originalFile.getBytes())
				.output(WebpWriter.DEFAULT.withLossless(), new File(originalFilename + ".webp"));
		} catch (IOException e) {
			log.info("이미지 압축 에러 e = {}", e.getMessage());
			throw new RuntimeException(e);
		}
	}
}
