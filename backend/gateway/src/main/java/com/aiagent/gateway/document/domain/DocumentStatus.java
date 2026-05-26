package com.aiagent.gateway.document.domain;

public enum DocumentStatus {
    PENDING,    // 업로드됨, 인덱싱 대기
    INDEXING,   // 청킹/임베딩 진행 중
    INDEXED,    // 검색 가능 상태
    FAILED      // 인덱싱 실패
}