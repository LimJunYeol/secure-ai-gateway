# API 개요 v0.1

상세 OpenAPI 스펙은 Spring Boot의 SpringDoc으로 자동 생성 예정.
여기는 큰 그림만.

## 인증
- 모든 API는 JWT Bearer 토큰 필수 (로그인/회원가입 제외)
- 헤더: `Authorization: Bearer <token>`
- 토큰에 포함: user_id, tenant_id, role
- Gateway에서 토큰 검증 후 tenant_id 컨텍스트 주입

## 엔드포인트 그룹

### Auth
- POST /api/v1/auth/signup        — 테넌트 + 첫 admin 가입
- POST /api/v1/auth/login         — 로그인 → JWT 발급
- POST /api/v1/auth/refresh       — 토큰 재발급
- POST /api/v1/auth/logout

### Users (Admin only)
- GET    /api/v1/users
- POST   /api/v1/users            — 사용자 초대/생성
- PATCH  /api/v1/users/{id}       — 역할/상태 변경
- DELETE /api/v1/users/{id}

### Groups
- GET    /api/v1/groups
- POST   /api/v1/groups
- POST   /api/v1/groups/{id}/members
- DELETE /api/v1/groups/{id}/members/{userId}

### Documents
- POST   /api/v1/documents/upload     — 파일 업로드 → 비동기 인덱싱 시작
- GET    /api/v1/documents            — 본인이 볼 수 있는 문서 목록
- GET    /api/v1/documents/{id}
- DELETE /api/v1/documents/{id}
- POST   /api/v1/documents/{id}/acl   — 그룹 접근 권한 설정

### Conversations & Chat (핵심)
- GET    /api/v1/conversations
- POST   /api/v1/conversations              — 새 대화 시작
- GET    /api/v1/conversations/{id}/messages
- POST   /api/v1/conversations/{id}/messages — 질문 전송 (SSE 스트리밍 응답)

### MCP Tools
- GET    /api/v1/mcp/servers          — 연결된 MCP 서버 목록
- POST   /api/v1/mcp/servers          — MCP 서버 등록 (Admin)
- GET    /api/v1/mcp/servers/{id}/tools — 해당 서버가 제공하는 도구

### Audit (Admin only)
- GET    /api/v1/audit/logs           — 필터: user, action, date_range

### Admin Dashboard
- GET    /api/v1/admin/stats          — 사용량/비용/Top users

## 응답 표준
```json
// 성공
{
  "data": { ... },
  "meta": { "trace_id": "..." }
}

// 에러
{
  "error": {
    "code": "PERMISSION_DENIED",
    "message": "해당 문서에 접근 권한이 없습니다",
    "trace_id": "..."
  }
}
```

## 채팅 응답 (SSE)
POST /api/v1/conversations/{id}/messages 는 Server-Sent Events로:
```
event: thinking
data: {"status": "retrieving documents"}

event: thinking  
data: {"status": "calling LLM"}

event: token
data: {"text": "휴가는 "}

event: token
data: {"text": "연 15일..."}

event: citation
data: {"document_id": "...", "chunk_id": "...", "page": 3}

event: done
data: {"message_id": "...", "tokens_used": 245, "cost_usd": 0.0012}
```