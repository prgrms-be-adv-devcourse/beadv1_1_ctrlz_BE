package com.aiservice.application;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.infrastructure.feign.dto.ProductPostEsSearchResponse;

@Component
public class RRFMerger {

    /*
     * RRF 알고리즘으로 결과 병합
     * k = 60 (일반적인 상수)
     */
    public List<DocumentSearchResponse> mergeWithRRF(
            List<ProductPostEsSearchResponse> esResults,
            List<DocumentSearchResponse> vectorResults,
            int limit) {

        final int k = 60;
        Map<String, Double> rrfScores = new LinkedHashMap<>();
        Map<String, DocumentSearchResponse> docMap = new LinkedHashMap<>();

        // es 결과 점수 계산
        for (int i = 0; i < esResults.size(); i++) {
            ProductPostEsSearchResponse esDoc = esResults.get(i);
            String productId = esDoc.id();
            double score = 1.0 / (k + i + 1);
            rrfScores.merge(productId, score, Double::sum);

            // ES 결과를 DocumentSearchResponse로 변환
            if (!docMap.containsKey(productId)) {
                docMap.put(productId, convertToDocumentSearchResponse(esDoc));
            }
        }

        // vector 결과 점수 계산
        for (int i = 0; i < vectorResults.size(); i++) {
            DocumentSearchResponse vectorDoc = vectorResults.get(i);
            String productId = (String) vectorDoc.metadata().get("productId");
            if (productId == null)
                continue;

            double score = 1.0 / (k + i + 1);
            rrfScores.merge(productId, score, Double::sum);

            // vector 결과가 더 상세한 정보를 가질 수 있으므로 우선 사용
            docMap.putIfAbsent(productId, vectorDoc);
        }

        // RRF 점수로 정렬
        return rrfScores.keySet().stream()
                .sorted(Comparator.comparing(rrfScores::get).reversed())// 내림차순
                .limit(limit)
                .map(docMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private DocumentSearchResponse convertToDocumentSearchResponse(ProductPostEsSearchResponse esDoc) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("productId", esDoc.id());
        metadata.put("title", esDoc.title());
        metadata.put("categoryName", esDoc.categoryName());
        metadata.put("price", esDoc.price());
        metadata.put("tags", esDoc.tags());

        return new DocumentSearchResponse(
                esDoc.id(),
                esDoc.description(),
                metadata,
                0.0);
    }
}
