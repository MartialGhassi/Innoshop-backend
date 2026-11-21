package com.innowise.academy.userservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

/**
 * Web configuration of the application.
 *
 * <p>Serializes {@link org.springframework.data.domain.Page} results through a stable
 * DTO ({@code content} + {@code page} metadata) instead of exposing the internal
 * {@code PageImpl} structure in the JSON responses.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class WebConfig {
}
