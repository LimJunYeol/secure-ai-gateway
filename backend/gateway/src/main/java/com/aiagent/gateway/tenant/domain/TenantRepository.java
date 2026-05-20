package com.aiagent.gateway.tenant.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByDomain(String domain);
    boolean existsByDomain(String domain);
}