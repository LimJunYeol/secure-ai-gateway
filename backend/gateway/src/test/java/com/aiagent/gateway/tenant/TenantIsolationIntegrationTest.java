package com.aiagent.gateway.tenant;

import com.aiagent.gateway.common.tenant.TenantContext;
import com.aiagent.gateway.tenant.domain.TestNote;
import com.aiagent.gateway.tenant.service.TestNoteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.aiagent.gateway.tenant.domain.TestNoteRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TenantIsolationIntegrationTest {

    @Autowired
    private TestNoteService service;

    private final UUID tenantA = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID tenantB = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private TestNoteRepository repository;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
        repository.deleteAll();
    }

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("서로 다른 테넌트의 데이터는 격리된다")
    void differentTenantsAreIsolated() {
        // 디버그: 시작 시점 DB 상태
        System.out.println("=== test start, db count: " + repository.count());

        // given
        TenantContext.set(tenantA);
        service.create("A의 노트");
        TenantContext.clear();

        TenantContext.set(tenantB);
        service.create("B의 노트");
        TenantContext.clear();

        // 디버그: insert 후 DB 상태
        System.out.println("=== after inserts, total in db: " + repository.count());

        // when: A로 조회
        TenantContext.set(tenantA);
        List<TestNote> aNotes = service.findAll();
        TenantContext.clear();

        System.out.println("=== aNotes size: " + aNotes.size());
        aNotes.forEach(n -> System.out.println("  - " + n.getTenantId() + ": " + n.getContent()));

        assertThat(aNotes).hasSize(1);
        assertThat(aNotes.get(0).getContent()).isEqualTo("A의 노트");
        assertThat(aNotes.get(0).getTenantId()).isEqualTo(tenantA);

        // when: B로 조회
        TenantContext.set(tenantB);
        List<TestNote> bNotes = service.findAll();
        TenantContext.clear();

        System.out.println("=== bNotes size: " + bNotes.size());
        bNotes.forEach(n -> System.out.println("  - " + n.getTenantId() + ": " + n.getContent()));

        assertThat(bNotes).hasSize(1);
        assertThat(bNotes.get(0).getContent()).isEqualTo("B의 노트");
        assertThat(bNotes.get(0).getTenantId()).isEqualTo(tenantB);
    }

    @Test
    @DisplayName("tenant_id 미설정 시 자동 주입된다")
    void tenantIdIsAutoInjectedFromContext() {
        TenantContext.set(tenantA);
        TestNote saved = service.create("자동 주입 테스트");
        TenantContext.clear();

        assertThat(saved.getTenantId()).isEqualTo(tenantA);
    }
}