package com.caiyu.deepseek_for_android.core

import android.util.Log
import com.caiyu.deepseek_for_android.beans.BalanceBody
import com.caiyu.deepseek_for_android.beans.DeepSeekRequestBody
import com.caiyu.deepseek_for_android.beans.DeepSeekResponseBody
import com.caiyu.deepseek_for_android.beans.Message
import com.caiyu.deepseek_for_android.beans.Model
import com.caiyu.deepseek_for_android.utils.ApiConstants
import com.caiyu.deepseek_for_android.utils.Header
import com.caiyu.deepseek_for_android.utils.MediaType
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import java.io.IOException
import java.net.SocketTimeoutException


class DeepSeekClient private constructor (
    private var token: String,
    private var model: Model
) {
    private val tag: String = "DeepSeekClient"
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val client = OkHttpClient().newBuilder()
//        .connectTimeout(60, TimeUnit.SECONDS)
//        .callTimeout(60, TimeUnit.SECONDS)
//        .readTimeout(60, TimeUnit.SECONDS)
//        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun setModel(model: Model) {
        this.model = model
    }

    fun setToken(token: String) {
        this.token = token
    }

//    /**
//     * 基础的对话接口，采用user角色
//     */
//    suspend fun chat(message: String): String {
//        val messages = ArrayList<Message>()
//        messages.add(UserMessage(message))
//        return chatWithMuitiplyMessage(messages, model)
//    }
//
//    /**
//     * 进阶的对话接口，开发者可以自定义传入的Message类型，不限于user角色
//     * 注意: Message列表每种Message类型最多出现一次，否则可能会出现不可预料的结果
//     */
//    suspend fun chatWithMuitiplyMessage(messages: List<Message>, model: Model): String {
//        val requestBody = createChatRequestBody(messages, model)
//        val request: Request = Request.Builder()
//            .url(ApiConstants.API_CHAT_COMPLETIONS)
//            .post(requestBody)
//            .addHeader(Header.CONTENT_TYPE, MediaType.APPLICATION_JSON)
//            .addHeader(Header.ACCEPT, MediaType.TEXT_EVENT_STREAM)
//            .addHeader(Header.AUTHORIZATION, "Bearer $token")
//            .build()
//        return performStreamRequest(request) {
//
//        }
//    }

    /**
     * deepseek api 账户余额查询, 返回字符串结果
     */
    suspend fun getBalanceStringResponse(): String {
        val request = Request.Builder()
            .url(ApiConstants.API_USER_BALANCE)
            .get()
            .addHeader(Header.ACCEPT, MediaType.APPLICATION_JSON)
            .addHeader(Header.AUTHORIZATION, "Bearer $token")
            .build()
        return performNetworkRequest(request) { balanceString ->
            return@performNetworkRequest balanceFormatOutput(gson.fromJson(balanceString, BalanceBody::class.java))
        }
    }

    suspend fun getBalanceResponse(): BalanceBody? {
        val request = Request.Builder()
            .url(ApiConstants.API_USER_BALANCE)
            .get()
            .addHeader(Header.ACCEPT, MediaType.APPLICATION_JSON)
            .addHeader(Header.AUTHORIZATION, "Bearer $token")
            .build()
        val result = performNetworkRequest(request) {
            return@performNetworkRequest it
        }
        return try {
            gson.fromJson(result, BalanceBody::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun createChatRequest(messages: List<Message>): Request {
        val body = DeepSeekRequestBody.create(messages, model)
        val jsonBody = gson.toJson(body, DeepSeekRequestBody::class.java)
        val mediaType = MediaType.APPLICATION_JSON.toMediaTypeOrNull()
        val requestBody = jsonBody.toRequestBody(mediaType)
        return Request.Builder()
            .url(ApiConstants.API_CHAT_COMPLETIONS)
            .post(requestBody)
            .addHeader(Header.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .addHeader(Header.ACCEPT, MediaType.TEXT_EVENT_STREAM)
            .addHeader(Header.AUTHORIZATION, "Bearer $token")
            .build()
    }

    /**
     * 格式化余额输出
     */
    private fun balanceFormatOutput(balanceBody: BalanceBody): String {
        if (!balanceBody.isAvailable) {
            return "余额不足"
        }
        val totalBalance = balanceBody.balanceInfos[0].totalBalance
        val grantedBalance = balanceBody.balanceInfos[0].grantedBalance
        val toppedUpBalance = balanceBody.balanceInfos[0].toppedUpBalance

        val unit =  if (balanceBody.balanceInfos[0].currency == "CNY") "￥" else "$"
        return "总余额: $totalBalance$unit, 未过期的赠金余额: $grantedBalance$unit, 充值余额: $toppedUpBalance$unit"
    }

    /**
     * 执行流式网络请求
     */
    suspend fun performStreamRequest(
        request: Request,
        onData: suspend (DeepSeekResponseBody) -> Unit,
        onComplete: suspend () -> Unit,
        onError: suspend (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            response.body?.source()?.use { source ->
                var line: String?
                // 逐行解析 SSE 格式的响应
                while (true) {
                    ensureActive()
                    line = try {
                        source.readUtf8Line()
                    } catch (e: Exception) {
                        if (e is SocketTimeoutException) {
                            break
                        }
                        throw e
                    } ?: break
                    if (line.startsWith("data: ")) {
                        val data = line.substring(6).trim()
                        if (data == "[DONE]") {
                            withContext(Dispatchers.Main) {
                                onComplete()
                            }
                            break
                        } else {
                            try {
                                val streamResponse = gson.fromJson(data, DeepSeekResponseBody::class.java)
                                onData(streamResponse)
                            } catch (e: Exception) {
                                onError(e)
                            }
                        }
                    }
                }
            } ?: onError(IOException("Response body is null"))
            Log.d(tag, "response: End")
        }
    }

    private suspend fun performNetworkRequest(
        request: Request,
        onProcessBodyString: (String) -> String
    ): String = withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                "Unexpected code ${response.code}"
            }
            val bodyString = response.body?.string() ?: throw IOException("Empty response body")
            onProcessBodyString(bodyString)
        } catch (e: IOException) {
            "network error: ${e.message}"
        }
    }

    class Builder {
        private var token: String? = null
        private var model: Model = Model.DeepSeek_R1
        fun setToken(token: String) = apply {
            this.token = token
        }

        fun setModel(model: Model) = apply {
            this.model = model
        }
        fun build(): DeepSeekClient {
            require(!token.isNullOrEmpty()) {
                "the token has not set"
            }
            return DeepSeekClient(token!!, model)
        }
    }
}