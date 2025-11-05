package com.domainservice.domain.product.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.domainservice.domain.product.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
}
