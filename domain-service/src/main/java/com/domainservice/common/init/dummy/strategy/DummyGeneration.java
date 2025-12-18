package com.domainservice.common.init.dummy.strategy;

/**
 * 더미 데이터 생성 전략 인터페이스
 */
public interface DummyGeneration {
	void generateProducts(int productCount);
	String getType();
}