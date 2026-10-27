package com.innowise.academy.userservice.model;

import java.math.BigDecimal;

public record ProductResponse(Long id, String name, BigDecimal price, String description) {
}
