package com.aiagent.gateway.tenant.controller;

import com.aiagent.gateway.common.tenant.TenantContext;
import com.aiagent.gateway.tenant.domain.TestNote;
import com.aiagent.gateway.tenant.service.TestNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dev/tenant-test")
@RequiredArgsConstructor
public class TenantDevController {

    private final TestNoteService service;

    @PostMapping
    public TestNote createNote(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestBody CreateNoteRequest request
    ) {
        TenantContext.set(tenantId);
        try {
            return service.create(request.content());
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping
    public List<TestNote> listNotes(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        TenantContext.set(tenantId);
        try {
            return service.findAll();
        } finally {
            TenantContext.clear();
        }
    }

    public record CreateNoteRequest(String content) {}
}