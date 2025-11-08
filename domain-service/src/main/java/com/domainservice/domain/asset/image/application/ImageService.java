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
	public Image uploadProfileImage(MultipartFile file) {
		return uploadProfileImageByTarget(file, ImageTarget.NONE);
	}

	/**
	 * ImageTarget(USER, PRODUCT, REVIEW)을 구분하여 이미지 파일을 업로드합니다.
	 * 원본 파일을 WEBP 형식으로 압축한 후 S3에 업로드하고, 이미지 정보를 DB에 저장합니다.
	 *
	 * @param file   업로드할 이미지 파일
	 * @param target 이미지 타겟 타입 (USER, PRODUCT, REVIEW)
	 * @return 업로드된 이미지 엔티티 (S3 URL, 파일 정보 포함)
	 */
	public Image uploadProfileImageByTarget(MultipartFile file, ImageTarget target) {
		validateFile(file);

		File compressedFile = compressor.compressToWebp(file);
		String storedFileName = generateFileName(compressedFile.getName());
		String s3key = generateS3Key(storedFileName, target.name());

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
			.imageTarget(target)
			.build();

		return imageRepository.save(image);
	}

	@Override
	public List<Image> uploadProfileImageList(List<MultipartFile> files) {
		return uploadProfileImageListByTarget(files, ImageTarget.NONE);
	}

	/**
	 * 여러 개의 이미지 파일을 동일한 타겟 타입으로 일괄 업로드합니다.
	 * 각 파일은 개별적으로 압축 및 업로드, DB에 저장되어 리스트로 반환됩니다.
	 *
	 * @param files  업로드할 이미지 파일 리스트
	 * @param target 이미지 타겟 타입 (USER, PRODUCT, REVIEW)
	 * @return 업로드된 이미지 엔티티 리스트
	 */
	public List<Image> uploadProfileImageListByTarget(List<MultipartFile> files, ImageTarget target) {
		return files.stream()
			.map(file -> uploadProfileImageByTarget(file, target))
			.toList();
	}

	@Override
	public Image updateProfileImage(MultipartFile profileImage, String imageId) {
		deleteProfileImageById(imageId);
		return uploadProfileImage(profileImage);
	}

	public Image updateProfileImageByTarget(MultipartFile profileImage, ImageTarget target, String imageId) {
		deleteProfileImageById(imageId);
		return uploadProfileImageByTarget(profileImage, target);
	}

	@Override
	public void deleteProfileImageByS3Url(String s3Url) {
		imageRepository.findByS3Url(s3Url)
			.ifPresent(image -> {
				imageRepository.deleteById(image.getId());
				s3ImageClient.deleteFileFromS3(image.getS3Url());
			});
	}

	@Override
	public void deleteProfileImageById(String imageId) {
		imageRepository.findById(imageId)
			.ifPresent(image -> {
				imageRepository.deleteById(image.getId());
				s3ImageClient.deleteFileFromS3(image.getS3Url());
			});
	}

	@Override
	public Image getImage(String id) {
		return imageRepository.findById(id)
			.orElseThrow(() -> new CustomException(FileExceptionCode.NO_SUCH_IMAGE.getValue()));
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
