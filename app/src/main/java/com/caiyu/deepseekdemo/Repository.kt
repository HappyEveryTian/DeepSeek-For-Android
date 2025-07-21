package com.caiyu.deepseekdemo

import com.caiyu.deepseek_for_android.beans.Message
import com.caiyu.deepseek_for_android.beans.Model
import com.caiyu.deepseek_for_android.core.DeepSeekClient
import okhttp3.Request

object Repository {
    private val token by lazy { BuildConfig.DEEPSEEK_API_KEY }
    private val deepSeekClient = DeepSeekClient.Builder()
                            .setToken(token)
                            .setModel(Model.DeepSeek_R1)
                            .build()

    suspend fun getBalanceStringResponse() = deepSeekClient.getBalanceStringResponse()

    fun performStreamRequestFlow(request: Request) = deepSeekClient.performStreamRequestFlow(request)

    fun createChatRequest(messages: List<Message>) = deepSeekClient.createChatRequest(messages)
}