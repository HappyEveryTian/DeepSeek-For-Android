package com.caiyu.deepseekdemo

import androidx.lifecycle.ViewModel
import com.caiyu.deepseek_for_android.beans.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Request

class MainActivityViewModel: ViewModel() {
    private val _chatTextArea = MutableStateFlow("")
    val chatTextArea: Flow<String> = _chatTextArea.asStateFlow()

    fun setText(newText: String) {
        _chatTextArea.value = newText
    }

    fun getText() = _chatTextArea.value

    fun clearText() {
        _chatTextArea.value = ""
    }

    suspend fun getBalance() = Repository.getBalanceStringResponse()

    fun createChatRequest(messages: List<Message>) = Repository.createChatRequest(messages)

    fun performStreamRequestFlow(request: Request) = Repository.performStreamRequestFlow(request)

}