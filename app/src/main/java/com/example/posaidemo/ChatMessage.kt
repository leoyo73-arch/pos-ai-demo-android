package com.example.posaidemo

data class ChatMessage(
    val id: Long,
    val text: String,
    val isUser: Boolean,                     // true면 내 말풍선(오른쪽), false면 AI(왼쪽)
    val isLoading: Boolean = false,
    val sources: List<String> = emptyList(), // 상담 답변의 근거 매뉴얼 제목
    val isNotice: Boolean = false,           // 승인·거절 결과 같은 시스템 안내 (가운데 작은 글씨)
    val retryText: String? = null            // 실패한 답변이면 다시 보낼 질문, 아니면 null
)

enum class ChatMode(val greeting: String, val hint: String, val examples: List<String>) {
    CONSULT(
        "안녕하세요! 스마트포스 사용 중 궁금한 점을 물어보세요.",
        "궁금한 점을 입력하세요",
        listOf("결제 취소는 어떻게 해요?", "카드 리더기가 안 붙어요", "정산금은 언제 들어와요?")
    ),
    AGENT(
        "매출 조회와 취소·환불 업무를 도와드려요. 결제 취소와 손님 안내는 사장님 승인 후에만 실행됩니다.",
        "업무를 말씀해 주세요",
        listOf("이번 주 매출 어때?", "어제 취소된 결제 중 환불 안 된 거 있어?", "방금 결제한 거 하나 취소해 줘")
    )
}
