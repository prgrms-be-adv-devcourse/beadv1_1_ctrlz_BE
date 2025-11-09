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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import com.domainservice.domain.asset.image.infrastructure.s3.S3ImageClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.IntStream;

import static com.domainservice.domain.asset.image.application.ImageUtils.*;
import static java.nio.file.Files.probeContentType;

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

    @Transactional
    public List<Image> uploadProductImages(List<MultipartFile> files) {
        return IntStream.range(0, files.size())
                .mapToObj(i -> {
                    MultipartFile file = files.get(i);
                    return uploadUserProfile(file, ImageTarget.PRODUCT);
                })
                .toList();
    }

    // ImageTarget 으로 구분하는 경우는 없는 것 같아서 기존 코드는 냅두고 새로 만듬 - 정현
    public Image uploadUserProfile(MultipartFile file, ImageTarget target) {
        validateFile(file);

        String originalFileName = file.getOriginalFilename();
        File compressedFile = compressor.compressToWebp(originalFileName, file);

        String storedFileName = generateFileName(compressedFile.getName());
        String s3key = generateS3Key(storedFileName, target.name());
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
                    .imageTarget(target)
                    .build();

            return imageRepository.save(image);
        } catch (Exception e) {
            log.error("이미지 저장 실패 error: {} ", e.getMessage());
            throw new RuntimeException(e);
        }
    }

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

    /**
     * 업로드 후 반환된 URL을 이용해 S3 객체를 삭제합니다.
     */
    public void deleteByUrl(String fileUrl) {
        try {
            // URL의 path 부분에서 "/" 제거 후 key 추출
            URI uri = URI.create(fileUrl);
            String path = uri.getPath();
            String key = path.startsWith("/") ? path.substring(1) : path;

            deleteByKey(key);
        } catch (Exception e) {
            log.error("Failed to delete S3 object from URL: {}", fileUrl, e);
            throw new RuntimeException("S3 이미지 삭제 실패", e);
        }
    }

    /**
     * S3에 저장된 객체를 key로 삭제합니다.
     */
    public void deleteByKey(String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build()
        );
    }
}
