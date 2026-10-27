package com.innowise.academy.innoshop.service;

import com.innowise.academy.innoshop.model.ProductResponse;

import java.util.List;

public interface ProductService {

    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(Long id);
}
