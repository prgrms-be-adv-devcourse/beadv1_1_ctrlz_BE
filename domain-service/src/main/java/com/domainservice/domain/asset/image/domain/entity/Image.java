package com.domainservice.domain.asset.image.domain.entity;

import com.common.model.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "images")
public class Image extends BaseEntity {

	@Column(nullable = false)
	private String originalFileName;

	@Column(nullable = false)
	private String storedFileName;

	@Column(name = "s3_url", nullable = false, length = 1000)
	private String s3Url;

	@Column(name = "s3_key", nullable = false)
	private String s3Key;

	@Column(nullable = false)
	private Long originalFileSize;

	@Column(nullable = false)
	private String originalContentType;

	@Column(nullable = false)
	private Long compressedFileSize;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ImageType convertedContentType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ImageTarget imageTarget;

	@Builder
	public Image(
		String originalFileName,
		String storedFileName,
		String s3Url,
		String s3Key,
		Long originalFileSize,
		String originalContentType,
		Long compressedFileSize,
		ImageType convertedContentType,
		ImageTarget imageTarget
	) {
		this.originalFileName = originalFileName;
		this.storedFileName = storedFileName;
		this.s3Url = s3Url;
		this.s3Key = s3Key;
		this.originalFileSize = originalFileSize;
		this.originalContentType = originalContentType;
		this.compressedFileSize = compressedFileSize;
		this.convertedContentType = convertedContentType;
		this.imageTarget = imageTarget;
	}

	@Override
	protected String getEntitySuffix() {
		return this.getClass().getAnnotation(Table.class).name();
	}

}
