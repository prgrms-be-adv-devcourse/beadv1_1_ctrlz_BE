package com.domainservice.domain.search.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;

public interface ProductPostElasticRepository
	extends ElasticsearchRepository<ProductPostDocumentEntity, String> {

	@Query("""
		{
		  "bool": {
		    "must": [
		      {
		        "multi_match": {
		          "query": "?0",
		          "fields": [
		            "name^5",
		            "name.ngram^3",
		            "title^4",
		            "title.ngram^2",
		            "description^1.5",
		            "tags^3",
		            "category_name^2"
		          ],
		          "type": "best_fields",
		          "fuzziness": "AUTO"
		        }
		      }
		    ],
		    "filter": [
		      {
		        "term": {
		          "delete_status": "N"
		        }
		      },
		      {
		        "match": {
		          "category_name": "?1"
		        }
		      },
		      {
		        "range": {
		          "price": {
		            "gte": ?2,
		            "lte": ?3
		          }
		        }
		      },
		      {
		        "terms": {
		          "tags": ?4
		        }
		      }
		    ]
		  }
		}
		""")
	Page<ProductPostDocumentEntity> search(
		String query,          // ?0
		String category,       // ?1
		double minPrice,       // ?2
		double maxPrice,       // ?3
		List<String> tags,     // ?4
		Pageable pageable
	);
}