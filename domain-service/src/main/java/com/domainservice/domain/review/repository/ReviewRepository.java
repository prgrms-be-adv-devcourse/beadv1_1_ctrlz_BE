package com.domainservice.domain.review.repository;


import com.domainservice.domain.review.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String>, ReviewQueryRepository {

    List<Review> findAllByUserId(String userId);

    Optional<Review> findByProductPostId(String productPostId);
}
