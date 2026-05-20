# secure-ai-gateway

기업 내부 문서·시스템을 LLM과 연결하는 엔터프라이즈 RAG + MCP 게이트웨이.
권한, 감사, 출처, 민감정보 마스킹을 통합 통제.

## 진행 현황
- Week 1: 설계 (요구사항 / 데이터 모델 / ADR-001)
- Week 2 Mon-Wed: 백엔드 골격 + 멀티테넌시 격리 ★
- Week 2 Thu-Fri: 인증/JWT
- Week 3: RAG 파이프라인
- Week 4: MCP Hub
- Week 5: 감사/마스킹/대시보드
- Week 6: K8s 배포 + 데모

## 핵심 의사결정 (ADR)
- [ADR-001](docs/adr/001-multitenancy-strategy.md): Shared DB w/ tenant_id

## 아키텍처
(다이어그램 추가)
