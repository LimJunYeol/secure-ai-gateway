import io
from pypdf import PdfReader
from docx import Document as DocxDocument


def extract_text(file_bytes: bytes, mime_type: str) -> str:
    """파일 바이트에서 텍스트를 추출한다. 파일 종류(mime_type)에 따라 분기."""
    if mime_type == "application/pdf":
        return _extract_pdf(file_bytes)
    elif mime_type == "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
        return _extract_docx(file_bytes)
    elif mime_type in ("text/plain", "text/markdown"):
        return file_bytes.decode("utf-8", errors="ignore")
    else:
        raise ValueError(f"지원하지 않는 파일 형식: {mime_type}")


def _extract_pdf(file_bytes: bytes) -> str:
    reader = PdfReader(io.BytesIO(file_bytes))
    pages = []
    for page in reader.pages:
        text = page.extract_text()
        if text:
            pages.append(text)
    return "\n\n".join(pages)


def _extract_docx(file_bytes: bytes) -> str:
    doc = DocxDocument(io.BytesIO(file_bytes))
    paragraphs = [p.text for p in doc.paragraphs if p.text.strip()]
    return "\n".join(paragraphs)