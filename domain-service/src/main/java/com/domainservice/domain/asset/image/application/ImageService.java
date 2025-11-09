package com.domainservice.domain.asset.image.application;

import static com.domainservice.domain.asset.image.application.ImageUtils.*;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.common.exception.CustomException;
import com.common.exception.vo.FileExceptionCode;
import com.domainservice.domain.asset.image.domain.entity.Image;
import com.domainservice.domain.asset.image.domain.entity.ImageTarget;
import com.domainservice.domain.asset.image.domain.entity.ImageType;
import com.domainservice.domain.asset.image.domain.repository.ImageRepository;
import com.domainservice.domain.asset.image.domain.service.AssetService;
import com.domainservice.domain.asset.image.infrastructure.s3.S3ImageClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ImageService implements AssetService<Image> {

	@Value("${custom.image.allowed-extension}")
	private String allowedExtension;
	@Value("${custom.image.max-size}")
	private long maxSize;

	private final S3ImageClient s3ImageClient;
	private final ImageRepository imageRepository;
	private final ImageCompressor compressor;

	@Override
	public Image uploadUserProfile(MultipartFile file) {

		validateFile(file);

		File compressedFile = compressor.compressToWebp(file);
		String storedFileName = generateFileName(compressedFile.getName());
		String s3key = generateS3Key(storedFileName, ImageTarget.USER.name());

		String s3Url = s3ImageClient.uploadUserProfileToS3(compressedFile, s3key);

		Image image = Image.builder()
			.originalFileName(file.getOriginalFilename())
			.storedFileName(storedFileName)
			.s3Url(s3Url)
			.s3Key(s3key)
			.originalFileSize(file.getSize())
			.originalContentType(file.getContentType())
			.compressedFileSize(compressedFile.length())
			.convertedContentType(ImageType.WEBP)
			.imageTarget(ImageTarget.USER)
			.build();

		return imageRepository.save(image);
	}

	@Override
	public Image getImage(String id) {
		return imageRepository.findById(id)
			.orElseThrow(() -> new CustomException(FileExceptionCode.NO_SUCH_IMAGE.getValue()));
	}

	@Override
	public Image updateProfileImage(MultipartFile profileImage, String imageId) {

		imageRepository.findById(imageId)
			.ifPresent(image -> {
				image.delete();
				s3ImageClient.deleteFileFromS3(image.getS3Url());
			});

		return uploadUserProfile(profileImage);
	}

	@Override
	public void delete(String id) {
		imageRepository.deleteById(id);
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
