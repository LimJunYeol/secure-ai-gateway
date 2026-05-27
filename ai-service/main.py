import logging
import base64
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

import extractor
import embedder
import vector_store

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Secure AI Gateway - AI Service",
    description="청킹, 임베딩, 벡터 검색을 담당하는 AI 워커",
    version="0.1.0",
)


# ===== 요청/응답 형식 정의 =====

class HealthResponse(BaseModel):
    status: str
    service: str


class IndexRequest(BaseModel):
    tenant_id: str
    document_id: str
    file_base64: str    # 파일 내용을 base64 문자열로 받음
    mime_type: str


class IndexResponse(BaseModel):
    document_id: str
    chunk_count: int


class SearchRequest(BaseModel):
    tenant_id: str
    query: str
    limit: int = 5


class SearchHit(BaseModel):
    document_id: str
    chunk_index: int
    content: str
    score: float


class SearchResponse(BaseModel):
    hits: list[SearchHit]


# ===== API 엔드포인트 =====

@app.get("/health", response_model=HealthResponse)
def health():
    return HealthResponse(status="ok", service="ai-service")


@app.post("/index", response_model=IndexResponse)
def index_document(req: IndexRequest):
    """문서 받기 → 텍스트 추출 → 청킹 → 임베딩 → Qdrant 저장."""
    try:
        # 1. base64 디코드해서 원본 바이트 복원
        file_bytes = base64.b64decode(req.file_base64)

        # 2. 텍스트 추출
        text = extractor.extract_text(file_bytes, req.mime_type)
        if not text.strip():
            raise ValueError("문서에서 텍스트를 추출할 수 없습니다")

        # 3. 청킹
        chunks = embedder.chunk_text(text)
        if not chunks:
            raise ValueError("청크를 생성할 수 없습니다")

        # 4. 임베딩
        embeddings = embedder.embed_texts(chunks)

        # 5. Qdrant 저장
        count = vector_store.store_chunks(
            tenant_id=req.tenant_id,
            document_id=req.document_id,
            chunks=chunks,
            embeddings=embeddings,
        )

        return IndexResponse(document_id=req.document_id, chunk_count=count)

    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception:
        logger.exception("인덱싱 실패")
        raise HTTPException(status_code=500, detail="인덱싱 중 오류가 발생했습니다")


@app.post("/search", response_model=SearchResponse)
def search(req: SearchRequest):
    """질의를 벡터로 바꿔서 tenant 격리된 검색."""
    try:
        query_vector = embedder.embed_query(req.query)
        results = vector_store.search(
            tenant_id=req.tenant_id,
            query_vector=query_vector,
            limit=req.limit,
        )
        return SearchResponse(hits=[SearchHit(**r) for r in results])
    except Exception:
        logger.exception("검색 실패")
        raise HTTPException(status_code=500, detail="검색 중 오류가 발생했습니다")


class DeleteRequest(BaseModel):
    tenant_id: str
    document_id: str

@app.post("/delete")
def delete_document(req: DeleteRequest):
    """특정 문서의 모든 청크를 Qdrant에서 삭제."""
    try:
        vector_store.delete_document(req.tenant_id, req.document_id)
        return {"deleted": True, "document_id": req.document_id}
    except Exception:
        logger.exception("삭제 실패")
        raise HTTPException(status_code=500, detail="삭제 중 오류가 발생했습니다")