package com.domainservice.common.init.dummy.dto;

public record DummyResultResponse(
	boolean success,
	int productCount,
	long durationSeconds
) {
	public static DummyResultResponse success(int count, long durationMillis) {
		return new DummyResultResponse(true, count, durationMillis / 1000);
	}
}