package com.innowise.academy.userservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing so that {@code @CreatedDate} and
 * {@code @LastModifiedDate} fields are populated automatically.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
