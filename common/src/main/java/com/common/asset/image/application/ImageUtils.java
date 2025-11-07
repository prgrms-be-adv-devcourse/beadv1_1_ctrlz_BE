package com.common.asset.image.application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageUtils {
	public static String getFileExtension(String fileName) {
		int lastIndexOf = fileName.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return "";
		}
		return fileName.substring(lastIndexOf + 1).toLowerCase();
	}

	public static String generateFileName(String originalFileName) {
		String extension = getFileExtension(originalFileName);
		String uuid = UUID.randomUUID()
			.toString()
			.replace("-", "")
			.substring(0, 12);
		return uuid + "." + extension;
	}

	public static String generateS3Key(String fileName, String from) {
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		return "images/%s/%s/%s".formatted(from, date, fileName);
	}

	public static String getS3Url(String bucketName, String s3Key) {
		return "https://%s.s3.amazonaws.com/%s".formatted(bucketName, s3Key);
	}
}
