import logging
from langchain_text_splitters import RecursiveCharacterTextSplitter
from sentence_transformers import SentenceTransformer

from config import settings

logger = logging.getLogger(__name__)

# 모델은 한 번만 로드 (무거움)
_model: SentenceTransformer | None = None


def get_model() -> SentenceTransformer:
    global _model
    if _model is None:
        logger.info(f"Loading embedding model: {settings.embedding_model}")
        _model = SentenceTransformer(settings.embedding_model)
        logger.info("Embedding model loaded")
    return _model


def chunk_text(text: str) -> list[str]:
    """긴 텍스트를 의미 단위로 자른다."""
    splitter = RecursiveCharacterTextSplitter(
        chunk_size=settings.chunk_size,
        chunk_overlap=settings.chunk_overlap,
        separators=["\n\n", "\n", ". ", " ", ""],
    )
    chunks = splitter.split_text(text)
    return [c.strip() for c in chunks if c.strip()]


def embed_texts(texts: list[str]) -> list[list[float]]:
    """텍스트 리스트를 벡터 리스트로 변환한다."""
    model = get_model()
    embeddings = model.encode(texts, normalize_embeddings=True)
    return embeddings.tolist()


def embed_query(query: str) -> list[float]:
    """검색 질의 하나를 벡터로 변환한다."""
    model = get_model()
    embedding = model.encode(query, normalize_embeddings=True)
    return embedding.tolist()