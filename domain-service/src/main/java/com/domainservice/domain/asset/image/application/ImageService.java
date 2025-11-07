package com.domainservice.domain.asset.image.application;

import static com.domainservice.domain.asset.image.application.ImageUtils.*;
import static java.nio.file.Files.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.domainservice.domain.asset.image.domain.entity.Image;
import com.domainservice.domain.asset.image.domain.entity.ImageTarget;
import com.domainservice.domain.asset.image.domain.repository.ImageRepository;
import com.domainservice.domain.asset.image.domain.service.AssetService;
import com.common.exception.CustomException;
import com.common.exception.vo.FileExceptionCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ImageService implements AssetService<Image> {

	@Value("${custom.bucket-name}")
	private String bucketName;
	@Value("${custom.image.allowed-extension}")
	private String allowedExtension;
	@Value("${custom.image.max-size}")
	private long maxSize;

	private final S3Client s3Client;
	private final ImageRepository imageRepository;
	private final ImageCompressor compressor;

	@Override
	public Image uploadUserProfile(MultipartFile file) {

		validateFile(file);

		String originalFileName = file.getOriginalFilename();
		File compressedFile = compressor.compressToWebp(originalFileName, file);

		String storedFileName = generateFileName(compressedFile.getName());
		String s3key = generateS3Key(storedFileName, ImageTarget.USER.name());
		String s3Url = getS3Url(bucketName, s3key);

		try {
			uploadToS3(compressedFile, s3key);

			Image image = Image.builder()
				.originalFileName(originalFileName)
				.storedFileName(storedFileName)
				.s3Url(s3Url)
				.s3Key(s3key)
				.originalFileSize(file.getSize())
				.originalContentType(file.getContentType())
				.compressedFileSize(compressedFile.length())
				.convertedContentType(Files.probeContentType(compressedFile.toPath()))
				.imageTarget(ImageTarget.USER)
				.build();

			return imageRepository.save(image);
		} catch (Exception e) {
			log.error("이미지 저장 실패 error: {} ", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	@Override
	public Image getImage(String id) {
		return imageRepository.findById(id)
			.orElseThrow(() -> new CustomException(FileExceptionCode.NO_SUCH_IMAGE.getValue()));
	}

	private void uploadToS3(File file, String s3key) throws IOException {
		PutObjectRequest request = PutObjectRequest.builder()
			.bucket(bucketName)
			.key(s3key)
			.contentType(probeContentType(file.toPath()))
			.contentLength(file.length())
			.build();

		s3Client.putObject(request, RequestBody.fromInputStream(new FileInputStream(file), file.length()));
	}

	private void validateFile(MultipartFile file) {

		if (file.getSize() > maxSize) {
			throw new CustomException(
				FileExceptionCode.FILE_EMPTY.addArgument(String.valueOf(file.getSize()))
			);
		}

		String originalFileName = file.getOriginalFilename();
		if (originalFileName == null || !isValidExtension(originalFileName)) {
			throw new CustomException(
				FileExceptionCode.NOT_VALID_EXTENSION.addArgument(originalFileName)
			);
		}

		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new CustomException(FileExceptionCode.NOT_IMAGE.getValue());
		}
	}

	private boolean isValidExtension(String fileName) {
		String extension = getFileExtension(fileName);
		List<String> allowed = List.of(allowedExtension.split(","));
		return allowed.stream()
			.anyMatch(ext -> ext.trim().equalsIgnoreCase(extension));
	}
}
