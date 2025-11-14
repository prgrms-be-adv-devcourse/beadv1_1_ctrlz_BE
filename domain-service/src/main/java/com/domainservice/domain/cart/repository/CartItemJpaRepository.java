package com.domainservice.domain.cart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.domainservice.domain.cart.model.entity.Cart;
import com.domainservice.domain.cart.model.entity.CartItem;

@Repository
public interface CartItemJpaRepository extends JpaRepository<CartItem, String> {
	List<CartItem> findByCart(Cart cart);

	List<CartItem> findAllByIdIn(List<String> ids);

	void deleteAllByIdIn(List<String> ids);

}
