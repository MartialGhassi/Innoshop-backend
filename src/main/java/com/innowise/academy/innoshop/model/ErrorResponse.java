package com.innowise.academy.innoshop.model;

public record ErrorResponse(int status, String error, String message) {
}
