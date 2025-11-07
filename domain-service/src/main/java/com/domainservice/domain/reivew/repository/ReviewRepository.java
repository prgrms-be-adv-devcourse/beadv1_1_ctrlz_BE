package com.domainservice.domain.reivew.repository;


import com.domainservice.domain.reivew.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    List<Review> findAllByUserId(String userId);
}
