# ADR-001: 멀티테넌시 격리 전략
- **상태**: Accepted
- **결정일**: 2026-05-20
- **작성자**: Adu

## 컨텍스트
Secure AI Gateway는 B2B SaaS로 여러 기업(테넌트)이 동시에 사용한다.
테넌트 간 데이터는 절대 섞이면 안 된다. (RAG 검색 결과, 감사 로그, 사용자 정보 등)

3가지 격리 전략 중 선택해야 한다:
1. DB-per-tenant: 테넌트마다 별도 DB
2. Schema-per-tenant: 같은 DB, 다른 스키마
3. Shared DB w/ tenant_id 컬럼

## 결정
**3번 (Shared DB with tenant_id column)** 채택.

모든 테넌트 데이터 테이블은 `tenant_id` 컬럼을 필수로 가지며,
모든 쿼리에 `WHERE tenant_id = ?` 조건이 자동으로 추가되는 메커니즘을 구현한다.
구체적으로 Hibernate Filter 또는 JPA Specification으로 강제.

## 대안 비교

| 항목 | DB-per-tenant | Schema-per-tenant | Shared (선택) |
|---|---|---|---|
| 격리도 | 최강 | 강 | 보통 (앱 로직 의존) |
| 운영 복잡도 | 매우 높음 (DB 마이그레이션 N회) | 높음 | 낮음 |
| 비용 | 매우 높음 (테넌트당 DB) | 중간 | 낮음 |
| 토이 프로젝트 적합성 | ✗ | △ | ◯ |
| 백업/복구 | 단순 | 복잡 | 단순 |
| 분석 쿼리 | 어려움 (DB 횡단) | 어려움 | 쉬움 |

## 선택 근거
1. **토이 프로젝트 적정성**: 단일 DB로 데모/시연 환경 단순화
2. **확장 가능성**: 추후 대형 테넌트는 DB-per-tenant로 마이그레이션 가능한 아키텍처
3. **운영 비용**: 클라우드 비용 최소화
4. **분석 용이성**: 전체 사용량/비용 통계가 단일 쿼리로 가능

## 트레이드오프 (인지된 위험)
- **개발 실수 위험**: 쿼리에서 tenant_id 빠뜨리면 데이터 유출
  → **완화**: Hibernate Filter로 자동 주입, 모든 Repository 인터페이스에 강제 적용
  → **테스트**: tenant 격리 검증용 통합 테스트 작성

- **대형 테넌트 성능 영향**: 한 테넌트의 무거운 쿼리가 다른 테넌트에 영향
  → **완화 (v1 범위 외)**: Connection Pool 분리, Rate Limit, Query Timeout

- **컴플라이언스 요구사항**: 일부 산업(금융/의료)은 물리적 분리 요구
  → **인지함**: v2에서 엔터프라이즈 티어로 DB-per-tenant 옵션 추가

## 영향
- 모든 도메인 엔티티 클래스에 `tenant_id` 필드 필수
- BaseEntity 추상 클래스 도입 검토
- TenantContext (ThreadLocal 또는 RequestScope) 구현 필요
- JWT 토큰에 tenant_id 클레임 포함

## 참고
- Microsoft - Multi-tenant SaaS database tenancy patterns
- AWS - SaaS architecture fundamentals  
- "Building Multi-Tenant SaaS Architectures" (O'Reilly)