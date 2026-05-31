from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # Qdrant
    qdrant_host: str = "localhost"
    qdrant_port: int = 6333
    collection_name: str = "documents"

    # 임베딩
    embedding_model: str = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
    embedding_dim: int = 384

    # 청킹
    chunk_size: int = 500
    chunk_overlap: int = 50

    # LLM (Groq)
    groq_api_key: str = ""
    llm_model: str = "llama-3.3-70b-versatile"
    llm_max_tokens: int = 1000

    class Config:
        env_file = ".env"


settings = Settings()