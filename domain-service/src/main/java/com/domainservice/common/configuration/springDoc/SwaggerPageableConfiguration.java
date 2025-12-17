package com.domainservice.common.configuration.springDoc;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;

import com.domainservice.domain.search.docs.GlobalSearchApiDocs;

import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;

@Configuration
public class SwaggerPageableConfiguration {

	@Bean
	public OperationCustomizer customizePageable() {
		return (operation, handlerMethod) -> {

			boolean hasPageable = java.util.Arrays.stream(handlerMethod.getMethodParameters())
				.anyMatch(param -> Pageable.class.equals(param.getParameterType()));

			if (!hasPageable || operation.getParameters() == null) {
				return operation;
			}

			// @HidePageableSort 어노테이션이 있는지 확인
			boolean hidePageableSort = handlerMethod.getMethod().isAnnotationPresent(HidePageableSort.class);
			boolean isGlobalSearch = handlerMethod.getMethod().isAnnotationPresent(GlobalSearchApiDocs.class);

			for (int i = operation.getParameters().size() - 1; i >= 0; i--) {
				Parameter param = operation.getParameters().get(i);
				String paramName = param.getName();

				if ("page".equals(paramName)) {
					param.setDescription("페이지 번호");
					param.setSchema(new IntegerSchema()._default(0));
				}

				if ("size".equals(paramName)) {
					param.setDescription("페이지 당 조회 개수");
					int defaultSize = hidePageableSort ? 24 : 12;
					param.setSchema(new IntegerSchema()._default(defaultSize));
				}

				if ("sort".equals(paramName)) {
					if (hidePageableSort) { operation.getParameters().remove(i); }
					else if (isGlobalSearch) { return operation; }
					else {
						param.setDescription("""
                      정렬 기준
                      
                      **형식**: `필드명,정렬방향`
                      
                      **정렬 방향**:
                      - `desc`: 내림차순
                      - `asc`: 오름차순
                      
                      **사용 가능한 필드**: createdAt, price, viewCount, likedCount
                      """);
						param.setSchema(new StringSchema().example("createdAt,desc"));
					}
				}
			}

			return operation;
		};
	}
}