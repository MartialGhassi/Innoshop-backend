package com.innowise.academy.userservice.service;

import com.innowise.academy.userservice.model.ProductResponse;

import java.util.List;

public interface ProductService {

    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(Long id);
}
