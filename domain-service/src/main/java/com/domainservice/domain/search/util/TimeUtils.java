package com.domainservice.domain.search.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeUtils {

	public static String convertDateTimeToString(LocalDateTime dateTime) {
		return dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
	}

	public static LocalDateTime convertFromStringToDateTime(String dateStr) {
		return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
	}

	public static String getCurrentDateForRedisKey() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ":";
	}

	public static String getCurrentDateTimeStr() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
	}

	public static String getPreviousDateTimeStr() {
		return LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
	}

	public static LocalDateTime getLastBatchExecutionTime() {
		return convertFromStringToDateTime(getCurrentDateTimeStr());
	}

	public static LocalDateTime getCurrentBatchExecutionTime() {
		return convertFromStringToDateTime(getPreviousDateTimeStr());
	}


}
