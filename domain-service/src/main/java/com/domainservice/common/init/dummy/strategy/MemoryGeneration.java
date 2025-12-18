package com.domainservice.common.init.dummy.strategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.domainservice.common.init.dummy.dto.vo.ProductNameData;
import com.domainservice.common.init.dummy.service.DummyDataGenerator;
import com.domainservice.common.init.dummy.service.DummyDataGenerator.TemplateData;
import com.github.f4b6a3.uuid.UuidCreator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 메모리 방식 - Java 메모리에서 직접 처리
 * 권장: 10만건 이하
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryGeneration implements DummyGeneration {

	private final DummyDataGenerator dataGenerator;

	@Override
	public void generateProducts(int productCount) {
		TemplateData templateData = dataGenerator.loadTemplateData();
		Map<String, String> categoryMapping = dataGenerator.loadCategoryMapping();

		List<ProductNameData> productNamesList = prepareProductNamesInMemory(
			templateData.productNames(),
			categoryMapping
		);

		generateProductPosts(productCount, productNamesList, templateData);
	}

	@Override
	public String getType() {
		return "메모리";
	}

	/**
	 * 메모리에서 상품명 데이터 준비
	 */
	private List<ProductNameData> prepareProductNamesInMemory(
		Map<String, List<String>> productNames,
		Map<String, String> categoryMapping
	) {
		List<ProductNameData> result = new ArrayList<>();

		for (Map.Entry<String, List<String>> entry : productNames.entrySet()) {
			String categoryId = categoryMapping.get(entry.getKey());
			if (categoryId != null) {
				for (String name : entry.getValue()) {
					result.add(new ProductNameData(categoryId, name));
				}
			}
		}

		return result;
	}

	/**
	 * 상품 생성
	 */
	private void generateProductPosts(
		int count,
		List<ProductNameData> productNamesList,
		TemplateData templateData
	) {
		long startTime = System.currentTimeMillis();

		if (productNamesList.isEmpty()) {
			log.error("상품명 데이터 없음");
			return;
		}

		List<Object[]> batchData = new ArrayList<>(DummyDataGenerator.BATCH_SIZE);
		Random random = dataGenerator.getRandom();

		int productsPerUser = 5;
		int totalUsers = (int)Math.ceil((double)count / productsPerUser);
		int createdCount = 0;

		// 자주 사용하는 값들 미리 계산
		int productNamesSize = productNamesList.size();
		int titlePrefixesSize = templateData.titlePrefixes().size();
		int conditionWordsSize = templateData.conditionWords().size();
		int descriptionPrefixesSize = templateData.descriptionPrefixes().size();
		int descriptionsSize = templateData.descriptions().size();
		String[] statuses = {"NEW", "GOOD", "FAIR"};
		String[] tradeStatuses = {"SELLING", "PROCESSING", "SOLDOUT"};

		for (int u = 0; u < totalUsers; u++) {
			String userId = UuidCreator.getTimeOrderedEpoch().toString();

			for (int p = 0; p < productsPerUser && createdCount < count; p++) {
				ProductNameData product = productNamesList.get(random.nextInt(productNamesSize));
				String productId = UuidCreator.getTimeOrderedEpoch().toString();

				String title = product.name() + " " +
					templateData.conditionWords().get(random.nextInt(conditionWordsSize)) + " " +
					templateData.titlePrefixes().get(random.nextInt(titlePrefixesSize));

				String description = product.name() + " " +
					templateData.descriptionPrefixes().get(random.nextInt(descriptionPrefixesSize)) + " " +
					templateData.descriptions().get(random.nextInt(descriptionsSize));

				batchData.add(new Object[] {
					productId, userId, product.categoryId(), title, product.name(),
					(random.nextInt(2001) * 1000) + 500000, description,
					statuses[random.nextInt(3)], tradeStatuses[random.nextInt(3)],
					random.nextInt(1001), random.nextInt(101), "N",
					LocalDateTime.now().minusDays(random.nextInt(365)),
					LocalDateTime.now().minusDays(random.nextInt(365))
				});

				createdCount++;

				if (batchData.size() >= DummyDataGenerator.BATCH_SIZE) {
					dataGenerator.saveProductBatch(batchData);
				}
			}
		}

		if (!batchData.isEmpty()) {
			dataGenerator.saveProductBatch(batchData);
		}

		long duration = System.currentTimeMillis() - startTime;
		log.info("상품 생성 완료: {}개 ({}초)", createdCount, duration / 1000);
	}
}