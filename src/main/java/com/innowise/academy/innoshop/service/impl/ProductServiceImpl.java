package com.innowise.academy.innoshop.service.impl;

import com.innowise.academy.innoshop.exception.ProductNotFoundException;
import com.innowise.academy.innoshop.model.Product;
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
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}