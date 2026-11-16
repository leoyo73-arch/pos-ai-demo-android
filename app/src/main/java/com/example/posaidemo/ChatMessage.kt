package com.example.posaidemo

data class ChatMessage(
    val id: Long,
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false,
    val sources: List<String> = emptyList()
)
