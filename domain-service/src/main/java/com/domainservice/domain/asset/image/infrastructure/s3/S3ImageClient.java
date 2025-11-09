package com.domainservice.domain.asset.image.infrastructure.s3;

import static com.domainservice.domain.asset.image.application.ImageUtils.*;
import static java.nio.file.Files.*;

import java.io.File;
import java.io.FileInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3ImageClient {

	@Value("${custom.bucket-name}")
	private String bucketName;

	private final S3Client s3Client;

	public String uploadUserProfileToS3(File file, String s3key) {
		try {
			PutObjectRequest request = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(s3key)
				.contentType(probeContentType(file.toPath()))
				.contentLength(file.length())
				.build();

			s3Client.putObject(request, RequestBody.fromInputStream(new FileInputStream(file), file.length()));

			return getS3Url(bucketName, s3key);
		} catch (Exception e) {
			log.error("이미지 저장 실패 error: {} ", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public void deleteFileFromS3(String s3key) {
		try {
			s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(s3key).build());
		}catch (Exception e) {
			log.error("s3 파일 삭제 실패 e : {}", e.getMessage());
			throw new RuntimeException(e);
		}
	}
}
