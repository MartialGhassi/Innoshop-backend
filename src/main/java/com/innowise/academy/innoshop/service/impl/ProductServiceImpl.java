package com.innowise.academy.innoshop.service.impl;

import com.innowise.academy.innoshop.exception.ProductNotFoundException;
import com.innowise.academy.innoshop.model.Product;
import com.innowise.academy.innoshop.model.ProductResponse;
import com.innowise.academy.innoshop.repository.ProductRepository;
import com.innowise.academy.innoshop.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription());
    }
}