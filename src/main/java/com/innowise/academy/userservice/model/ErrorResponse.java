package com.innowise.academy.userservice.model;

public record ErrorResponse(int status, String error, String message) {
}
