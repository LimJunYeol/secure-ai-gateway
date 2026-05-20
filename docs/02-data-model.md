# 데이터 모델 v0.1

## 핵심 테이블

### tenants (회사)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID PK | |
| name | VARCHAR(100) | 회사명 |
| domain | VARCHAR(50) UNIQUE | 서브도메인 (예: acme) |
| status | ENUM | active / suspended |
| created_at | TIMESTAMP | |

### users (사용자)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID PK | |
| tenant_id | UUID FK | tenants.id |
| email | VARCHAR(255) | |
| password_hash | VARCHAR(255) | bcrypt |
| name | VARCHAR(100) | |
| role | ENUM | admin / member |
| status | ENUM | active / disabled |
| created_at | TIMESTAMP | |
| UNIQUE | (tenant_id, email) | |

### groups (부서/그룹)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID PK | |
| tenant_id | UUID FK | |
| name | VARCHAR(100) | 예: "개발팀" |
| created_at | TIMESTAMP | |

### user_groups (사용자-그룹 다대다)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| user_id | UUID FK | |
| group_id | UUID FK | |
| PRIMARY KEY | (user_id, group_id) | |

### documents (업로드 문서)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID PK | |
| tenant_id | UUID FK | |
| uploader_id | UUID FK | users.id |
| filename | VARCHAR(255) | |
| file_path | VARCHAR(500) | S3/MinIO 경로 |
| mime_type | VARCHAR(50) | |
| status | ENUM | pending / indexed / failed |
| created_at | TIMESTAMP | |

### document_acl (문서 접근 권한)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| document_id | UUID FK | |
| group_id | UUID FK | 어느 그룹이 볼 수 있나 |
| PRIMARY KEY | (document_id, group_id) | |

### document_chunks (청크 + 벡터 메타)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID PK | |
| document_id | UUID FK | |
| tenant_id | UUID FK | (검색 최적화용 중복) |
| chunk_index | INT | 문서 내 순번 |
| content | TEXT | 청크 본문 |
| vector_id | VARCHAR(100) | Qdrant 등 벡터DB의 ID |
| token_count | INT | |

### conversations (대화 세션)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID PK | |
| tenant_id | UUID FK | |
| user_id | UUID FK | |
| title | VARCHAR(255) | 자동 생성 또는 사용자 지정 |
| created_at | TIMESTAMP | |

### messages (대화 안의 개별 메시지)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID PK | |
| conversation_id | UUID FK | |
| role | ENUM | user / assistant / tool |
| content | TEXT | |
| citations | JSONB | [{document_id, chunk_id, page}] |
| tool_calls | JSONB | MCP 도구 호출 기록 |
| masked_fields | JSONB | 마스킹된 필드 정보 |
| token_input | INT | |
| token_output | INT | |
| cost_usd | DECIMAL(10,6) | |
| created_at | TIMESTAMP | |

### audit_logs (감사 로그)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID PK | |
| tenant_id | UUID FK | |
| user_id | UUID FK | nullable (시스템 이벤트) |
| action | VARCHAR(50) | login, query, tool_call, doc_upload |
| resource_type | VARCHAR(50) | document, mcp_tool, etc |
| resource_id | UUID | |
| trace_id | VARCHAR(100) | end-to-end 추적용 |
| metadata | JSONB | 자유 형식 |
| ip_address | VARCHAR(45) | |
| created_at | TIMESTAMP | |

### mcp_servers (등록된 MCP Server)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID PK | |
| tenant_id | UUID FK | |
| name | VARCHAR(100) | "GitHub" |
| endpoint | VARCHAR(500) | MCP Server URL |
| auth_config | JSONB | 암호화된 토큰 등 |
| status | ENUM | active / disabled |
| created_at | TIMESTAMP | |

## 관계도 (텍스트)
```
tenants 1 ─ N users
tenants 1 ─ N groups
users N ─ N groups (via user_groups)
tenants 1 ─ N documents
documents N ─ N groups (via document_acl)
documents 1 ─ N document_chunks
users 1 ─ N conversations
conversations 1 ─ N messages
tenants 1 ─ N audit_logs
tenants 1 ─ N mcp_servers
```

## 인덱스 전략 (예상)
- users (tenant_id, email)
- documents (tenant_id, status)
- document_chunks (tenant_id, document_id)
- audit_logs (tenant_id, created_at)
- conversations (user_id, created_at DESC)

## 멀티테넌시 격리 원칙
- 모든 테넌트 데이터 테이블은 tenant_id 컬럼 필수
- 모든 쿼리에 tenant_id 조건 강제 (Hibernate Filter 사용 예정)
- ADR-002에서 상세 결정 기록