package com.caiyu.deepseekdemo

sealed class ChatMessage {
    data class UserMessage(val content: String) : ChatMessage()

    data class ResponseMessage(var content: String) : ChatMessage()

    object LoadingMessage : ChatMessage()

    data class ErrorMessage(val content: String) : ChatMessage()

    fun getItemType(): Int {
        return when (this) {
            is UserMessage -> VIEW_TYPE_USER
            is ResponseMessage -> VIEW_TYPE_RESPONSE
            is LoadingMessage -> VIEW_TYPE_LOADING
            is ErrorMessage -> VIEW_TYPE_ERROR
        }
    }
    companion object {
        const val VIEW_TYPE_USER = 0
        const val VIEW_TYPE_RESPONSE = 1
        const val VIEW_TYPE_LOADING = 2
        const val VIEW_TYPE_ERROR = 3
    }
}