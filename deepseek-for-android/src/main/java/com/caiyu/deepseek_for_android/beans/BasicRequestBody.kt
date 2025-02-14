package com.caiyu.deepseek_for_android.beans

data class BasicRequestBody (
    val messages: List<Message>,
    val model: String,
) {
    companion object {
        fun create(messages: List<Message>, model: Model): BasicRequestBody {
            return BasicRequestBody(messages, model.value)
        }
    }
}