package com.example.posaidemo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessage>>(
        listOf(ChatMessage(0, "안녕하세요! POS 사용 중 궁금한 점을 물어보세요.", isUser = false))
    )
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private var nextId = 1L

    fun send(text: String) {
        if (_loading.value == true) return

        val userMsg = ChatMessage(nextId++, text, isUser = true)
        val waitingMsg = ChatMessage(nextId++, "답변 작성 중…", isUser = false, isLoading = true)
        _messages.value = _messages.value.orEmpty() + userMsg + waitingMsg

        viewModelScope.launch {
            _loading.value = true
            val (answer, sources) = try {
                val res = ApiClient.api.chat(ChatRequest(text))
                res.answer to res.sources.orEmpty()
            } catch (e: Exception) {
                "답변을 받지 못했어요. 잠시 후 다시 시도해 주세요.\n(${e.message})" to emptyList()
            }
            _messages.value = _messages.value.orEmpty().map {
                if (it.id == waitingMsg.id) it.copy(text = answer, isLoading = false, sources = sources)
                else it
            }
            _loading.value = false
        }
    }
}
