import logging
from groq import Groq

from config import settings

logger = logging.getLogger(__name__)

client = Groq(api_key=settings.groq_api_key)


SYSTEM_PROMPT = """당신은 기업 내부 문서를 기반으로 답변하는 AI 어시스턴트입니다.

규칙:
1. 반드시 제공된 '문서 내용'에만 근거해서 답변하세요.
2. 문서에 없는 내용은 지어내지 말고 "제공된 문서에서 해당 정보를 찾을 수 없습니다"라고 답하세요.
3. 답변은 한국어로, 간결하고 명확하게 작성하세요.
4. 답변의 근거가 된 내용을 자연스럽게 인용하세요.
"""


def generate_answer(query: str, contexts: list[dict]) -> str:
    """검색된 청크들(contexts)을 근거로 질문(query)에 답변한다."""

    if not contexts:
        return "제공된 문서에서 관련 정보를 찾을 수 없습니다."

    # 검색된 청크들을 하나의 컨텍스트 텍스트로 합침
    context_text = "\n\n".join(
        f"[문서 {i + 1}]\n{ctx['content']}"
        for i, ctx in enumerate(contexts)
    )

    user_prompt = f"""다음 문서 내용을 참고해서 질문에 답해주세요.

=== 문서 내용 ===
{context_text}

=== 질문 ===
{query}
"""

    try:
        response = client.chat.completions.create(
            model=settings.llm_model,
            max_tokens=settings.llm_max_tokens,
            messages=[
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": user_prompt},
            ],
            temperature=0.2,
        )
        answer = response.choices[0].message.content
        logger.info(f"Generated answer ({len(answer)} chars)")
        return answer

    except Exception:
        logger.exception("LLM 답변 생성 실패")
        raise