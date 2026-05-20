# Secure AI Gateway

기업 내부 문서·시스템을 LLM과 안전하게 연결하는 엔터프라이즈 RAG + MCP 게이트웨이.
권한 / 감사 / 출처 / 민감정보 마스킹을 단일 출입구에서 통제.

## 아키텍처
<img width="500" height="300" alt="secure_ai_gateway_architecture" src="https://github.com/user-attachments/assets/b1f445a0-8c78-46d0-875c-808f889fba46" /><svg width="100%" viewBox="0 0 680 410" role="img" xmlns="http://www.w3.org/2000/svg" style="">

## 핵심 메커니즘: 멀티테넌시 격리
<img width="500" height="300" alt="multitenancy_isolation_flow" src="https://github.com/user-attachments/assets/7aa57dd3-bbf7-464b-ab1d-7db75e25c32a" />

## 진행 상태
- Week 1: 설계 (요구사항 / 데이터 모델 / ADR-001)
- Week 2 Mon-Wed: 백엔드 골격 + 멀티테넌시 격리
- Week 2 Thu-Fri: 인증 / JWT
- Week 3: RAG 파이프라인
- Week 4: MCP Hub
- Week 5-6: 감사·마스킹·배포·데모

## 기술 스택
**Backend**: Java 21 · Spring Boot 3.5 · Spring Security · JPA/Hibernate  
**Database**: PostgreSQL 16 · (Qdrant — RAG 단계 예정)  
**Infrastructure**: Docker · (Kubernetes — 6주차 예정)
