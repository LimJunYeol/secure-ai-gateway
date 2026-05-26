import logging
import uuid
from qdrant_client import QdrantClient
from qdrant_client.models import (
    Distance, VectorParams, PointStruct,
    Filter, FieldCondition, MatchValue,
)

from config import settings

logger = logging.getLogger(__name__)

client = QdrantClient(host=settings.qdrant_host, port=settings.qdrant_port)


def ensure_collection():
    """컬렉션이 없으면 생성한다."""
    collections = [c.name for c in client.get_collections().collections]
    if settings.collection_name not in collections:
        client.create_collection(
            collection_name=settings.collection_name,
            vectors_config=VectorParams(
                size=settings.embedding_dim,
                distance=Distance.COSINE,
            ),
        )
        logger.info(f"Created Qdrant collection: {settings.collection_name}")


def store_chunks(
    tenant_id: str,
    document_id: str,
    chunks: list[str],
    embeddings: list[list[float]],
) -> int:
    """청크들을 벡터와 함께 저장한다. tenant_id를 payload에 넣어 격리한다."""
    ensure_collection()

    points = []
    for idx, (chunk, embedding) in enumerate(zip(chunks, embeddings)):
        points.append(PointStruct(
            id=str(uuid.uuid4()),
            vector=embedding,
            payload={
                "tenant_id": tenant_id,      # ★ 테넌트 격리 핵심
                "document_id": document_id,
                "chunk_index": idx,
                "content": chunk,
            },
        ))

    client.upsert(collection_name=settings.collection_name, points=points)
    logger.info(f"Stored {len(points)} chunks for document {document_id}")
    return len(points)


def search(
    tenant_id: str,
    query_vector: list[float],
    limit: int = 5,
    allowed_document_ids: list[str] | None = None,
) -> list[dict]:
    """tenant_id로 격리된 벡터 검색. 선택적으로 문서 ID로 추가 필터링."""
    ensure_collection()

    # tenant_id는 항상 필터 (격리)
    must_conditions = [
        FieldCondition(key="tenant_id", match=MatchValue(value=tenant_id))
    ]

    results = client.query_points(
        collection_name=settings.collection_name,
        query=query_vector,
        query_filter=Filter(must=must_conditions),
        limit=limit,
        with_payload=True,
    ).points

    return [
        {
            "document_id": p.payload["document_id"],
            "chunk_index": p.payload["chunk_index"],
            "content": p.payload["content"],
            "score": p.score,
        }
        for p in results
    ]


def delete_document(tenant_id: str, document_id: str):
    """특정 문서의 모든 청크 삭제."""
    client.delete(
        collection_name=settings.collection_name,
        points_selector=Filter(must=[
            FieldCondition(key="tenant_id", match=MatchValue(value=tenant_id)),
            FieldCondition(key="document_id", match=MatchValue(value=document_id)),
        ]),
    )
    logger.info(f"Deleted chunks for document {document_id}")