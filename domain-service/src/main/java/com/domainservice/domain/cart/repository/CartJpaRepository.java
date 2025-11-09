package com.domainservice.domain.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.domainservice.domain.cart.model.entity.Cart;

@Repository
public interface CartJpaRepository extends JpaRepository<Cart, String> {
	Optional<Cart> findByUserId(String userId);
}
