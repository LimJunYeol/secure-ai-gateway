package com.aiagent.gateway.tenant.service;

import com.aiagent.gateway.common.tenant.TenantContext;
import com.aiagent.gateway.tenant.domain.TestNote;
import com.aiagent.gateway.tenant.domain.TestNoteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestNoteService {

    private final TestNoteRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public TestNote create(String content) {
        // INSERT는 @PrePersist 리스너가 tenant_id 자동 주입하므로 OK
        return repository.save(
                TestNote.builder().content(content).build()
        );
    }

    @Transactional(readOnly = true)
    public List<TestNote> findAll() {
        // 필터를 트랜잭션 안에서 직접 활성화 (같은 세션 보장)
        enableTenantFilter();
        return repository.findAll();
    }

    private void enableTenantFilter() {
        if (TenantContext.isSet()) {
            entityManager.unwrap(Session.class)
                    .enableFilter("tenantFilter")
                    .setParameter("tenantId", TenantContext.get());
        }
    }
}