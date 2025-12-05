package com.aiservice.application;

public interface RagService<T> {
	String uploadData(T data);
	void deleteData(String productId);
}
