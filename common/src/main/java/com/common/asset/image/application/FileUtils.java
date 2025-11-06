package com.common.asset.image.application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class FileUtils {
	public static String getFileExtension(String fileName) {
		int lastIndexOf = fileName.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return "";
		}
		return fileName.substring(lastIndexOf + 1).toLowerCase();
	}

	public static String generateFileName(String originalFileName) {
		String extension = getFileExtension(originalFileName);
		String uuid = UUID.randomUUID().toString();
		return uuid + "." + extension;
	}

	public static String generateS3Key(String fileName) {
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		return "images/" + date + fileName;
	}

	public static String getS3Url(String bucketName, String s3Key) {
		return "https://%s.s3.amazonaws.com/%s".formatted(bucketName, s3Key);
	}
}
