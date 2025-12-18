package com.domainservice.common.configuration.springDoc;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Pageable의 sort 파라미터를 Swagger 문서에서 숨깁니다.
 * Elasticsearch 등 자체 정렬 파라미터를 사용하는 경우에 적용합니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HidePageableSort {
}