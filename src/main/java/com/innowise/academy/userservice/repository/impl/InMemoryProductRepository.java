package com.innowise.academy.userservice.repository.impl;

import com.innowise.academy.userservice.model.Product;
import com.innowise.academy.userservice.repository.ProductRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final Map<Long, Product> products = new LinkedHashMap<>();

    public InMemoryProductRepository() {
        save(new Product(1L, "Laptop Pro 14", new BigDecimal("1299.99"), "Lightweight laptop for study and work"));
        save(new Product(2L, "Wireless Mouse", new BigDecimal("24.90"), "Ergonomic Bluetooth mouse"));
        save(new Product(3L, "USB-C Hub", new BigDecimal("39.50"), "7-in-1 hub with HDMI and card reader"));
        save(new Product(4L, "27-inch Monitor", new BigDecimal("279.00"), "QHD monitor with height-adjustable stand"));
    }

    private void save(Product product) {
        products.put(product.getId(), product);
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(products.get(id));
    }
}