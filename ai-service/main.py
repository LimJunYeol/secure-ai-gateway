from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI(
    title="Secure AI Gateway - AI Service",
    description="청킹, 임베딩, 벡터 검색, LLM 호출을 담당하는 AI 워커",
    version="0.1.0",
)


class HealthResponse(BaseModel):
    status: str
    service: str


@app.get("/health", response_model=HealthResponse)
def health():
    return HealthResponse(status="ok", service="ai-service")


@app.get("/")
def root():
    return {"message": "Secure AI Gateway AI Service is alive!"}