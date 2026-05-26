from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # Qdrant 벡터 DB 접속 정보
    qdrant_host: str = "localhost"
    qdrant_port: int = 6333
    collection_name: str = "documents"

    # 임베딩 모델 (텍스트 → 숫자 변환기)
    embedding_model: str = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
    embedding_dim: int = 384

    # 청킹 설정 (긴 글 자르기)
    chunk_size: int = 500
    chunk_overlap: int = 50

    class Config:
        env_file = ".env"


settings = Settings()