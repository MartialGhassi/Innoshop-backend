package com.innowise.webtech.innoshop.controller;

import com.innowise.webtech.innoshop.model.Product;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public List<Product> getProducts() {
        return List.of(
                new Product(1L, "Laptop Pro 14", new BigDecimal("1299.99"), "Lightweight laptop for study and work"),
                new Product(2L, "Wireless Mouse", new BigDecimal("24.90"), "Ergonomic Bluetooth mouse"),
                new Product(3L, "USB-C Hub", new BigDecimal("39.50"), "7-in-1 hub with HDMI and card reader"),
                new Product(4L, "27-inch Monitor", new BigDecimal("279.00"), "QHD monitor with height-adjustable stand")
        );
    }
}