package com.aiagent.gateway.tenant.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TestNoteRepository extends JpaRepository<TestNote, UUID> {
}