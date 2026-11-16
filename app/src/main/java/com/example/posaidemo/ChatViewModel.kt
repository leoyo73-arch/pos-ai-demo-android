package com.example.posaidemo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 상담 탭과 비서 탭이 같이 쓰는 ViewModel.
 * Fragment arguments의 "mode" 값이 SavedStateHandle로 자동 전달된다.
 */
class ChatViewModel(state: SavedStateHandle) : ViewModel() {

    val mode: ChatMode = ChatMode.valueOf(state.get<String>(ARG_MODE) ?: ChatMode.CONSULT.name)
    private val sessionId = UUID.randomUUID().toString()   // 서버가 대화 기록을 구분하는 값
    private var nextId = 1L

    private val _messages = MutableLiveData(listOf(ChatMessage(0, mode.greeting, isUser = false)))
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    /** 사장님 확인을 기다리는 작업들. 화면은 맨 앞 항목부터 확인 창을 띄운다. */
    private val _pending = MutableLiveData<List<PendingAction>>(emptyList())
    val pending: LiveData<List<PendingAction>> = _pending

    init {
        if (mode == ChatMode.AGENT) refreshPending()   // 앱을 다시 켰을 때 남아 있던 승인 대기 복원
    }

    fun send(text: String) {
        if (_loading.value == true) return

        val userMsg = ChatMessage(nextId++, text, isUser = true)
        val waitingMsg = ChatMessage(
            nextId++,
            if (mode == ChatMode.AGENT) "확인하고 있어요…" else "답변 작성 중…",
            isUser = false,
            isLoading = true
        )
        _messages.value = _messages.value.orEmpty() + userMsg + waitingMsg

        viewModelScope.launch {
            _loading.value = true
            val reply: ChatMessage = try {
                when (mode) {
                    ChatMode.CONSULT -> {
                        val res = ApiClient.api.chat(ChatRequest(text))
                        waitingMsg.copy(text = res.answer, isLoading = false, sources = res.sources.orEmpty())
                    }
                    ChatMode.AGENT -> {
                        val res = ApiClient.api.agent(AgentRequest(text, sessionId))
                        _pending.value = res.pendingActions.orEmpty()
                        waitingMsg.copy(text = res.answer, isLoading = false)
                    }
                }
            } catch (e: Exception) {
                waitingMsg.copy(text = e.toUserMessage(), isLoading = false)
            }
            replace(waitingMsg.id, reply)
            _loading.value = false
        }
    }

    /** 확인 창의 승인/거절 버튼에서 호출. 결정은 항상 사람이 한다. */
    fun decide(action: PendingAction, approve: Boolean) {
        // 같은 항목이 두 번 처리되지 않게 먼저 대기 목록에서 뺀다
        _pending.value = _pending.value.orEmpty().filterNot { it.id == action.id }

        viewModelScope.launch {
            val text = try {
                ApiClient.api.decide(action.id, DecisionRequest(approve)).message
            } catch (e: Exception) {
                "처리하지 못했어요: ${e.toUserMessage()}"
            }
            addNotice(text)
        }
    }

    private fun refreshPending() {
        viewModelScope.launch {
            runCatching { ApiClient.api.actions() }
                .onSuccess { _pending.value = it }
        }
    }

    private fun addNotice(text: String) {
        _messages.value = _messages.value.orEmpty() +
            ChatMessage(nextId++, text, isUser = false, isNotice = true)
    }

    private fun replace(id: Long, newMsg: ChatMessage) {
        // 리스트를 새로 만들어야 ListAdapter(DiffUtil)가 변경을 감지한다
        _messages.value = _messages.value.orEmpty().map { if (it.id == id) newMsg else it }
    }

    companion object {
        const val ARG_MODE = "mode"
    }
}
